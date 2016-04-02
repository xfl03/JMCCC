package org.to2mbn.jmccc.mcdownloader.util;

import java.net.URI;
import java.net.URISyntaxException;

public final class URIUtils {

	public static URI toURI(String str) {
		try {
			return new URI(str);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private URIUtils() {
	}
}
