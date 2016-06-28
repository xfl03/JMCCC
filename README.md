# JMCCC
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) [![Build Status](https://travis-ci.org/to2mbn/JMCCC.svg?branch=master)](https://travis-ci.org/to2mbn/JMCCC)<br/>
A powerful open-source library for launching and downloading Minecraft.

## License
JMCCC is licensed under [the MIT license](https://to2mbn.github.io/jmccc/LICENSE.txt).

## Features
 * Launches all versions of Minecraft
 * Scalable authentication
   * Supports Yggdrasil/Offline, and can be extended
 * Downloads all versions of Minecraft
   * Supports Forge/Liteloader
     * Supports Liteloader snapshots
   * Customizable download source
   * Asynchronous task system
   * Supports BIO/NIO
     * Can work on top of [Apache HttpAsyncClient](http://hc.apache.org/httpcomponents-asyncclient-dev/) or JDK
   * Supports caching
     * Can work on top of Ehcache or javax.cache
     * Different strategies for different files
 * Mojang API supports
   * Game profiles lookup
   * Fetches/Uploads textures
   * Fetches account information
   * Fetches name history
   * Blocked servers checking

## Quick Start
### Dependencies
|Dependency                                |Description                      |
|------------------------------------------|---------------------------------|
|`org.to2mbn:jmccc`                        |Minecraft launching feature.     |
|`org.to2mbn:jmccc-yggdrasil-authenticator`|Yggdrasil authentication feature.|
|`org.to2mbn:jmccc-mcdownloader`           |Minecraft downloading feature.   |
|`org.to2mbn:jmccc-mojang-api`             |Mojang API client.               |

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

### Launching Minecraft
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
Launcher launcher = LauncherBuilder.buildDefault();
launcher.launch(new LaunchOption("1.10", YggdrasilAuthenticator.password("<email>", "<password>"), dir));
```
You can replace `YggdrasilAuthenticator.password("<email>", "<password>")` with `new OfflineAuthenticator("<username>")` if you want to use offline authentication.

### Downloading Minecraft
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
MinecraftDownloader downloader = MinecraftDownloaderBuilder.buildDefault();
downloader.downloadIncrementally(dir, "1.10", new CallbackAdapter<Version>() {
	
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
		return new CallbackAdapter<R>() {

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

You can pass a `null` callback if you don't want to monitor the whole task.
You can also return `null` in `taskStart()` if you don't want to monitor sub tasks.

Don't forget to shutdown the downloader when you are no longer going to use it.
```java
downloader.shutdown();
```

### Downloading Forge/Liteloader
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
ForgeDownloadProvider forgeProvider = new ForgeDownloadProvider();
LiteloaderDownloadProvider liteloaderProvider = new LiteloaderDownloadProvider();
MinecraftDownloader downloader = MinecraftDownloaderBuilder.create()
	.providerChain(DownloadProviderChain.create()
		.addProvider(forgeProvider)
		.addProvider(liteloaderProvider))
	.build();

downloader.downloadIncrementally(dir, "1.8-forge1.8-11.14.3.1514", new CallbackAdapter<Version>() {...});
downloader.downloadIncrementally(dir, "1.7.10-LiteLoader1.7.10", new CallbackAdapter<Version>() {...});
downloader.download(forgeProvider.forgeVersionList(), new CallbackAdapter<ForgeVersionList>() {...});
downloader.download(liteloaderProvider.liteloaderVersionList(), new CallbackAdapter<LiteloaderVersionList>() {...});
```

### FML options
JMCCC won't add fml options (such as `-Dfml.ignoreInvalidMinecraftCertificates=true` and `-Dfml.ignorePatchDiscrepancies=true`) to the command line automatically.
If you have problems launching forge, you may need to add these arguments manually.
These arguments are already defined in class `ExtraArgumentsTemplates`.
```java
option.extraJvmArguments().add(ExtraArgumentsTemplates.FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES);
option.extraJvmArguments().add(ExtraArgumentsTemplates.FML_IGNORE_PATCH_DISCREPANCISE);
```
