package org.to2mbn.jmccc.version

import com.fasterxml.jackson.annotation.JsonProperty

class LoggingConfig(
		val argument: String?,
		val type: String?,
		@JsonProperty("file")
		val configInfo: LoggingConfigDownloadInfo?
)

class LoggingConfigDownloadInfo(
		url: String,
		sha1: String?,
		size: Int?,
		val id: String
) : DownloadInfo(url, sha1, size)
