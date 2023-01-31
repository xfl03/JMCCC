package org.to2mbn.jmccc.mcdownloader;

public enum AssetOption implements MinecraftDownloadOption {

    /**
     * Skip downloading assets.
     */
    SKIP_ASSETS,

    /**
     * Download assets forcibly. This means the whole task will fail if an asset
     * is failed.
     */
    FORCIBLY_DOWNLOAD;

}
