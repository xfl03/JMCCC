package com.darkyoooooo.jmccc.version;

import java.util.List;

public interface IVersion {
	String getPath();
	boolean isValid();
	String getLaunchArgs();
	String getId();
	String getAssets();
	String getMainClass();
	String getJarId();
	List<Library> getLibraries();
	List<Native> getNatives();
}
