package org.to2mbn.jmccc.mcdownloader.provider.forge;

public class DefaultForgeDownloadSource implements ForgeDownloadSource {

	@Override
	public String forgeVersionList() {
		return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
	}

	@Override
	public String forgeMavenRepo() {
		return "http://files.minecraftforge.net/maven/";
	}

}
