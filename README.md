# JMCCC
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) [![Build Status](https://travis-ci.org/to2mbn/JMCCC.svg?branch=master)](https://travis-ci.org/to2mbn/JMCCC)<br/>
An open-source lightweight library for launching and downloading Minecraft.

## Download
You can get the latest releases from [the maven central repository](https://search.maven.org/#search|ga|1|g%3A%22org.to2mbn%22).

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
        System.err.println("Exit code: " + code);
    }
});
```
In the example above, we use `/home/user/.minecraft` as the .minecraft directory, and launches Minecraft 1.8 with an offline
account `user`. And the logs output from game process will print to stdout and stderr. When the game process terminated,
this program will print `Exit code: <process exit code>` to stderr, and then the monitor threads terminates.

### Yggdrasil authentication
#### Login with password
```java
YggdrasilAuthenticator.password("<username>", "<password>")
```

#### Interactive login
```java
YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator() {

	Scanner scanner = new Scanner(System.in);

	@Override
	protected PasswordProvider tryPasswordLogin() throws AuthenticationException {
		return new PasswordProvider() {

			@Override
			public String getUsername() throws AuthenticationException {
				System.out.printf("login: ");
				return scanner.nextLine();
			}

			@Override
			public String getPassword() throws AuthenticationException {
				System.out.printf("password: ");
				return scanner.nextLine();
			}

			@Override
			public CharacterSelector getCharacterSelector() {
				return null;
			}
		};
	}
};
authenticator.auth();
System.out.println("Logged in!");
```
The console output:
```
login: <username>
password: <password>
Logged in!
```

When method `auth()` is called, YggdrasilAuthenticator validates the current token. If the current token is not available, YggdrasilAuthenticator will try refreshing the token. When YggdrasilAuthenticator failed to refresh, it will call method `tryPasswordLogin()` to ask the password for authentication. If no password is available, YggdrasilAuthenticator will throw a `AuthenticationException`. The default implementation of `tryPasswordLogin()` returns `null`, you may need to override it.

If you want to update the current token manually, you ought to call `refresh()`, `refreshWithToken(String)` or `refreshWithPassword(String, String)`.
If you want to save the authentication, you ought to call `getCurrentSession()` to get the current authentication and serialize it, and call `setCurrentSession(Session)` to load the authentication.

### Game & Asset Download
> jmccc-mcdownloader can work on top of [Apache HttpAsyncClient](http://hc.apache.org/httpcomponents-asyncclient-dev/) or JDK. Note that the JDK implementation uses BIO, so you should limit your max connections, because each connetion will start a thread. If you want to use Apache HttpAsyncClient, just put it in the classpath. This is an optional dependency in the POM.

##### Minecraft downloading
The following code snippet downloads minecraft 1.8.8:
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
MinecraftDownloader downloader=MinecraftDownloaderBuilder.create().build();
downloader.downloadIncrementally(dir, "1.8.8", new CombinedDownloadCallback<Version>() {
	
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
downloader.fetchRemoteVersionList(new CombinedDownloadCallback<RemoteVersionList>() {...});
```

##### Forge and LiteLoader supports
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
ForgeDownloadProvider forgeProvider = new ForgeDownloadProvider();
LiteloaderDownloadProvider liteloaderProvider = new LiteloaderDownloadProvider();
MinecraftDownloader downloader = MinecraftDownloaderBuilder.create().appendProvider(forgeProvider).appendProvider(liteloaderProvider).build();

downloader.downloadIncrementally(dir, "1.8-forge1.8-11.14.3.1514", new CombinedDownloadCallback<Version>() {...});
downloader.downloadIncrementally(dir, "1.7.10-LiteLoader1.7.10", new CombinedDownloadCallback<Version>() {...});
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
These arguments are already defined in class `ExtraArgumentsTemplates`.
```java
option.setExtraJvmArguments(Arrays.asList(ExtraArgumentsTemplates.FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES, ExtraArgumentsTemplates.FML_IGNORE_PATCH_DISCREPANCISE));
```

### Change Logs
See [wiki](https://github.com/to2mbn/JMCCC/wiki/Change-logs).

### Contributing
Contributing is gooooood. But please read the following requirements first before your PR.
* Use tabs.
* No trailing whitespaces.
* No \r\n line endings, \n only.
