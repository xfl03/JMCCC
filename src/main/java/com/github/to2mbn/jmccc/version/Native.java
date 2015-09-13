package com.github.to2mbn.jmccc.version;

import java.util.Objects;
import java.util.Set;

public class Native extends Library {

    private String arch;
    private Set<String> extractExcludes;

    /**
     * Creates a native.
     * 
     * @param domain the domain of the native
     * @param name the name of the native
     * @param version the version of the native
     * @param arch the arch of the native
     * @param extractExcludes the extract excludes list of the native, null if no excludes
     * @throws NullPointerException if <code>domain==null||name==null||version==null||arch==null</code>
     */
    public Native(String domain, String name, String version, String arch, Set<String> extractExcludes) {
        this(domain, name, version, arch, extractExcludes, null);
    }

    /**
     * Creates a native with the custom download url.
     * 
     * @param domain the domain of the native
     * @param name the name of the native
     * @param version the version of the native
     * @param arch the arch of the native
     * @param extractExcludes the extract excludes list of the native, null if no excludes
     * @param customUrl the custom maven repository url
     * @throws NullPointerException if <code>domain==null||name==null||version==null||arch==null</code>
     */
    public Native(String domain, String name, String version, String arch, Set<String> extractExcludes, String customUrl) {
        super(domain, name, version, customUrl);
        Objects.requireNonNull(arch);
        this.arch = arch;
        this.extractExcludes = extractExcludes;
    }

    /**
     * Gets the arch of the native
     * 
     * @return the arch of the native
     */
    public String getArch() {
        return arch;
    }

    /**
     * Gets the extract excludes list of the native, null if no excludes.
     * 
     * @return the extract excludes list of the native, null if no excludes
     */
    public Set<String> getExtractExcludes() {
        return extractExcludes;
    }

    @Override
    public String toString() {
        return super.toString() + "@" + arch;
    }

    @Override
    public String getPath() {
        return getDomain().replace('.', '/') + "/" + getName() + "/" + getVersion() + "/" + getName() + "-" + getVersion() + "-" + arch + ".jar";
    }
}
