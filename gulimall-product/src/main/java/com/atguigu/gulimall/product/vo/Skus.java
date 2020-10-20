/**
 * Copyright 2019 bejson.com
 */
package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * Auto-generated: 2019-11-26 10:50:34
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Skus {

    private List<Attr> attr;
    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;
    private List<Images> images;
    private List<String> descar;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;


    public static void main(String[] args) {
        Connection con;
        String driver = "com.mysql.jdbc.Driver";
        //这里我的数据库是cgjr
        String url = "jdbc:mysql://49.234.18.154:3306/django_test?useUnicode=true&characterEncoding=utf-8&useSSL=false";
        String user = "xiaoma";
        String password = "555dedd+5";

        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, password);
            if (!con.isClosed()) {
                System.out.println("数据库连接成功");
            }
            Statement statement = con.createStatement();
            for (int i = 1; i <6; i++) {
                new Thread(() -> {
                    String sql3 = "UPDATE auth_group set name=\"徐志摩\" WHERE `id`=";
                    try {
                        statement.executeUpdate(sql3);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }, String.valueOf(i)).start();
            }

//               执行删除语句
            String sql4 = "delete from persons WHERE `name`=\"徐志摩\"";
            statement.executeUpdate(sql4);

//            执行调用存储过程

            String sql5 = "call add_student(3)";
            statement.executeUpdate(sql5);


//            关闭连接
            con.close();
            System.out.println("数据库已关闭连接");
        } catch (ClassNotFoundException e) {
            System.out.println("数据库驱动没有安装");

        } catch (SQLException e) {
            System.out.println("数据库连接失败");
        }
    }

}
