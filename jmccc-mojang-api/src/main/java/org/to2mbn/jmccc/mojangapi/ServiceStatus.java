package org.to2mbn.jmccc.mojangapi;

/**
 * Describes the status of a Mojang service.
 * 
 * @author yushijinhun
 */
public enum ServiceStatus {

	/**
	 * Service available.
	 */
	GREEN,

	/**
	 * Something wrong with the service.
	 */
	YELLOW,

	/**
	 * Service unavailable.
	 */
	RED;

}
