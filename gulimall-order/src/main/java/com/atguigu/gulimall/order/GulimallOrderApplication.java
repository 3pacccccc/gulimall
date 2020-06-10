package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMq
 * 1.引入amqp场景: RabbitAutoConfiguration就会生效
 * 2.给容器中自动配置了
 *      rabbitConnectionFactory、AmqpAdmin、RabbitTemplate等bean
 *      所有的属性都是@ConfigurationProperties(prefix = "spring.rabbitmq")里面配置
 * 3. 给配置文件中配置spring.rabbitmq信息
 * 4. @EnableRabbit; @EnableXxxx
 * 5. 监听消息: 使用@RabbitListener: 必须有@EnableRabbit
 *      @RabbitListener: 可以标注在类+方法上(监听哪些队列即可)
 *      @RabbitHandler: 标在方法上(重载区分不同的消息，即同一个队列，里面有不同类的消息。RabbitHandler标注在方法上之后来处理不同的类)
 *
 * 6. 本地事务失效问题：
 * 同一个对象内事务方法互调默认失效，原因：绕过了代理对象，事务使用代理对象来控制的
 * 解决：使用代理对象来调用事务方法
 *      1). 引入aop-starter、spring-boot-starter-aop；引入了aspectj
 *         <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-aop</artifactId>
 *         </dependency>
 *      2). @EnableAspectJAutoProxy(exposeProxy = true); 开启aspectj动态代理功能。以后所有的动态代理都是aspectj创建的(即使没有接口也可以创建动态代理)
 *      3). 本类互调用调用对象
 *         OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
 *         orderService.b();
 *         orderService.c();
 *
 *  seata控制分布式事务
 *      1). 每一个微服务数据库中先必须创建一个undo_log表
 *      2). 安装事务协调器: seata-server: https://github.com/seata/seata/release
 *      3). 整合
 *          1.导入依赖: spring-cloud-starter-alibaba-seata seata-all-0.7.1
 *          2. 解压并启动seata-server
 *                  registry.conf: 注册中心配置；修改registry type=nacos
 *                  file.conf;
 *          3. 所有想要用到分布式事务的微服务使用seata DataSourceProxy代理自己的数据源
 *          4. 每个微服务，都必须导入registry.conf、file.conf文件到resources目录,并修改 file.conf里面的vgroup_mapping.{application-name}-fescar-service-group="default"
 *          5. 启动服务
 *          6. 给分布式大事务的入口标注@GlobalTransactional,其他服务还是标注@Transaactional
 */

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
