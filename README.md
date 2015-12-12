# jmccc-mcdownloader
Minecraft Downloader Library for JMCCC

### Download
You can get the latest maven release from [here](https://search.maven.org/#search|ga|1|g%3A%22com.github.to2mbn%22%20a%3A%22jmccc-mcdownloader%22).

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

### Compile
```
mvn clean package
```

### Examples
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
