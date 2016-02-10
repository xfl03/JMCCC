# JMCCC
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)<br/>
An open-source lightweight library for launching and downloading Minecraft.

## Download
You can get the latest release from [the maven central repository](https://search.maven.org/#search|ga|1|g%3A%22com.github.to2mbn%22).

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

|ArtifactId                   |Description                                    |
|-----------------------------|-----------------------------------------------|
|jmccc                        |Provides classes for launching minecraft.      |
|jmccc-yggdrasil-authenticator|Provides the yggdrasil authentication feature. |
|jmccc-mcdownloader           |Provides the download feature.                 |

## Compile
```
mvn clean install
```

## Examples

### Minecraft launching
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
Launcher launcher = Jmccc.getLauncher();
launcher.launch(new LaunchOption(Versions.resolveVersion(dir, "1.8"), new OfflineAuthenticator("user"), dir), new GameProcessListener() {

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

### Yggdrasil authentication
For password login:
```java
new YggdrasilPasswordAuthenticator("<email>", "<password>");
```
<p/>

For token login:
```java
new YggdrasilTokenAuthenticator(<clientToken>, "<accessToken>");
```
<p/>

`YggdrasilTokenAuthenticator` is serializable. If you want to save the authentication (aka 'remember password'),
just save the YggdrasilTokenAuthenticator object.
We recommend you to use YggdrasilTokenAuthenticator because YggdrasilTokenAuthenticator only saves the access token.
It's much safer.

You should call `YggdrasilTokenAuthenticator.isValid()` first to check if the access token is valid.
If this method returns false, you should ask the user to login with password again.

```java
File passwordFile = new File("passwd.dat");
YggdrasilTokenAuthenticator authenticator = null;
if (passwordFile.exists()) {
	// read the stored token from file
	try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(passwordFile))) {
		authenticator = (YggdrasilTokenAuthenticator) in.readObject();
	}
}

if (authenticator == null || !authenticator.isValid()) {
	// no token is stored, or the stored token is invalid
	// ...... - ask user to login with password
	authenticator = YggdrasilTokenAuthenticator.loginWithToken("<email>", "<password>");
}

// ...... - use the authenticator (such as launching minecraft)

// store the token
try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(passwordFile))) {
	out.writeObject(authenticator);
}
```

### Game & Asset Download
##### Minecraft downloading
The following code snippet downloads minecraft 1.8.8:
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
MinecraftDownloader downloader=MinecraftDownloaderBuilder.create().build();
downloader.downloadIncrementally(dir, "1.8.8", new MultipleDownloadCallback<Version>() {
	
	@Override
	public void failed(Throwable e) {
		// when the task fails
	}
	
	@Override
	public void done(Version result) {
		// when the task finishes
	}
	
	@Override
	public void cancelled() {
		// when the task cancels
	}
	
	@Override
	public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
		// when a new sub download task starts
		// return a DownloadCallback to listen the status of the task
		return new DownloadCallback<R>() {

			@Override
			public void done(R result) {
				// when the sub download task finishes
			}

			@Override
			public void failed(Throwable e) {
				// when the sub download task fails
			}

			@Override
			public void cancelled() {
				// when the sub download task cancels
			}

			@Override
			public void updateProgress(long done, long total) {
				// when the progress of the sub download task has updated
			}

			@Override
			public void retry(Throwable e, int current, int max) {
				// when the sub download task fails, and the downloader decides to retry the task
				// in this case, failed() won't be called
			}
		};
	}
});
```
`MinecraftDownloader.downloadIncrementally()` will find out the missing libraries, broken assets, etc, and download them.

##### Minecraft version list downloading
```java
downloader.fetchRemoteVersionList(new MultipleDownloadCallback<RemoteVersionList>() {...});
```

##### Forge and LiteLoader supports
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
ForgeDownloadProvider forgeProvider = new ForgeDownloadProvider();
LiteloaderDownloadProvider liteloaderProvider = new LiteloaderDownloadProvider();
MinecraftDownloader downloader = MinecraftDownloaderBuilder.create().appendProvider(forgeProvider).appendProvider(liteloaderProvider).build();

downloader.downloadIncrementally(dir, "1.8-forge1.8-11.14.3.1514", new MultipleDownloadCallback<Version>() {...});
downloader.downloadIncrementally(dir, "1.7.10-LiteLoader1.7.10", new MultipleDownloadCallback<Version>() {...});
downloader.download(forgeProvider.forgeVersionList(), new DownloadCallback<ForgeVersionList>() {...});
downloader.download(liteloaderProvider.liteloaderVersionList(), new DownloadCallback<LiteloaderVersionList>() {...});
```

##### Customized download provider
```java
MinecraftDownloader downloader = MinecraftDownloaderBuilder.create().setProvider(new CustomizedDownloadProvider()).build();
```
If you use a customized download provier, setProvider() must be called before appendProvider().


Finally, don't forget to shutdown the downloader.
```java
downloader.shutdown();
```

### Forge
JMCCC won't add fml options (such as `-Dfml.ignoreInvalidMinecraftCertificates=true` and `-Dfml.ignorePatchDiscrepancies=true`) to the command line automatically.
If you have problems launching forge, you may need to add these arguments manually.
These arguments are already defined in `ExtraArgumentsTemplates`.
```java
option.setExtraArguments(Arrays.asList(ExtraArgumentsTemplates.FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES, ExtraArgumentsTemplates.FML_IGNORE_PATCH_DISCREPANCISE));
```

### Change Logs
See [wiki](https://github.com/to2mbn/JMCCC/wiki/Change-logs).

### Contributing
Contributing is awesome. But please read the following code requirements first before you PR.
* Use tabs.
* No trailing whitespaces.
* No \r\n line endings, \n only.
