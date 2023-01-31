package org.to2mbn.jmccc.version;

import org.to2mbn.jmccc.util.Arch;

import java.util.Objects;
import java.util.Set;

public class Native extends Library {

    private static final long serialVersionUID = 1L;

    private Set<String> extractExcludes;

    public Native(String groupId, String artifactId, String version, String classifier, String type, Set<String> extractExcludes) {
        super(groupId, artifactId, version, classifier, type);
        this.extractExcludes = extractExcludes;
    }

    public Native(String groupId, String artifactId, String version, String classifier, String type, LibraryInfo downloadInfo, Set<String> extractExcludes) {
        super(groupId, artifactId, version, classifier, type, downloadInfo);
        this.extractExcludes = extractExcludes;
    }

    public Native(String groupId, String artifactId, String version, String classifier, String type, LibraryInfo downloadInfo, String customizedUrl, String[] checksums, Set<String> extractExcludes) {
        super(groupId, artifactId, version, classifier, type, downloadInfo, customizedUrl, checksums);
        this.extractExcludes = extractExcludes;
    }

    // Getters
    // @formatter:off
    public Set<String> getExtractExcludes() {
        return extractExcludes;
    }
    // @formatter:on

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Native) {
            Native another = (Native) obj;
            return super.equals(obj)
                    && Objects.equals(extractExcludes, another.extractExcludes);
        }
        return false;
    }

    public Arch getArch() {
        String classifier = getClassifier();
        if (classifier == null) {
            return Arch.DEFAULT;
        }
        String[] strs = classifier.split("-");
        return Arch.inferArch(strs[strs.length - 1]);
    }
}
