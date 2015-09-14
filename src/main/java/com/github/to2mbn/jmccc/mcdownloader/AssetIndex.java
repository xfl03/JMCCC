package com.github.to2mbn.jmccc.mcdownloader;

import java.util.Set;

public class AssetIndex {

	private Set<Asset> assets;

	/**
	 * Creates an AssetIndex.
	 * 
	 * @param assets the assets
	 */
	public AssetIndex(Set<Asset> assets) {
		this.assets = assets;
	}

	/**
	 * Gets the assets.
	 * 
	 * @return the assets
	 */
	public Set<Asset> getAssets() {
		return assets;
	}

}
