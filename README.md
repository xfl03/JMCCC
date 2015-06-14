# JMCCC
An open-source library for launching Minecraft (included mclaunch-util-lib by Kronos666).<br>
It can run Minecraft client with a few codes.

### Download
See [releases](https://github.com/Southern-InfinityStudio/JMCCC/tree/master/releases).

### Sample
```java
Jmccc jmccc = new Jmccc(new BaseOptions("/path/to/your/minecraft/client/.minecraft"));
LaunchOption option = new LaunchOption((Version) jmccc.getBaseOptions().getVersionsHandler().getVersions().toArray()[0],
    new OfflineAuthenticator("Player"));
option.setMaxMemory(1024); //optional
option.setMinMemory(512); //optional
option.setServerInfo(new ServerInfo("helloworld", 25565)); //optional
option.setWindowSize(new WindowSize(512, 1024)); //optional
jmccc.launchGame(option);
```

### Dependencies
* Google Gson 2.2.4 - https://code.google.com/p/google-gson/
* Lombok 1.14.4 - http://projectlombok.org/
* mclaunch-util-lib 0.1 https://github.com/Kronos666/mclaunch-util-lib/tree/master/release/

### Change Logs
##### 1.0.3
* Removed IGameListener, the function has a fatal bug. Fixing in progress.
* Forge lastest version support.
