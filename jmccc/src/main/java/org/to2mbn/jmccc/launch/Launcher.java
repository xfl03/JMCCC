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
	 * The launcher will create a group of daemon threads to pump the stdout and
	 * stderr of the game process.<br>
	 * We recommend you NOT to call {@link ProcessMonitor#stop()}. This may
	 * cause the game process to be blocked, and it's not always effective. You
	 * don't need to stop the monitor threads because they are daemon.
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
	 * If <code>listener!=null</code>, the launcher will create a group of
	 * non-daemon threads to receive the logs from the game process and report
	 * them to the given listener. Otherwise, the Jmccc will create a group of
	 * daemon threads to pump the stdout and stderr of the sub process.
	 * <p>
	 * We recommend you NOT to call {@link ProcessMonitor#stop()} unless you are
	 * going to exit the jvm and <code>listener!=null</code>. This may cause the
	 * game process to be blocked and it's not always effective.
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
