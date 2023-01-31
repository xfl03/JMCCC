package org.to2mbn.jmccc.version;

import org.to2mbn.jmccc.option.MinecraftDirectory;

import java.util.Objects;

public class Library extends Artifact {

    private static final long serialVersionUID = 1L;

    // Minecraft customized fields
    private LibraryInfo downloadInfo;
    private String customizedUrl;
    private String[] checksums;

    public Library(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null, "jar");
    }

    public Library(String groupId, String artifactId, String version, String classifier, String type) {
        this(groupId, artifactId, version, classifier, type, null);
    }

    public Library(String groupId, String artifactId, String version, String classifier, String type, LibraryInfo downloadInfo) {
        this(groupId, artifactId, version, classifier, type, downloadInfo, null, null);
    }

    public Library(String groupId, String artifactId, String version, String classifier, String type, LibraryInfo downloadInfo, String customizedUrl, String[] checksums) {
        super(groupId, artifactId, version, classifier, type);
        this.downloadInfo = downloadInfo;
        this.customizedUrl = customizedUrl;
        this.checksums = checksums;
    }

    public LibraryInfo getDownloadInfo() {
        return downloadInfo;
    }

    public String getCustomizedUrl() {
        return customizedUrl;
    }

    public String[] getChecksums() {
        return checksums;
    }

    /**
     * Checks if the library is missing in the given minecraft directory.
     *
     * @param minecraftDir the minecraft directory to check
     * @return true if the library is missing
     */
    public boolean isMissing(MinecraftDirectory minecraftDir) {
        return !minecraftDir.getLibrary(this).isFile();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Library) {
            Library another = (Library) obj;
            return super.equals(obj)
                    && Objects.equals(downloadInfo, another.downloadInfo)
                    && Objects.equals(customizedUrl, another.customizedUrl)
                    && Objects.deepEquals(checksums, another.checksums);
        }
        return false;
    }

}
