package org.to2mbn.jmccc.mcdownloader.util;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPoolUtils {

    private ThreadPoolUtils() {
    }

    public static ThreadFactory createNamedThreadFactory(String name) {
        return new NamedThreadFactory(Objects.requireNonNull(name));
    }

    public static ThreadPoolExecutor createPool(int threads, long keepAliveTime, TimeUnit unit, String poolName) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>(), createNamedThreadFactory(poolName));
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final String name;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final int poolIdx;

        public NamedThreadFactory(String name) {
            this.name = name;
            poolIdx = poolNumber.getAndIncrement();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "pool-" + poolIdx + "-" + name + "-thread-" + threadNumber.getAndIncrement());
            return t;
        }

    }
}
