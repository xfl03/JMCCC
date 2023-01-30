package org.to2mbn.jmccc.mcdownloader.provider.forge;

public class DefaultForgeDownloadSource implements ForgeDownloadSource {

	@Override
	public String getForgeVersionListUrl() {
		//Official json has changed, use BMCL API instead
		//https://files.minecraftforge.net/net/minecraftforge/forge/maven-metadata.json
		//https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json
		return "https://bmclapi2.bangbang93.com/maven/net/minecraftforge/forge/json";
	}

	@Override
	public String getForgeMavenRepositoryUrl() {
		return "https://files.minecraftforge.net/maven/";
	}

}
