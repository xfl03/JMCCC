package com.github.to2mbn.jmccc.exec;

public interface GameProcessListener {

	/**
	 * Calls when receives a log from stdout.
	 * 
	 * @param log the log
	 */
	void onLog(String log);

	/**
	 * Calls when receives a log from stderr
	 * 
	 * @param log the log
	 */
	void onErrorLog(String log);

	/**
	 * Calls when the game process exits.
	 * 
	 * @param code the exit code of the game process
	 */
	void onExit(int code);
}
