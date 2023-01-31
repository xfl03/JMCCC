package org.to2mbn.jmccc.auth.yggdrasil.core;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PropertiesGameProfile extends GameProfile {

    private static final long serialVersionUID = 1L;

    private Map<String, String> properties;

    public PropertiesGameProfile(UUID uuid, String name, Map<String, String> properties) {
        super(uuid, name);
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertiesGameProfile && super.equals(obj)) {
            PropertiesGameProfile another = (PropertiesGameProfile) obj;
            return Objects.equals(properties, another.properties);
        }
        return false;
    }

    @Override
    public String toString() {
        return "GameProfile [uuid=" + getUUID() + ", name=" + getName() + ", properties=" + getProperties() + "]";
    }

}
