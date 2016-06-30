package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.Serializable;
import java.util.Objects;

public class ForgeVersion implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String minecraftVersion;
	private String forgeVersion;
	private int buildNumber;
	private String branch;

	public ForgeVersion(String minecraftVersion, String forgeVersion, int buildNumber, String branch) {
		this.minecraftVersion = Objects.requireNonNull(minecraftVersion);
		this.forgeVersion = Objects.requireNonNull(forgeVersion);
		this.buildNumber = buildNumber;
		this.branch = branch;
	}

	// Getters
	// @formatter:off
	public String getMinecraftVersion() { return minecraftVersion; }
	public String getForgeVersion() { return forgeVersion; }
	public int getBuildNumber() { return buildNumber; }
	public String getBranch() { return branch; }
	// @formatter:on

	public String getVersionName() {
		return minecraftVersion + "-forge" + minecraftVersion + "-" + forgeVersion;
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
