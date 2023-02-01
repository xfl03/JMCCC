# JMCCC
[![Maven Central](https://img.shields.io/maven-central/v/dev.3-3/jmccc)](https://central.sonatype.com/search?q=jmccc&namespace=dev.3-3)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

一个用来下载并启动Minecraft的强大的开源类库。

## 许可证
JMCCC使用[MIT许可证](https://github.com/xfl03/JMCCC/LICENSE)。

## 功能
 * 启动各个版本的Minecraft
 * 可拓展的登录系统
   * 默认支持Microsoft账户/Mojang账户/离线，可以自行扩展
 * 下载各个版本的Minecraft
   * 支持Forge/Liteloader/Fabric/Quilt下载
     * 支持Liteloader的快照版本
     * 支持在Minecraft快照版本使用Fabric/Quilt
   * 可自定义下载源
   * 异步的任务系统
   * 支持阻塞式IO(BIO)和非阻塞式IO(NIO)
     * 可以使用[Apache HttpAsyncClient](http://hc.apache.org/httpcomponents-asyncclient-dev/)或JDK作为底层
   * 支持缓存
     * 可以使用Ehcache或者javax.cache作为底层
     * 对于不同文件可以指定不同缓存策略
 * 支持Mojang API
   * 查询玩家信息
   * 获取/上传皮肤（及披风、Elytra）
   * 查询账号信息
   * 查询改名历史
   * 检查服务器是否被封禁（Mojang封禁不遵守EULA的服务器）

## 快速开始
### 依赖库
| 依赖                                      | 说明                |
|-----------------------------------------|-------------------|
| `dev.3-3:jmccc`                         | 提供启动Minecraft的功能  |
| `dev.3-3:jmccc-mcdownloader`            | 提供下载Minecraft的功能  |
| `dev.3-3:jmccc-microsoft-authenticator` | 提供Microsoft账户验证功能 |
| `dev.3-3:jmccc-mojang-api`              | Mojang API客户端     |
| `dev.3-3:jmccc-yggdrasil-authenticator` | 提供Mojang账户验证功能    |

JMCCC的**正式版本**（release） 已上传至 **Maven Central**:
```
https://repo1.maven.org/maven2/
```

快照版本（snapshot）的Maven仓库:
```
https://s01.oss.sonatype.org/content/repositories/snapshots/
```

### 启动Minecraft
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
Launcher launcher = LauncherBuilder.buildDefault();
launcher.launch(new LaunchOption("1.19.3", MicrosoftAuthenticator.login(it -> System.out.println(it.message)), dir));
```
除了使用Microsoft账户`MicrosoftAuthenticator.login(it -> System.out.println(it.message))`以外，还支持Mojang账户`YggdrasilAuthenticator.password("<email>", "<password>")`，也支持离线模式`OfflineAuthenticator.name("<username>")`。

### 下载Minecraft
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
MinecraftDownloader downloader = MinecraftDownloaderBuilder.buildDefault();
downloader.downloadIncrementally(dir, "1.19.3", new CallbackAdapter<Version>() {
	
	@Override
	public void failed(Throwable e) {
		// 任务失败时
	}
	
	@Override
	public void done(Version result) {
		// 任务成功完成时
	}
	
	@Override
	public void cancelled() {
		// 任务取消时
	}
	
	@Override
	public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
		// 派生出一个子任务时
		// 在这里返回一个Callback便可以监视该子任务的状态
		return new CallbackAdapter<R>() {

			@Override
			public void done(R result) {
				// 子任务完成时
			}

			@Override
			public void failed(Throwable e) {
				// 子任务失败时
			}

			@Override
			public void cancelled() {
				// 子任务取消时
			}

			@Override
			public void updateProgress(long done, long total) {
				// 子任务的下载进度更新时
			}

			@Override
			public void retry(Throwable e, int current, int max) {
				// 子任务下载出错，并且将要重试时
				// 注：下载出错不代表任务失败，只有多次重试都出错后才算失败
			}
		};
	}
});
```

如果说不需要跟踪任务进度，那么传入一个`null` callback即可。
如果说不需要跟踪子任务的进度，那么在`taskStart()`里返回`null`即可。

当不再使用downloader时，需要关闭它：
```java
downloader.shutdown();
```

### 下载Forge/Liteloader/Fabric/Quilt
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
        ForgeDownloadProvider forgeProvider = new ForgeDownloadProvider();
        LiteloaderDownloadProvider liteloaderProvider = new LiteloaderDownloadProvider();
        MinecraftDownloader downloader = MinecraftDownloaderBuilder.create()
        .providerChain(DownloadProviderChain.create()
        .addProvider(forgeProvider)
        .addProvider(liteloaderProvider))
        .build();

        downloader.downloadIncrementally(dir, "1.19.3-forge-44.1.7", new CallbackAdapter<Version>() {...});
        downloader.downloadIncrementally(dir, "1.12.2-LiteLoader1.12.2", new CallbackAdapter<Version>() {...});
        downloader.downloadIncrementally(dir, "fabric-loader-0.14.13-1.19.3", new CallbackAdapter<Version>() {...});
        downloader.downloadIncrementally(dir, "quilt-loader-0.17.11-1.19.3", new CallbackAdapter<Version>() {...});
        downloader.download(forgeProvider.forgeVersionList(), new CallbackAdapter<ForgeVersionList>() {...});
        downloader.download(liteloaderProvider.liteloaderVersionList(), new CallbackAdapter<LiteloaderVersionList>() {...});
```

### FML参数
JMCCC不像其它一些启动器，JMCCC不会自动添加类似于`-Dfml.ignoreInvalidMinecraftCertificates=true`或`-Dfml.ignorePatchDiscrepancies=true`之类的FML参数。
所以可能会无法启动一些Forge版本，您可能需要手动添加这些参数。
这些参数都已经在`ExtraArgumentsTemplates`中被预先定义好了，您只需要引用一下即可。
```java
option.extraJvmArguments().add(ExtraArgumentsTemplates.FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES);
option.extraJvmArguments().add(ExtraArgumentsTemplates.FML_IGNORE_PATCH_DISCREPANCISE);
```

