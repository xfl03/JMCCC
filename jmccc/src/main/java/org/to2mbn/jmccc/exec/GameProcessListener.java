package org.to2mbn.jmccc.exec;

public interface GameProcessListener {

	/**
	 * Called when receiving a log from stdout.
	 * 
	 * @param log the log
	 */
	void onLog(String log);

	/**
	 * Called when receiving a log from stderr.
	 * 
	 * @param log the log
	 */
	void onErrorLog(String log);

	/**
	 * Called when the game process terminates.
	 * 
	 * @param code the exit code
	 */
	void onExit(int code);
}
