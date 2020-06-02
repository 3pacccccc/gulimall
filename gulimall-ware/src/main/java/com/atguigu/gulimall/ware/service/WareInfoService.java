package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.FareVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author maruimin
 * @email maruimin666@gmail.com
 * @date 2020-04-02 15:07:31
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据用户的收货地址计算运费
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}

