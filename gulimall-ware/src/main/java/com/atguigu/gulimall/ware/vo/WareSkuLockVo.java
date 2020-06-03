package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: maruimin
 * @date: 2020/6/3 23:23
 */

@Data
public class WareSkuLockVo {

    private String orderSn; // 订单号

    private List<OrderItemVo> locks; // 需要锁住的所有库存信息

}
