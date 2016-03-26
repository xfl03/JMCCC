package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResolvedForgeVersion implements Serializable {

	private static final Pattern FORGE_VERSION_PATTERN = Pattern.compile("^([\\w\\.\\-]+)-[Ff]orge\\1-([\\w\\.\\-]+)$");
	private static final Pattern OLD_FORGE_VERSION_PATTERN = Pattern.compile("^([\\w\\.\\-]+)-[Ff]orge([\\w\\.\\-]+)$");

	public static ResolvedForgeVersion resolve(String version) {
		Matcher matcher = FORGE_VERSION_PATTERN.matcher(version);
		if (matcher.matches()) {
			String forgeVersion = matcher.group(2);
			String mcversion = matcher.group(1);
			return new ResolvedForgeVersion(forgeVersion, mcversion);
		}

		matcher = OLD_FORGE_VERSION_PATTERN.matcher(version);
		if (matcher.matches()) {
			String forgeVersion = matcher.group(2);
			String mcversion = matcher.group(1);
			return new ResolvedForgeVersion(forgeVersion, mcversion);
		}

		return null;
	}
	
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

	public String getForgeVersion() {
		return forgeVersion;
	}

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
