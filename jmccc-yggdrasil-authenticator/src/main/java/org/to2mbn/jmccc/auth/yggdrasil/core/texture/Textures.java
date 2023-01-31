package org.to2mbn.jmccc.auth.yggdrasil.core.texture;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public final class Textures {

    private Textures() {
    }

    public static Texture createTexture(String url, Map<String, String> metadata) throws MalformedURLException {
        return createTexture(new URL(url), metadata);
    }

    public static Texture createTexture(URL url, Map<String, String> metadata) {
        return new URLTexture(url, metadata);
    }

    public static Texture createTexture(RenderedImage image, Map<String, String> metadata) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", byteOut);
        return new ByteArrayTexture(byteOut.toByteArray(), metadata);
    }

    public static Texture createTexture(byte[] image, Map<String, String> metadata) {
        return new ByteArrayTexture(image, metadata);
    }

}
