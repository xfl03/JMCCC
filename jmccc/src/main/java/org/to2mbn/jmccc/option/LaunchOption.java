package org.to2mbn.jmccc.option;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.version.Version;

/**
 * Notes for serialization:<br>
 * Make sure the authenticator is serializable, otherwise, an error will occur
 * during serialization.
 * 
 * @author yushijinhun
 */
public class LaunchOption implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The max memory of game JVM(MB), default to 1024
	 */
	private int maxMemory = 1024;

	/**
	 * The max memory of game JVM(MB)
	 */
	private int minMemory;

	/**
	 * The version of the game to launch
	 */
	private Version version;

	/**
	 * The authenticator
	 */
	private Authenticator authenticator;

	/**
	 * The server to join when the game finished initializing, null if you don't
	 * join a server automatically
	 */
	private ServerInfo serverInfo;

	/**
	 * The size of game window, default to null
	 */
	private WindowSize windowSize;

	/**
	 * The java environment option
	 */
	private JavaOption javaOption;

	/**
	 * The minecraft directory
	 */
	private MinecraftDirectory minecraftDirectory;

	/**
	 * The game directory
	 */
	private MinecraftDirectory gameDirectory;

	/**
	 * The extra arguments to append to the jvm command line, default to null
	 */
	private List<String> extraJvmArguments;

	/**
	 * The extra arguments to append to the minecraft command line, default to
	 * null
	 */
	private List<String> extraMinecraftArguments;

	/**
	 * Creates a LaunchOption with the default JavaOption and
	 * MinecraftDirectory.
	 * 
	 * @param version the version to launch
	 * @param authenticator the authenticator
	 * @throws NullPointerException if any of the arguments is null
	 */
	public LaunchOption(Version version, Authenticator authenticator) {
		this(version, authenticator, new MinecraftDirectory(), new JavaOption());
	}

	/**
	 * Creates a LaunchOption with the default JavaOption.
	 * 
	 * @param version the version to launch
	 * @param authenticator the authenticator
	 * @param minecraftDir the minecraft directory
	 * @throws NullPointerException if any of the arguments is null
	 */
	public LaunchOption(Version version, Authenticator authenticator, MinecraftDirectory minecraftDir) {
		this(version, authenticator, minecraftDir, new JavaOption());
	}

	/**
	 * Creates a LaunchOption.
	 * 
	 * @param version the version to launch
	 * @param authenticator the authenticator
	 * @param minecraftDir the minecraft directory
	 * @param javaOption the JavaOption
	 * @throws NullPointerException if any of the arguments is null
	 */
	public LaunchOption(Version version, Authenticator authenticator, MinecraftDirectory minecraftDir, JavaOption javaOption) {
		Objects.requireNonNull(version);
		Objects.requireNonNull(authenticator);
		Objects.requireNonNull(javaOption);
		Objects.requireNonNull(minecraftDir);

		this.version = version;
		this.authenticator = authenticator;
		this.minecraftDirectory = minecraftDir;
		this.javaOption = javaOption;
		this.gameDirectory = minecraftDir;
	}

	/**
	 * Gets the version to launch.
	 * 
	 * @return the version to launch
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Gets the authenticator.
	 * 
	 * @return the authenticator
	 */
	public Authenticator getAuthenticator() {
		return authenticator;
	}

	/**
	 * Gets the minecraft directory.
	 * 
	 * @return the minecraft directory
	 */
	public MinecraftDirectory getMinecraftDirectory() {
		return minecraftDirectory;
	}

	/**
	 * Gets the JavaOption.
	 * 
	 * @return the JavaOption
	 */
	public JavaOption getJavaOption() {
		return javaOption;
	}

	/**
	 * Gets the max memory(MB), default to 1024.
	 * <p>
	 * If <code>maxMemory == 0</code>, the max memory option won't be specified.
	 * 
	 * @return the max memory(MB)
	 */
	public int getMaxMemory() {
		return maxMemory;
	}

	/**
	 * Sets the max memory(MB).
	 * <p>
	 * If <code>maxMemory == 0</code>, the max memory option won't be specified.
	 * 
	 * @param maxMemory the max memory(MB) to set
	 * @throws IllegalArgumentException if <code>maxMemory&lt;0</code>
	 */
	public void setMaxMemory(int maxMemory) {
		if (maxMemory < 0) {
			throw new IllegalArgumentException("maxMemory<0");
		}

		this.maxMemory = maxMemory;
	}

	/**
	 * Gets the min memory(MB).
	 * <p>
	 * If <code>minMemory == 0</code>, the min memory option won't be specified.
	 * 
	 * @return the min memory(MB)
	 */
	public int getMinMemory() {
		return minMemory;
	}

	/**
	 * Sets the min memory(MB).
	 * <p>
	 * If <code>minMemory == 0</code>, the min memory option won't be specified.
	 * 
	 * @param minMemory the min memory(MB) to set
	 * @throws IllegalArgumentException if <code>minMemory&lt;0</code>
	 */
	public void setMinMemory(int minMemory) {
		if (minMemory < 0) {
			throw new IllegalArgumentException("minMemory<0");
		}

		this.minMemory = minMemory;
	}

	/**
	 * Gets the server info, default to null.
	 * <p>
	 * If the server is specified, minecraft will join the specified server
	 * automatically.
	 * 
	 * @return the server info, default to null
	 */
	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	/**
	 * Sets the server info.
	 * 
	 * @param serverInfo the server info to set
	 */
	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	/**
	 * Gets the window size, default to null.
	 * 
	 * @return the window size, default to null
	 */
	public WindowSize getWindowSize() {
		return windowSize;
	}

	/**
	 * Sets the window size.
	 * 
	 * @param windowSize the window size to set
	 */
	public void setWindowSize(WindowSize windowSize) {
		this.windowSize = windowSize;
	}

	/**
	 * Gets the extra jvm arguments, default to null.
	 * <p>
	 * The arguments will be added to the JVM arguments. Null elements are
	 * allowed, if an element is null, it won't be added to the JVM arguments.
	 * 
	 * @return the extra jvm arguments, default to null
	 */
	public List<String> getExtraJvmArguments() {
		return extraJvmArguments;
	}

	/**
	 * Sets the extra jvm arguments.
	 * 
	 * @param extraJvmArguments the extra jvm arguments to set
	 * @see #getExtraJvmArguments()
	 */
	public void setExtraJvmArguments(List<String> extraJvmArguments) {
		this.extraJvmArguments = extraJvmArguments;
	}

	/**
	 * Gets the extra minecraft arguments, default to null.
	 * <p>
	 * The arguments will be added to the minecraft arguments. Null elements are
	 * allowed, if an element is null, it won't be added to the minecraft
	 * arguments.
	 * 
	 * @return the extra minecraft arguments, default to null.
	 */
	public List<String> getExtraMinecraftArguments() {
		return extraMinecraftArguments;
	}

	/**
	 * Sets the extra minecraft arguments.
	 * 
	 * @param extraMinecraftArguments the extra minecraft arguments to set
	 * @see #getExtraMinecraftArguments()
	 */
	public void setExtraMinecraftArguments(List<String> extraMinecraftArguments) {
		this.extraMinecraftArguments = extraMinecraftArguments;
	}

	/**
	 * Gets the game working directory.
	 * <p>
	 * The <code>game_directory</code> and the subprocess working directory will
	 * be set to this.<br>
	 * By default, the <code>game directory</code> and the
	 * <code>minecraft directory</code> are the same. The
	 * <code>minecraft directory</code> contains libraries, versions, assets,
	 * and so on. The <code>game directory</code> contains saves, resourcepacks,
	 * screenshots, and so on.
	 * 
	 * @return the game directory
	 */
	public MinecraftDirectory getGameDirectory() {
		return gameDirectory;
	}

	/**
	 * Sets the game working directory.
	 * <p>
	 * If you want to launch different minecraft versions in their own
	 * directories, use this.
	 * 
	 * @param gameDirectory the game directory
	 * @throws NullPointerException <code>gameDirectory=null</code>
	 * @see #getGameDirectory()
	 */
	public void setGameDirectory(MinecraftDirectory gameDirectory) {
		Objects.requireNonNull(gameDirectory);
		this.gameDirectory = gameDirectory;
	}

	@Override
	public String toString() {
		return "[maxMemory=" + maxMemory + ", minMemory=" + minMemory + ", version=" + version + ", authenticator=" + authenticator + ", serverInfo=" + serverInfo + ", windowSize=" + windowSize + ", javaOption=" + javaOption + ", minecraftDirectory=" + minecraftDirectory + ", extraArguments=" + extraJvmArguments + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof LaunchOption) {
			LaunchOption another = (LaunchOption) obj;
			return maxMemory == another.maxMemory &&
					minMemory == another.minMemory &&
					version.equals(another.version) &&
					authenticator.equals(another.authenticator) &&
					minecraftDirectory.equals(another.minecraftDirectory) &&
					gameDirectory.equals(another.gameDirectory) &&
					javaOption.equals(another.javaOption) &&
					Objects.equals(serverInfo, another.serverInfo) &&
					Objects.equals(windowSize, another.windowSize) &&
					Objects.equals(extraJvmArguments, another.extraJvmArguments) &&
					Objects.equals(extraMinecraftArguments, another.extraMinecraftArguments);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maxMemory, minMemory, version, authenticator, minecraftDirectory, gameDirectory, javaOption, serverInfo, windowSize, extraJvmArguments, extraMinecraftArguments);
	}

}
