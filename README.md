# JMCCC
![](http://i1.tietuku.com/e86de030295d85ac.png)<br/>
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)<br/>
An open-source lightweight library for launching Minecraft.<br/>
Thanks @Hookan for testing.

### Download
See [Jenkins](http://ci.infinity-studio.org/job/JMCCC/).<br/>
Or add this library as a maven dependency.<br/>
```xml
<dependency>
	<groupId>com.github.to2mbn</groupId>
	<artifactId>jmccc</artifactId>
	<version>2.1</version>
</dependency>
```

### Dependencies
* org.json

### Compile
Require Maven

	mvn clean install

### Examples
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
Launcher launcher = Jmccc.getLauncher();
launcher.launch(new LaunchOption(launcher.getVersion(dir, "1.8"), new OfflineAuthenticator("user"), dir), new GameProcessListener() {

	@Override
	public void onLog(String log) {
		System.out.println(log);
	}

	@Override
	public void onExit(int code) {
		System.err.println("***EXIT " + code + "***");
	}

	@Override
	public void onErrorLog(String log) {
		System.out.println(log);
	}
});
```
In this example, we use `/home/user/.minecraft` as the .minecraft directory, and launches Minecraft 1.8 with an offline
account `user`. And the logs from game process will be printed to stdout or stderr. When the game process terminates, 
this program will print `***EXIT <the exit code>***` to the console, and then the monitor threads will terminate.<br/>
See JavaDoc in the code for more usages.

### Yggdrasil Auth
If you are looking for Yggdrasil auth, please see https://github.com/to2mbn/jmccc-jyal-authenticator.
We moved Yggdrasil auth to another repo since 2.1.

### Forge
JMCCC won't add `-Dfml.ignoreInvalidMinecraftCertificates=true` and `-Dfml.ignorePatchDiscrepancies=true` to the command line  automatically.
If you have problems launching forge, you need to add these arguments manually.
These arguments are already defined in `ExtraArgumentsTempletes`.<br/>
```java
option.setExtraArguments(Arrays.asList(ExtraArgumentsTempletes.FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES, ExtraArgumentsTempletes.FML_IGNORE_PATCH_DISCREPANCISE));
```

### Change Logs
##### 2.1.1
* Add hash verifying for libraries
* Add toString(), hashCode(), equals() overrides
* Add null checks
* Fix using relative path in the constructor of `MinecraftDirectory` causes classpath error
* Fix compatible problems with java 7

##### 2.1
* Split Yggdrasil auth into repo `jyal` and `jmccc-jyal-authenticator`
* Use org.json
* Code refactor
* Bugs fix

##### 2.0
* Code refactor

##### 1.4
* Readd `IGameListener`
* Complete Linux/Osx Support
* Bugs fixing.

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

