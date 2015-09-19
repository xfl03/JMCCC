package com.github.to2mbn.jmccc.version;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.util.Utils;

public class Library {

    private String domain;
    private String name;
    private String version;
    private String customUrl;
    private String[] checksums;

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
     * Creates a library with the custom download url and checksums.
     * 
     * @param domain the domain of the library
     * @param name the name of the library
     * @param version the version of the library
     * @param customUrl the custom maven repository url
     * @param checksums the checksums
     * @throws NullPointerException if <code>domain==null||name==null||version==null</code>
     */
    public Library(String domain, String name, String version, String customUrl, String[] checksums) {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
        this.domain = domain;
        this.name = name;
        this.version = version;
        this.customUrl = customUrl;
        this.checksums = checksums;
    }

    /**
     * Gets the relative path of the library.
     * <p>
     * Use '/' as the separator char, and 'libraries' as the base dir.
     * 
     * @return the relative path of the library
     */
    public String getPath() {
        return domain.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".jar";
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
     * Gets the custom maven repository url, null for default repository.
     * 
     * @return the custom maven repository url, null for default repository
     */
    public String getCustomUrl() {
        return customUrl;
    }

    /**
     * Returns the sha1 checksums, null if no need to check.<br>
     * If the sha1 hash of the library matches one of the hashes, this library is valid.
     * 
     * @return a map of checksums
     */
    public String[] getChecksums() {
        return checksums;
    }

    /**
     * Checks if the library is missing in the given minecraft directory.
     * 
     * @param minecraftDir the minecraft directory to check
     * @return true if the library is missing in the given minecraft directory
     */
    public boolean isMissing(MinecraftDirectory minecraftDir) {
        return !new File(minecraftDir.getLibraries(), getPath()).isFile();
    }

    /**
     * Checks the hash of the library, returns false if hash mismatches.
     * 
     * @param minecraftDir the minecraft directory to check
     * @return false if hash mismatches
     * @throws NoSuchAlgorithmException if the required hash algorithm is missing
     * @throws FileNotFoundException if the library file not found
     * @throws IOException if an I/O error occures
     */
    public boolean checkHash(MinecraftDirectory minecraftDir) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        if (checksums == null || checksums.length == 0) {
            return true;
        }

        byte[] sha1sum = Utils.hash("SHA-1", new File(minecraftDir.getLibraries(), getPath()));
        for (String checksum : checksums) {
            if (Arrays.equals(sha1sum, Utils.hexToBytes(checksum))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return domain + ":" + name + ":" + version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Library) {
            Library another = (Library) obj;
            return domain.equals(another.domain) && name.equals(another.name) && version.equals(another.version) && Objects.equals(customUrl, another.customUrl) && Arrays.equals(checksums, another.checksums);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[] { domain, name, version, customUrl, checksums });
    }

}
