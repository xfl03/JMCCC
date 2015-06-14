package com.darkyoooooo.jmccc.util;

import java.util.ArrayList;
import java.util.List;

import com.darkyoooooo.jmccc.Jmccc;
import com.darkyoooooo.jmccc.version.Library;
import com.darkyoooooo.jmccc.version.Native;

public class FilePathResolver {
	public static List<String> resolveRealLibPaths(Jmccc jmccc, List<Library> list) {
		List<String> realPaths = new ArrayList<String>();
		for(Library lib : list) {
			realPaths.add(Utils.resolvePath(String.format("%s/libraries/%s/%s/%s/%s-%s.jar", jmccc.getBaseOptions().getGameRoot(),
				lib.getDomain().replace(".", "/"), lib.getName(), lib.getVersion(), lib.getName(), lib.getVersion())));
		}
		return realPaths;
	}
	
	public static List<String> resolveRealNativePaths(Jmccc jmccc, List<Native> list) {
		List<String> realPaths = new ArrayList<String>();
		for(Native nat : list) {
			if(!nat.isAllowed()) {
				continue;
			}
			realPaths.add(Utils.resolvePath(String.format("%s/libraries/%s/%s/%s/%s-%s-%s.jar", jmccc.getBaseOptions().getGameRoot(),
				nat.getDomain().replace(".", "/"), nat.getName(), nat.getVersion(), nat.getName(), nat.getVersion(),
				nat.getSuffix())));
		}
		return realPaths;
	}
}
