package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.Serializable;
import java.util.Objects;

public class ForgeVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String minecraftVersion;
    private final String forgeVersion;
    private final int buildNumber;
    private final String branch;

    public ForgeVersion(String minecraftVersion, String forgeVersion, int buildNumber, String branch) {
        this.minecraftVersion = Objects.requireNonNull(minecraftVersion);
        this.forgeVersion = Objects.requireNonNull(forgeVersion);
        this.buildNumber = buildNumber;
        this.branch = branch;
    }

    public static ForgeVersion from(String fullVersion) {
        String[] split = fullVersion.split("-", 3);
        String mcVersion = split[0];
        String forgeVersion = split[1];
        String[] split1 = forgeVersion.split("\\.");
        int buildNumber = Integer.parseInt(split1[split1.length - 1]);
        String branch = split.length == 3 ? split[2] : null;
        return new ForgeVersion(mcVersion, forgeVersion, buildNumber, branch);
    }

    // Getters
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getForgeVersion() {
        return forgeVersion;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public String getBranch() {
        return branch;
    }
    // @formatter:on

    public String getVersionName() {
        return minecraftVersion + "-forge-" + forgeVersion;
    }

    public String getMavenVersion() {
        String ver = minecraftVersion + "-" + forgeVersion;
        if (branch != null)
            ver += "-" + branch;
        return ver;
    }

    @Override
    public String toString() {
        return getVersionName();
    }

    @Override
    public int hashCode() {
        return forgeVersion.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ForgeVersion) {
            ForgeVersion another = (ForgeVersion) obj;
            return Objects.equals(minecraftVersion, another.minecraftVersion)
                    && Objects.equals(forgeVersion, another.forgeVersion)
                    && buildNumber == another.buildNumber
                    && Objects.equals(branch, another.branch);
        }
        return false;
    }

}
