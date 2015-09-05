package com.github.to2mbn.jmccc.option;

import java.io.File;
import java.util.Objects;
import com.github.to2mbn.jmccc.util.Utils;

public class EnvironmentOption {

    /**
     * the .minecraft dir
     */
    private File minecraftDir;

    /**
     * the 'java'
     */
    private File javaPath;

    /**
     * Creates a EnvironmentOption with the given .minecraft dir and the given 'java' path.
     * 
     * @param minecraftDir the .minecraft dir
     * @param javaPath the 'java'
     * @throws NullPointerException if <code>minecraftDir==null||javaPath==null</code>
     * @see Utils#getJavaPath()
     */
    public EnvironmentOption(File minecraftDir, File javaPath) {
        Objects.requireNonNull(minecraftDir);
        Objects.requireNonNull(javaPath);
        
        this.minecraftDir = minecraftDir;
        this.javaPath = javaPath;
    }

    /**
     * Creates a EnvironmentOption with the given .minecraft dir and the default 'java' path.
     * 
     * @param minecraftDir the .minecraft dir
     * @throws NullPointerException if <code>minecraftDir==null</code>
     * @see Utils#getJavaPath()
     */
    public EnvironmentOption(File minecraftDir) {
        this(minecraftDir, Utils.getJavaPath());
    }

    /**
     * Creates a EnvironmentOption with the .minecraft dir in the current dir and the default 'java' path.
     * 
     * @see Utils#getJavaPath()
     */
    public EnvironmentOption() {
        this(new File(".minecraft"), Utils.getJavaPath());
    }

    /**
     * Gets the .minecraft dir.
     * 
     * @return the .minecraft dir
     */
    public File getMinecraftDir() {
        return minecraftDir;
    }

    /**
     * Sets the .minecraft dir.
     * 
     * @param minecraftDir the .minecraft dir to set
     * @throws NullPointerException if <code>minecraftDir==null</code>
     */
    public void setMinecraftDir(File minecraftDir) {
        Objects.requireNonNull(minecraftDir);

        this.minecraftDir = minecraftDir;
    }

    /**
     * Gets the java path.
     * 
     * @return the java path
     */
    public File getJavaPath() {
        return javaPath;
    }

    /**
     * Sets the java path.
     * 
     * @param javaPath the java path to set
     * @throws NullPointerException if <code>javaPath==null</code>
     */
    public void setJavaPath(File javaPath) {
        Objects.requireNonNull(javaPath);

        this.javaPath = javaPath;
    }

}
