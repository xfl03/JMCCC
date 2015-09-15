package com.github.to2mbn.jmccc.mcdownloader;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.json.JSONObject;

public class AssetsIndex {

	public static AssetsIndex fromJson(JSONObject json) {
		JSONObject objects = json.getJSONObject("objects");
		Set<Asset> assets = new HashSet<>();
		for (String virtualPath : objects.keySet()) {
			JSONObject object = objects.getJSONObject(virtualPath);
			String hash = object.getString("hash");
			int size = object.getInt("size");
			assets.add(new Asset(virtualPath, hash, size));
		}
		return new AssetsIndex(assets);
	}

	private Set<Asset> assets;

	/**
	 * Creates an AssetsIndex.
	 * 
	 * @param assets the assets
	 * @throws NullPointerException <code>assets==null</code>
	 */
	public AssetsIndex(Set<Asset> assets) {
		Objects.requireNonNull(assets);
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

	@Override
	public String toString() {
		return assets.toString();
	}

}
