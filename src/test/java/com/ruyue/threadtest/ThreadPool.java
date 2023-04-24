package com.ruyue.threadtest;

import java.util.concurrent.*;



public class ThreadPool {
    private static ExecutorService pool;
    public static void main(String[] args) throws InterruptedException {
        LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();

        pool = new ThreadPoolExecutor(3, 3, 1000, TimeUnit.MILLISECONDS,  new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        for(int a=0;a<1000;a++){
            pool.submit(new ThreadTask());
        }

    }
}


