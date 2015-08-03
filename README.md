![](http://i1.tietuku.com/e86de030295d85ac.png)<br>
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)<br>
An open-source library for launching Minecraft (uses `mclaunch-util-lib` by Kronos666).<br>
It can run Minecraft client with a few codes.

### Download
See [releases](https://github.com/Southern-InfinityStudio/JMCCC/tree/master/releases).

### Dependencies
* gson 2.2.4 https://code.google.com/p/google-gson/
* (Included) mclaunch-util-lib 0.1 https://github.com/Kronos666/mclaunch-util-lib/tree/master/release/

### Samples
##### Create Jmccc Instance:
```java
Jmccc jmccc = new Jmccc(new BaseOptions("/path/to/your/minecraft/client/.minecraft", "/path/to/you/java/path"));
Jmccc jmccc = new Jmccc(new BaseOptions("/path/to/your/minecraft/client/.minecraft"));
Jmccc jmccc = new Jmccc(); // equals: Jmccc jmccc = new Jmccc(new BaseOptions());
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
##### Create LaunchArgument Instance
```java
LaunchArgument arg = jmccc.generateLaunchArgs(option);
```
##### Get Missing Libraries & Natives
```java
//Remember to generate LaunchArgs before getting the missing libraries and natives.
for(Library lib : Jmccc.MISSING_LIBRARIES) {
    System.out.println("Missing Library: " + lib.getName());
}
for(Native nat : Jmccc.MISSING_NATIVES) {
    System.out.println("Missing Native: " + nat.getName());
}
```
##### Launch Game
```java
jmccc.launchGame(arg);
```

### Compiling
Windows:
```
gradlew clean build
```
Linux:
```
./gradlew clean build
```

### Change Logs
##### 1.3
* Bugs fixing.
* `Jmccc.VERSION` -> `Reporter.version`

##### 1.2
* Bugs fixing.

##### 1.1
* Bugs fixing.
* Added method `VersionsHandler.getVersionById(String id)`.

##### 1.0.6
* Removed Lombok dependency.
* Bugs fixing.
* `IGameListener` is still WIP.
