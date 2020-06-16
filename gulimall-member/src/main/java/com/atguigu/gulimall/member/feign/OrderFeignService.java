package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author: maruimin
 * @date: 2020/6/16 23:00
 */

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @PostMapping("/order/order/listWithItem")
    //@RequiresPermissions("order:order:list")
    R listWithItem(@RequestBody Map<String, Object> params);
}
