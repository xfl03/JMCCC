package net.kronos.mclaunch_util_lib.auth.model;

public class YggdrasilAgent
{
	private String name;
	
	private int version;
	
	public static YggdrasilAgent getMinecraftAgent()
	{
		YggdrasilAgent agent = new YggdrasilAgent();
		agent.setName("Minecraft");
		agent.setVersion(1);
		
		return agent;
	}
	
	//===================Darkyoooooo Edited=======================
	public void setName(String name) {
		this.name = name;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	//============================================================
}
