package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author: maruimin
 * @date: 2020/5/30 16:14
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 可以让生产者发送的对象信息，以json的格式发送到broker
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1. broker收到消息的回调
     *      1.spring.rabbitmq.publisher-confirm-type=correlated?(我猜的。以前的spring.rabbitmq.publisher-confirms=true已经弃用了)
     *      2.设置确认回调
     * 2. 消息抵达队列消费者的回调
     *      1. spring.rabbitmq.publisher-returns=true
     *      2. spring.rabbitmq.template.mandatory=true(非必须)
     *
     * 3. 消费端确认(保证每个消息都被正确消费，此时broker才可以删除这个消息)
     *      1.默认是auto-ack自动确认的，只要消息接收到，客户端会自动确认，broker就会删除这个消息
     *      问题:
     *              我们收到很多消息，自动回复给服务器，只有一个消息处理成功，宕机了，发生消息丢失；
     *              消费者手动确认模式，只要我们没有ack，消息就不会被删除。及时宕机了消息也会一直存在
     *
     */
    @PostConstruct // MyRabbitConfig构建完成之后执行这个方法
    public void initRabbitTemplate() {
        // 设置消息抵达broker确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                System.out.println(correlationData + "-" + b + ":" + s);
            }
        });

        // 设置消息抵达队列的确认回调(在成功抵达的时候不会回调，失败了才会回调这个方法)
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

            }
        });
    }

}
