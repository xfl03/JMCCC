package org.to2mbn.jmccc.util;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

public final class ExtraArgumentsTemplates {

	public static final String FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES = "-Dfml.ignoreInvalidMinecraftCertificates=true";
	public static final String FML_IGNORE_PATCH_DISCREPANCISE = "-Dfml.ignorePatchDiscrepancies=true";
	
	public static final String OSX_DOCK_NAME="-Xdock:name=Minecraft";
	
	public static String OSX_DOCK_ICON(MinecraftDirectory minecraftDir, Set<Asset> assetIndex) {
		Objects.requireNonNull(minecraftDir);
		Objects.requireNonNull(assetIndex);
		for (Asset asset : assetIndex) {
			if ("icons/minecraft.icns".equals(asset.getVirtualPath())) {
				return "-Xdock:icon=" + minecraftDir.getAsset(asset).getAbsolutePath();
			}
		}
		return null;
	}

	public static String OSX_DOCK_ICON(MinecraftDirectory minecraftDir, Version version) throws IOException {
		Set<Asset> assetIndex = Versions.resolveAssets(minecraftDir, version.getAssets());
		if (assetIndex == null) {
			return null;
		}
		return OSX_DOCK_ICON(minecraftDir, assetIndex);
	}

	private ExtraArgumentsTemplates() {
	}

}
