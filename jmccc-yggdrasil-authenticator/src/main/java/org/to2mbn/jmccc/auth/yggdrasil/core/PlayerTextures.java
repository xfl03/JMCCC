package org.to2mbn.jmccc.auth.yggdrasil.core;

import java.io.Serializable;
import java.util.Objects;

public class PlayerTextures implements Serializable {

	private static final long serialVersionUID = 1L;

	private Texture skin;
	private Texture cape;
	private Texture elytra;

	public PlayerTextures(Texture skin, Texture cape, Texture elytra) {
		this.skin = skin;
		this.cape = cape;
		this.elytra = elytra;
	}

	public Texture getSkin() {
		return skin;
	}

	public Texture getCape() {
		return cape;
	}

	public Texture getElytra() {
		return elytra;
	}

	@Override
	public String toString() {
		return String.format("PlayerTextures [skin=%s, cape=%s, elytra=%s]", skin, cape, elytra);
	}

	@Override
	public int hashCode() {
		return Objects.hash(skin, cape, elytra);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof PlayerTextures) {
			PlayerTextures another = (PlayerTextures) obj;
			return Objects.equals(skin, another.skin) && Objects.equals(cape, another.cape) && Objects.equals(elytra, another.elytra);
		}
		return false;
	}

}
