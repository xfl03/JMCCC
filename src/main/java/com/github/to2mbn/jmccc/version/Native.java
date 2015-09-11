package com.github.to2mbn.jmccc.version;

import java.util.Objects;
import java.util.Set;

public class Native extends Library {

    private String natives;
    private Set<String> extractExcludes;

    /**
     * Creates a native.
     * 
     * @param domain the domain of the native
     * @param name the name of the native
     * @param version the version of the native
     * @param natives the arch of the native
     * @param extractExcludes the extract excludes list of the native, null if no excludes
     * @throws NullPointerException if <code>domain==null||name==null||version==null||natives==null</code>
     */
    public Native(String domain, String name, String version, String natives, Set<String> extractExcludes) {
        super(domain, name, version);
        Objects.requireNonNull(natives);
        this.natives = natives;
        this.extractExcludes = extractExcludes;
    }

    /**
     * Gets the arch of the native
     * 
     * @return the arch of the native
     */
    public String getNatives() {
        return natives;
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
    protected String generatePath() {
        return getDomain().replace('.', '/') + "/" + getName() + "/" + getVersion() + "/" + getName() + "-" + getVersion() + "-" + natives + ".jar";
    }
}
