package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author: maruimin
 * @date: 2020/6/3 23:25
 */

@Data
public class LockStockResult {

    private Long skuId;

    private Integer num;

    private Boolean locked;
}
