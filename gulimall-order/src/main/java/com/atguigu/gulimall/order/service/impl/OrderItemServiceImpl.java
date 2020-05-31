package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues: 声明需要监听的所有队列
     * org.springframework.amqp.core.Message
     * 参数可以写以下类型：
     * 1. Message message: 原声消息详细信息，头+体
     * 2. T<发送消息的类型> OrderReturnReasonEntity content;
     * 3. Channel channel: 当前传输数据的通道
     *
     * Queue: 可以很多人都来监听。只要收到消息，只能有一个客户端收到
     * 场景：
     *      1). 订单服务启动多个： 同一个消息，只能有一个客户端收到
     *      2). 只有一个消息完全处理完，方法运行结束。我们就可以接收到下一个消息(ps: 可以设置spring.rabbitmq.prefetch, spring.rabbitmq.maxConcurrency等一次处理多个)
     */
//    @RabbitListener(queues = {"hello-java-queue"}) // queues = {"hello-java-queue"}可以监听多个队列
//    public void receiveMessage(Message message,
//                               OrderReturnReasonEntity content,
//                               Channel channel) {
//        byte[] body = message.getBody();
//        MessageProperties messageProperties = message.getMessageProperties();
//
//        // 手动签收
//        long deliveryTag = message.getMessageProperties().getDeliveryTag();
//        try {
//            channel.basicAck(deliveryTag, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


}