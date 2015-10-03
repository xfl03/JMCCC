package com.github.to2mbn.jmccc.option;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import com.github.to2mbn.jmccc.auth.Authenticator;
import com.github.to2mbn.jmccc.version.Version;

/**
 * Notes for serialization:<br>
 * Make sure the authenticator implements <code>Serializable</code>, or an error will occur during serialization.
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
     * Creates a LaunchOption with the default JavaOption and MinecraftDirectory.
     * 
     * @param version the version to launch
     * @param authenticator the authenticator
     * @throws NullPointerException if <code>version==null||authenticator==null</code>
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
     * @throws NullPointerException if <code>version==null||authenticator==null||minecraftDir==null</code>
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
     * @throws NullPointerException if
     *             <code>version==null||authenticator==null||minecraftDir==null||javaOption==null</code>
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

    @Override
    public String toString() {
        return "[maxMemory=" + maxMemory + ", minMemory=" + minMemory + ", version=" + version + ", authenticator=" + authenticator + ", serverInfo=" + serverInfo + ", windowSize=" + windowSize + ", javaOption=" + javaOption + ", minecraftDirectory=" + minecraftDirectory + ", extraArguments=" + extraArguments + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof LaunchOption) {
            LaunchOption another = (LaunchOption) obj;
            return maxMemory == another.maxMemory && minMemory == another.minMemory && version.equals(another.version) && authenticator.equals(another.authenticator) && minecraftDirectory.equals(another.minecraftDirectory) && javaOption.equals(another.javaOption) && Objects.equals(serverInfo, another.serverInfo) && Objects.equals(windowSize, another.windowSize) && Objects.equals(extraArguments, another.extraArguments);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxMemory, minMemory, version, authenticator, minecraftDirectory, javaOption, serverInfo, windowSize, extraArguments);
    }

}
