package com.github.to2mbn.jmccc.version;

import java.io.File;
import java.util.Objects;
import java.util.Set;
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
    private String natives;
    private Set<String> extractExcludes;

    /**
     * Creates a library.
     * 
     * @param domain the domain of the library
     * @param name the name of the library
     * @param version the version of the library
     * @throws NullPointerException if <code>domain==null||name==null||version==null</code>
     */
    public Library(String domain, String name, String version) {
        this(domain, name, version, null, null);
    }

    /**
     * Creates a library.
     * 
     * @param domain the domain of the library
     * @param name the name of the library
     * @param version the version of the library
     * @param natives the natives the library needs, null if it's not a native library
     * @param extractExcludes the extract excludes list of the natives, null if no excludes
     * @throws NullPointerException if <code>domain==null||name==null||version==null</code>
     */
    public Library(String domain, String name, String version, String natives, Set<String> extractExcludes) {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);

        this.name = name;
        this.domain = domain;
        this.version = version;
        this.natives = natives;
        this.extractExcludes = extractExcludes;

        if (natives == null) {
            path = domain.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".jar";
        } else {
            path = domain.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + "-" + natives + ".jar";
        }
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
     * Gets the natives the library needs, null if it's not a native library.
     * 
     * @return the natives the library needs, null if it's not a native library
     */
    public String getNatives() {
        return natives;
    }

    /**
     * Gets the extract excludes list of the natives, null if it's not a native library or no excludes.
     * 
     * @return the extract excludes list of the natives, null if it's not a native library or no excludes
     */
    public Set<String> getExtractExcludes() {
        return extractExcludes;
    }

    /**
     * Returns true if the library is a native library.
     * 
     * @return true if the library is a native library
     */
    public boolean isNatives() {
        return natives != null;
    }

    /**
     * Checks if the library is missing in the given minecraft directory.
     * 
     * @param minecraftDir the minecraft directory to check
     * @return true if the library is missing in the given minecraft directory
     */
    public boolean isMissing(MinecraftDirectory minecraftDir) {
        return new File(minecraftDir.getLibraries(), path).isFile();
    }

}
