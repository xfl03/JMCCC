package com.github.to2mbn.jmccc;

import java.io.IOException;
import java.util.Set;
import com.github.to2mbn.jmccc.exec.GameProcessListener;
import com.github.to2mbn.jmccc.exec.ProcessMonitor;
import com.github.to2mbn.jmccc.launch.Jmccc;
import com.github.to2mbn.jmccc.launch.LaunchException;
import com.github.to2mbn.jmccc.launch.LaunchResult;
import com.github.to2mbn.jmccc.option.LaunchOption;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Version;
import com.google.gson.JsonParseException;

/**
 * A <code>Launcher</code> is used to launch minecraft.<br>
 * You can use {@link Jmccc#getLauncher()} or {@link Jmccc#getLauncher(String)} to get a Launcher object.
 * <p>
 * We recommend you to use {@link Jmccc#getLauncher(String)} with the name and the version of your launcher as the
 * argument (extended identity). Then if the report mode is on, the report data will include the extended identity, so
 * we can distinguish the different launchers.
 * <p>
 * By default, we will send the launching data to our server to for statistics and debugging. If you don't want this,
 * you can use <code>launcher.setReport(false)</code> to turn off this.
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
     * subprocess and reported them to the given listener.
     * Else if <code>listener==null</code>, we will create a group of daemon threads to pump the stdout and stderr of
     * the sub process.
     * <p>
     * We recommend you NOT to call {@link ProcessMonitor#stop()} unless you are going to exit the and
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

    /**
     * Gets the Version object of the given version in the given minecraft directory.
     * 
     * @param minecraftDir the minecraft directory
     * @param version the version name
     * @return the Version object, null if <code>version==null</code>, or the version does not exist
     * @throws IOException if an I/O exception has occurred during resolving version
     * @throws JsonParseException if an JSON syntax exception has occurred during resolving version json
     * @throws NullPointerException if <code>minecraftDir==null</code>
     * @see Version
     * @see Launcher#getVersions(MinecraftDirectory)
     */
    Version getVersion(MinecraftDirectory minecraftDir, String version) throws JsonParseException, IOException;

    /**
     * Gets the names of the versions in the given minecraft directory.
     * <p>
     * This method returns a non-threaded safe, unordered set.
     * 
     * @param minecraftDir the minecraft directory
     * @return a set of the names of the versions in the given .minecraft dir
     * @throws NullPointerException if <code>minecraftDir==null</code>
     * @see Launcher#getVersion(MinecraftDirectory, String)
     */
    Set<String> getVersions(MinecraftDirectory minecraftDir);

    /**
     * Sets the report mode to on or off.
     * <p>
     * By default the report mode is on. You can use <code>launcher.setReport(false)</code> to turn off the report mode.
     * <br>
     * If the report mode is on, we will send the following data to our server to for statistics and debugging:<br>
     * 
     * <pre>
     * the timestamp of launching
     * the version of JMCCC
     * the extended id
     * the operating system version
     * the java version
     * the version of the launching game
     * the path of java
     * the path of .minecraft
     * the max memory of the game
     * the min memory of the game
     * the stack trace (if launching failed)
     * </pre>
     * 
     * @param on the new state of the reporter
     * @see Jmccc#getLauncher()
     * @see Jmccc#getLauncher(String)
     */
    void setReport(boolean on);

}
