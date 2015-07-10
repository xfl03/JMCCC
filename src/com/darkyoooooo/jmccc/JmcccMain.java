package com.darkyoooooo.jmccc;

import com.darkyoooooo.jmccc.Jmccc.BaseOptions;
import com.darkyoooooo.jmccc.auth.OfflineAuthenticator;
import com.darkyoooooo.jmccc.launch.LaunchOption;
import com.darkyoooooo.jmccc.version.Version;

public class JmcccMain {
	public static void main(String[] args) {
		Jmccc jmccc = new Jmccc(new BaseOptions("C:/Users/Administrator/Desktop/Private/1.8/.minecraft"));
		Object[] versions = jmccc.getVersionsHandler().getVersions().toArray();
		jmccc.launchGame(new LaunchOption((Version) versions[1], new OfflineAuthenticator("player")));
	}
}
