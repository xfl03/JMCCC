package org.to2mbn.jmccc.launch.platform

import org.to2mbn.jmccc.version.PlatformRule
import org.to2mbn.jmccc.version.RuleAction.ALLOW
import org.to2mbn.jmccc.version.RuleAction.DISALLOW

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

fun PlatformRule.isApplicable(platform: PlatformDescription): Boolean = os?.let { rule ->
	(rule.identity?.let { it == platform.platformIdentity } ?: true) &&
			(rule.arch?.let { Regex(it) matches platform.arch } ?: true) &&
			(rule.version?.let {
				platform.version?.let { platformVersion -> Regex(it) matches platformVersion } ?: false
			} ?: true)
} ?: true

fun List<PlatformRule>?.isApplicable(platform: PlatformDescription) =
		this?.let {
			var action = DISALLOW
			for (rule in it)
				if (rule.isApplicable(platform))
					action = rule.action
			return action == ALLOW
		} ?: true
