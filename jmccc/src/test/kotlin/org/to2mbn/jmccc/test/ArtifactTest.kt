package org.to2mbn.jmccc.test

import org.junit.Test
import org.to2mbn.jmccc.version.Artifact
import org.to2mbn.jmccc.version.SnapshotVersion
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArtifactTest {

	@Test
	fun test_isSnapshot() = assertTrue(
			Artifact(groupId = "group", artifactId = "artifact", version = "1.0-SNAPSHOT").isSnapshot())

	@Test
	fun test_not_isSnapshot() = assertFalse(
			Artifact(groupId = "group", artifactId = "artifact", version = "1.0").isSnapshot())

	@Test
	fun test_toString_without_classifier() = assertEquals(
			"group:artifact:1.0:jar",
			Artifact(groupId = "group", artifactId = "artifact", version = "1.0").toString())

	@Test
	fun test_toString_with_classifier() = assertEquals(
			"group:artifact:1.0:jar:classifier",
			Artifact(groupId = "group", artifactId = "artifact", version = "1.0", classifier = "classifier").toString())

	@Test(expected = IllegalArgumentException::class)
	fun test_resolveSnapshot_failed() {
		Artifact(groupId = "group", artifactId = "artifact", version = "1.0", classifier = "classifier")
				.resolveSnapshot(SnapshotVersion(timestamp = "20170101", buildNumber = 1))
	}

	@Test
	fun test_resolveSnapshot() =
			SnapshotVersion(timestamp = "20170101", buildNumber = 1).let { snapshot ->
				with(Artifact(groupId = "group", artifactId = "artifact", version = "1.0-SNAPSHOT").resolveSnapshot(snapshot)) {
					assertTrue(isSnapshotResolved())
					assertEquals(snapshot, getSnapshotVersion())
					assertEquals("1.0-20170101-1", getResolvedVersion())
				}
			}

	@Test
	fun test_getSnapshotVersion_non_snapshot_version() =
			with(Artifact(groupId = "group", artifactId = "artifact", version = "1.0")) {
				assertFalse(isSnapshotResolved())
				assertNull(getSnapshotVersion())
				assertEquals("1.0", getResolvedVersion())
			}

	@Test
	fun test_getSnapshotVersion_non_snapshot_version_with_metadata() =
			with(Artifact(groupId = "group", artifactId = "artifact", version = "1.0",
					metadata = mapOf(Artifact.META_SNAPSHOT to SnapshotVersion(timestamp = "20170101", buildNumber = 1)))) {
				assertFalse(isSnapshotResolved())
				assertNull(getSnapshotVersion())
				assertEquals("1.0", getResolvedVersion())
			}

	@Test
	fun test_getSnapshotVersion_unresolved_snapshot_version() =
			with(Artifact(groupId = "group", artifactId = "artifact", version = "1.0-SNAPSHOT")) {
				assertFalse(isSnapshotResolved())
				assertNull(getSnapshotVersion())
				assertEquals("1.0-SNAPSHOT", getResolvedVersion())
			}
}
