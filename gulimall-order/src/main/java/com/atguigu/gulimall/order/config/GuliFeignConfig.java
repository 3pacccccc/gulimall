package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: maruimin
 * @date: 2020/5/31 15:14
 */

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        /**
         * 给容器中放一个RequestInterceptor，feign在调用其他服务的时候，就会扫描容器中的所有RequestInterceptor组件的apply方法，增强
         * request请求，我们这里在每一个请求中都加入原始请求的cookie请求头，防止出现请求头丢失问题！
         */
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1.RequestContextHolder可以拿到原始的前端发过来的http请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = attributes.getRequest();
                if (request != null) {
                    // 2. 同步请求头数据
                    String cookie = request.getHeader("Cookie");
                    template.header("Cookie", cookie);
                }
            }
        };
    }
}
