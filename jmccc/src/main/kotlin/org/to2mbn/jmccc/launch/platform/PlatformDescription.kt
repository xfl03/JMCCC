package org.to2mbn.jmccc.launch.platform

data class PlatformDescription(
		val platformIdentity: String,
		val arch: String,
		val version: String?
)

fun getCurrentPlatformDescription() = PlatformDescription(
		platformIdentity = currentPlatform?.identity ?: throw UnknownPlatformException("Unknown platform"),
		arch = currentArch ?: throw UnknownPlatformException("Unknown architecture"),
		version = currentOsVersion
)
