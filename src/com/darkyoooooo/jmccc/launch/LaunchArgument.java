package com.darkyoooooo.jmccc.launch;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import com.darkyoooooo.jmccc.Jmccc;
import com.darkyoooooo.jmccc.util.OsTypes;
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.Version;

public class LaunchArgument {
	private final Jmccc jmccc;
	@Getter private LaunchOption launchOption;
	@Getter private String argTemplet, mainClass, nativePath;
	@Getter private List<String> libraries, advArgs;
	@Getter private Map<String, String> tokens;
	@Getter private int maxMemory, minMemory;
	@Getter private boolean enableCGC;
	@Getter private ServerInfo serverInfo;
	@Getter private WindowSize windowSize;
	
	public LaunchArgument(Jmccc jmccc, LaunchOption launchOption, Map<String, String> tokens,
			List<String> advArgs, boolean enableCGC, List<String> libraries, String nativesPath) {
		this.jmccc = jmccc;
		this.launchOption = launchOption;
	    this.argTemplet = launchOption.getVersion().getLaunchArgs();
	    this.mainClass = launchOption.getVersion().getMainClass();
	    this.libraries = libraries;
	    this.maxMemory = launchOption.getMaxMemory();
	    this.minMemory = launchOption.getMinMemory();
	    this.enableCGC = enableCGC;
	    this.nativePath = nativesPath;
	    this.tokens = tokens;
	    this.serverInfo = launchOption.getServerInfo();
	    this.windowSize = launchOption.getWindowSize();
	    this.advArgs = advArgs;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		OsTypes os = OsTypes.CURRENT;
		if(os == OsTypes.WINDOWS) {
			buffer.append('\"').append(this.jmccc.getBaseOptions().getJavaPath()).append("\" ");
		} else {
			buffer.append("jar ");
		}
		
		buffer.append(this.enableCGC ? "-Xincgc " : " ");
		buffer.append("-Xms" + (this.minMemory > 0 ? this.minMemory + "M " : " "));
		buffer.append("-Xmx" + (this.maxMemory > 0 ? this.maxMemory + "M " : "1024M "));
		for(String adv : this.advArgs) {
			buffer.append(adv).append(' ');
		}
		buffer.append("-Djava.library.path=\"" + this.nativePath + "\" ");
		
		buffer.append("-cp \"");
		for(String lib : this.libraries) {
			buffer.append(lib).append(os.getPathSpearator());
		}
		Version ver = this.launchOption.getVersion();
		if(!ver.isInheritsForm()) {
			buffer.append(String.format("%s.jar%s\" ", Utils.resolvePath(ver.getPath() + "/" + ver.getId()), os.getPathSpearator()));
		} else {
			buffer.append(String.format("%s.jar%s\" ", Utils.resolvePath(ver.getInheritsPath() + "/" + ver.getInheritsFormName()), os.getPathSpearator()));
		}
		
		buffer.append(this.mainClass).append(' ');
		buffer.append(this.replaceLaunchArgs()).append(' ');
		
		if(this.serverInfo != null && this.serverInfo.getAddress() != null && !this.serverInfo.getAddress().equals("")) {
			buffer.append("--server ").append(this.serverInfo.getAddress()).append(' ');
			buffer.append("--port ").append(this.serverInfo.getPort() == 0 ? 25565 : this.serverInfo.getPort()).append(' ');
		}
		if(this.windowSize != null) {
			if(this.windowSize.isFullSize()) {
				buffer.append("--fullscreen").append(' ');
			}
			if(this.windowSize.getHeight() > 0) {
				buffer.append("--height " + this.windowSize.getHeight()).append(' ');
			}
			if(this.windowSize.getWidth() > 0) {
				buffer.append("--width " + this.windowSize.getWidth()).append(' ');
			}
		}
		return buffer.toString();
	}
	
	private String replaceLaunchArgs() {
		String arg = this.argTemplet;
		Iterator<String> left = this.tokens.keySet().iterator();
		Iterator<String> right = this.tokens.values().iterator();
		while(left.hasNext() && right.hasNext()) {
			arg = arg.replace("${" + left.next() + "}", right.next());
		}
		return arg;
	}
}
