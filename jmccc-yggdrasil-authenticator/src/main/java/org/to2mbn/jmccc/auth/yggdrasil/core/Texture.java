package org.to2mbn.jmccc.auth.yggdrasil.core;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class Texture implements Serializable {

	private static final long serialVersionUID = 1L;

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

	@Override
	public String toString() {
		return "Texture [url=" + url + ", metadata=" + metadata + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(url, metadata);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Texture) {
			Texture another = (Texture) obj;
			return url.equals(another.url) && Objects.equals(metadata, another.metadata);
		}
		return false;
	}

}
