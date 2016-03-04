package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallbackGroup;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncFuture;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Cancellable;

class CallableTaskHandler<T> implements Cancellable, Runnable {

	private class CallableWrapper implements Callable<T> {

		private final Callable<T> wrapped;

		public CallableWrapper(Callable<T> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public T call() {
			/*
			 * # Task processing
			 * ++++lock
			 * 	1. Has the cancelled flag been set? .............................................. read cancelled
			 * 		Yes - Stop processing.
			 * 				Don't need to notify the listeners.
			 * 				If a task has been cancelled before it really starts,
			 * 				the listeners have already been notified during the cancel operation.
			 * 		No - Go on.
			 * 
			 * 	2. Has the running flag been set? ................................................ read running
			 * 		Yes - Stop processing.
			 * 				It's disallowed to execute an already started task.
			 * 		No - Go on.
			 * 
			 * 	3. Sets the running flag to true. ................................................ write running
			 * ----unlock
			 * 
			 * 	4. Execute the callable.
			 * 		Succeed - Notify the listeners that the task is done.
			 * 		Caught an exception -
			 * 			Is the exception an InterruptedException?
			 * 				Yes - Notify the listeners that the task is cancelled.
			 * 				No - Notify the listeners that the task is failure.
			 */

			synchronized (lock) {
				if (cancelled || running) {
					return null;
				}
				running = true;
			}

			T result;
			try {
				result = wrapped.call();
			} catch (Throwable e) {
				if (e instanceof InterruptedException) {
					callback.cancelled();
				} else {
					callback.failed(e);
				}
				return null;
			}
			callback.done(result);
			return null;
		}

	}

	public final AsyncFuture<T> future;

	private final Callable<T> callable;
	private final AsyncCallback<T> callback;
	private final ExecutorService excutor;
	private volatile Future<T> executingFuture;
	private volatile boolean cancelled;
	private volatile boolean running;
	private final Object lock = new Object();

	public CallableTaskHandler(Callable<T> callable, Collection<AsyncCallback<T>> callbacks, ExecutorService excutor) {
		this.callable = new CallableWrapper(callable);
		this.excutor = excutor;
		future = new AsyncFuture<>(this);

		List<AsyncCallback<T>> allcallbacks = new ArrayList<>(callbacks);
		allcallbacks.add(future);
		@SuppressWarnings("unchecked")
		AsyncCallback<T>[] allcallbacksArray = allcallbacks.toArray(new AsyncCallback[allcallbacks.size()]);
		callback = AsyncCallbackGroup.group(allcallbacksArray);
	}

	@Override
	public void run() {
		/*
		 * # Start task
		 * ++++lock
		 * 	1. Has the task been cancelled? .................................................. read cancelled
		 * 		Yes - Do nothing.
		 * 				Don't need to notify the listeners.
		 * 				If a task has been cancelled before it really starts,
		 * 				the listeners have already been notified during the cancel operation.
		 * 		No - Go on.
		 * 
		 * 	2. Submit the task to the thread pool, store the future to executingFuture. ...... write executingFuture
		 * ----unlock
		 */
		synchronized (lock) {
			if (!cancelled) {
				executingFuture = excutor.submit(callable);
			}
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		/*
		 * # Cancel Task
		 * ++++lock
		 * 	1. Has the task already been cancelled? .......................................... read cancelled
		 * 		Yes - Do nothing.
		 * 		No - Go on.
		 * 
		 * 	2. Set the cancel flag to true. .................................................. set cancelled
		 * 
		 * 	3. Has the running flag been set?
		 * 		Yes - Send a cancel signal to the underlying executor. ....................... read executingFuture
		 * 				If the task is really running, it will notify the callbacks when it terminates.
		 * 				If the task is already terminated, nothing will be done.
		 * 					(The has already notified the callbacks.)
		 * 		No - 
		 * 			----unlock
		 * 				1. Unlock the current lock first to prevent deadlock.
		 * 					(Because we don't know the callbacks will do what.)
		 * 				2. Notify the callbacks that the task is cancelled.
		 * ----unlock
		 */

		boolean notifyCallbacks = false;

		synchronized (lock) {
			if (!cancelled) {
				cancelled = true;
				if (running) {
					executingFuture.cancel(true);
				} else {
					notifyCallbacks = true;
				}
			}
		}

		if (notifyCallbacks) {
			callback.cancelled();
		}

		return true;
	}

}