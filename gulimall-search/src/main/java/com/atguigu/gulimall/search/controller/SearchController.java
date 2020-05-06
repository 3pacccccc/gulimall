package com.atguigu.gulimall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: maruimin
 * @date: 2020/5/6 23:39
 */
@Controller
public class SearchController {

    @GetMapping("/list.html")
    public String listPage() {
        return "list";
    }
}
