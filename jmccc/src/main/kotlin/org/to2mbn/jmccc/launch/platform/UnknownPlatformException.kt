package org.to2mbn.jmccc.launch.platform

import org.to2mbn.jmccc.launch.LaunchException

open class UnknownPlatformException : LaunchException {
	constructor() : super()
	constructor(message: String?) : super(message)
}
