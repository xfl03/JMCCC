package net.kronos.mclaunch_util_lib.profiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import net.kronos.mclaunch_util_lib.util.OSUtil;
import net.kronos.mclaunch_util_lib.util.Util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * @author Kronos
 */
public class LauncherProfiles
{
	/**
	 * Read and serialize the launcher_profiles.json file in the minecraft folder.
	 * This file contains profiles, settings and authentication values.
	 * 
	 * @return A LauncherProfiles object
	 * 
	 * @throws FileNotFoundException When the file can't be found
	 * @throws JsonSyntaxException When the file is not json-valid
	 * @throws IOException
	 */
	public static LauncherProfiles getLauncherProfiles() throws FileNotFoundException, JsonSyntaxException, IOException
	{
		// Json file
		File launcherProfilesFile = new File(OSUtil.getMCLocalStorage(), "launcher_profiles.json");
		
		// Serialized object
		LauncherProfiles launcherProfiles = new Gson().fromJson(Util.readFile(launcherProfilesFile), LauncherProfiles.class);
		
		// Post-serialization stuff
		for(Entry<String, Account> entry : launcherProfiles.getAuthenticationDatabase().entrySet())
			entry.getValue().setDemo(entry.getKey().startsWith("demo"));
		
		return launcherProfiles;
	}
	
	private HashMap<String, JsonObject> profiles;
	private String selectedProfile;
	
	private String clientToken;
	private HashMap<String, Account> authenticationDatabase;
	private String selectedUser;

	private LauncherVersion launcherVersion;
	
	/**
	 * @return All the profiles in the launcher
	 */
	public HashMap<String, JsonObject> getProfiles() {
		return profiles;
	}
	
	/**
	 * @return The last selected profile in the launcher
	 */
	public String getSelectedProfile() {
		return selectedProfile;
	}
	
	/**
	 * @return The client token of the launcher
	 */
	public String getClientToken() {
		return clientToken;
	}
	
	/**
	 * @return The authentication "database" (Key: UUID)
	 */
	public HashMap<String, Account> getAuthenticationDatabase() {
		return authenticationDatabase;
	}
	
	/**
	 * @return The UUID of the last selected user in the launcher
	 */
	public String getSelectedUser() {
		return selectedUser;
	}
	
	/**
	 * @return The launcher version
	 */
	public LauncherVersion getLauncherVersion() {
		return launcherVersion;
	}
}
