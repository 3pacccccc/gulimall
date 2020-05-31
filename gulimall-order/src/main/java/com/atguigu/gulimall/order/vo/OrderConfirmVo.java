package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: maruimin
 * @date: 2020/5/31 14:12
 */

public class OrderConfirmVo {

    // 收货地址，ums_member_receive_address表
    @Setter
    @Getter
    List<MemberAddressVo> address;

    // 所有选中的购物项
    @Setter
    @Getter
    List<OrderItemVo> items;

    // 发票记录

    // 优惠券信息
    @Setter
    @Getter
    Integer integration;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }

    // 防重令牌
    @Setter @Getter
    String orderToken;

}
