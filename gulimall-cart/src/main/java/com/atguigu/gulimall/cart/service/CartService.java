package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItem;

/**
 * @author: maruimin
 * @date: 2020/5/28 21:18
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num);
}
