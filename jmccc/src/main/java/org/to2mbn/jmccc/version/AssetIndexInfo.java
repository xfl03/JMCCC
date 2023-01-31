package org.to2mbn.jmccc.version;

import java.util.Objects;

public class AssetIndexInfo extends DownloadInfo {

    private static final long serialVersionUID = 1L;

    private String id;
    private long totalSize;

    /**
     * Constructor of AssetIndexInfo.
     *
     * @param url       the url, or null if the url is unknown
     * @param checksum  the SHA-1 checksum, or null if the checksum is unknown
     * @param size      the file size, or -1 if the size is unknown
     * @param id        the version of the asset index
     * @param totalSize the total size of the assets, or -1 if the total size is
     *                  unknown
     * @throws NullPointerException if {@code id == null}
     */
    public AssetIndexInfo(String url, String checksum, long size, String id, long totalSize) {
        super(url, checksum, size);
        this.id = Objects.requireNonNull(id);
        this.totalSize = totalSize;
    }

    /**
     * Gets the version of the asset index.
     *
     * @return the version of the asset index.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the total size of the assets, or -1 if the total size if unknown.
     * (the 'totalSize' element in the json)
     *
     * @return the total size of the assets, or -1 if the total size if unknown
     */
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AssetIndexInfo) {
            AssetIndexInfo another = (AssetIndexInfo) obj;
            return super.equals(obj)
                    && Objects.equals(id, another.id)
                    && totalSize == another.totalSize;
        }
        return false;
    }

}
