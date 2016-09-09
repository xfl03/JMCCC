package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.io.IOException;
import java.util.Objects;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;

/**
 * A task that can derive multiple DownloadTasks.
 * 
 * @param <T>
 * @author yushijinhun
 * @see DownloadTask
 */
abstract public class CombinedDownloadTask<T> {

	public static enum CacheStrategy {
		DEFAULT, FORCIBLY_CACHE, CACHEABLE, NON_CACHEABLE;
	}

	/**
	 * Creates a CombinedDownloadTask from a DownloadTask.
	 * 
	 * @param task the download task
	 * @param <T> the type of the DownloadTask
	 * @return the CombinedDownloadTask
	 * @throws NullPointerException if <code>task == null</code>
	 */
	public static <T> CombinedDownloadTask<T> single(DownloadTask<T> task) {
		Objects.requireNonNull(task);
		return new SingleCombinedTask<T>(task);
	}

	public static CombinedDownloadTask<Void> multiple(DownloadTask<?>... tasks) {
		Objects.requireNonNull(tasks);
		CombinedDownloadTask<?>[] combinedTasks = new CombinedDownloadTask<?>[tasks.length];
		for (int i = 0; i < tasks.length; i++) {
			combinedTasks[i] = single(tasks[i]);
		}
		return multiple(combinedTasks);
	}

	public static CombinedDownloadTask<Void> multiple(CombinedDownloadTask<?>... tasks) {
		Objects.requireNonNull(tasks);
		return new MultipleCombinedTask(tasks);
	}

	@SafeVarargs
	public static <T> CombinedDownloadTask<T> any(DownloadTask<T>... tasks) {
		Objects.requireNonNull(tasks);

		@SuppressWarnings("unchecked")
		CombinedDownloadTask<T>[] combinedTasks = new CombinedDownloadTask[tasks.length];
		for (int i = 0; i < tasks.length; i++) {
			combinedTasks[i] = single(tasks[i]);
		}
		return any(combinedTasks);
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <T> CombinedDownloadTask<T> any(CombinedDownloadTask<T>... tasks) {
		Objects.requireNonNull(tasks);
		return any(new Class[] { IOException.class }, tasks);
	}

	@SafeVarargs
	public static <T> CombinedDownloadTask<T> any(Class<? extends Throwable>[] expectedExceptions, CombinedDownloadTask<T>... tasks) {
		Objects.requireNonNull(tasks);
		Objects.requireNonNull(expectedExceptions);
		if (tasks.length == 0) {
			throw new IllegalArgumentException("Tasks cannot be empty");
		}
		return new AnyCombinedTask<>(tasks, expectedExceptions);
	}

	abstract public void execute(CombinedDownloadContext<T> context) throws Exception;

	public CacheStrategy getCacheStrategy() {
		return CacheStrategy.DEFAULT;
	}

	public String getCachePool() {
		return null;
	}

	public final CombinedDownloadTask<T> cacheable(CacheStrategy strategy) {
		Objects.requireNonNull(strategy);
		if (getCacheStrategy() == strategy) {
			return this;
		}
		return new CombinedTaskCacheStrategyDecorator<>(this, strategy);
	}

	public final CombinedDownloadTask<T> cachePool(String pool) {
		if (Objects.equals(getCachePool(), pool)) {
			return this;
		}
		return new CombinedTaskCachePoolDecorator<>(this, pool);
	}

	public final <R> CombinedDownloadTask<R> andThen(ResultProcessor<T, R> processor) {
		return new AndThenCombinedTask<>(this, processor);
	}

	public final <R> CombinedDownloadTask<R> andThenDownload(ResultProcessor<T, CombinedDownloadTask<R>> then) {
		return new AndThenDownloadCombinedTask<>(this, then);
	}

	public final <R> CombinedDownloadTask<R> andThenReturn(final R result) {
		return andThen(new ResultProcessor<T, R>() {

			@Override
			public R process(T arg) throws Exception {
				return result;
			}
		});
	}

}
