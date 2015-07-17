package net.kronos.mclaunch_util_lib.auth.model;

import java.util.List;

public class YggdrasilAuthenticateRes
{
	private String accessToken;
	
	private String clientToken;
	
	private YggdrasilProfile selectedProfile;
	
	private List<YggdrasilProfile> availableProfiles;
	
	//===================Darkyoooooo Edited=======================
	public String getAccessToken() {
		return this.accessToken;
	}
	
	public String getClientToken() {
		return this.clientToken;
	}
	
	public YggdrasilProfile getSelectedProfile() {
		return this.selectedProfile;
	}
	
	public List<YggdrasilProfile> getAvailableProfiles() {
		return this.availableProfiles;
	}
	//============================================================
}