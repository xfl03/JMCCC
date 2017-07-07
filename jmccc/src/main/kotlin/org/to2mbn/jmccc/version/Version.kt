package org.to2mbn.jmccc.version

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

class Version(
		val id: String,
		val inheritsFrom: String?,
		val minecraftArguments: String?,
		val mainClass: String,
		val jar: String?,
		val libraries: List<Library>?,
		val minimumLauncherVersion: Int?,
		val incompatibilityReason: String?,
		val assets: String?,
		val compatibilityRules: List<PlatformRule>?,
		val downloads: Map<String, DownloadInfo>?,
		@JsonProperty("assetIndex")
		val assetIndexInfo: AssetIndexDownloadInfo?,
		val time: LocalDateTime?,
		val releaseTime: LocalDateTime?,
		val type: String?,
		val logging: Map<String, LoggingConfig>?
)
