package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: maruimin
 * @date: 2020/6/2 22:37
 */

@Data
public class OrderSubmitVo {

    private Long addrId; // 收货地址的id

    private Integer payType; // 支付方式

    private String orderToken; // 防重令牌

    private BigDecimal payPrice; // 应付价格 验价

    private String note; // 订单备注

}
