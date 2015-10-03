package com.github.to2mbn.jyal;

import java.util.Objects;

public class PlayerTextures {

	private Texture skin;
	private Texture cape;

	public PlayerTextures(Texture skin, Texture cape) {
		this.skin = skin;
		this.cape = cape;
	}

	public Texture getSkin() {
		return skin;
	}

	public Texture getCape() {
		return cape;
	}

	@Override
	public String toString() {
		return "PlayerTextures [skin=" + skin + ", cape=" + cape + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(skin, cape);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof PlayerTextures) {
			PlayerTextures another = (PlayerTextures) obj;
			return Objects.equals(cape, another.cape) && Objects.equals(skin, another.skin);
		}
		return false;
	}

}
