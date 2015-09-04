# JMCCC 2.0
![](http://i1.tietuku.com/e86de030295d85ac.png)<br>
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Southern-InfinityStudio/JMCCC?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)<br>
An open-source lightweight library for launching Minecraft.<br>

### Download
See [jenkins](http://ci.infinity-studio.org/job/JMCCC/).

### Dependencies
* gson 2.2.4 https://code.google.com/p/google-gson/
* (Included) mclaunch-util-lib 0.1 https://github.com/Kronos666/mclaunch-util-lib/tree/master/release/

### Compile
Require Maven

	mvn clean install

### Examples

	File md = new File("/home/test/.minecraft");
	Launcher launcher = Jmccc.getLauncher("test");
	launcher.launch(new LaunchOption(launcher.getVersion(md, "1.8"), new OfflineAuthenticator("test"), new EnvironmentOption(md)), new IGameListener() {
	
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
			System.err.println(log);
		}
	});

In this example, we use `/home/test/.minecraft` as the .minecraft directory, and launches Minecraft 1.8 with an offine
account `test`. And the logs from game process will be printed to stdout or stderr. When the game process terminates, 
this program will print `***EXIT <the exit code>***` to the console, and then the monitor threads will terminate.<br/>
See JavaDoc in the code for more usages.

### Change Logs
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

