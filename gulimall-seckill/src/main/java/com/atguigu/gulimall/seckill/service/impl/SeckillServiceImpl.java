package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // 后面再加一个商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 1.扫描需要参与秒杀的商品
        R session = couponFeignService.getLatest3DaySession();
        if (session.getCode() == 0) {
            // 上架商品
            List<SeckillSessionsWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            // 缓存到redis
            //1. 缓存活动信息
            saveSessionInfos(sessionData);
            //2. 缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    // 返回当前时间可以参与的秒杀商品
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        // 1. 确定当前时间属于哪个秒杀场次
        // 1970 -
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        assert keys != null;
        for (String key : keys) {
            // seckill:sessions:1571280398129_15823984723957
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                // 2. 获取这个秒杀场次需要的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<Object> list = hashOps.multiGet(range);
                if (list != null) {
                    return list.stream().map(item -> {
                        return JSON.parseObject((String) item, SeckillSkuRedisTo.class);
                    }).collect(Collectors.toList());
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //1 .找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

                    // 随机码
                    long current = new Date().getTime();
                    if (current > skuRedisTo.getStartTime() && current <= skuRedisTo.getEndTime()) {
                    } else {
                        skuRedisTo.setRandomCode(null);
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {

        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();

        // 1. 获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SeckillSkuRedisTo redis = JSON.parseObject(json, SeckillSkuRedisTo.class);
            // 校验合法性
            Long startTime = redis.getStartTime();
            Long endTime = redis.getEndTime();
            long time = new Date().getTime();
            long ttl = endTime - time;

            // 1. 校验时间的合法性
            if (time >= startTime && time <= endTime) {
                // 2. 校验随机码和商品Id
                String randomCode = redis.getRandomCode();
                String skuId = redis.getPromotionId() + "_" + redis.getSkuId();
                if (randomCode.equals(key) && killId.equals(skuId)) {
                    // 3. 验证购物数量是否合理
                    if (num <= redis.getSeckillLimit()) {
                        // 4. 验证这个人是否已经购买过。幂等性，如果只要秒杀成功，就去站位 userId_sessionId_skuId
                        // setnx
                        String redisKey = respVo.getId() + "_" + skuId;
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            // 占位成功，说明该用户从来没有买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            boolean b = semaphore.tryAcquire(num);
                            if (b) {
                                // 秒杀成功
                                // 快速下单，发送MQ消息，10ms
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(respVo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redis.getPromotionSessionId());
                                orderTo.setSkuId(redis.getSkuId());
                                orderTo.setSeckillPrice(redis.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                return timeId;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = redisTemplate.hasKey(key);
            if (!hasKey) {
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                // 缓存活动进redis
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            // 准备hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            String token = UUID.randomUUID().toString().replace("-", "");
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                if (!ops.hasKey(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString())) {
                    // 缓存商品
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    // 1. sku的基本数据
                    R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(info);
                    }
                    // 2. sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);

                    // 3. 设置上当前商品的秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    // 4. 给秒杀商品设置随机码
                    redisTo.setRandomCode(token);
                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(), jsonString);
                    // 5. 使用库存作为分布式的信号量，限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());

                }
            });
        });
    }
}
