package net.kronos.mclaunch_util_lib.profiles;

public class Account
{
    private String username;
    private String accessToken;
    private String userid;
    private String uuid;
    private String displayName;
    
    // This field isn't serialized from the file
    private boolean demo;

    public void setDemo(boolean demo) {
		this.demo = demo;
	}
    
    /**
     * @return True if this is a demo account, false otherwise
     */
    public boolean isDemo() {
		return demo;
	}
    
    /**
     * @return The username of the account (this can be a minecraft username or an email, you may want to use {@link #getDisplayName()} instead.)
     */
    public String getUsername() {
		return username;
	}

    /**
     * @return The last access token of this account
     */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @return The userID of this account
	 */
	public String getUserID() {
		return userid;
	}

	/**
	 * Note: This return the UUID in the form "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" but the keys of the hashmap that {@link LauncherProfiles#getAuthenticationDatabase()} returns are in the form "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" 
	 * 
	 * @return The UUID of this account, can be null with demo accounts
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * @return The display name (the one displayed ingame) of this account, can be null with demo accounts
	 */
	public String getDisplayName() {
		return displayName;
	}
}
