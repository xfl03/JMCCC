package org.to2mbn.jmccc.auth.yggdrasil.core.texture;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum SkinModel {
    STEVE(""), ALEX("slim");

    public static final String METADATA_KEY_MODEL = "model";
    private String modelName;
    private Map<String, String> metadata;

    SkinModel(String modelName) {
        this.modelName = modelName;

        Map<String, String> modifiableMetadata = new HashMap<>();
        modifiableMetadata.put(METADATA_KEY_MODEL, modelName);
        metadata = Collections.unmodifiableMap(modifiableMetadata);
    }

    public static SkinModel inferModel(Texture texture) {
        if (texture == null) {
            return STEVE;
        } else {
            return inferModel(texture.getMetadata());
        }
    }

    public static SkinModel inferModel(Map<String, String> metadata) {
        if (metadata != null && ALEX.modelName.equals(metadata.get(METADATA_KEY_MODEL))) {
            return ALEX;
        } else {
            return STEVE;
        }
    }

    public String modelName() {
        return modelName;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

}
