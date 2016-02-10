package org.to2mbn.jmccc.mcdownloader.download.multiple;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallback;

abstract public class MultipleDownloadTask<T> {

	private static class AppendedMultipleDownloadContext<R, S> implements MultipleDownloadContext<R> {

		ResultProcessor<R, S> processor;
		MultipleDownloadContext<S> proxied;

		AppendedMultipleDownloadContext(ResultProcessor<R, S> processor, MultipleDownloadContext<S> proxied) {
			this.processor = processor;
			this.proxied = proxied;
		}

		@Override
		public void done(R result) {
			try {
				proxied.done(processor.process(result));
			} catch (Exception e) {
				throw new IllegalStateException("unable to convert result", e);
			}
		}

		@Override
		public void failed(Throwable e) {
			proxied.failed(e);
		}

		@Override
		public void cancelled() {
			proxied.cancelled();
		}

		@Override
		public Future<?> submit(Runnable task, AsyncCallback<?> callback, boolean fatal) throws InterruptedException {
			return proxied.submit(task, callback, fatal);
		}

		@Override
		public <U> Future<U> submit(Callable<U> task, AsyncCallback<U> callback, boolean fatal) throws InterruptedException {
			return proxied.submit(task, callback, fatal);
		}

		@Override
		public <U> Future<U> submit(DownloadTask<U> task, DownloadCallback<U> callback, boolean fatal) throws InterruptedException {
			return proxied.submit(task, callback, fatal);
		}
		@Override
		public <U> Future<U> submit(MultipleDownloadTask<U> task, MultipleDownloadCallback<U> callback, boolean fatal) throws InterruptedException {
			return proxied.submit(task, callback, fatal);
		}

		@Override
		public void awaitAllTasks(Runnable callback) throws InterruptedException {
			proxied.awaitAllTasks(callback);
		}


	}

	private static class AppendedMultipleDownloadTask<R, S> extends MultipleDownloadTask<S> {

		ResultProcessor<R, S> processor;
		MultipleDownloadTask<R> proxied;

		AppendedMultipleDownloadTask(MultipleDownloadTask<R> proxied, ResultProcessor<R, S> processor) {
			this.proxied = proxied;
			this.processor = processor;
		}

		@Override
		public void execute(MultipleDownloadContext<S> context) throws Exception {
			proxied.execute(new AppendedMultipleDownloadContext<R, S>(processor, context));
		}

	}

	private static class SimpleMultipleDownloadTask<U> extends MultipleDownloadTask<U> {

		DownloadTask<U> task;

		public SimpleMultipleDownloadTask(DownloadTask<U> task) {
			this.task = task;
		}

		@Override
		public void execute(final MultipleDownloadContext<U> context) throws Exception {
			context.submit(task, new DownloadCallback<U>() {

				@Override
				public void done(U result) {
					context.done(result);
				}

				@Override
				public void failed(Throwable e) {
				}

				@Override
				public void cancelled() {
				}

				@Override
				public void updateProgress(long done, long total) {
				}

				@Override
				public void retry(Throwable e, int current, int max) {
				}
			}, true);
		}

	}

	private static class GroupMultipleDownloadTask extends MultipleDownloadTask<Object> {

		DownloadTask<?>[] tasks;

		public GroupMultipleDownloadTask(DownloadTask<?>[] tasks) {
			this.tasks = tasks;
		}

		@Override
		public void execute(final MultipleDownloadContext<Object> context) throws Exception {
			for (DownloadTask<?> task : tasks) {
				if (task == null) {
					context.submit(task, null, true);
				}
			}
			context.awaitAllTasks(new Runnable() {

				@Override
				public void run() {
					context.done(null);
				}
			});
		}

	}

	/**
	 * Creates a MultipleDownloadTask from a DownloadTask.
	 * 
	 * @param task the download task
	 * @param <T> the type of the DownloadTask
	 * @return the MultipleDownloadTask
	 * @throws NullPointerException if <code>task == null</code>
	 */
	public static <T> MultipleDownloadTask<T> simple(DownloadTask<T> task) {
		Objects.requireNonNull(task);
		return new SimpleMultipleDownloadTask<T>(task);
	}

	/**
	 * Creates a MultipleDownloadTask from a group of DownloadTasks.
	 * 
	 * @param tasks the download tasks, null element will be ignored
	 * @return the MultipleDownloadTask
	 * @throws NullPointerException if <code>tasks == null</code>
	 */
	public static MultipleDownloadTask<Object> group(DownloadTask<?>... tasks) {
		Objects.requireNonNull(tasks);
		return new GroupMultipleDownloadTask(tasks);
	}

	abstract public void execute(MultipleDownloadContext<T> context) throws Exception;

	public <R> MultipleDownloadTask<R> andThen(ResultProcessor<T, R> processor) {
		return new AppendedMultipleDownloadTask<>(this, processor);
	}

}
