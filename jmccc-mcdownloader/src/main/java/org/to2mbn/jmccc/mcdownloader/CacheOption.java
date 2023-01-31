package org.to2mbn.jmccc.mcdownloader;

public enum CacheOption implements MinecraftDownloadOption {

    /**
     * Cache all the downloads forcibly.
     */
    FORCIBLY_CACHE,

    /**
     * Cache downloads as much as possible, excluding non-cacheable downloads.
     */
    CACHE,

    /**
     * Do not cache any downloads.
     */
    NO_CACHE;
}
