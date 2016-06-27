package org.to2mbn.jmccc.auth.yggdrasil.core.texture;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum TextureModel {
	STEVE(""), ALEX("slim");

	public static final String METADATA_KEY_MODEL = "model";

	public static TextureModel inferModel(Texture texture) {
		if (texture == null) {
			return STEVE;
		} else {
			return inferModel(texture.getMetadata());
		}
	}

	public static TextureModel inferModel(Map<String, String> metadata) {
		if (metadata != null && ALEX.modelName.equals(metadata.get(METADATA_KEY_MODEL))) {
			return ALEX;
		} else {
			return STEVE;
		}
	}

	private String modelName;
	private Map<String, String> metadata;

	TextureModel(String modelName) {
		this.modelName = modelName;

		Map<String, String> modifiableMetadata = new HashMap<>();
		modifiableMetadata.put(METADATA_KEY_MODEL, modelName);
		metadata = Collections.unmodifiableMap(modifiableMetadata);
	}

	public String modelName() {
		return modelName;
	}

	public Map<String, String> metadata() {
		return metadata;
	}

}
