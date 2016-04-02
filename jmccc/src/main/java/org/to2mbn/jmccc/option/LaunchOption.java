package org.to2mbn.jmccc.option;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
	 * The max memory of JVM(MB), default to 1024
	 */
	private int maxMemory = 1024;

	/**
	 * The max memory of JVM(MB)
	 */
	private int minMemory;

	/**
	 * The version to launch
	 */
	private final Version version;

	/**
	 * The authenticator
	 */
	private final Authenticator authenticator;

	/**
	 * The minecraft directory
	 */
	private final MinecraftDirectory minecraftDirectory;

	/**
	 * The java environment
	 */
	private JavaEnvironment javaEnvironment;

	/**
	 * The runtime directory
	 */
	private MinecraftDirectory runtimeDirectory;

	/**
	 * The server to join when the game finishes initialization
	 */
	private Optional<ServerInfo> serverInfo = Optional.empty();

	/**
	 * The size of the game window
	 */
	private Optional<WindowSize> windowSize = Optional.empty();

	/**
	 * The extra arguments to append to the jvm command line
	 */
	private List<String> extraJvmArguments = new ArrayList<>();

	/**
	 * The extra arguments to append to the minecraft command line
	 */
	private List<String> extraMinecraftArguments = new ArrayList<>();

	/**
	 * Customized minecraft commandline variables
	 */
	private Map<String, String> commandlineVariables = new HashMap<>();

	/**
	 * Resolves the given version and creates a LaunchOption.
	 * 
	 * @param version the version id
	 * @param authenticator the authenticator
	 * @param minecraftDir the minecraft directory
	 * @throws IOException if an I/O error has occurred during resolving version
	 * @throws NullPointerException if any of the arguments is {@code null}
	 */
	public LaunchOption(String version, Authenticator authenticator, MinecraftDirectory minecraftDir) throws IOException {
		this(Versions.resolveVersion(minecraftDir, version), authenticator, minecraftDir);
	}

	/**
	 * Creates a LaunchOption.
	 * 
	 * @param version the version to launch
	 * @param authenticator the authenticator
	 * @param minecraftDir the minecraft directory
	 * @throws NullPointerException if any of the arguments is {@code null}
	 */
	public LaunchOption(Version version, Authenticator authenticator, MinecraftDirectory minecraftDir) {
		Objects.requireNonNull(version);
		Objects.requireNonNull(authenticator);
		Objects.requireNonNull(minecraftDir);

		this.version = version;
		this.authenticator = authenticator;
		this.minecraftDirectory = minecraftDir;

		this.javaEnvironment = JavaEnvironment.current();
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
	 * Gets the java environment.
	 * 
	 * @return the java environment
	 */
	public JavaEnvironment getJavaEnvironment() {
		return javaEnvironment;
	}

	/**
	 * Sets the java environment.
	 * 
	 * @param javaEnvironment the java environment to set
	 * @throws NullPointerException if {@code javaEnvironment} is {@code null}
	 */
	public void setJavaEnvironment(JavaEnvironment javaEnvironment) {
		Objects.requireNonNull(javaEnvironment);
		this.javaEnvironment = javaEnvironment;
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
	 * @throws IllegalArgumentException if <code>maxMemory &lt; 0</code>
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
	 * @throws IllegalArgumentException if <code>minMemory &lt; 0</code>
	 */
	public void setMinMemory(int minMemory) {
		if (minMemory < 0) {
			throw new IllegalArgumentException("minMemory<0");
		}

		this.minMemory = minMemory;
	}

	/**
	 * Gets the server info.
	 * <p>
	 * If the info server is specified, minecraft will join the specified server
	 * automatically.
	 * 
	 * @return the server info
	 */
	public Optional<ServerInfo> getServerInfo() {
		return serverInfo;
	}

	/**
	 * Sets the server info.
	 * 
	 * @param serverInfo the server info to set, can be {@code null}
	 */
	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = Optional.ofNullable(serverInfo);
	}

	/**
	 * Sets the server info.
	 * 
	 * @param serverInfo the server info to set
	 * @throws NullPointerException if {@code serverInfo} is {@code null} (
	 *             please use an empty {@link Optional} instead of {@code null}
	 *             )
	 */
	public void setServerInfo(Optional<ServerInfo> serverInfo) {
		Objects.requireNonNull(serverInfo);
		this.serverInfo = serverInfo;
	}

	/**
	 * Gets the window size.
	 * 
	 * @return the window size
	 */
	public Optional<WindowSize> getWindowSize() {
		return windowSize;
	}

	/**
	 * Sets the window size.
	 * 
	 * @param windowSize the window size to set, can be {@code null}
	 */
	public void setWindowSize(WindowSize windowSize) {
		this.windowSize = Optional.ofNullable(windowSize);
	}

	/**
	 * Sets the window size.
	 * 
	 * @param windowSize the windows size to set
	 * @throws NullPointerException if {@code windowSize} is {@code null} (
	 *             please use an empty {@link Optional} instead of {@code null}
	 *             )
	 */
	public void setWindowSize(Optional<WindowSize> windowSize) {
		Objects.requireNonNull(windowSize);
		this.windowSize = windowSize;
	}

	/**
	 * Gets the extra jvm arguments.
	 * <p>
	 * The arguments will be added to the JVM arguments.
	 * 
	 * @return the extra jvm arguments
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
		this.extraJvmArguments = new ArrayList<>(extraJvmArguments);
	}

	/**
	 * Adds an argument to the jvm arguments.
	 * 
	 * @param extraJvmArgument the argument to add to the jvm arguments
	 */
	public void addExtraJvmArgument(String extraJvmArgument) {
		Objects.requireNonNull(extraJvmArgument);
		this.extraJvmArguments.add(extraJvmArgument);
	}

	/**
	 * Gets the extra minecraft arguments.
	 * <p>
	 * The arguments will be added to the minecraft arguments.
	 * 
	 * @return the extra minecraft arguments.
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
		this.extraMinecraftArguments = new ArrayList<>(extraMinecraftArguments);
	}

	/**
	 * Adds an argument to the minecraft arguments.
	 * 
	 * @param extraMinecraftArgument the argument to add to the minecraft
	 *            arguments
	 */
	public void addExtraMinecraftArgument(String extraMinecraftArgument) {
		Objects.requireNonNull(extraMinecraftArgument);
		this.extraMinecraftArguments.add(extraMinecraftArgument);
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
	 * @throws NullPointerException <code>runtimeDirectory == null</code>
	 * @see #getRuntimeDirectory()
	 */
	public void setRuntimeDirectory(MinecraftDirectory runtimeDirectory) {
		Objects.requireNonNull(runtimeDirectory);
		this.runtimeDirectory = runtimeDirectory;
	}

	/**
	 * Gets the customized minecraft commandline variables.
	 * <p>
	 * When generating launch commandline, the variables in
	 * {@link Version#getLaunchArgs()} will be replaced. For example,
	 * <code>${version_name}</code> will be replaced by the version id. Some
	 * variables are automatically replaced by the launcher. If you want to add
	 * customized variables, you can set the values of the variables via
	 * {@link #setCommandlineVariables(Map)}. The customized values can override
	 * default values.
	 * 
	 * @return the customized minecraft commandline variables
	 */
	public Map<String, String> getCommandlineVariables() {
		return commandlineVariables;
	}

	/**
	 * Sets the customized minecraft commandline variables.
	 * 
	 * @param commandlineVariables the customized minecraft commandline
	 *            variables to set
	 * @see #getCommandlineVariables()
	 */
	public void setCommandlineVariables(Map<String, String> commandlineVariables) {
		this.commandlineVariables = new HashMap<>(commandlineVariables);
	}

	/**
	 * Adds a variable to the minecraft commandline variables.
	 * 
	 * @param key the name of the variable
	 * @param value the value of the variable
	 * @see #getCommandlineVariables()
	 */
	public void addCommandlineVariable(String key, String value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		this.commandlineVariables.put(key, value);
	}

	@Override
	public String toString() {
		return String.format("LaunchOption [maxMemory=%s, minMemory=%s, version=%s, authenticator=%s, serverInfo=%s, windowSize=%s, javaOption=%s, minecraftDirectory=%s, runtimeDirectory=%s, extraJvmArguments=%s, extraMinecraftArguments=%s, commandlineVariables=%s]", maxMemory, minMemory, version, authenticator, serverInfo, windowSize, javaEnvironment, minecraftDirectory, runtimeDirectory, extraJvmArguments, extraMinecraftArguments, commandlineVariables);
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
					Objects.equals(version, another.version) &&
					Objects.equals(authenticator, another.authenticator) &&
					Objects.equals(minecraftDirectory, another.minecraftDirectory) &&
					Objects.equals(runtimeDirectory, another.runtimeDirectory) &&
					Objects.equals(javaEnvironment, another.javaEnvironment) &&
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
		return Objects.hash(maxMemory, minMemory, version, authenticator, minecraftDirectory, runtimeDirectory, javaEnvironment, serverInfo, windowSize, extraJvmArguments, extraMinecraftArguments, commandlineVariables);
	}

}
