package com.atguigu.gulimall.member.exception;

/**
 * @author: maruimin
 * @date: 2020/5/20 20:36
 */
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名已经存在");
    }
}
