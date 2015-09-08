package com.github.to2mbn.jmccc.option;

import java.io.File;
import java.util.Objects;
import com.github.to2mbn.jmccc.util.Utils;

public class EnvironmentOption {

    /**
     * the 'java'
     */
    private File javaPath;

    /**
     * Creates a EnvironmentOption with the given minecraft dir and the given 'java' path.
     * 
     * @param javaPath the 'java'
     * @throws NullPointerException if <code>minecraftDir==null||javaPath==null</code>
     * @see Utils#getJavaPath()
     */
    public EnvironmentOption(File javaPath) {
        Objects.requireNonNull(javaPath);

        this.javaPath = javaPath;
    }

    /**
     * Creates a EnvironmentOption with the .minecraft dir in the current dir and the default 'java' path.
     * 
     * @see Utils#getJavaPath()
     */
    public EnvironmentOption() {
        this(Utils.getJavaPath());
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
