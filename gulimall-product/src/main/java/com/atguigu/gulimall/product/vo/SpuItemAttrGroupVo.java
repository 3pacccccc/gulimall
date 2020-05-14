package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: maruimin
 * @date: 2020/5/14 22:33
 */
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
