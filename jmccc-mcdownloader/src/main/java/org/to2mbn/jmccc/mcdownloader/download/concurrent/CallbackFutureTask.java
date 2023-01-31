package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallbackFutureTask<V> extends FutureTask<V> implements Cancelable {

    private volatile Callback<V> callback;

    public CallbackFutureTask(Callable<V> callable) {
        super(callable);
    }

    public CallbackFutureTask(Runnable runnable, V result) {
        super(runnable, result);
    }

    public Callback<V> getCallback() {
        return callback;
    }

    public void setCallback(Callback<V> callback) {
        this.callback = callback;
    }

    @Override
    protected void done() {
        Callback<V> c = callback;
        if (c != null) {
            V result;
            try {
                result = get();
            } catch (InterruptedException e) {
                // We shouldn't get an InterruptedException here
                // because the task has done
                throw new IllegalStateException("Unexpected InterruptedException", e);
            } catch (ExecutionException e) {
                Throwable exception = e.getCause();
                if (exception == null)
                    exception = e;
                c.failed(exception);
                return;
            } catch (CancellationException e) {
                c.cancelled();
                return;
            }
            c.done(result);
        }
    }

}
