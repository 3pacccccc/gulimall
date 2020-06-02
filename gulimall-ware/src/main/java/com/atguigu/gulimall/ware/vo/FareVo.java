package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: maruimin
 * @date: 2020/6/2 22:02
 */
@Data
public class FareVo {

    private MemberAddressVo address;

    private BigDecimal fare;

}
