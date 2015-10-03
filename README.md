# JMCCC
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)<br/>
An open-source lightweight library for launching Minecraft.

### Download
You can get the latest maven release from [here](https://search.maven.org/#search|ga|1|g%3A%22com.github.to2mbn%22%20a%3A%22jmccc%22).

The snapshot repository:
```xml
<repository>
	<id>ossrh</id>
	<url>https://oss.sonatype.org/content/groups/public/</url>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
</repository>
```
Or see [Jenkins](http://ci.infinity-studio.org/job/JMCCC/).


### Dependencies
* org.json

### Compile
```
mvn clean install
```

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
    public void onErrorLog(String log) {
        System.err.println(log);
    }

    @Override
    public void onExit(int code) {
        System.err.println("***EXIT " + code + "***");
    }
});
```
In this example, we use `/home/user/.minecraft` as the .minecraft directory, and launches Minecraft 1.8 with an offline
account `user`. And the logs from game process will be printed to stdout and stderr. When the game process terminates, 
this program will print `***EXIT <the exit code>***` to stderr, and then the monitor threads terminate.

See JavaDoc for more usages.

### Yggdrasil Auth
If you are looking for Yggdrasil auth, please see [jmccc-jyal-authenticator](https://github.com/to2mbn/jmccc-jyal-authenticator).
We splitted yggdrasil auth into another repo since 2.1.

### Forge
JMCCC won't add fml options (such as `-Dfml.ignoreInvalidMinecraftCertificates=true` and `-Dfml.ignorePatchDiscrepancies=true`) to the command line automatically.
If you have problems launching forge, you may need to add these arguments manually.
These arguments are already defined in `ExtraArgumentsTempletes`.
```java
option.setExtraArguments(Arrays.asList(ExtraArgumentsTempletes.FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES, ExtraArgumentsTempletes.FML_IGNORE_PATCH_DISCREPANCISE));
```

### Change Logs
See [wiki](https://github.com/to2mbn/JMCCC/wiki/Change-logs).
