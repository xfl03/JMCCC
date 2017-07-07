package org.to2mbn.jmccc.version

import com.fasterxml.jackson.annotation.JsonProperty

class Library(
		@JsonProperty("name")
		val coordinate: GAV,
		val rules: List<PlatformRule>?,
		@JsonProperty("extract")
		val extractExclude: ExtractExclude?,
		val natives: Map<String, String>?,
		val url: String?,
		val downloads: LibraryDownloads?,

		val checksums: List<String>?,
		val serverreq: Boolean?,
		val clientreq: Boolean?
)

class LibraryDownloads(
		val classifiers: Map<String, LibraryDownloadInfo>?,
		val artifact: LibraryDownloadInfo?
)

class LibraryDownloadInfo(
		url: String,
		sha1: String?,
		size: Int?,
		val path: String?
) : DownloadInfo(url, sha1, size)

data class ExtractExclude(
		@JsonProperty("exclude")
		val paths: Set<String>
)
