package org.to2mbn.jmccc.launch;

import org.to2mbn.jmccc.exec.GameProcessListener;
import org.to2mbn.jmccc.exec.ProcessMonitor;
import org.to2mbn.jmccc.option.LaunchOption;

/**
 * A <code>Launcher</code> is used to launch minecraft.<br>
 * You can use {@link LauncherBuilder} to create a launcher instance.
 */
public interface Launcher {

	/**
	 * Launches the game.
	 * <p>
	 * The launcher will start a group of daemon threads to pump the stdout and
	 * stderr of the subprocess.
	 * 
	 * @param option the launching option
	 * @return the result of launching
	 * @throws LaunchException when failed to launch the game
	 * @throws NullPointerException if <code>option==null</code>
	 * @see LaunchResult
	 * @see LaunchException
	 * @see ProcessMonitor#stop()
	 */
	LaunchResult launch(LaunchOption option) throws LaunchException;

	/**
	 * Launches the game.
	 * <p>
	 * If <code>listener!=null</code>, the launcher will start a group of
	 * non-daemon threads to receive the logs and report them to the given
	 * listener. Otherwise, the launcher will start a group of daemon threads to
	 * pump the stdout and stderr of the subprocess.
	 * 
	 * @param option the launching option
	 * @param listener the listener to receive logs from the game process
	 * @return the launching result
	 * @throws LaunchException if we fail to launch the game
	 * @throws NullPointerException if <code>option==null</code>
	 * @see LaunchResult
	 * @see ProcessMonitor
	 * @see GameProcessListener
	 * @see LaunchException
	 * @see ProcessMonitor#stop()
	 */
	LaunchResult launch(LaunchOption option, GameProcessListener listener) throws LaunchException;

}
