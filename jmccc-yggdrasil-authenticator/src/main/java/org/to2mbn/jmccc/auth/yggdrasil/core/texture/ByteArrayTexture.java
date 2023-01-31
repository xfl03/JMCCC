package org.to2mbn.jmccc.auth.yggdrasil.core.texture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class ByteArrayTexture implements Texture {

    private byte[] data;
    private Map<String, String> metadata;

    public ByteArrayTexture(byte[] data, Map<String, String> metadata) {
        this.data = Objects.requireNonNull(data);
        this.metadata = metadata;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public String toString() {
        return String.format("ByteArrayTexture [length=%d, metadata=%s]", data.length, metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ByteArrayTexture) {
            ByteArrayTexture another = (ByteArrayTexture) obj;
            return Objects.equals(metadata, another.metadata)
                    && Arrays.equals(data, another.data);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * data.length + Objects.hashCode(metadata);
    }

}
