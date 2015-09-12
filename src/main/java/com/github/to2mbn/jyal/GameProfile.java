package com.github.to2mbn.jyal;

import java.util.Objects;
import java.util.UUID;

public class GameProfile {

	private UUID uuid;
	private String name;

	public GameProfile(UUID uuid, String name) {
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(name);
		this.uuid = uuid;
		this.name = name;
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "GameProfile [uuid=" + uuid + ", name=" + name + "]";
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			return uuid.equals(another.uuid) && name.equals(another.name);
		}

		return false;
	}

}
