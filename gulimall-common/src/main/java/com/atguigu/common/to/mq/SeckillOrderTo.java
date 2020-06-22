package com.atguigu.common.to.mq;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderTo {

    private String orderSn; // ������

    private Long promotionSessionId; // �����ID

    private Long skuId; // ��ƷID

    private BigDecimal seckillPrice; // ��ɱ�۸�

    private Integer num; // ��������

    private Long memberId; // ��ԱID
}
