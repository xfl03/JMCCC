package org.to2mbn.jmccc.version;

import java.util.Objects;

public class LibraryInfo extends DownloadInfo {

    private static final long serialVersionUID = 1L;

    private String path;

    /**
     * Constructor of LibraryInfo.
     *
     * @param url      the download url, or null if the url is unknown
     * @param checksum the SHA-1 checksum, or null if the checksum is unknown
     * @param size     the file size, or -1 if the size is unknown
     * @param path     the path of the library, or null if the path is unknown
     */
    public LibraryInfo(String url, String checksum, long size, String path) {
        super(url, checksum, size);
        this.path = path;
    }

    /**
     * Gets the path of the library, or null if the path is unknown.
     *
     * @return the path of the library, or null if the path is unknown
     */
    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof LibraryInfo) {
            LibraryInfo another = (LibraryInfo) obj;
            return super.equals(obj)
                    && Objects.equals(path, another.path);
        }
        return false;
    }

}
