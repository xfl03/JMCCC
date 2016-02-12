package org.to2mbn.jmccc.option;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import org.to2mbn.jmccc.util.Platform;

public class JavaOption implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Gets the current 'java' path.
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
	 */
	public JavaOption(File javaPath) {
		Objects.requireNonNull(javaPath);
		this.javaPath = javaPath;
	}

	/**
	 * Creates a JavaOption with the default 'java' path.
	 * 
	 * @see JavaOption#getCurrentJavaPath()
	 */
	public JavaOption() {
		this(getCurrentJavaPath());
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
	 * Returns true if the target jvm is in the cgc-mode.
	 * <p>
	 * By default, the cgc-mode is off.
	 * 
	 * @return true if the target jvm is in the cgc-mode
	 */
	public boolean isInCGC() {
		return cgc;
	}

	/**
	 * Sets to true to turn on the cgc-mode.
	 * <p>
	 * If the target jvm doesn't support the cgc-mode option, turning this on may cause some problems.
	 * 
	 * @param cgc set it true to turn on the cgc-mode, set it false to turn it off
	 */
	public void setCGC(boolean cgc) {
		this.cgc = cgc;
	}

	@Override
	public String toString() {
		return cgc ? javaPath + " [CGC on]" : javaPath.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof JavaOption) {
			JavaOption another = (JavaOption) obj;
			return cgc == another.cgc && javaPath.equals(another.javaPath);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(javaPath, cgc);
	}

}
