package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.CateLog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2.1 找到所有1级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == 0;
        }).map((menu) -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> catIds) {
        baseMapper.deleteBatchIds(catIds);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    // 表示updateCascade方法下的数据一更新，就会删除category目录下的名字为getLevel1Categories的缓存
//    @CacheEvict(value = "category", key = "'getLevel1Categories'")

//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categories'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'"),
//    }) // 修改多个缓存操作,Caching是组合操作，清除多个缓存
    @CacheEvict(value = "category", allEntries = true) // 对该分区下的所有缓存进行组合删除。所以同一类型的缓存建议放在同一分区下面，后续比较好操作
    @Override
    public void updateCascade(CategoryEntity categoryEntity) {
        this.updateById(categoryEntity);
        categoryBrandRelationService.updateCategory(categoryEntity.getCatId(), categoryEntity.getName());
    }

    // todo 产生堆外内存溢出，OutOfDirectMemory
    // 1).spring boot2.0以后默认使用lettuce作为操作redis的客户端，他使用netty进行网络通信
    // 2).lettuce的bug导致netty堆外内存溢出，-Xmx300m；netty如果没有指定堆外内存，默认使用-Xmx300m
    // 可以通过-Dio.netty.maxDirectMemory进行设置
    // 3).解决方案：不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
    // ①. 升级lettuce客户端，②. 切换使用jedis

    /**
     * 1.每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型划分)】
     * 2. @Cacheable({"category"}) // 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。如果缓存中没有，才会调用方法
     * 3. 默认行为:
     *      1). 如果缓存中有，方法不用调用。
     *      2). key默认自动生成，缓存的名字：：SimpleKey [](自主生成的key值)。
     *      3). 缓存的value的值，默认使用json序列化机制，将序列化后的数据存到redis。
     *      4). 默认ttl时间：-1。
     * 自定义:
     *      1). 指定生成的缓存使用的key: key属性指定，接收SpEl表达式
     *      SpEl详细餐换文档：https://docs.spring.io/spring/docs/.............../
     *      2). 指定缓存的数据的存活时间: 配置文件中配置spring.cache.redis.time-to-live=3600000。(单位:ms)
     *      3). 将数据保存为json格式: 自定义RedisCacheConfiguration即可
     *
     *  4. Spring-Cache的不足:
     *      1). 读模式:
     *          缓存穿透:查询一个null的数据。解决: cache-null-values = true
     *          缓存击穿: 大量并发进来同事查询一个正好过期的数据。解决: 加锁？默认是无锁的.    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)加一个本地锁，而且是读的本地锁
     *          缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间. spring.cache.redis.time-to-live=xxx ms
     *      2). 写模式: (缓存与数据库一致)
     *          1). 读写加锁(适用于读多写少的情况)
     *          2). 引入canal, 感知到mysql的更新去更新数据库
     *          3). 读多写多，直接查询数据库
     *      总结:
     *           常规数据(读多写少, 即时性，一致性要求不高的数据): 完全可以使用spring-Cache
     *           特殊数据: 特殊设计
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true) // 加锁，使得缓存的读变为同步执行
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        // 1. 查出所有分类
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    public Map<String, List<CateLog2Vo>> getCatalogJson() {
        // 给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型

        /**
         * 1. 对空结果进行缓存: 解决缓存穿透
         * 2. 设置过期时间(时间为随机值): 解决缓存雪崩
         * 3. 加锁: 解决缓存击穿
         */

        //1. 加入缓存逻辑，缓存中存的数据是json字符串
        // Json跨语言，跨平台兼容
        return getDataFromDb();
    }

    private Map<String, List<CateLog2Vo>> getDataFromDb() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            // 2.缓存中没有，查询数据库
            System.out.println("走的是数据库");
            Map<String, List<CateLog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
            String s = JSON.toJSONString(catalogJsonFromDb);
            redisTemplate.opsForValue().set("catalogJson", s);
            return catalogJsonFromDb;
        } else {
            // 缓存中查询到了数据
            System.out.println("走的是redis缓存");
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<CateLog2Vo>>>() {
            });
        }
    }

    private Map<String, List<CateLog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        // 1.占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            // 加锁成功，执行业务
            System.out.println("获取分布式锁成功");
            Map<String, List<CateLog2Vo>> dataFromDb = null;
            try {
                dataFromDb = getCatalogJsonFromDb();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList("lock"), uuid);
            }

            //该操作不具有原子性，有瑕疵。
            //            String lockValue = redisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                // uuid一致，可以确保删除的是自己的锁。原因：假设getDataFromDb执行了400s,此时自己的锁已经失效。如果不判断会导致删除了别人的锁
//                redisTemplate.delete("lock");
//            }
            // 原子删除

            return dataFromDb;
        } else {
            // 加锁失败，重试。synchronized
            // 休眠100ms重试
            System.out.println("获取分布式锁不成功,等待重试");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        }

    }

    private Map<String, List<CateLog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        // 1.锁的名字。锁的粒度，越细越快
        // 锁的粒度：具体缓存的是某个数据，11-号商品:product-11-lock
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<CateLog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }


    public Map<String, List<CateLog2Vo>> getCatalogJsonFromDb() {
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        // 1. 查出所有1级分类
        List<CategoryEntity> level1Categories = getParentCid(selectList, 0L);

        // 2. 封装数据
        Map<String, List<CateLog2Vo>> result = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2.1 查出1该一级分类下对应的所有二级分类
            List<CategoryEntity> level2CategoryEntities = getParentCid(selectList, v.getCatId());
            // 2.2对该二级分下的数据进行组装
            List<CateLog2Vo> cateLog2VoList = null;
            if (level2CategoryEntities != null) {
                // 2.3 组装好指定一级分类下的二级分类列表cateLog2VoList
                cateLog2VoList = level2CategoryEntities.stream().map(level2CategoryEntity -> {
                    CateLog2Vo cateLog2Vo = new CateLog2Vo(v.getCatId().toString(), null, level2CategoryEntity.getCatId().toString(), level2CategoryEntity.getName());
                    List<CategoryEntity> level3CategoryEntities = getParentCid(selectList, level2CategoryEntity.getCatId());
                    if (level3CategoryEntities != null) {
                        List<CateLog2Vo.CateLog3Vo> catalog3List = level3CategoryEntities.stream().map(level3CategoryEntity -> {
                            return new CateLog2Vo.CateLog3Vo(level2CategoryEntity.getCatId().toString(), level3CategoryEntity.getCatId().toString(), level3CategoryEntity.getName());
                        }).collect(Collectors.toList());
                        cateLog2Vo.setCatalog3List(catalog3List);
                    }
                    return cateLog2Vo;
                }).collect(Collectors.toList());
            }
            return cateLog2VoList;
        }));
        return result;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parent_cid) {
        return selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 收集当前节点ID
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            // 1. 找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }


}