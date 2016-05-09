package org.to2mbn.jmccc.mcdownloader.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ThreadPoolUtils {

	public static ThreadPoolExecutor createPool(int threads, long keepAliveTime, TimeUnit unit) {
		return new ThreadPoolExecutor(0, threads, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>());
	}

	private ThreadPoolUtils() {}
}
