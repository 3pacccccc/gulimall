package com.atguigu.common.mq;

import lombok.Data;

/**
 * @author: maruimin
 * @date: 2020/6/13 16:49
 */

@Data
public class StockDetailTo {

    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    private Long wareId;

    private Integer lockStatus;

}
