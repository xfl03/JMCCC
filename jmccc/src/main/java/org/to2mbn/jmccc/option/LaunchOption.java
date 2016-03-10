package org.to2mbn.jmccc.option;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

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
	 * The runtime directory
	 */
	private MinecraftDirectory runtimeDirectory;

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
	 * Customized minecraft commandline variables, default to null
	 */
	private Map<String, String> commandlineVariables;

	/**
	 * Resolves the given version and creates a LaunchOption using the default
	 * java environment.
	 * 
	 * @param version the version id
	 * @param authenticator the authenticator
	 * @param minecraftDir the minecraft directory
	 * @throws IOException if an I/O error has occurred during resolving version
	 * @throws NullPointerException if any of the arguments is null
	 */
	public LaunchOption(String version, Authenticator authenticator, MinecraftDirectory minecraftDir) throws IOException {
		this(Versions.resolveVersion(minecraftDir, version), authenticator, minecraftDir, new JavaOption());
	}

	/**
	 * Creates a LaunchOption using the default java environment.
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
	 * @param javaOption the java environment
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
		this.runtimeDirectory = minecraftDir;
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
	 * be set to this.
	 * <p>
	 * By default, the <code>runtimeDirectory</code> and the
	 * <code>minecraftDirectory</code> are the same. The
	 * <code>minecraftDirectory</code> contains libraries, versions, assets, and
	 * so on. The <code>runtimeDirectory</code> contains saves, resourcepacks,
	 * screenshots, and so on.
	 * 
	 * @return the minecraft runtime directory
	 */
	public MinecraftDirectory getRuntimeDirectory() {
		return runtimeDirectory;
	}

	/**
	 * Sets the game working directory.
	 * <p>
	 * If you want to launch different minecraft versions in their own
	 * directories, use this.
	 * 
	 * @param runtimeDirectory the minecraft runtime directory
	 * @throws NullPointerException <code>runtimeDirectory=null</code>
	 * @see #getRuntimeDirectory()
	 */
	public void setRuntimeDirectory(MinecraftDirectory runtimeDirectory) {
		Objects.requireNonNull(runtimeDirectory);
		this.runtimeDirectory = runtimeDirectory;
	}

	/**
	 * Gets the customized minecraft commandline variables, default to null.
	 * <p>
	 * When generating launch commandline, the variables in
	 * {@link Version#getLaunchArgs()} will be replaced. For example,
	 * <code>${version_name}</code> will be replaced by the version id. Some
	 * variables are automatically replaced by the launcher. If you want to add
	 * customized variables, you can set the values of the variables via
	 * {@link #setCommandlineVariables(Map)}. The customized values can override
	 * default values.
	 * 
	 * @return the customized minecraft commandline variables, default to null
	 */
	public Map<String, String> getCommandlineVariables() {
		return commandlineVariables;
	}

	/**
	 * Sets the customized minecraft commandline variables, can be null.
	 * 
	 * @param commandlineVariables the customized minecraft commandline
	 *            variables to set, can be null
	 * @see #getCommandlineVariables()
	 */
	public void setCommandlineVariables(Map<String, String> commandlineVariables) {
		this.commandlineVariables = commandlineVariables;
	}

	@Override
	public String toString() {
		return String.format("LaunchOption [maxMemory=%s, minMemory=%s, version=%s, authenticator=%s, serverInfo=%s, windowSize=%s, javaOption=%s, minecraftDirectory=%s, runtimeDirectory=%s, extraJvmArguments=%s, extraMinecraftArguments=%s, commandlineVariables=%s]", maxMemory, minMemory, version, authenticator, serverInfo, windowSize, javaOption, minecraftDirectory, runtimeDirectory, extraJvmArguments, extraMinecraftArguments, commandlineVariables);
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
					runtimeDirectory.equals(another.runtimeDirectory) &&
					javaOption.equals(another.javaOption) &&
					Objects.equals(serverInfo, another.serverInfo) &&
					Objects.equals(windowSize, another.windowSize) &&
					Objects.equals(extraJvmArguments, another.extraJvmArguments) &&
					Objects.equals(extraMinecraftArguments, another.extraMinecraftArguments) &&
					Objects.equals(commandlineVariables, another.commandlineVariables);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maxMemory, minMemory, version, authenticator, minecraftDirectory, runtimeDirectory, javaOption, serverInfo, windowSize, extraJvmArguments, extraMinecraftArguments, commandlineVariables);
	}

}
