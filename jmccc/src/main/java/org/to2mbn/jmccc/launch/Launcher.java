package org.to2mbn.jmccc.launch;

import org.to2mbn.jmccc.option.LaunchOption;

/**
 * A <code>Launcher</code> is used to launch minecraft.<br>
 * You can use {@link LauncherBuilder} to create a launcher instance.
 */
public interface Launcher {

    /**
     * Launches the game.
     *
     * @param option the launching configuration
     * @return the subprocess
     * @throws LaunchException      when failing to launch
     * @throws NullPointerException if <code>option==null</code>
     */
    Process launch(LaunchOption option) throws LaunchException;

    /**
     * Launches the game.
     * <p>
     * If <code>listener!=null</code>, the launcher will the logs to the given
     * listener.
     *
     * @param option   the launching configuration
     * @param listener the listener to receive logs
     * @return the subprocess
     * @throws LaunchException      when failing to launch
     * @throws NullPointerException if <code>option==null</code>
     * @see ProcessListener
     */
    Process launch(LaunchOption option, ProcessListener listener) throws LaunchException;

    LaunchArgument generateLaunchArgs(LaunchOption option) throws LaunchException;
}
