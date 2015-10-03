package com.github.to2mbn.jyal;

import java.util.Map;
import java.util.Objects;

public class Texture {

	private String url;
	private Map<String, String> metadata;

	public Texture(String url, Map<String, String> metadata) {
		Objects.requireNonNull(url);
		this.url = url;
		this.metadata = metadata;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

}
