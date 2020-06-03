package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.LockStockResult;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author maruimin
 * @email maruimin666@gmail.com
 * @date 2020-04-02 15:07:31
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    List<LockStockResult> orderLockStock(WareSkuLockVo vo);
}

