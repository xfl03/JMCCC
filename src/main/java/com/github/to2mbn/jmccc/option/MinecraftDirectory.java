package com.github.to2mbn.jmccc.option;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/**
 * Describes a minecraft directory.
 * 
 * @author yushijinhun
 */
public class MinecraftDirectory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The '.minecraft' dir.
     */
    protected File rootDir;

    /**
     * Creates a MinecraftDirectory with the '.minecraft' directory in the current directory.
     */
    public MinecraftDirectory() {
        this(new File(".minecraft"));
    }

    /**
     * Creates a MinecraftDirectory with the given root directory.
     * 
     * @param rootDir the root directory of minecraft (eg. <code>".minecraft"</code>)
     */
    public MinecraftDirectory(String rootDir) {
        this(new File(rootDir));
    }

    /**
     * Creates a MinecraftDirectory with the given root directory.
     * 
     * @param rootDir the root directory of minecraft (eg. <code>".minecraft"</code>)
     */
    public MinecraftDirectory(File rootDir) {
        Objects.requireNonNull(rootDir);
        this.rootDir = rootDir.getAbsoluteFile();
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
     * @param version the owner of the natives
     * @return the natives directory
     */
    public File getNatives(String version) {
        return new File(getVersion(version), version + "-natives");
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
     * Gets the virtual assets directory.
     * 
     * @return the virtual assets directory
     */
    public File getVirtualAssets() {
        return new File(getAssets(), "virtual");
    }

    /**
     * Gets the virtual legacy assets directory.
     * 
     * @return the virtual legacy assets directory
     */
    public File getVirtualLegacyAssets() {
        return new File(getVirtualAssets(), "legacy");
    }

    /**
     * Gets the object assets directory.
     * 
     * @return the object assets directory
     */
    public File getAssetObjects() {
        return new File(getAssets(), "objects");
    }

    /**
     * Gets the asset indexes directory.
     * 
     * @return the asset indexes directory
     */
    public File getAssetIndexes() {
        return new File(getAssets(), "indexes");
    }

    /**
     * Gets the asset index file.
     * 
     * @param assets the name of the asset index
     * @return the asset index file
     */
    public File getAssetIndex(String assets) {
        return new File(getAssetIndexes(), assets + ".json");
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
        return getVersionJson(version, version);
    }

    /**
     * Gets the jar file of the given version
     * 
     * @param version the version
     * @return the jar file of the given version
     */
    public File getVersionJar(String version) {
        return getVersionJar(version, version);
    }

    /**
     * Gets the json file of the given version
     * 
     * @param version the version
     * @param jarName the jar name
     * @return the json file of the given version
     */
    public File getVersionJson(String version, String jarName) {
        return new File(getVersion(version), jarName + ".json");
    }

    /**
     * Gets the jar file of the given version
     * 
     * @param version the version
     * @param jarName the jar name
     * @return the jar file of the given version
     */
    public File getVersionJar(String version, String jarName) {
        return new File(getVersion(version), jarName + ".jar");
    }

    @Override
    public String toString() {
        return rootDir.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MinecraftDirectory) {
            MinecraftDirectory another = (MinecraftDirectory) obj;
            return rootDir.equals(another.rootDir);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return rootDir.hashCode();
    }

}
