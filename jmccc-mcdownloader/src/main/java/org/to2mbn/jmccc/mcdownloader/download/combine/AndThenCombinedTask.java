package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;

import java.util.Objects;
import java.util.concurrent.Callable;

class AndThenCombinedTask<R, S> extends CombinedDownloadTask<S> {

    private final CombinedDownloadTask<R> prev;
    private final ResultProcessor<R, S> next;

    public AndThenCombinedTask(CombinedDownloadTask<R> prev, ResultProcessor<R, S> next) {
        this.prev = Objects.requireNonNull(prev);
        this.next = Objects.requireNonNull(next);
    }

    @Override
    public void execute(final CombinedDownloadContext<S> context) throws Exception {
        context.submit(prev, new CallbackAdapter<R>() {

            @Override
            public void done(final R result1) {
                try {
                    // invoke ResultProcessor here will block the IO thread
                    // so we run it async
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
