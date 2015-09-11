package com.github.to2mbn.jmccc.version;

import java.io.File;
import java.util.Objects;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;

public class Library {

    /**
     * The relative path of the library.
     * <p>
     * Use '/' as the separator char, and 'libraries' as the base dir.
     */
    private String path;

    private String domain;
    private String name;
    private String version;

    /**
     * Creates a library.
     * 
     * @param domain the domain of the library
     * @param name the name of the library
     * @param version the version of the library
     * @throws NullPointerException if <code>domain==null||name==null||version==null</code>
     */
    public Library(String domain, String name, String version) {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
        this.domain = domain;
        this.name = name;
        this.version = version;

        path = generatePath();
    }

    /**
     * Gets the relative path of the library.
     * <p>
     * Use '/' as the separator char, and 'libraries' as the base dir.
     * 
     * @return the relative path of the library
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the name of the library.
     * 
     * @return the name of the library
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the domain of this library.
     * 
     * @return the domain of this library
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the version of this library.
     * 
     * @return the version of this library
     */
    public String getVersion() {
        return version;
    }

    /**
     * Checks if the library is missing in the given minecraft directory.
     * 
     * @param minecraftDir the minecraft directory to check
     * @return true if the library is missing in the given minecraft directory
     */
    public boolean isMissing(MinecraftDirectory minecraftDir) {
        return !new File(minecraftDir.getLibraries(), path).isFile();
    }

    @Override
    public String toString() {
        return domain + ":" + name + ":" + version;
    }

    protected String generatePath() {
        return domain.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".jar";
    }

}
