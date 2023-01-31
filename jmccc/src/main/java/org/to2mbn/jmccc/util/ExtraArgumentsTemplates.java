package org.to2mbn.jmccc.util;

import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public final class ExtraArgumentsTemplates {

    public static final String FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES = "-Dfml.ignoreInvalidMinecraftCertificates=true";
    public static final String FML_IGNORE_PATCH_DISCREPANCISE = "-Dfml.ignorePatchDiscrepancies=true";

    /**
     * Caution: This option is available only on OSX.
     * <p>
     * You may need to check the current os before using the option:
     * <pre>
     * <code>
     * if (Platform.CURRENT == Platform.OSX) {
     * 	// add the option to the argument list
     * }
     * </code>
     * </pre>
     *
     * @see #OSX_DOCK_ICON(MinecraftDirectory, Set)
     * @see #OSX_DOCK_ICON(MinecraftDirectory, Version)
     */
    public static final String OSX_DOCK_NAME = "-Xdock:name=Minecraft";

    private ExtraArgumentsTemplates() {
    }

    /**
     * Caution: This option is available only on OSX.
     *
     * @param minecraftDir the minecraft directory
     * @param assetIndex   the asset index
     * @return a <code>-Xdock:icon</code> option, null if the assets cannot be resolved
     * @see #OSX_DOCK_ICON(MinecraftDirectory, Version)
     * @see #OSX_DOCK_NAME
     */
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

    /**
     * Caution: This option is available only on OSX.
     *
     * @param minecraftDir the minecraft directory
     * @param version      the minecraft version
     * @return a <code>-Xdock:icon</code> option, null if the assets cannot be resolved
     * @throws IOException if an I/O error has occurred during resolving asset index
     * @see #OSX_DOCK_ICON(MinecraftDirectory, Set)
     * @see #OSX_DOCK_NAME
     */
    public static String OSX_DOCK_ICON(MinecraftDirectory minecraftDir, Version version) throws IOException {
        Set<Asset> assetIndex = Versions.resolveAssets(minecraftDir, version);
        if (assetIndex == null)
            return null;

        return OSX_DOCK_ICON(minecraftDir, assetIndex);
    }

}
