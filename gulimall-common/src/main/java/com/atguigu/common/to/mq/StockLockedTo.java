package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * @author: maruimin
 * @date: 2020/6/13 16:40
 */

@Data
public class StockLockedTo {

    private Long id; // 库存工作单ID

    private StockDetailTo detailTo; // 工作详情的所有ID

}
