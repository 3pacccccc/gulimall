package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: maruimin
 * @date: 2020/5/1 11:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CateLog2Vo {

    private String catalog1Id; // 1级父分类ID

    private List<CateLog3Vo> catalog3List; // 三级子分类

    private String id;

    private String name;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CateLog3Vo {
        private String catalog2Id; // 父分类，2级分类id

        private String id;

        private String name;
    }

}
