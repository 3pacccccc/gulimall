package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.ExecutionException;

/**
 * @author: maruimin
 * @date: 2020/5/30 23:07
 */

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    /**
     * 下单
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo) {
        /**
         * 下单：去创建订单，验令牌，验价格，锁库存
         * 下单成功来到支付选项页
         * 下单失败回到订单确认页重新确认订单信息
         */
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        if (responseVo.getCode() == 0 ){
            // 下单成功来到支付选项页
            return "pay";
        } else {
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

}
