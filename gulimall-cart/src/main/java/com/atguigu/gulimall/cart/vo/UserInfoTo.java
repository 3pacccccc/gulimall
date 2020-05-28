package com.atguigu.gulimall.cart.vo;

import lombok.Data;

/**
 * @author: maruimin
 * @date: 2020/5/28 21:29
 */
@Data
public class UserInfoTo {

    private String userId;

    private String userKey;

    private boolean tempUser = false;
}
