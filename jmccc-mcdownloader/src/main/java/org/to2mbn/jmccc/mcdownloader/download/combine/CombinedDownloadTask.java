package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;

abstract public class CombinedDownloadTask<T> {

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
		return new SingleCombinedDownloadTask<T>(task);
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
		return new MultipleCombinedDownloadTask(tasks);
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
			throw new IllegalArgumentException("The length of tasks cannot be zero");
		}
		return new AnyCombinedDownloadTask<>(tasks, expectedExceptions);
	}

	abstract public void execute(CombinedDownloadContext<T> context) throws Exception;

	public <R> CombinedDownloadTask<R> andThen(ResultProcessor<T, R> processor) {
		return new AppendedCombinedDownloadTask<>(this, processor);
	}

	public <R> CombinedDownloadTask<R> andThenDownload(ResultProcessor<T, CombinedDownloadTask<R>> then) {
		return new ExtendedDownloadTaskCombinedDownloadTask<>(this, then);
	}

	public <R> CombinedDownloadTask<R> andThenCall(ResultProcessor<T, Callable<R>> then) {
		return new ExtendedCallableCombinedDownloadTask<>(this, then);
	}

	public <R> CombinedDownloadTask<R> andThenReturn(final R result) {
		return new AppendedCombinedDownloadTask<>(this, new ResultProcessor<T, R>() {

			@Override
			public R process(T arg) throws Exception {
				return result;
			}
		});
	}

}
