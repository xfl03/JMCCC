package org.to2mbn.jmccc.mcdownloader.provider.forge;

public class DefaultForgeDownloadSource implements ForgeDownloadSource {

	@Override
	public String getForgeVersionListUrl() {
		return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
	}

	@Override
	public String getForgeMavenRepositoryUrl() {
		return "http://files.minecraftforge.net/maven/";
	}

}
