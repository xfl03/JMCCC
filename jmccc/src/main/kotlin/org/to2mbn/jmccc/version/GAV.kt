package org.to2mbn.jmccc.version

data class GAV(
		val groupId: String,
		val artifactId: String,
		val version: String
) {
	override fun toString() = "$groupId:$artifactId:$version"
}

fun GAV(gav: String) = gav.split(delimiters = ":", limit = 3).let { GAV(it[0], it[1], it[2]) }
