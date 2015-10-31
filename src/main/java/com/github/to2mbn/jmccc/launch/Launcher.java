package com.github.to2mbn.jmccc.launch;

import com.github.to2mbn.jmccc.exec.GameProcessListener;
import com.github.to2mbn.jmccc.exec.ProcessMonitor;
import com.github.to2mbn.jmccc.option.LaunchOption;

/**
 * A <code>Launcher</code> is used to launch minecraft.<br>
 * You can use {@link Jmccc#getLauncher()} to get a launcher object.
 */
public interface Launcher {

    /**
     * Launches the game with the given option.
     * <p>
     * We will create a group of daemon threads to pump the stdout and stderr of the subprocess.<br>
     * If we fail to launch the game, we will throw a {@link LaunchException}.<br>
     * We recommend you NOT to call {@link ProcessMonitor#stop()}. This may cause subprocess blocks and it's not always
     * effective. You don't need to stop the monitor threads because they are daemon.
     * 
     * @param option the launching option
     * @return the launching result
     * @throws LaunchException if we fail to launch the game
     * @throws NullPointerException if <code>option==null</code>
     * @see LaunchResult
     * @see LaunchException
     */
    LaunchResult launch(LaunchOption option) throws LaunchException;

    /**
     * Launches the game with the given option.<br>
     * If <code>listener!=null</code>, we will create a group of non-daemon threads to receive the logs from the
     * subprocess and report them to the given listener.
     * Else if <code>listener==null</code>, we will create a group of daemon threads to pump the stdout and stderr of
     * the sub process.
     * <p>
     * We recommend you NOT to call {@link ProcessMonitor#stop()} unless you are going to exit the jvm and
     * <code>listener!=null</code>. This may cause subprocess blocks and it's not always effective.
     * 
     * @param option the launching option
     * @param listener the listener to receive logs from the game
     * @return the launching result
     * @throws LaunchException if we fail to launch the game
     * @throws NullPointerException if <code>option==null</code>
     * @see LaunchResult
     * @see ProcessMonitor
     * @see GameProcessListener
     * @see ProcessMonitor#stop()
     * @see LaunchException
     */
    LaunchResult launch(LaunchOption option, GameProcessListener listener) throws LaunchException;

}
