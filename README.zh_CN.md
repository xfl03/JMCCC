# JMCCC
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) [![Build Status](https://travis-ci.org/to2mbn/JMCCC.svg?branch=master)](https://travis-ci.org/to2mbn/JMCCC)<br/>
一个用来下载并启动Minecraft的轻量级开源类库。

如果您使用Maven/Gradle，您可以直接将jmccc作为一个依赖添加（可以在[Maven中心仓库](https://search.maven.org/#search|ga|1|g%3A%22org.to2mbn%22)找到）。

开发版本（snapshots）可以从Sonatype Nexus下载：
```xml
<repository>
	<id>ossrh</id>
	<url>https://oss.sonatype.org/content/groups/public/</url>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
</repository>
```

|ArtifactId                   |用途                           |
|-----------------------------|-------------------------------|
|jmccc                        |启动Minecraft                  |
|jmccc-yggdrasil-authenticator|正版（Yggdrasil）验证的相关支持 |
|jmccc-mcdownloader           |下载Minecraft                  |

## 编译
```
mvn clean package
```

## License
JMCCC is licensed under [the MIT license](https://to2mbn.github.io/jmccc/LICENSE.txt).

## 例子

### 启动Minecraft
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
Launcher launcher = LauncherBuilder.buildDefault();
launcher.launch(new LaunchOption("1.9", new OfflineAuthenticator("user"), dir), new GameProcessListener() {

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
上面的例子中，我们启动了Minecraft 1.9。其中将`/home/user/.minecraft`作为.minecraft目录，使用了`user`这个离线账户，并且游戏进程的日志将被打印到控制台。当游戏进程结束的时候，会向控制台打印`Exit code: <退出码>`，接着用来监视游戏进程的线程会自动结束。

### 正版登录
#### 用密码登录
```java
YggdrasilAuthenticator.password("<username>", "<password>")
```

#### 交互式登录
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
控制台输出是这样的：
```
login: <username>
password: <password>
Logged in!
```

调用`auth()`方法时，YggdrasilAuthenticator先检查当前的token是否可用，假如不可用则尝试刷新一下token。假如刷新失败，则调用`tryPasswordLogin()`方法来询问密码，尝试使用密码来登录（您可能需要重写这个方法）。如果密码不可用，则会抛出一个`AuthenticationException`。`tryPasswordLogin()`的默认实现返回`null`。

调用`refresh()`，`refreshWithToken(String, String)`，`refreshWithPassword(String, String)`方法可以手动刷新token。
调用`getCurrentSession()`可以获取当前的token。您可以把它保存下来，等到下一次使用时调用`setCurrentSession(Session)`来加载token。（可以用来实现记住密码一类的功能）

### 下载
> jmccc-mcdownloader既可以使用[Apache HttpAsyncClient](http://hc.apache.org/httpcomponents-asyncclient-dev/)作为下载的底层实现，也可以直接使用JDK作为底层实现。需要注意的是基于JDK的实现使用的是阻塞式IO，所以在打开大量链接时会占用很多资源（因为一个链接就需要一个线程）。Apache HttpAsyncClient使用的是非阻塞式IO，所以没有这个问题。如果您想使用Apache HttpAsyncClient，则只需要将相关的依赖加入到classpath即可。

##### 下载Minecraft
下面的代码演示了如何下载Minecraft 1.9。
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
MinecraftDownloader downloader=MinecraftDownloaderBuilder.create().build();
downloader.downloadIncrementally(dir, "1.9", new CallbackAdapter<Version>() {
	
	@Override
	public void failed(Throwable e) {
		// 当下载失败时调用此方法
	}
	
	@Override
	public void done(Version result) {
		// 当下载成功时调用此方法
	}
	
	@Override
	public void cancelled() {
		// 当下载被取消时调用此方法
	}
	
	@Override
	public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
		// 当派生出一个新的子任务时调用此方法
		// 可以返回一个DownloadCallback对象来监视该子任务的进度
		return new CallbackAdapter<R>() {

			@Override
			public void done(R result) {
				// 当子任务成功时调用此方法
			}

			@Override
			public void failed(Throwable e) {
				// 当子任务失败时调用此方法
			}

			@Override
			public void cancelled() {
				// 当子任务被取消时调用此方法
			}

			@Override
			public void updateProgress(long done, long total) {
				// 当子任务的下载进度发生变化时调用此方法
			}

			@Override
			public void retry(Throwable e, int current, int max) {
				// 当子任务出错，但下载器决定重试该任务时调用
				// 这种情况下，failed()方法是不会被调用的
			}
		};
	}
});
```
`MinecraftDownloader.downloadIncrementally()`将会找出缺失或损坏的文件，并自动下载。

##### 获取版本列表
```java
downloader.fetchRemoteVersionList(new CombinedDownloadCallback<RemoteVersionList>() {...});
```

##### 下载Forge/Liteloader
```java
MinecraftDirectory dir = new MinecraftDirectory("/home/user/.minecraft");
ForgeDownloadProvider forgeProvider = new ForgeDownloadProvider();
LiteloaderDownloadProvider liteloaderProvider = new LiteloaderDownloadProvider();
MinecraftDownloader downloader = MinecraftDownloaderBuilder.create().appendProvider(forgeProvider).appendProvider(liteloaderProvider).build();

downloader.downloadIncrementally(dir, "1.8-forge1.8-11.14.3.1514", new CallbackAdapter<Version>() {...});
downloader.downloadIncrementally(dir, "1.7.10-LiteLoader1.7.10", new CallbackAdapter<Version>() {...});
downloader.download(forgeProvider.forgeVersionList(), new CallbackAdapter<ForgeVersionList>() {...});
downloader.download(liteloaderProvider.liteloaderVersionList(), new CallbackAdapter<LiteloaderVersionList>() {...});
```

##### 自定义下载源
```java
MinecraftDownloader downloader = MinecraftDownloaderBuilder.create().setBaseProvider(new CustomizedDownloadProvider()).build();
```
注：CustomizedDownloadProvider代表您自己的下载源。

最后不要忘记关闭下载器。
```java
downloader.shutdown();
```

### 关于Forge
jmccc不像其它一些启动器，jmccc不会自动添加类似于`-Dfml.ignoreInvalidMinecraftCertificates=true`或`-Dfml.ignorePatchDiscrepancies=true`之类的FML选项。
所以可能会无法启动一些Forge版本，您可能需要手动添加这些选项。
这些参数都已经在`ExtraArgumentsTemplates`中被预先定义好了，您只需要引用一下即可。
```java
option.setExtraJvmArguments(Arrays.asList(ExtraArgumentsTemplates.FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES, ExtraArgumentsTemplates.FML_IGNORE_PATCH_DISCREPANCISE));
```

### 更新日志
见[wiki](https://github.com/to2mbn/JMCCC/wiki/Change-logs).

### Contributing
Contributing is good. But please read the following requirements first before you PR.
* Use tabs.
* No trailing whitespaces.
* No \r\n line endings, \n only.

