package org.to2mbn.jmccc.version.parsing;

import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Version;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A tool class for resolving versions.
 *
 * @author yushijinhun
 */
public final class Versions {

    private final static VersionParser PARSER = new VersionParserImpl();

    private Versions() {
    }

    /**
     * Resolves the version.
     *
     * @param minecraftDir the minecraft directory
     * @param version      the version name
     * @return the version object, or null if the version does not exist
     * @throws IOException          if an I/O error has occurred during resolving version
     * @throws NullPointerException if
     *                              <code>minecraftDir==null || version==null</code>
     */
    public static Version resolveVersion(MinecraftDirectory minecraftDir, String version) throws IOException {
        Objects.requireNonNull(minecraftDir);
        Objects.requireNonNull(version);

        if (doesVersionExist(minecraftDir, version)) {
            try {
                return getVersionParser().parseVersion(resolveVersionHierarchy(version, minecraftDir), PlatformDescription.current());
            } catch (JSONException e) {
                throw new IOException("Couldn't parse version json: " + version, e);
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the set of versions in the given minecraft directory.
     * <p>
     * This method returns a non-threaded safe, unordered set.
     *
     * @param minecraftDir the minecraft directory
     * @return the set of versions
     * @throws NullPointerException if <code>minecraftDir==null</code>
     */
    public static Set<String> getVersions(MinecraftDirectory minecraftDir) {
        Objects.requireNonNull(minecraftDir);
        Set<String> versions = new TreeSet<>();

        // null if the 'versions' dir not exists
        File[] subdirs = minecraftDir.getVersions().listFiles();
        if (subdirs != null) {
            for (File file : subdirs) {
                if (file.isDirectory() && doesVersionExist(minecraftDir, file.getName())) {
                    versions.add(file.getName());
                }
            }
        }
        return Collections.unmodifiableSet(versions);
    }

    /**
     * Resolves the asset index.
     *
     * @param minecraftDir the minecraft directory
     * @param version      the owner version of the asset index
     * @return the asset index, or null if the asset index does not exist
     * @throws IOException          if an I/O error occurs during resolving asset index
     * @throws NullPointerException if
     *                              <code>minecraftDir==null || version==null</code>
     */
    public static Set<Asset> resolveAssets(MinecraftDirectory minecraftDir, Version version) throws IOException {
        return resolveAssets(minecraftDir, version.getAssets());
    }

    /**
     * Resolves the asset index.
     *
     * @param minecraftDir the minecraft directory
     * @param assets       the name of the asset index, you can get this via
     *                     {@link Version#getAssets()}
     * @return the asset index, null if the asset index does not exist
     * @throws IOException          if an I/O error has occurred during resolving asset
     *                              index
     * @throws NullPointerException if
     *                              <code>minecraftDir==null || assets==null</code>
     */
    public static Set<Asset> resolveAssets(MinecraftDirectory minecraftDir, String assets) throws IOException {
        Objects.requireNonNull(minecraftDir);
        Objects.requireNonNull(assets);
        if (!minecraftDir.getAssetIndex(assets).isFile()) {
            return null;
        }

        try {
            return getVersionParser().parseAssetIndex(IOUtils.toJson(minecraftDir.getAssetIndex(assets)));
        } catch (JSONException e) {
            throw new IOException("Couldn't parse asset index: " + assets, e);
        }
    }

    public static VersionParser getVersionParser() {
        return PARSER;
    }

    private static boolean doesVersionExist(MinecraftDirectory minecraftDir, String version) {
        return minecraftDir.getVersionJson(version).isFile();
    }

    private static Stack<JSONObject> resolveVersionHierarchy(String version, MinecraftDirectory mcdir) throws IOException, JSONException {
        Stack<JSONObject> result = new Stack<>();
        do {
            JSONObject json = IOUtils.toJson(mcdir.getVersionJson(version));
            result.push(json);
            version = json.optString("inheritsFrom", null);
        } while (version != null);
        return result;
    }

}
