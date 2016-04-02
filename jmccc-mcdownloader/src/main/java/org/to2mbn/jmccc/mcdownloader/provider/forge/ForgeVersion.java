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
		Objects.requireNonNull(forgeVersion);
		this.minecraftVersion = minecraftVersion;
		this.forgeVersion = forgeVersion;
		this.buildNumber = buildNumber;
		this.branch = branch;
	}

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

	public String getVersionName() {
		return minecraftVersion + "-forge" + minecraftVersion + "-" + forgeVersion;
	}

	@Override
	public String toString() {
		return getVersionName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(minecraftVersion, forgeVersion, buildNumber, branch);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ForgeVersion) {
			ForgeVersion another = (ForgeVersion) obj;
			return minecraftVersion.equals(another.minecraftVersion) && forgeVersion.equals(forgeVersion) && buildNumber == another.buildNumber && Objects.equals(branch, another.branch);
		}
		return false;
	}

}
