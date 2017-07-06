package org.to2mbn.jmccc.launch

import org.to2mbn.jmccc.launch.Platform.LINUX
import org.to2mbn.jmccc.launch.Platform.OSX
import org.to2mbn.jmccc.launch.Platform.UNKNOWN
import org.to2mbn.jmccc.launch.Platform.WINDOWS
import java.lang.System.getProperty

enum class Platform(val identity: String?) {
	LINUX("linux"), WINDOWS("windows"), OSX("osx"), UNKNOWN(null)
}

val currentPlatform = getProperty("os.name")?.let(::inferPlatform) ?: UNKNOWN

fun inferPlatform(osName: String) = osName.toLowerCase().let {
	when {
		"linux" in it || "unix" in it -> LINUX
		"mac" in it -> OSX
		"windows" in it -> WINDOWS
		else -> UNKNOWN
	}
}

fun isX64() =
		getProperty("sun.arch.data.model")?.let { it == "64" }
				?: getProperty("os.arch")?.let { "64" in it }
				?: false
