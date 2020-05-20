package com.atguigu.gulimall.member.exception;

/**
 * @author: maruimin
 * @date: 2020/5/20 20:36
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号已经存在");
    }
}
