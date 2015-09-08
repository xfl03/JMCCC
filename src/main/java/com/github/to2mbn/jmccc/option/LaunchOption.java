package com.github.to2mbn.jmccc.option;

import java.util.List;
import java.util.Objects;
import com.github.to2mbn.jmccc.auth.Authenticator;
import com.github.to2mbn.jmccc.version.Version;

public class LaunchOption {

    /**
     * The max memory of game JVM(MB), default to 1024
     */
    private int maxMemory = 1024;

    /**
     * The max memory of game JVM(MB)
     */
    private int minMemory;

    /**
     * The version to launch
     */
    private Version version;

    /**
     * The authenticator
     */
    private Authenticator authenticator;

    /**
     * The server to join, null if you don't join a server automatically
     */
    private ServerInfo serverInfo;

    /*
     * The size of game window, default to null
     */
    private WindowSize windowSize;

    /*
     * The environment option
     */
    private JavaOption javaOption;

    /**
     * The minecraft directory
     */
    private MinecraftDirectory minecraftDirectory;

    /**
     * The extra arguments to append to the command line, defualt to null
     */
    private List<String> extraArguments;

    /**
     * Creates a LaunchOption.
     * 
     * @param version the version to launch
     * @param authenticator the authenticator
     * @param javaOption the JavaOption
     * @throws NullPointerException if <code>version==null||authenticator==null</code>
     */
    public LaunchOption(Version version, Authenticator authenticator, JavaOption javaOption) {
        Objects.requireNonNull(version);
        Objects.requireNonNull(authenticator);
        Objects.requireNonNull(javaOption);

        this.version = version;
        this.authenticator = authenticator;
        this.javaOption = javaOption;
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
     * Sets the JavaOption.
     * 
     * @param javaOption the JavaOption to set
     * @throws NullPointerException if <code>javaOption=null</code>
     */
    public void setJavaOption(JavaOption javaOption) {
        Objects.requireNonNull(javaOption);
        this.javaOption = javaOption;
    }

    /**
     * Gets the max memory(MB), default to 1024, 0 to JVM default.
     * 
     * @return the max memory(MB)
     */
    public int getMaxMemory() {
        return maxMemory;
    }

    /**
     * Sets the max memory(MB), 0 to JVM default.
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
     * Gets the min memory(MB), default to 0, 0 to JVM default.
     * 
     * @return the min memory(MB)
     */
    public int getMinMemory() {
        return minMemory;
    }

    /**
     * Sets the min memory(MB), 0 to JVM default.
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
     * Gets the extra arguments, default to null.
     * 
     * @return the extra arguments, default to null
     */
    public List<String> getExtraArguments() {
        return extraArguments;
    }

    /**
     * Sets the extra arguments.
     * 
     * @param extraArguments the extra arguments to set
     */
    public void setExtraArguments(List<String> extraArguments) {
        this.extraArguments = extraArguments;
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
     * Sets the minecraft directory.
     * 
     * @param minecraftDirectory the minecraft directory
     * @throws NullPointerException if <code>minecraftDirectory==null</code>
     */
    public void setMinecraftDirectory(MinecraftDirectory minecraftDirectory) {
        Objects.requireNonNull(minecraftDirectory);
        this.minecraftDirectory = minecraftDirectory;
    }

}
