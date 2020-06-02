package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author: maruimin
 * @date: 2020/6/2 22:44
 */

@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    private Integer code;

}
