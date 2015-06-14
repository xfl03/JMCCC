package com.darkyoooooo.jmccc.launch;

import lombok.Getter;
import lombok.Setter;

import com.darkyoooooo.jmccc.auth.IAuthenticator;
import com.darkyoooooo.jmccc.version.Version;

public class LaunchOption {
	@Getter @Setter private int maxMemory, minMemory;
	@Getter private Version version;
	@Getter private IAuthenticator authenticator;
	@Getter @Setter private ServerInfo serverInfo;
	@Getter @Setter private WindowSize windowSize;
	
	public LaunchOption(Version version, IAuthenticator authenticator) {
		this.version = version;
		this.authenticator = authenticator;
	}
}
