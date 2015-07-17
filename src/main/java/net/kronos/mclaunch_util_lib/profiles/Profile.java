package net.kronos.mclaunch_util_lib.profiles;

public class Profile
{
	private String name;
	private String gameDir;
	private String lastVersionId;
	
	private String javaDir;
	private String javaArgs;
	
	private Resolution resolution;
    private String[] allowedReleaseTypes;
    private boolean useHopperCrashService;
    private String launcherVisibilityOnGameClose;
    
    /**
     * @return The name of the profile
     */
	public String getName() {
		return name;
	}
	
	/**
	 * Note: can be null
	 * 
	 * @return The game root directory
	 */
	public String getGameDir() {
		return gameDir;
	}
	
	/**
	 * Note: can be null
	 * 
	 * @return The version id selected in this profile
	 */
	public String getLastVersionId() {
		return lastVersionId;
	}
	
	/**
	 * Note: can be null
	 * 
	 * @return The java directory
	 */
	public String getJavaDir() {
		return javaDir;
	}
	
	/**
	 * Note: can be null
	 * 
	 * @return The additional java args
	 */
	public String getJavaArgs() {
		return javaArgs;
	}
	
	/**
	 * Note: can be null
	 * 
	 * @return A {@link Resolution} object
	 */
	public Resolution getResolution() {
		return resolution;
	}
	
	/**
	 * Note: can be null
	 * 
	 * @return An array containing the allowed release types
	 */
	public String[] getAllowedReleaseTypes() {
		return allowedReleaseTypes;
	}
	
	/**
	 * @return A boolean authorizing the use of the "hopper crash service" (Mojang automatic bug report system)
	 */
	public boolean useHopperCrashService() {
		return useHopperCrashService;
	}
	
	/**
	 * Note: can be null
	 * 
	 * @return A string containing the action to do when the game closes
	 */
	public String getLauncherVisibilityOnGameClose() {
		return launcherVisibilityOnGameClose;
	}
}
