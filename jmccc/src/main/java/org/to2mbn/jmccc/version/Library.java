package org.to2mbn.jmccc.version;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import org.to2mbn.jmccc.option.MinecraftDirectory;

public class Library implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String getArtifactBasePath(String groupId, String artifactId, String version) {
		return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/";
	}

	// Maven standard fields
	private String groupId;
	private String artifactId;
	private String version;
	private String classifier;
	private String type = "jar";

	// Minecraft customized fields
	private LibraryInfo downloadInfo;
	private String customizedUrl;
	private String[] checksums;

	public Library(String groupId, String artifactId, String version) {
		this(groupId, artifactId, version, null, "jar", null);
	}

	public Library(String groupId, String artifactId, String version, String classifier, String type) {
		this(groupId, artifactId, version, classifier, type, null);
	}

	public Library(String groupId, String artifactId, String version, String classifier, String type, LibraryInfo downloadInfo) {
		this(groupId, artifactId, version, classifier, type, downloadInfo, null, null);
	}

	public Library(String groupId, String artifactId, String version, String classifier, String type, LibraryInfo downloadInfo, String customizedUrl, String[] checksums) {
		this.groupId = Objects.requireNonNull(groupId);
		this.artifactId = Objects.requireNonNull(artifactId);
		this.version = Objects.requireNonNull(version);
		this.classifier = classifier;
		this.type = Objects.requireNonNull(type);
		this.downloadInfo = downloadInfo;
		this.customizedUrl = customizedUrl;
		this.checksums = checksums;
	}

	// Getters
	// @formatter:off
	public String getGroupId() { return groupId; }
	public String getArtifactId() { return artifactId; }
	public String getVersion() { return version; }
	public String getClassifier() { return classifier; }
	public String getType() { return type; }
	public LibraryInfo getDownloadInfo() { return downloadInfo; }
	public String getCustomizedUrl() { return customizedUrl; }
	public String[] getChecksums() { return checksums; }
	// @formatter:on

	public boolean isSnapshotArtifact() {
		return version.endsWith("-SNAPSHOT");
	}

	/**
	 * Checks if the library is missing in the given minecraft directory.
	 * 
	 * @param minecraftDir the minecraft directory to check
	 * @return true if the library is missing in the given minecraft directory
	 */
	public boolean isMissing(MinecraftDirectory minecraftDir) {
		return !minecraftDir.getLibrary(this).isFile();
	}

	public String getPath() {
		return getPath0(version);
	}

	public String getPath(String snapshotPostfix) {
		if (!isSnapshotArtifact()) {
			throw new IllegalArgumentException("The artifact is not a snapshot.");
		}
		return getPath0(version.substring(0, version.length() - "SNAPSHOT".length()) + snapshotPostfix);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Library) {
			Library another = (Library) obj;
			return Objects.equals(groupId, another.groupId)
					&& Objects.equals(artifactId, another.artifactId)
					&& Objects.equals(version, another.version)
					&& Objects.equals(classifier, another.classifier)
					&& Objects.equals(type, another.type)
					&& Objects.equals(downloadInfo, another.downloadInfo)
					&& Objects.equals(customizedUrl, another.customizedUrl)
					&& Objects.deepEquals(checksums, another.checksums);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupId, artifactId, version, classifier, type, downloadInfo, customizedUrl, Arrays.hashCode(checksums));
	}

	@Override
	public String toString() {
		return groupId + ":" + artifactId + ":" + version + (classifier == null ? "" : ":" + classifier);
	}

	private String getPath0(String version0) {
		return getArtifactBasePath(groupId, artifactId, version) + artifactId + "-" + version0 + (classifier == null ? "" : "-" + classifier) + "." + type;
	}

}
