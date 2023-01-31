package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import org.to2mbn.jmccc.internal.org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class LiteloaderVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String minecraftVersion;
    private String liteloaderVersion;
    private String superVersion;
    private String tweakClass;
    private String repoUrl;
    private Set<JSONObject> libraries;

    public LiteloaderVersion(String minecraftVersion, String liteloaderVersion, String tweakClass, String repoUrl, Set<JSONObject> libraries) {
        this(minecraftVersion, liteloaderVersion, minecraftVersion, tweakClass, repoUrl, libraries);
    }

    public LiteloaderVersion(String minecraftVersion, String liteloaderVersion, String superVersion, String tweakClass, String repoUrl, Set<JSONObject> libraries) {
        this.minecraftVersion = Objects.requireNonNull(minecraftVersion);
        this.liteloaderVersion = Objects.requireNonNull(liteloaderVersion);
        this.superVersion = Objects.requireNonNull(superVersion);
        this.tweakClass = tweakClass;
        this.repoUrl = repoUrl;
        this.libraries = libraries;
    }

    // Getters
    // @formatter:off
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getLiteloaderVersion() {
        return liteloaderVersion;
    }

    public String getSuperVersion() {
        return superVersion;
    }

    public String getTweakClass() {
        return tweakClass;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public Set<JSONObject> getLibraries() {
        return libraries;
    }
    // @formatter:on

    public String getVersionName() {
        return superVersion + "-LiteLoader" + minecraftVersion;
    }

    public LiteloaderVersion customize(String superVersion) {
        return new LiteloaderVersion(minecraftVersion, liteloaderVersion, superVersion, tweakClass, repoUrl, libraries);
    }

    @Override
    public String toString() {
        return getVersionName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(minecraftVersion, liteloaderVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof LiteloaderVersion) {
            LiteloaderVersion another = (LiteloaderVersion) obj;
            return Objects.equals(minecraftVersion, another.minecraftVersion) &&
                    Objects.equals(liteloaderVersion, another.liteloaderVersion) &&
                    Objects.equals(superVersion, another.superVersion) &&
                    Objects.equals(tweakClass, another.tweakClass) &&
                    Objects.equals(repoUrl, another.repoUrl) &&
                    Objects.equals(libraries, another.libraries);
        }
        return false;
    }

}
