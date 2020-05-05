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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

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
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            // 2.缓存中没有，查询数据库
            System.out.println("走的是数据库");
            Map<String, List<CateLog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();
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