package com.atguigu.gulimall.product.config;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: maruimin
 * @date: 2020/5/5 19:45
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class MyRedissonConfigTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void redisson() {

        System.out.println(redissonClient);
    }
}