package com.github.jarome.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;


public class ThreadFactoryImpl implements ThreadFactory {
    private final AtomicLong threadIndex;
    private final String threadNamePrefix;
    private final boolean daemon;

    public ThreadFactoryImpl(String threadNamePrefix) {
        this(threadNamePrefix, false);
    }

    public ThreadFactoryImpl(String threadNamePrefix, boolean daemon) {
        this.threadIndex = new AtomicLong(0L);
        this.threadNamePrefix = threadNamePrefix;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, this.threadNamePrefix + this.threadIndex.incrementAndGet());
        thread.setDaemon(this.daemon);
        return thread;
    }
}
