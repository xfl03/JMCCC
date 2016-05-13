package org.to2mbn.jmccc.mcdownloader.provider;

public class MojangDownloadProvider extends DefaultLayoutProvider {

	@Override
	protected String getLibraryBaseURL() {
		return "https://libraries.minecraft.net/";
	}

	@Override
	protected String getVersionBaseURL() {
		return "http://s3.amazonaws.com/Minecraft.Download/versions/";
	}

	@Override
	protected String getAssetIndexBaseURL() {
		return "http://s3.amazonaws.com/Minecraft.Download/indexes/";
	}

	@Override
	protected String getVersionListURL() {
		return "https://launchermeta.mojang.com/mc/game/version_manifest.json";
	}

	@Override
	protected String getAssetBaseURL() {
		return "http://resources.download.minecraft.net/";
	}

}
