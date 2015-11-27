package com.github.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.util.Date;
import java.util.Objects;

public class LiteloaderVersion {

	private String minecraftVersion;
	private String liteloaderVersion;
	private Date releaseDate;

	public LiteloaderVersion(String minecraftVersion, String liteloaderVersion, Date releaseDate) {
		Objects.requireNonNull(minecraftVersion);
		Objects.requireNonNull(liteloaderVersion);
		this.minecraftVersion = minecraftVersion;
		this.liteloaderVersion = liteloaderVersion;
		this.releaseDate = releaseDate;
	}

	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	public String getLiteloaderVersion() {
		return liteloaderVersion;
	}

	public Date getReleaseDate() {
		return releaseDate;
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
		return Objects.hash(minecraftVersion, liteloaderVersion, releaseDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof LiteloaderVersion) {
			LiteloaderVersion another = (LiteloaderVersion) obj;
			return minecraftVersion.equals(another.minecraftVersion) && liteloaderVersion.equals(another.liteloaderVersion) && Objects.equals(releaseDate, another.releaseDate);
		}
		return false;
	}

}
