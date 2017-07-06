package org.to2mbn.jmccc.launch

open class LaunchException : Exception {
	constructor() : super()
	constructor(message: String?) : super(message)
	constructor(cause: Throwable?) : super(cause)
	constructor(message: String?, cause: Throwable?) : super(message, cause)
}
