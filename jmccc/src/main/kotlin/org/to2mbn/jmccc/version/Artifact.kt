package org.to2mbn.jmccc.version

import com.fasterxml.jackson.annotation.JsonIgnore

data class Artifact(
		val groupId: String,
		val artifactId: String,
		val version: String,
		val type: String = "jar",
		val classifier: String? = null,
		val metadata: Map<String, Any> = emptyMap()
) {

	companion object {
		const val RELEASE_VERSION = "RELEASE"
		const val LATEST_VERSION = "LATEST"
		const val SNAPSHOT_VERSION = "SNAPSHOT"

		const val META_SNAPSHOT = "maven.snapshotVersion";
	}

	override fun toString() = "$groupId:$artifactId:$version:$type${if (classifier == null) "" else ":$classifier"}"

	@JsonIgnore
	fun isSnapshot() = version.endsWith(SNAPSHOT_VERSION)

	@JsonIgnore
	fun isSnapshotResolved() = isSnapshot() && metadata.contains(META_SNAPSHOT)

	@JsonIgnore
	fun getSnapshotVersion() = if (isSnapshot()) metadata.get(META_SNAPSHOT) as? SnapshotVersion else null

	@JsonIgnore
	fun getResolvedVersion() = getSnapshotVersion()?.let { version.removeSuffix(SNAPSHOT_VERSION) + it } ?: version

	fun resolveSnapshot(snapshot: SnapshotVersion) =
			if (isSnapshot()) Artifact(groupId, artifactId, version, type, classifier, metadata.plus(Pair(META_SNAPSHOT, snapshot)))
			else throw IllegalArgumentException("Not a snapshot version")

}
