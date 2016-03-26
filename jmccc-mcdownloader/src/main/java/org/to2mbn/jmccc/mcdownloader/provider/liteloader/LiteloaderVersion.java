package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.json.JSONObject;

public class LiteloaderVersion implements Serializable {

	private static final long serialVersionUID = 1L;

	private String minecraftVersion;
	private String liteloaderVersion;
	private String superVersion;
	private String tweakClass;
	private String repoUrl;
	private transient Set<JSONObject> libraries;

	public LiteloaderVersion(String minecraftVersion, String liteloaderVersion, String tweakClass, String repoUrl, Set<JSONObject> libraries) {
		this(minecraftVersion, liteloaderVersion, minecraftVersion, tweakClass, repoUrl, libraries);
	}

	public LiteloaderVersion(String minecraftVersion, String liteloaderVersion, String superVersion, String tweakClass, String repoUrl, Set<JSONObject> libraries) {
		Objects.requireNonNull(minecraftVersion);
		Objects.requireNonNull(liteloaderVersion);
		Objects.requireNonNull(superVersion);
		this.minecraftVersion = minecraftVersion;
		this.liteloaderVersion = liteloaderVersion;
		this.superVersion = superVersion;
		this.tweakClass = tweakClass;
		this.repoUrl = repoUrl;
		this.libraries = libraries;
	}

	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	public String getLiteloaderVersion() {
		return liteloaderVersion;
	}

	public String getSuperVersion() {
		return superVersion;
	}

	public String getVersionName() {
		return superVersion + "-LiteLoader" + minecraftVersion;
	}

	public String getTweakClass() {
		return tweakClass;
	}

	public String getRepoUrl() {
		return repoUrl;
	}

	public Set<JSONObject> getLibraries() {
		return libraries;
	}

	public LiteloaderVersion customize(String superVersion) {
		return new LiteloaderVersion(minecraftVersion, liteloaderVersion, superVersion, tweakClass, repoUrl, libraries);
	}

	@Override
	public String toString() {
		return getVersionName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(minecraftVersion, liteloaderVersion, superVersion, tweakClass, repoUrl, libraries);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof LiteloaderVersion) {
			LiteloaderVersion another = (LiteloaderVersion) obj;
			return minecraftVersion.equals(another.minecraftVersion) &&
					liteloaderVersion.equals(another.liteloaderVersion) &&
					superVersion.equals(another.superVersion) &&
					Objects.equals(tweakClass, another.tweakClass) &&
					Objects.equals(repoUrl, another.repoUrl) &&
					Objects.equals(libraries, another.libraries);
		}
		return false;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeBoolean(libraries == null);
		if (libraries != null) {
			out.writeInt(libraries.size());
			for (JSONObject element : libraries) {
				out.writeUTF(element.toString());
			}
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if (!in.readBoolean()) {
			int size = in.readInt();
			Set<JSONObject> newLibraries = new HashSet<>();
			for (int i = 0; i < size; i++)
				newLibraries.add(new JSONObject(in.readUTF()));
			libraries = Collections.unmodifiableSet(newLibraries);
		} else {
			libraries = null;
		}
	}

}
