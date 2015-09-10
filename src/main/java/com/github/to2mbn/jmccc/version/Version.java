package com.github.to2mbn.jmccc.version;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;

public class Version {

    private String version;
    private String mainClass;
    private String assets;
    private String launchArgs;
    private File jar;
    private Set<Library> libraries = new HashSet<>();

    /**
     * Creates a Version object.
     * 
     * @param version the version number
     * @param mainClass the main class
     * @param assets the assets index name
     * @param launchArgs the launch arguments
     * @param jar the relative path of the jar file
     * @param libraries the libraries to add to classpath
     * @throws NullPointerException if
     *             <code>version==null||mainClass==null||assets==null||launchArgs==null||jar==null||libraries==null</code>
     */
    public Version(String version, String mainClass, String assets, String launchArgs, File jar, Set<Library> libraries) {
        this.version = version;
        this.mainClass = mainClass;
        this.assets = assets;
        this.launchArgs = launchArgs;
        this.jar = jar;
        this.libraries = libraries;
    }

    /**
     * Gets the version number.
     * 
     * @return the version number
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the main class.
     * 
     * @return the main class
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * Gets the assets index name.
     * 
     * @return the assets index name
     */
    public String getAssets() {
        return assets;
    }

    /**
     * Gets the launch arguments.
     * 
     * @return the launch arguments
     */
    public String getLaunchArgs() {
        return launchArgs;
    }

    /**
     * Gets the jar file.
     * 
     * @return the jar file
     */
    public File getJar() {
        return jar;
    }

    /**
     * Gets the required libraries.
     * 
     * @return the required libraries
     */
    public Set<Library> getLibraries() {
        return libraries;
    }

    /**
     * Returns the missing libraries in the given minecraft directory.
     * 
     * @param minecraftDir the minecraft directory to check
     * @return true the missing libraries in the given minecraft directory, an empty set if no library is missing
     */
    public Set<Library> getMissingLibraries(MinecraftDirectory minecraftDir) {
        Set<Library> missing = new HashSet<>();
        for (Library library : libraries) {
            if (library.isMissing(minecraftDir)) {
                missing.add(library);
            }
        }
        return missing;
    }

}
