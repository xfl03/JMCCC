package net.kronos.mclaunch_util_lib.auth.model;

public class YggdrasilRefreshRes
{
	private String accessToken;
	
	private String clientToken;
	
	private YggdrasilProfile selectedProfile;
	
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
	//============================================================
}
