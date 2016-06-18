package org.to2mbn.jmccc.mojangapi;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import org.to2mbn.jmccc.auth.yggdrasil.core.Texture;

public class StreamTexture extends Texture {

	private static final long serialVersionUID = 1L;

	private InputStream in;

	public StreamTexture(InputStream in, Map<String, String> metadata) {
		super(null, metadata);
		this.in = Objects.requireNonNull(in);
	}

	public InputStream getInputStream() {
		return in;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), in);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StreamTexture && super.equals(obj)) {
			StreamTexture another = (StreamTexture) obj;
			return Objects.equals(in, another.in);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("StreamTexture [in=%s, metadata=%s]", in, getMetadata());
	}
}
