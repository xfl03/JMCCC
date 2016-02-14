package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.Objects;
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

	/**
	 * Creates a CombinedDownloadTask from a group of DownloadTasks.
	 * 
	 * @param tasks the download tasks, null element will be ignored
	 * @return the CombinedDownloadTask
	 * @throws NullPointerException if <code>tasks == null</code>
	 */
	public static CombinedDownloadTask<Object> multiple(DownloadTask<?>... tasks) {
		Objects.requireNonNull(tasks);
		return new MultipleCombinedDownloadTask(tasks);
	}

	abstract public void execute(CombinedDownloadContext<T> context) throws Exception;

	public <R> CombinedDownloadTask<R> andThen(ResultProcessor<T, R> processor) {
		return new AppendedCombinedDownloadTask<>(this, processor);
	}

}
