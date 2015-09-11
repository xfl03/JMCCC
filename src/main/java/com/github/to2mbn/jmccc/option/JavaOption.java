package com.github.to2mbn.jmccc.option;

import java.io.File;
import java.util.Objects;
import com.github.to2mbn.jmccc.util.Utils;

public class JavaOption {

    /**
     * the 'java' executable file
     */
    private File javaPath;

    /**
     * Sets true to turn on cgc
     */
    private boolean cgc = false;

    /**
     * Creates a JavaOption with the given 'java' path.
     * 
     * @param javaPath the 'java' executable file
     * @throws NullPointerException if <code>minecraftDir==null||javaPath==null</code>
     * @see Utils#getJavaPath()
     */
    public JavaOption(File javaPath) {
        Objects.requireNonNull(javaPath);
        this.javaPath = javaPath;
    }

    /**
     * Creates a JavaOption with the default 'java' path.
     * <p>
     * This constructor will turn on cgc if the default jvm supports cgc.
     * 
     * @see Utils#getJavaPath()
     */
    public JavaOption() {
        this(Utils.getJavaPath());
        cgc = Utils.isCGCSupported();
    }

    /**
     * Gets the java path.
     * 
     * @return the 'java' executable file
     */
    public File getJavaPath() {
        return javaPath;
    }

    /**
     * Sets the java path.
     * 
     * @param javaPath the java executable file path to set
     * @throws NullPointerException if <code>javaPath==null</code>
     */
    public void setJavaPath(File javaPath) {
        Objects.requireNonNull(javaPath);
        this.javaPath = javaPath;
    }

    /**
     * Returns true if the target jvm is in cgc.
     * <p>
     * By default, cgc is off.
     * 
     * @return true if the target jvm is in cgc
     */
    public boolean isInCGC() {
        return cgc;
    }

    /**
     * Sets to true to turn on cgc.
     * <p>
     * It's only effective when the target jvm supports cgc.<br>
     * If the target jvm doesn't support cgc option, this may cause some errors.
     * 
     * @param cgc true to turn on cgc, false to turn off
     */
    public void setCGC(boolean cgc) {
        this.cgc = cgc;
    }

}
