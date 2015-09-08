package com.github.to2mbn.jmccc.option;

import java.io.File;
import java.util.Objects;

/**
 * Describes a minecraft directory.
 * 
 * @author yushijinhun
 */
public class MinecraftDirectory {

    /**
     * The '.minecraft' dir.
     */
    protected File rootDir;

    /**
     * Creates a MinecraftDirectory with the given root directory.
     * 
     * @param rootDir the root directory of minecraft (eg. <code>".minecraft"<code>)
     */
    public MinecraftDirectory(File rootDir) {
        Objects.requireNonNull(rootDir);
        this.rootDir = rootDir;
    }

    /**
     * Gets the root of the minecraft directory.
     * <p>
     * Usually it's '.minecraft'.
     * 
     * @return the root of the minecraft directory
     */
    public File getRoot() {
        return rootDir;
    }

    /**
     * Gets the versions directory.
     * 
     * @return the versions directory
     */
    public File getVersions() {
        return new File(rootDir, "versions");
    }

    /**
     * Gets the libraries directory.
     * 
     * @return the libraries directory
     */
    public File getLibraries() {
        return new File(rootDir, "libraries");
    }

    /**
     * Gets the saves directory.
     * 
     * @return the saves directory
     */
    public File getSaves() {
        return new File(rootDir, "saves");
    }

    /**
     * Gets the natives directory.
     * 
     * @return the natives directory
     */
    public File getNatives() {
        return new File(rootDir, "natives");
    }

    /**
     * Gets the assets directory.
     * 
     * @return the assets directory
     */
    public File getAssets() {
        return new File(rootDir, "assets");
    }

    /**
     * Gets the base directory of the given version
     * 
     * @param version the version
     * @return the base directory of the given version
     */
    public File getVersion(String version) {
        return new File(getVersions(), version);
    }

    /**
     * Gets the json file of the given version
     * 
     * @param version the version
     * @return the json file of the given version
     */
    public File getVersionJson(String version) {
        return new File(getVersion(version), version + ".json");
    }

    /**
     * Gets the jar file of the given version
     * 
     * @param version the version
     * @return the jar file of the given version
     */
    public File getVersionJar(String version) {
        return new File(getVersion(version), version + ".jar");
    }

}
