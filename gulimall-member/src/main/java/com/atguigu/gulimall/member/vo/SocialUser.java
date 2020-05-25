package com.atguigu.gulimall.member.vo;

import lombok.Data;

/**
 * @author: maruimin
 * @date: 2020/5/21 21:53
 */

@Data
public class SocialUser {

    private String access_token;

    private String remind_min;

    private String expires_in;

    private String uid;

    private String isRealName;

}
