package org.to2mbn.jmccc.mcdownloader.download.concurrent;

public interface Shutdownable extends AutoCloseable {

	/**
	 * Shutdown the executor.
	 */
	void shutdown();

	/**
	 * Returns true if this executor has been shutdown.
	 *
	 * @return true if this executor has been shutdown
	 */
	boolean isShutdown();

	@Override
	default void close() throws Exception {
		shutdown();
	}

}
