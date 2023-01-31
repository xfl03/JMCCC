package org.to2mbn.jmccc.launch;

import org.to2mbn.jmccc.util.Builder;

/**
 * Builder for {@link Launcher}.
 *
 * @author yushijinhun
 */
public class LauncherBuilder implements Builder<Launcher> {

    private boolean nativeFastCheck = false;
    private boolean printDebugCommandline = false;
    private boolean useDaemonThreads = false;
    protected LauncherBuilder() {
    }

    /**
     * Creates a new <code>LauncherBuilder</code> instance.
     *
     * @return a new <code>LauncherBuilder</code> instance
     */
    public static LauncherBuilder create() {
        return new LauncherBuilder();
    }

    /**
     * Creates a new <code>Launcher</code> instance with default configurations.
     *
     * @return a <code>Launcher</code> instance
     */
    public static Launcher buildDefault() {
        return create().build();
    }

    /**
     * Sets whether to do a fast check on natives.
     * <p>
     * By default, this feature is off. In this case, when decompressing
     * natives, the jmccc will fully compare the existing natives and the
     * natives in jars. If, and only if, a existing native is modified, jmccc
     * will replace it. Because replacing a native in use may cause the running
     * JVM to be crashed.<br>
     * If the feature is on, the jmccc won't compare the full content of
     * natives. Jmccc only compares the sizes. This can improve the launching
     * speed. But we cannot ensure the contents of the natives are correct.
     *
     * @param nativeFastCheck true to let jmccc do a fast check on natives
     * @return the builder itself
     */
    public LauncherBuilder nativeFastCheck(boolean nativeFastCheck) {
        this.nativeFastCheck = nativeFastCheck;
        return this;
    }

    /**
     * Sets whether to print the launch commandline for debugging.
     * <p>
     * The commandline will be printed to stderr in the following format:
     * <blockquote>
     *
     * <pre>
     * jmccc:
     * &lt;argument1&gt;
     * &lt;argument2&gt;
     * ......
     * </pre>
     *
     * </blockquote>
     *
     * @param printDebugCommandline whether to print the launch commandline for
     *                              debugging.
     * @return the builder itself
     */
    public LauncherBuilder printDebugCommandline(boolean printDebugCommandline) {
        this.printDebugCommandline = printDebugCommandline;
        return this;
    }

    /**
     * Sets whether to use daemon threads to receive logs from the subprocess.
     *
     * @param useDaemonThreads whether to use daemon threads to receive logs
     *                         from the subprocess
     * @return the builder itself
     */
    public LauncherBuilder useDaemonThreads(boolean useDaemonThreads) {
        this.useDaemonThreads = useDaemonThreads;
        return this;
    }

    /**
     * Creates a new <code>Launcher</code> instance according to the
     * configurations.
     *
     * @return a <code>Launcher</code> instance
     */
    @Override
    public Launcher build() {
        LauncherImpl launcher = new LauncherImpl();
        launcher.setNativeFastCheck(nativeFastCheck);
        launcher.setPrintDebugCommandline(printDebugCommandline);
        launcher.setUseDaemonThreads(useDaemonThreads);
        return launcher;
    }

}
