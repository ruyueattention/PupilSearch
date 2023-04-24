package com.ruyue.util;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class UniqueLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

    private static final long serialVersionUID = 6523405086129214113L;
    private final ReentrantLock putLock = new ReentrantLock();

    @Override
    public void put(E e) throws InterruptedException {
        putLock.lock();
        try {
            if (!contains(e)) {
                super.put(e);
            }
        } finally {
            putLock.unlock();
        }
    }

    @Override
    public boolean offer(E e) {
        if (!contains(e)) {
            super.offer(e);
        }
        return true;
    }


}

