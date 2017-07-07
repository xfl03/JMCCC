package org.to2mbn.jmccc.version

import com.fasterxml.jackson.annotation.JsonProperty

enum class RuleAction {
	@JsonProperty("allow")
	ALLOW,
	@JsonProperty("disallow")
	DISALLOW
}

data class PlatformCondition(
		@JsonProperty("name")
		val identity: String?,
		val arch: String?,
		val version: String?
)

data class PlatformRule(
		val action: RuleAction,
		val os: PlatformCondition?
)
