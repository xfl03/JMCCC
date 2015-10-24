package com.github.to2mbn.jmccc.mcdownloader.provider;

public class MojangDownloadProvider extends DefaultLayoutProvider {

	@Override
	protected String getLibraryBaseURL() {
		return "https://libraries.minecraft.net/";
	}

	@Override
	protected String getVersionBaseURL() {
		return "https://s3.amazonaws.com/Minecraft.Download/versions/";
	}

	@Override
	protected String getAssetIndexBaseURL() {
		return "https://s3.amazonaws.com/Minecraft.Download/indexes/";
	}

	@Override
	protected String getVersionListURL() {
		return "https://s3.amazonaws.com/Minecraft.Download/indexes/";
	}

	@Override
	protected String getAssetBaseURL() {
		return "http://resources.download.minecraft.net/";
	}

}
