package org.to2mbn.jmccc.option;

import org.to2mbn.jmccc.util.Platform;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class JavaEnvironment implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * the 'java' executable file
     */
    private final File javaPath;

    /**
     * Creates a JavaEnvironment with the given 'java' path.
     *
     * @param javaPath the 'java' executable file
     * @throws NullPointerException if
     *                              <code>minecraftDir==null||javaPath==null</code>
     */
    public JavaEnvironment(File javaPath) {
        Objects.requireNonNull(javaPath);
        this.javaPath = javaPath;
    }

    /**
     * Returns the current java environment.
     *
     * @return the current JavaOption
     */
    public static JavaEnvironment current() {
        return new JavaEnvironment(getCurrentJavaPath());
    }

    /**
     * Gets the current 'java' executable file path.
     * <p>
     * On *nix systems it's <code>$java.home/bin/java</code>.<br>
     * On Windows it's <code>$java.home\bin\java.exe</code>.
     *
     * @return the current 'java' path
     */
    public static File getCurrentJavaPath() {
        return new File(System.getProperty("java.home"), "bin/java" + (Platform.CURRENT == Platform.WINDOWS ? ".exe" : ""));
    }

    /**
     * Gets the java path.
     *
     * @return the 'java' executable file
     */
    public File getJavaPath() {
        return javaPath;
    }

    @Override
    public String toString() {
        return javaPath.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof JavaEnvironment) {
            JavaEnvironment another = (JavaEnvironment) obj;
            return javaPath.equals(another.javaPath);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return javaPath.hashCode();
    }

}