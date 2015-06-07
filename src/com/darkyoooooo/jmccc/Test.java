package com.darkyoooooo.jmccc;

import com.darkyoooooo.jmccc.Jmccc.BaseOptions;
import com.darkyoooooo.jmccc.auth.OfflineAuthenticator;
import com.darkyoooooo.jmccc.launch.LaunchOption;
import com.darkyoooooo.jmccc.launch.ServerInfo;
import com.darkyoooooo.jmccc.launch.WindowSize;
import com.darkyoooooo.jmccc.process.IGameListener;
import com.darkyoooooo.jmccc.version.IVersion;

public class Test {
	public static void main(String[] args) {
		Jmccc jmccc = new Jmccc(new BaseOptions("C:/MyMinecraftClient/.minecraft", "C:/java"), new IGameListener() {
			@Override
			public void onLog(String log) { System.out.println("Game prints:" + log); }
			@Override
			public void onExit(int exitCode) { System.out.println("Game exited with code " + exitCode); }
		});
		jmccc.launchGame(new LaunchOption(1024, 512, (IVersion) jmccc.getOptions().getVersionHandler().getVersions().toArray()[0],
			new OfflineAuthenticator("Player"), new ServerInfo("www.google.com", 25565), new WindowSize(512, 1024)));
	}
}
