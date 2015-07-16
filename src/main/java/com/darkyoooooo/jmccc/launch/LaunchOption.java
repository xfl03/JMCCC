package com.darkyoooooo.jmccc.launch;

import com.darkyoooooo.jmccc.auth.IAuthenticator;
import com.darkyoooooo.jmccc.version.Version;

public class LaunchOption {
	private int maxMemory, minMemory;
	private Version version;
	private IAuthenticator authenticator;
	private ServerInfo serverInfo;
	private WindowSize windowSize;
	
	public LaunchOption(Version version, IAuthenticator authenticator) {
		this.version = version;
		this.authenticator = authenticator;
	}

	public int getMaxMemory() {
		return this.maxMemory;
	}

	public void setMaxMemory(int maxMemory) {
		this.maxMemory = maxMemory;
	}

	public int getMinMemory() {
		return this.minMemory;
	}

	public void setMinMemory(int minMemory) {
		this.minMemory = minMemory;
	}

	public ServerInfo getServerInfo() {
		return this.serverInfo;
	}

	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}

	public WindowSize getWindowSize() {
		return this.windowSize;
	}

	public void setWindowSize(WindowSize windowSize) {
		this.windowSize = windowSize;
	}

	public Version getVersion() {
		return this.version;
	}

	public IAuthenticator getAuthenticator() {
		return this.authenticator;
	}
}
