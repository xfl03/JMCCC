package com.github.to2mbn.jmccc.mcdownloader;

import java.util.Set;

public class AssetsIndex {

	private Set<Asset> assets;

	/**
	 * Creates an AssetsIndex.
	 * 
	 * @param assets the assets
	 */
	public AssetsIndex(Set<Asset> assets) {
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
