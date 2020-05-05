package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.CateLog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author: maruimin
 * @date: 2020/5/1 1:01
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 1. 查出所有的1级分类
        List<CategoryEntity> level1Categories = categoryService.getLevel1Categories();
        model.addAttribute("categories", level1Categories);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String, List<CateLog2Vo>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 1. 获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        // 2.加锁
        lock.lock(); // 不指定时间，默认30s过期，如果代码业务没有执行完会自动续期，如果执行过程中系统崩溃，则30s后锁在redis中自动被删除
//        lock.lock(10, TimeUnit.SECONDS); // 指定时间,10s之后自动解锁。解锁时间必须大于业务时间，因为不会自动续期，否则会出现系统异常。
        // 如果传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间。
        // 如果我们未指定锁的超时时间，就使用30 * 1000【lockWatchdogTimeout看门狗的默认时间】
        //      只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】
        //      internalLockLeaseTime【看门狗时间】 / 3 -> 10s;

        // 最佳实战：
        // 1).  lock.lock(10, TimeUnit.SECONDS)最好使用指定时间的方法，没有续期的操作，时间设置大一点。
        try {
            System.out.println("加锁成功，执行业务" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放锁......." + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        String s = "";
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.writeLock();
        try {
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    // 保证一定能读到最新的数据，修改期间，写锁是一个排他锁(互斥锁)。读锁是一个共享锁
    // 写锁没释放读就必须等待
    // 写 + 读：等待写锁释放
    // 写 + 写：阻塞方式
    // 读 + 写：有读锁，写锁也会等待读锁释放后进行。
    // 读 + 读：相当于无锁,并发读，只会在redis中记录好，所有当前的读锁，他们都在同时加锁成功。
    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        String s = "";
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        try {
            s = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }


    /**
     * 车库停车
     * 信号量也可以用作分布式限流
     *
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
//        park.acquire(); // 获取一个车位。如果没有获取到，会一直阻塞在这里，直到获取到
        boolean b = park.tryAcquire(); //会返回一个布尔值，直接返回结果
        return "ok";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release(); // 释放一个车位
        return "ok";
    }

    /**
     * 放假锁门
     * 5个班的全部走完，才可以锁大门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch countDownLatch = redisson.getCountDownLatch("door");
        countDownLatch.trySetCount(5);
        countDownLatch.await();
        return "ok";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Integer id) {
        RCountDownLatch countDownLatch = redisson.getCountDownLatch("door");
        countDownLatch.countDown();
        return id + "班的人都走了！";
    }

    /**
     * 数据库与redis缓存一致性的解决方案：
     *  1.缓存的所有数据都有过期时间，数据过期下一次查询触发主动更新
     *  2.读写数据的时候，加上分布式的读写锁。
     *  3. 经常写，经常读的操作，这种解决方案会对系统性能造成印象，所以经常写，经常读的操作建议不上缓存。
     */
}
