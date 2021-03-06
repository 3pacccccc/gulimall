package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.CateLog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author maruimin
 * @email maruimin666@gmail.com
 * @date 2020-04-01 23:12:17
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> catIds);

    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity categoryEntity);

    List<CategoryEntity> getLevel1Categories();

    Map<String, List<CateLog2Vo>> getCatalogJson();
}

