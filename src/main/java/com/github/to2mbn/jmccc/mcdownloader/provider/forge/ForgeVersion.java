package com.github.to2mbn.jmccc.mcdownloader.provider.forge;

import java.util.Objects;
import org.json.JSONObject;

public class ForgeVersion {
	
	public static ForgeVersion fromJson(JSONObject json) {
		return new ForgeVersion(json.getString("mcversion"), json.getString("version"), json.getInt("build"));
	}

	private String minecraftVersion;
	private String forgeVersion;
	private int buildNumber;

	public ForgeVersion(String minecraftVersion, String forgeVersion, int buildNumber) {
		Objects.requireNonNull(minecraftVersion);
		Objects.requireNonNull(forgeVersion);
		this.minecraftVersion = minecraftVersion;
		this.forgeVersion = forgeVersion;
		this.buildNumber = buildNumber;
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

	public String getVersionName() {
		return minecraftVersion + "-forge" + minecraftVersion + "-" + forgeVersion;
	}

	@Override
	public String toString() {
		return getVersionName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(minecraftVersion, forgeVersion, buildNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ForgeVersion) {
			ForgeVersion another = (ForgeVersion) obj;
			return minecraftVersion.equals(another.minecraftVersion) && forgeVersion.equals(forgeVersion) && buildNumber == another.buildNumber;
		}
		return false;
	}

}
