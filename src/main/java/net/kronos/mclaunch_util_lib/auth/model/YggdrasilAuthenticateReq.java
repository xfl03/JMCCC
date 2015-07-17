package net.kronos.mclaunch_util_lib.auth.model;

public class YggdrasilAuthenticateReq
{
	private YggdrasilAgent agent;
	
	private String username;
	
	private String password;
	
	private String clientToken;
	
	//===================Darkyoooooo Edited=======================
	public void setAgent(YggdrasilAgent agent) {
		this.agent = agent;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}
	//============================================================
}
