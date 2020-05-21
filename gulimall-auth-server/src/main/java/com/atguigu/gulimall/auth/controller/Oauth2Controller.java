package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: maruimin
 * @date: 2020/5/21 21:43
 */

@Controller
public class Oauth2Controller {

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "2558995808");
        map.put("client_secret", "8ae6c9a73882582c8f9890df8a4b6df6");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);

        // 1. 根据code换取access token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", header, query, map);

        // 2. 处理access token
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了access token
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }

        return "redirect:https://baidu.com";
    }
}
