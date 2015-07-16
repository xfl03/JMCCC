![](http://i1.tietuku.com/e86de030295d85ac.png)<br>
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)<br>
An open-source library for launching Minecraft (uses `mclaunch-util-lib` by Kronos666).<br>
It can run Minecraft client with a few codes.

### Download
See [releases](https://github.com/Southern-InfinityStudio/JMCCC/tree/master/releases).

### Dependencies
* gson 2.2.4 https://code.google.com/p/google-gson/
* mclaunch-util-lib 0.1 https://github.com/Kronos666/mclaunch-util-lib/tree/master/release/

### Samples
##### Create Jmccc Instance:
```java
Jmccc jmccc = new Jmccc(new BaseOptions("/path/to/your/minecraft/client/.minecraft", "/path/to/you/java/path"));
Jmccc jmccc = new Jmccc(new BaseOptions("/path/to/your/minecraft/client/.minecraft"));
Jmccc jmccc = new Jmccc();
```
##### Find Versions
```java
Version versionToLaunch = jmccc.getVersionsHandler().getVersionById("1.8");
```
##### Create Authenticator Instance
```java
IAuthenticator authenticator = new OfflineAuthenticator("your_name");
IAuthenticator authenticator = new YggdrasilAuthenticator("your@e.mail", "your_password");
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
##### 1.1
* Bugs fixing.
* Added method `VersionsHandler.getVersionById(String id)`.

##### 1.0.6
* Removed Lombok dependency.
* Bugs fixing.
* `IGameListener` is still WIP.

##### 1.0.5
* Java9 support.
* Bugs fixing.

##### 1.0.4
* Added method `Jmccc.getLaunchTime()` to get the time for launching game (ms).
* Added method `LaunchResult.getExceptionInstance()` to get the instance of exception if it exists.
* Added method `VersionsHandler.getUnvalidVersions()` to get the versions which are invalid.
* Changed `BaseOptions.getVersionsHandler()` to `Jmccc.getVersionsHandler()`.
* Changed `Jmccc.DEFAULT_ADV_ARGS` to `Jmccc.ADV_ARGS`.
* Changed the name of class `OSNames` to `OsTypes`.
* Removed class `FilePathResolver`, the methods have been moved into class `Utils`.

##### 1.0.3
* Removed class `IGameListener`, the function has a fatal bug. Fixing in progress.
* Forge lastest version support.
