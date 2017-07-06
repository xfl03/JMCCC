package org.to2mbn.jmccc.launch.platform

import org.to2mbn.jmccc.launch.platform.Platform.LINUX
import org.to2mbn.jmccc.launch.platform.Platform.OSX
import org.to2mbn.jmccc.launch.platform.Platform.WINDOWS
import java.lang.System.getProperty

enum class Platform(val identity: String) {
	LINUX("linux"), WINDOWS("windows"), OSX("osx")
}

val currentPlatform = inferPlatform(getProperty("os.name"))
val currentArch = inferArch(dataModel = getProperty("sun.arch.data.model"), arch = getProperty("os.arch"))
val currentOsVersion: String? = getProperty("os.version")

fun inferPlatform(osName: String?) = osName?.toLowerCase()?.let {
	when {
		"linux" in it -> LINUX
		"mac os x" in it -> OSX
		"windows" in it -> WINDOWS
		else -> null
	}
}

fun inferArch(dataModel: String?, arch: String?) =
		when (dataModel) {
			"32", "64" -> dataModel
			else -> arch?.let {
				when {
					"64" in it -> "64" // x86_64, amd64
					"86" in it -> "32" // x86, i386, i486, i586, i686
					else -> null // powerpc
				}
			}
		}
