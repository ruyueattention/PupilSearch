package com.ruyue.threadtest;


import java.util.concurrent.Callable;

public class ThreadTask implements Callable {

    public ThreadTask() throws InterruptedException {

    }
    public Object call() {

        System.out.println(Thread.currentThread().getName());

        return null;
    }
}
