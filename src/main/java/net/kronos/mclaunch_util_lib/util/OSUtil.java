package net.kronos.mclaunch_util_lib.util;

import java.io.File;

public class OSUtil
{
	enum OperatingSystem
	{
		windows,
		macos,
		linux,
		unknown
	}
	
	public static OperatingSystem getOS()
	{
		String osName = System.getProperty("os.name").toLowerCase();
		
		if(osName.contains("win"))
			return OperatingSystem.windows;
		else if(osName.contains("mac"))
			return OperatingSystem.macos;
		else if(osName.contains("linux"))
			return OperatingSystem.linux;
		else
			return OperatingSystem.unknown;
			
	}
	
	public static File getLocalStorage(String dir)
	{
		String userHome = System.getProperty("user.home");
		
		if(getOS() == OperatingSystem.windows)
			return new File(System.getenv("appdata"), "." + dir);
		else if(getOS() == OperatingSystem.macos)
			return new File(userHome, "Library/Application Support/" + dir);
		else
			return new File(userHome, dir);
	}
	
	public static File getMCLocalStorage()
	{
		return getLocalStorage("minecraft");
	}
}
