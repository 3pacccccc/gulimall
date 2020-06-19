package com.atguigu.gulimall.seckill.scheduled;


import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 秒杀商品的定时上架
 *      每天晚上3点；商家最近3天需要秒杀的商品。
 *          当天00：00：00 - 23：59：59
 *          明天00：00：00 - 23：59：59
 *          后天00：00：00 - 23：59：59
 */

@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // 1. 重复上架无需处理
        seckillService.uploadSeckillSkuLatest3Days();
    }
}

