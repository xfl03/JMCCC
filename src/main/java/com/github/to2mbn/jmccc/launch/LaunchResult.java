package com.github.to2mbn.jmccc.launch;

import com.github.to2mbn.jmccc.exec.GameProcessMonitor;

/**
 * The result of launching.
 * <p>
 * This will be returned only when the game is launched successfully.
 */
public class LaunchResult {

    private GameProcessMonitor monitor;
    private Process process;

    public LaunchResult(GameProcessMonitor monitor, Process process) {
        this.monitor = monitor;
        this.process = process;
    }

    /**
     * Gets the game monitor, null if no monitor started
     * 
     * @return the game monitor, null if no monitor started
     */
    public GameProcessMonitor getMonitor() {
        return monitor;
    }

    /**
     * Gets the game process
     * 
     * @return the game process
     */
    public Process getProcess() {
        return process;
    }


}
