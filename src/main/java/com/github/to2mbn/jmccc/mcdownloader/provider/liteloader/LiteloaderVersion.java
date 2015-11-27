package com.github.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.util.Objects;

public class LiteloaderVersion {

	private String minecraftVersion;
	private String liteloaderVersion;

	public LiteloaderVersion(String minecraftVersion, String liteloaderVersion) {
		Objects.requireNonNull(minecraftVersion);
		Objects.requireNonNull(liteloaderVersion);
		this.minecraftVersion = minecraftVersion;
		this.liteloaderVersion = liteloaderVersion;
	}

	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	public String getLiteloaderVersion() {
		return liteloaderVersion;
	}

	public String getVersionName() {
		return minecraftVersion + "-LiteLoader" + minecraftVersion;
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
			return minecraftVersion.equals(another.minecraftVersion) && liteloaderVersion.equals(another.liteloaderVersion);
		}
		return false;
	}

}
