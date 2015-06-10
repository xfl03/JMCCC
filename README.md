# JMCCC
An open-source library for launching Minecraft (included mclaunch-util-lib by Kronos666).<br>
It can run Minecraft client with a few codes.

### Download
See [releases](https://github.com/Southern-InfinityStudio/JMCCC/releases).

### Sample
```java
Jmccc jmccc = new Jmccc(new BaseOptions("C:\\MyMinecraftClient\\.minecraft", "C:\\java"), new IGameListener() {
	@Override
	public void onLog(String log) { System.out.println("Game prints:" + log); }
	@Override
	public void onExit(int exitCode) { System.out.println("Game exited with code " + exitCode); }
});
jmccc.launchGame(new LaunchOption(1024, 512, (IVersion) jmccc.getOptions().getVersionsHandler().getVersions().toArray()[0],
	new OfflineAuthenticator("Player"), new ServerInfo("www.google.com", 25565), new WindowSize(512, 1024)));
```

### Dependencies
* Google Gson 2.2.4 - https://code.google.com/p/google-gson/
* Lombok 1.14.4 - http://projectlombok.org/
* mclaunch-util-lib 0.1 https://github.com/Kronos666/mclaunch-util-lib/tree/master/release/
