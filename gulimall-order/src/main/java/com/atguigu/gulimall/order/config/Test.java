package com.atguigu.gulimall.order.config;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


class test1 {


    volatile int number = 0;


    public void addTO60() {
        this.number = 60;
    }


    // 请注意,此时的number前面是加了volatile关键字修饰的，volatile不保证原子性
    public void addPlusPlus() {
        number++;
    }

    AtomicInteger atomicInteger = new AtomicInteger();

    public void addMyAtomic() {
        atomicInteger.getAndIncrement(); // 也是++的意思，初始值atomicInteger为1
    }


}


/**
 * 1验证volatile的可见性
 * 1.1 假如int number = 0; number变量之前根本没有添加volatile关键字修饰,没有可见性
 * 1.2 添加了volatile,可以解决可见性问题
 * 2 验证volatile不保证原子性
 * 2.1 原子性指的是什么意思?
 * 不可分割，完整性，也即某个线程正在做某个具体业务时，中间不可以被加塞或者被分割，需要整体完整
 * 要么同时成功，要么同时失败
 * 2.2 volatile不保证原子性的案例演示
 * 2.3 why
 * 2.4 如何解决原子性
 * * 加synchronized
 * * 使用juc下的AtomicInteger
 */


public class Test {

    public static void main(String[] args) {
        HashMap<String, String> a = new HashMap<>();
//        a.putAll();

    }
}

class Animal {
    public void move() {
        System.out.println("animal move");
    }
}

class Dog extends Animal {
    public void move() {
        System.out.println("dog move");
    }

    public void bark() {
        System.out.println("dog bark");
    }
}
