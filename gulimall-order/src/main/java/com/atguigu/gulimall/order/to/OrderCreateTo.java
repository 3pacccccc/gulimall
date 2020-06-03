package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: maruimin
 * @date: 2020/6/3 19:56
 */

@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare; // 运费

}
