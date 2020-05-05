package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author: maruimin
 * @date: 2020/5/5 19:39
 */

@Configuration
public class MyRedissonConfig {
    /**
     * reddison相对于手动操作redis setnx的优点：
     * 1. 锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s, 不用担心业务时间长，锁自动过期被删掉。
     * 2. 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s之后删除
     */

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException {
        //1. 创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://49.234.18.154:6380");

        //2.根据config创建出redissonClient示例
        return Redisson.create(config);
    }
}
