package org.to2mbn.jmccc.version;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.Platform;

class VersionParser {

	public Version parseVersion(MinecraftDirectory minecraftDir, String name) throws IOException, JSONException {
		Set<Library> libraries = new HashSet<>();

		JSONObject json = readJson(minecraftDir.getVersionJson(name));
		String version = json.getString("id");
		String assets = json.optString("assets", "legacy");
		String mainClass = json.getString("mainClass");
		String launchArgs = json.getString("minecraftArguments");
		String type = json.optString("type", null);
		loadDepends(json.getJSONArray("libraries"), libraries);

		String jarPath;

		if (json.has("inheritsFrom")) {
			String inheritsFrom;
			String inheritsJar;
			do {
				inheritsFrom = json.getString("inheritsFrom");
				inheritsJar = json.has("jar") ? json.getString("jar") : inheritsFrom;
				json = readJson(minecraftDir.getVersionJson(inheritsFrom, inheritsJar));
				loadDepends(json.getJSONArray("libraries"), libraries);
				assets = json.optString("assets", "legacy");
			} while (json.has("inheritsFrom"));
			jarPath = getVersionJarPath(inheritsFrom, inheritsJar);
		} else {
			jarPath = getVersionJarPath(name, version);
		}

		return new Version(version, type, mainClass, assets, launchArgs, jarPath, libraries, assets.equals("legacy"));
	}

	public Set<Asset> parseAssets(MinecraftDirectory minecraftDir, String name) throws IOException, JSONException {
		JSONObject json = readJson(minecraftDir.getAssetIndex(name));
		JSONObject objects = json.getJSONObject("objects");
		Set<Asset> assets = new HashSet<>();
		for (Object oVirtualPath : objects.keySet()) {
			String virtualPath = (String) oVirtualPath;
			JSONObject object = objects.getJSONObject(virtualPath);
			String hash = object.getString("hash");
			int size = object.getInt("size");
			assets.add(new Asset(virtualPath, hash, size));
		}
		return assets;
	}

	private void loadDepends(JSONArray librariesList, Collection<Library> libraries) {
		for (int i = 0; i < librariesList.length(); i++) {
			JSONObject library = librariesList.getJSONObject(i);

			if (library.has("rules") && !isAllow(library.getJSONArray("rules"))) {
				continue;
			}

			String[] splited = library.getString("name").split(":", 3);
			String domain = splited[0];
			String name = splited[1];
			String version = splited[2];

			String url = library.has("url") ? library.getString("url") : null;
			String[] checksums = library.has("checksums") ? resolveChecksums(library.getJSONArray("checksums")) : null;

			boolean isNative = library.has("natives");
			if (isNative) {
				String natives = resolveNatives(library.getJSONObject("natives"));
				Set<String> excludes = library.has("extract") ? resolveExtractExclude(library.getJSONObject("extract")) : null;
				libraries.add(new Native(domain, name, version, natives, excludes, url, checksums));
			} else {
				libraries.add(new Library(domain, name, version, url, checksums));
			}
		}
	}

	private boolean isAllow(JSONArray rules) {
		// by default it's allow
		if (rules.length() == 0) {
			return true;
		}

		// else it's disallow by default
		boolean allow = false;
		for (int i = 0; i < rules.length(); i++) {
			JSONObject rule = rules.getJSONObject(i);

			boolean action = rule.get("action").equals("allow");

			// apply by default
			boolean apply = true;

			if (rule.has("os")) {
				// don't apply by default if has os rule
				apply = false;

				JSONObject osRule = rule.getJSONObject("os");
				String name = osRule.getString("name");
				String version = osRule.has("version") ? osRule.getString("version") : null;

				if (Platform.CURRENT.name().equalsIgnoreCase(name)) {
					if (version == null || System.getProperty("os.version").matches(version)) {
						apply = true;
					}
				}
			}

			if (apply) {
				allow = action;
			}
		}

		return allow;
	}

	private String resolveNatives(JSONObject natives) {
		String archName = Platform.CURRENT.name().toLowerCase();

		if (natives.has(archName)) {
			return natives.getString(archName).replaceAll("\\Q${arch}", System.getProperty("os.arch").contains("64") ? "64" : "32");
		} else {
			return null;
		}
	}

	private Set<String> resolveExtractExclude(JSONObject extract) {
		if (!extract.has("exclude")) {
			return null;
		}

		Set<String> excludes = new HashSet<>();
		JSONArray excludesArray = extract.getJSONArray("exclude");
		for (int i = 0; i < excludesArray.length(); i++) {
			excludes.add(excludesArray.getString(i));
		}
		return excludes;
	}

	private String getVersionJarPath(String version, String jar) {
		return version + "/" + jar + ".jar";
	}

	private String[] resolveChecksums(JSONArray sumarray) {
		String[] checksums = new String[sumarray.length()];
		for (int i = 0; i < sumarray.length(); i++) {
			checksums[i] = sumarray.getString(i);
		}
		return checksums;
	}

	private JSONObject readJson(File file) throws IOException, JSONException {
		try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8")) {
			return new JSONObject(new JSONTokener(reader));
		}
	}
}
