package org.to2mbn.jmccc.version

data class SnapshotVersion(
		val timestamp: String,
		val buildNumber: Int
) {

	override fun toString() = "$timestamp-$buildNumber"

}
