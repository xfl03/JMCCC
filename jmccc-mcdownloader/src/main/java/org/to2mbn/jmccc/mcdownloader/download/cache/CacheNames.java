package org.to2mbn.jmccc.mcdownloader.download.cache;

public final class CacheNames {

    private static final String PREFIX = "org.to2mbn.jmccc.mcdownloader.cache.";
    // Default cache
    public static final String DEFAULT = PREFIX + "default";
    private static final String PREFIX_STATIC = PREFIX + "static.";
    public static final String ASSET_INDEX = PREFIX_STATIC + "assetsIndex";
    public static final String GAME_JAR = PREFIX_STATIC + "gameJar";
    public static final String LIBRARY = PREFIX_STATIC + "library";
    public static final String ASSET = PREFIX_STATIC + "asset";
    public static final String FORGE_INSTALLER = PREFIX_STATIC + "forge.installer";
    public static final String FORGE_UNIVERSAL = PREFIX_STATIC + "forge.universal";
    private static final String PREFIX_DYNAMIC = PREFIX + "dynamic.";
    // Official minecraft caches
    public static final String VERSION_LIST = PREFIX_DYNAMIC + "versionList";
    public static final String VERSION_JSON = PREFIX_DYNAMIC + "versionJson";
    // Forge caches
    public static final String FORGE_VERSION_META = PREFIX_DYNAMIC + "forge.versionMeta";
    public static final String FORGE_VERSION_PROMO = PREFIX_DYNAMIC + "forge.versionPromo";
    // Liteloader caches
    public static final String LITELOADER_VERSION_LIST = PREFIX_DYNAMIC + "liteloader.versionList";
    public static final String LITELOADER_VERSION_JSON = PREFIX_DYNAMIC + "liteloader.versionJson";
    // Fabric caches
    public static final String FABRIC_VERSION_LIST = PREFIX_DYNAMIC + "fabric.versionList";

    // Maven
    public static final String M2_METADATA = PREFIX_DYNAMIC + "m2.metadata";

    private CacheNames() {
    }

}
