package org.to2mbn.jmccc.exec;

public interface GameProcessListener {

	/**
	 * Calls when receives a log from the stdout of the game process.
	 * 
	 * @param log the log
	 */
	void onLog(String log);

	/**
	 * Calls when receives a log from the stderr of the game process.
	 * 
	 * @param log the log
	 */
	void onErrorLog(String log);

	/**
	 * Calls when the game process terminates.
	 * 
	 * @param code the exit code of the game process
	 */
	void onExit(int code);
}
