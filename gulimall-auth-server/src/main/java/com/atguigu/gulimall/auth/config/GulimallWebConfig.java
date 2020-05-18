package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: maruimin
 * @date: 2020/5/18 20:55
 */

@Configuration
public class GulimallWebConfig  implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /**
         * 可以更加便捷地映射html页面
         *     @GetMapping("/login.html")
         *     public String loginPage() {
         *         return "login";
         *     }
         *
         *     @GetMapping("/reg.html")
         *     public String regPage() {
         *         return "reg";
         *     }
         */
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
