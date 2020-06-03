package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: maruimin
 * @date: 2020/6/3 21:09
 */

@Data
public class FareVo {

    private MemberAddressVo address;

    private BigDecimal fare;

}
