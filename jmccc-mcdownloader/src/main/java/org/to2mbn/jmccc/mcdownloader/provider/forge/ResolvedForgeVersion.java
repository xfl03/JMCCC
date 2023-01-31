package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ResolvedForgeVersion implements Serializable {

    private static final Pattern FORGE_VERSION_PATTERN_1 = Pattern.compile("^([\\w\\.\\-]+)-[Ff]orge\\1?-([\\w\\.\\-]+)$");
    private static final Pattern FORGE_VERSION_PATTERN_2 = Pattern.compile("^([\\w\\.\\-]+)-[Ff]orge([\\w\\.\\-]+)$");
    private static final Pattern FORGE_VERSION_PATTERN_3 = Pattern.compile("^Forge([\\w\\.\\-]+)$");
    private static final long serialVersionUID = 1L;
    private String forgeVersion;
    private String minecraftVersion;
    public ResolvedForgeVersion(ForgeVersion version) {
        this(version.getForgeVersion(), version.getMinecraftVersion());
    }

    public ResolvedForgeVersion(String forgeVersion, String minecraftVersion) {
        this.forgeVersion = forgeVersion;
        this.minecraftVersion = minecraftVersion;
    }

    public static ResolvedForgeVersion resolve(String version) {
        Matcher matcher = FORGE_VERSION_PATTERN_1.matcher(version);
        if (matcher.matches()) {
            String forgeVersion = matcher.group(2);
            String mcversion = matcher.group(1);
            return new ResolvedForgeVersion(forgeVersion, mcversion);
        }

        matcher = FORGE_VERSION_PATTERN_2.matcher(version);
        if (matcher.matches()) {
            String forgeVersion = matcher.group(2);
            String mcversion = matcher.group(1);
            return new ResolvedForgeVersion(forgeVersion, mcversion);
        }

        matcher = FORGE_VERSION_PATTERN_3.matcher(version);
        if (matcher.matches()) {
            String forgeVersion = matcher.group(1);
            return new ResolvedForgeVersion(forgeVersion, null);
        }

        return null;
    }

    public String getForgeVersion() {
        return forgeVersion;
    }

    /**
     * @return the minecraft version of the forge version, may be null
     */
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getVersionName() {
        return minecraftVersion + "-" + forgeVersion;
    }

    @Override
    public String toString() {
        return getVersionName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(forgeVersion, minecraftVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ResolvedForgeVersion) {
            ResolvedForgeVersion another = (ResolvedForgeVersion) obj;
            return Objects.equals(forgeVersion, another.forgeVersion) &&
                    Objects.equals(minecraftVersion, another.minecraftVersion);
        }
        return false;
    }

}
