package com.github.to2mbn.jmccc.mcdownloader.download.concurrent;

public interface AsyncCallback<T> {

	/**
	 * Calls when the operation completed successfully.
	 * 
	 * @param result the result of the async operation
	 */
	void done(T result);

	/**
	 * Calls when the operation failed.
	 * 
	 * @param e the thrown exception
	 */
	void failed(Throwable e);

	/**
	 * Calls when the operation has been cancelled.
	 */
	void cancelled();

}
