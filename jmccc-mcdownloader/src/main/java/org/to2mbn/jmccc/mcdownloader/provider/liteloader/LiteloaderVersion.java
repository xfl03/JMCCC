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
	private String file;
	private String md5;
	private Long timestamp;
	private String tweakClass;
	private transient Set<JSONObject> libraries;

	public LiteloaderVersion(String minecraftVersion, String liteloaderVersion, String file, String md5, Long timestamp, String tweakClass, Set<JSONObject> libraries) {
		Objects.requireNonNull(minecraftVersion);
		Objects.requireNonNull(liteloaderVersion);
		this.minecraftVersion = minecraftVersion;
		this.liteloaderVersion = liteloaderVersion;
		this.file = file;
		this.md5 = md5;
		this.timestamp = timestamp;
		this.tweakClass = tweakClass;
		this.libraries = libraries;
	}

	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	public String getLiteloaderVersion() {
		return liteloaderVersion;
	}

	public String getVersionName() {
		return minecraftVersion + "-LiteLoader" + minecraftVersion;
	}

	public String getFile() {
		return file;
	}

	public String getMd5() {
		return md5;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public String getTweakClass() {
		return tweakClass;
	}

	public Set<JSONObject> getLibraries() {
		return libraries;
	}

	@Override
	public String toString() {
		return getVersionName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(minecraftVersion, liteloaderVersion, file, md5, timestamp, tweakClass, libraries);
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
					Objects.equals(file, another.file) &&
					Objects.equals(md5, another.md5) &&
					Objects.equals(timestamp, another.timestamp) &&
					Objects.equals(tweakClass, another.tweakClass) &&
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
