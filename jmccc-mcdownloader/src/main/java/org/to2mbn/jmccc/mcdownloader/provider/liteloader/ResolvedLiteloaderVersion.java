package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ResolvedLiteloaderVersion implements Serializable {

    private static final Pattern LITELOADER_VERSION_PATTERN = Pattern.compile("^([\\w\\.\\-]+)-[lL]ite[lL]oader([\\w\\.\\-]+)$");
    private static final long serialVersionUID = 1L;
    private String minecraftVersion;
    private String superVersion;
    public ResolvedLiteloaderVersion(LiteloaderVersion version) {
        this(version.getMinecraftVersion(), version.getSuperVersion());
    }

    public ResolvedLiteloaderVersion(String minecraftVersion, String superVersion) {
        this.minecraftVersion = minecraftVersion;
        this.superVersion = superVersion;
    }

    public static ResolvedLiteloaderVersion resolve(String version) {
        Matcher matcher = LITELOADER_VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            String superVersion = matcher.group(1);
            String minecraftVersion = matcher.group(2);
            return new ResolvedLiteloaderVersion(minecraftVersion, superVersion);
        }

        return null;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getSuperVersion() {
        return superVersion;
    }

    public String getVersionName() {
        return superVersion + "-" + minecraftVersion;
    }

    @Override
    public String toString() {
        return getVersionName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(minecraftVersion, superVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ResolvedLiteloaderVersion) {
            ResolvedLiteloaderVersion another = (ResolvedLiteloaderVersion) obj;
            return Objects.equals(minecraftVersion, another.minecraftVersion) &&
                    Objects.equals(superVersion, another.superVersion);
        }
        return false;
    }

}
