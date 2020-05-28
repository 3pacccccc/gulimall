package com.atguigu.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: maruimin
 * @date: 2020/5/28 20:57
 */
public class Cart {

    List<CartItem> items;

    private Integer countNum;  // 商品数量

    private Integer countType; // 商品类型数量

    private BigDecimal totalAmount;  // 商品总价

    private BigDecimal reduce = new BigDecimal("0"); // 减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem cartItem : items) {
                count += cartItem.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem cartItem : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 1. 计算购物车总价
        if (items != null && items.size() > 0) {
            for (CartItem cartItem : items) {
                amount = amount.add(cartItem.getTotalPrice());
            }
        }
        // 2. 减去优惠价格
        return amount.subtract(getReduce());
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
