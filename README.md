# JMCCC
An open-source library for launching Minecraft (uses `mclaunch-util-lib` by Kronos666).

It can run Minecraft client with a few codes.

### Download
See [releases](https://github.com/Southern-InfinityStudio/JMCCC/tree/master/releases).

### Dependencies
* Google Gson 2.2.4 https://code.google.com/p/google-gson/
* Lombok 1.14.4 http://projectlombok.org/
* mclaunch-util-lib 0.1 https://github.com/Kronos666/mclaunch-util-lib/tree/master/release/

### Samples
##### Create Jmccc Instance:
```java
Jmccc jmccc = new Jmccc(new BaseOptions("/path/to/your/minecraft/client/.minecraft", "/path/to/you/java/path"));
Jmccc jmccc = new Jmccc(new BaseOptions("/path/to/your/minecraft/client/.minecraft"));
Jmccc jmccc = new Jmccc(new BaseOptions());
```
##### Find Versions
```java
Version versionToLaunch = null;
for (Version version : jmccc.getVersionsHandler().getVersions()) {
    System.out.println("-->" + version.getId());
    if (version.getId().equals("1.8")) versionToLaunch = version;
}
```
##### Create Authenticator Instance
```java
IAuthenticator authenticator = new OfflineAuthenticator("your_name");
IAuthenticator authenticator = new YggdrasilAuthenticator("your@e.mail", "yourpassword", true /*enableTwitch*/);
```
##### Create LaunchOption Instance
```java
LaunchOption option = new LaunchOption(versionToLaunch, authenticator);
option.setMaxMemory(1024); //optional
option.setMinMemory(512); //optional
option.setServerInfo(new ServerInfo("helloworld", 25565)); //optional
option.setWindowSize(new WindowSize(512, 1024)); //optional
```
##### Launch Game
```java
jmccc.launchGame(option);
```

### Change Logs
##### 1.0.4
* Added function `Jmccc.getLaunchTime()` to get the time for launching game (ms).
* Added function `LaunchResult.getExceptionInstance()` to get the instance of exception if it exists.
* Added function `VersionsHandler.getUnvalidVersions()` to get the versions which are invalid.
* Changed `BaseOptions.getVersionsHandler()` to `Jmccc.getVersionsHandler()`.
* Changed `Jmccc.DEFAULT_ADV_ARGS` to `Jmccc.ADV_ARGS`.
* Changed the name of class `OSNames` to `OsTypes`.
* Removed class `FilePathResolver`, the functions moved into class `Utils`.

##### 1.0.3
* Removed class `IGameListener`, the function has a fatal bug. Fixing in progress.
* Forge lastest version support.
