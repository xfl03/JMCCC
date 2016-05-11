package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.Objects;
import java.util.concurrent.Callable;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.mcdownloader.download.task.ResultProcessor;

class AppendedCombinedDownloadTask<R, S> extends CombinedDownloadTask<S> {

	private final CombinedDownloadTask<R> prev;
	private final ResultProcessor<R, S> next;

	public AppendedCombinedDownloadTask(CombinedDownloadTask<R> prev, ResultProcessor<R, S> next) {
		Objects.requireNonNull(prev);
		Objects.requireNonNull(next);

		this.prev = prev;
		this.next = next;
	}

	@Override
	public void execute(final CombinedDownloadContext<S> context) throws Exception {
		context.submit(prev, new CallbackAdapter<R>() {

			@Override
			public void done(final R result1) {
				try {
					context.submit(new Callable<S>() {

						@Override
						public S call() throws Exception {
							return next.process(result1);
						}

					}, new CallbackAdapter<S>() {

						@Override
						public void done(S result2) {
							context.done(result2);
						}

					}, true);
				} catch (Throwable e) {
					context.failed(e);
				}
			}

		}, true);
	}

}
