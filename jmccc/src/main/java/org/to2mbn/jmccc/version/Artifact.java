package org.to2mbn.jmccc.version;

import java.io.Serializable;
import java.util.Objects;

public class Artifact implements Serializable {

    private static final long serialVersionUID = 1L;
    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;
    private String type = "jar";
    public Artifact(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null, "jar");
    }

    public Artifact(String groupId, String artifactId, String version, String classifier, String type) {
        this.groupId = Objects.requireNonNull(groupId);
        this.artifactId = Objects.requireNonNull(artifactId);
        this.version = Objects.requireNonNull(version);
        this.classifier = classifier;
        this.type = Objects.requireNonNull(type);
    }

    public static String getArtifactBasePath(String groupId, String artifactId, String version) {
        return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/";
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getType() {
        return type;
    }

    public boolean isSnapshotArtifact() {
        return version.endsWith("-SNAPSHOT");
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

    private String getPath0(String version0) {
        return getArtifactBasePath(groupId, artifactId, version) + artifactId + "-" + version0 + (classifier == null ? "" : "-" + classifier) + "." + type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Artifact) {
            Artifact another = (Artifact) obj;
            return Objects.equals(groupId, another.groupId)
                    && Objects.equals(artifactId, another.artifactId)
                    && Objects.equals(version, another.version)
                    && Objects.equals(classifier, another.classifier)
                    && Objects.equals(type, another.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, classifier, type);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + (classifier == null ? "" : ":" + classifier);
    }

}
