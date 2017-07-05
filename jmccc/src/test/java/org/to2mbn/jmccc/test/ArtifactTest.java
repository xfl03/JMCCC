package org.to2mbn.jmccc.test;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.to2mbn.jmccc.version.Artifact.META_SNAPSHOT;
import org.junit.Test;
import org.to2mbn.jmccc.version.Artifact;
import org.to2mbn.jmccc.version.SnapshotVersion;

public class ArtifactTest {

	@Test
	public void test_isSnapshot() {
		assertTrue(new Artifact("group", "artifact", "1.0-SNAPSHOT", "jar", null, emptyMap()).isSnapshot());
	}

	@Test
	public void test_not_isSnapshot() {
		assertFalse(new Artifact("group", "artifact", "1.0", "jar", null, emptyMap()).isSnapshot());
	}

	@Test
	public void test_toString_without_classifier() {
		assertEquals("group:artifact:1.0:jar", new Artifact("group", "artifact", "1.0", "jar", null, emptyMap()).toString());
	}

	@Test
	public void test_toString_with_classifier() {
		assertEquals("group:artifact:1.0:jar:classifier", new Artifact("group", "artifact", "1.0", "jar", "classifier", emptyMap()).toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_resolveSnapshot_failure() {
		new Artifact("group", "artifact", "1.0", "jar", "classifier", emptyMap()).resolveSnapshot(new SnapshotVersion("20170101", 1));
	}

	@Test
	public void test_resolveSnapshot() {
		SnapshotVersion snapshot = new SnapshotVersion("20170101", 1);
		Artifact resolved = new Artifact("group", "artifact", "1.0-SNAPSHOT", "jar", null, emptyMap()).resolveSnapshot(snapshot);
		assertTrue(resolved.isSnapshotResolved());
		assertEquals(snapshot, resolved.getSnapshotVersion());
		assertEquals("1.0-20170101-1", resolved.getResolvedVersion());
	}

	@Test
	public void test_getSnapshotVersion_non_snapshot_version() {
		Artifact artifact = new Artifact("group", "artifact", "1.0", "jar", null, emptyMap());
		assertFalse(artifact.isSnapshotResolved());
		assertNull(artifact.getSnapshotVersion());
		assertEquals("1.0", artifact.getResolvedVersion());
	}

	@Test
	public void test_getSnapshotVersion_non_snapshot_version_with_metadata() {
		SnapshotVersion snapshot = new SnapshotVersion("20170101", 1);
		Artifact artifact = new Artifact("group", "artifact", "1.0", "jar", null, singletonMap(META_SNAPSHOT, snapshot));
		assertFalse(artifact.isSnapshotResolved());
		assertNull(artifact.getSnapshotVersion());
		assertEquals("1.0", artifact.getResolvedVersion());
	}

	@Test
	public void test_getSnapshotVersion_unresolved_snapshot_version() {
		Artifact artifact = new Artifact("group", "artifact", "1.0-SNAPSHOT", "jar", null, emptyMap());
		assertFalse(artifact.isSnapshotResolved());
		assertNull(artifact.getSnapshotVersion());
		assertEquals("1.0-SNAPSHOT", artifact.getResolvedVersion());
	}

}
