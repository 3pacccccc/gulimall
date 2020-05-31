package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: maruimin
 * @date: 2020/5/30 20:55
 */
@Controller
public class HelloController {

    @GetMapping("{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }

}
