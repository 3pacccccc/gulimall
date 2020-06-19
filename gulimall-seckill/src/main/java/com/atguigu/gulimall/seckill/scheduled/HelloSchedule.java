package com.atguigu.gulimall.seckill.scheduled;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 *     1. @EnableScheduling开始定时任务
 *     2. @Scheduled 开启一个定时任务
 *     3. 自动配置类 TaskSchedulingAutoConfiguration
 * 异步任务
 *      1. @EnableAsync 开启异步任务功能
 *      2. @Async 给希望异步执行的方法上标注
 *      3. 自动配置类 TaskExecutionAutoConfiguration
 */
//@EnableScheduling
//@EnableAsync
@Component
@Slf4j
public class HelloSchedule {

    /**
     * 1. spring中6位组成，不允许第七位的年
     * 2. 在周几的位置，1-7代表周一到周日；MON-SUN
     * 3. 定时任务不应该阻塞，但是spring默认是阻塞的
     *      1). 可以让业务运行以异步的方式，自己提交到线程池
     *              CompletableFuture.runAsync(() -> {
     *                  xxxx.service.hello();
     *              }, executor);
     *      2). 支持定时任务线程池, 在application.properties中设置TaskSchedulingProperties自定义线程池的大小(但是不太管用)
     *              spring.task.scheduling.pool.size=5
     *      3).
     */
    @Async
    @Scheduled(cron = "* * * ? * 5")
    public void hello() throws InterruptedException {
        log.info("hello");
        Thread.sleep(3000);
    }

}
