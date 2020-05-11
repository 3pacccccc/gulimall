package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author: maruimin
 * @date: 2020/5/11 21:50
 */
public class ThreadTest {

    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /**
         * 1.继承Thread
         *         Thread01 thread01 = new Thread01();
         *         thread01.start();
         * 2.实现Runnable接口
         *         Runnable01 runnable01 = new Runnable01();
         *         new Thread(runnable01).start();
         * 3.实现Callable接口 + FutureTask(可以拿到返回结果，可以异常处理)
         *         FutureTask<Integer> integerFutureTask = new FutureTask<>(new Callable01());
         *         new Thread(integerFutureTask).start();
         *         // 阻塞等待整个线程执行完成，得到返回结果
         *         Integer integer = integerFutureTask.get();
         * 4.线程池[ExecutorService]
         *          给线程池提交任务.
         *         service.execute(new Runnable01());
         *         1.创建
         *              1). Executors;
         *              2).new ThreadPoolExecutor
         * 区别:
         *      1,2不能得到返回值，3可以获取返回值
         *      1、2、3都不能控制资源
         *      4可以控制资源，性能稳定。(1,2,3都是自动创建新的线程，会出现资源耗尽的问题，4是将线程维持在一个稳定的数量)
         */

        /**
         *
         */
        System.out.println("main .... start ....");
        service.execute(new Runnable01());
        System.out.println("main .... end ....");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果:" + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果:" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果:" + i);
            return i;
        }
    }

}
