package com.darkyoooooo.jmccc.launch;

import lombok.Getter;

import com.darkyoooooo.jmccc.auth.IAuthenticator;
import com.darkyoooooo.jmccc.version.IVersion;

public class LaunchOption {
	@Getter private final int maxMemory, minMemory;
	@Getter private final IVersion version;
	@Getter private final IAuthenticator authenticator;
	@Getter private final ServerInfo serverInfo;
	@Getter private final WindowSize windowSize;
	
	public LaunchOption(int maxMemory, int minMemory, IVersion version, IAuthenticator authenticator,
			ServerInfo serverInfo, WindowSize windowSize) {
		this.maxMemory = maxMemory;
		this.minMemory = minMemory;
		this.version = version;
		this.authenticator = authenticator;
		this.serverInfo = serverInfo;
		this.windowSize = windowSize;
	}
}
