package com.github.to2mbn.jyal;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GameProfile {

	private UUID uuid;
	private String name;
	private boolean legacy;
	private Map<String, String> properties;

	public GameProfile(UUID uuid, String name, boolean legacy, Map<String, String> properties) {
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(name);
		Objects.requireNonNull(properties);
		this.uuid = uuid;
		this.name = name;
		this.legacy = legacy;
		this.properties = properties;
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public boolean isLegacy() {
		return legacy;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		return "GameProfile [uuid=" + uuid + ", name=" + name + ", legacy=" + legacy + ", properties=" + properties + "]";
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (legacy ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof GameProfile) {
			GameProfile another = (GameProfile) obj;
			return legacy == another.legacy && uuid.equals(another.uuid) && name.equals(another.name) && properties.equals(another.properties);
		}

		return false;
	}

}
