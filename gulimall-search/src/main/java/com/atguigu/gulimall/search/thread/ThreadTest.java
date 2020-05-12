package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author: maruimin
 * @date: 2020/5/11 21:50
 */
public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main ... start ...");
        // 方法1：无返回结果的
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果:" + i);
//        }, executor);

        // 方法2：有返回结果的,并且使用whenComplete，exceptionally等方法处理结果
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果:" + i);
//            return i;
//        }, executor).whenComplete((res, exception) -> {
//            // whenComplete虽然能够得到异常信息，但是没法修改返回数据
//            System.out.println("异步任务完成了... 结果是:" + res + "异常是:" + exception);
//        }).exceptionally(t -> {
//            // 可以感知异常，同时返回默认值
//            return 10;
//        });
//        Integer integer = future.get();

        // 方法3：用handle处理返回。方法执行完成后的处理
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getId());
//            int i = 3 / 4;
//            System.out.println("运行结果:" + i);
//            return i;
//        }, executor).handle((res, thr) -> {
//            if (res != null) {
//                return res * 2;
//            } else if (thr != null) {
//                return 0;
//            } else {
//                return 0;
//            }
//        });
//        Integer integer = future.get();

        /**
         * 方法5: 线程串行化
         * 1). thenRun: 不能获取到上一步的执行结果，无返回值
         * thenRunAsync(() -> {
         *             System.out.println("任务2启动了");
         *         }, executor);
         *
         * 2). thenAcceptAsync能接收上一步结果，但是无返回值
         * thenAcceptAsync((res) -> {
         *             System.out.println("任务2启动了" + res);
         *         }, executor)
         *
         * 3). thenApplyAsync 能接收上一步结果，也有返回值
         * .thenApplyAsync((res) -> {
         *             System.out.println("任务2启动了... " + res);
         *             return "hello" + res;
         *         }, executor)
         */
//        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果:" + i);
//            return i;
//        }, executor).thenApplyAsync((res) -> {
//            System.out.println("任务2启动了... " + res);
//            return "hello" + res;
//        }, executor);

        // 方法6: 两个都完成
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程:" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("任务1结束");
            return i;
        }, executor);
        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程:" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("任务1结束");
            return "hello";
        }, executor);

        // runAfterBothAsync无法感知future01跟future02的结果
//        future01.runAfterBothAsync(future02, () -> {
//            System.out.println("任务3开始");
//        }, executor);

        // thenAcceptBothAsync可以接收前两个的结果，无返回值
//        future01.thenAcceptBothAsync(future02, (f1, f2) -> {
//            System.out.println("任务3开始... 之前的结果" + f1 + f2);
//        }, executor);

        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
            return f1 + ":" + f2 + ": hello";
        }, executor);
        System.out.println("main ... end ..." + future.get());

    }

    public void thread(String[] args) throws ExecutionException, InterruptedException {
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
         * ThreadPoolExecutor七大参数:
         *      corePoolSize: 核心线程数(会一直存在。除非设置了allowCoreThreadTimeOut,会将空闲的线程回收)；线程池创建好以后就准备就绪的线程数量，就等待来接受异步任务去执行。
         *      maximumPoolSize：线程池中的最大线程数量。用来控制资源
         *      keepAliveTime： 存活时间，如果当前的线程数量大于core数量。释放空闲的线程(maximumPoolSize-corePoolSize)。只要线程大于指定的keepAliveTime
         *      unit: 时间单位
         *      BlockingQueue<Runnable> workQueue: 阻塞队列，如果任务有很多，就会将目前多的任务放在队列里面。只要有线程空闲，就回去队列里面取出新的任务继续执行。
         *      threadFactory：线程的创建工厂
         *      RejectedExecutionHandler handler：如果队列满了，按照我们指定的拒绝策略拒绝执行任务
         *
         *  工作顺序:
         *      1).线程池创建，准备好core数量的核心线程，准备接受任务
         *      1.1 core满了，就将再进来的任务放入阻塞队列中。空闲的core就会自己去阻塞队列获取任务执行
         *      1.2 阻塞队列满了，就直接开新线程执行。最大只能开大max指定的数量。
         *      1.3 max满了就用RejectedExecutionHandler拒绝任务
         *      1.4 max都执行任务完成，有很多空闲，在指定的时间keepAliveTime以后，释放max-core这些线程
         *
         *      new LinkedBlockingDeque<>(): 默认容量是integer的最大值，所以为了避免内存不足，必须自己指定容量。
         *
         *  问题：一个线程池 core=7,max=20,queue:50,100并发进来怎么分配？
         *      7个会立即执行，50个会进入阻塞队列。17个会另外开线程执行。剩下30个使用拒绝策略。执行完空闲超过keepAliveTime之后，13个线程会被回收
         *
         * 另外几种常见的线程池：
         *         Executors.newCachedThreadPool(); // core是0，所有都可回收
         *         Executors.newFixedThreadPool(10); // 固定大小，core=max，都不可回收
         *         Executors.newScheduledThreadPool(5); // 定时任务的线程池
         *         Executors.newSingleThreadExecutor(); // 单线程的线程池，后台从队列里面获取任务，挨个执行
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
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
