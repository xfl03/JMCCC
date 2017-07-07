package org.to2mbn.jmccc.version

data class Asset(
		val hash: String,
		val size: Int?
)

data class AssetIndex(
		val objects: Map<String, Asset>,
		val virtual: Boolean = false
)

class AssetIndexDownloadInfo(
		url: String,
		sha1: String?,
		size: Int?,
		val id: String,
		val totalSize: Long?
) : DownloadInfo(url, sha1, size)
