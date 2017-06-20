package org.to2mbn.jmccc.version.parsing;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.AssetIndexInfo;
import org.to2mbn.jmccc.version.DownloadInfo;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.LibraryInfo;
import org.to2mbn.jmccc.version.Native;
import org.to2mbn.jmccc.version.Version;

class VersionParserImpl implements VersionParser {

	@Override
	public DownloadInfo parseDownloadInfo(JSONObject json) throws JSONException {
		if (json == null) return null;

		String url = json.optString("url", null);
		String checksum = json.optString("sha1", null);
		long size = json.optLong("size", -1);
		return new DownloadInfo(url, checksum, size);
	}

	@Override
	public AssetIndexInfo parseAssetIndexInfo(JSONObject json) throws JSONException {
		if (json == null) return null;

		DownloadInfo base = parseDownloadInfo(json);
		String id = json.getString("id");
		long totalSize = json.optLong("totalSize", -1);
		return new AssetIndexInfo(base.getUrl(), base.getChecksum(), base.getSize(), id, totalSize);
	}

	@Override
	public LibraryInfo parseLibraryInfo(JSONObject json) throws JSONException {
		if (json == null) return null;

		DownloadInfo base = parseDownloadInfo(json);
		String path = json.optString("path", null);
		return new LibraryInfo(base.getUrl(), base.getChecksum(), base.getSize(), path);
	}

	@Override
	public Library parseLibrary(JSONObject json, PlatformDescription platformDescription) throws JSONException {
		if (json == null) return null;

		if (!checkAllowed(json.optJSONArray("rules"), platformDescription)
				|| !json.optBoolean("clientreq", true)) {
			return null;
		}

		String[] splitedGav = json.getString("name").split(":", 3);
		String groupId = splitedGav[0];
		String artifactId = splitedGav[1];
		String version = splitedGav[2];

		String url = json.optString("url", null);
		String[] checksums = parseChecksums(json.optJSONArray("checksums"));

		JSONObject jsonNatives = json.optJSONObject("natives");

		boolean isNative = jsonNatives != null;
		String classifier = isNative ? parseNativeClassifier(json.getJSONObject("natives"), platformDescription) : null;
		LibraryInfo libinfo = parseLibraryDownloads(json.optJSONObject("downloads"), classifier);
		String type = "jar";

		if (isNative) {
			Set<String> excludes = parseExtractExcludes(json.getJSONObject("extract"));
			return new Native(groupId, artifactId, version, classifier, type, libinfo, url, checksums, excludes);
		} else {
			return new Library(groupId, artifactId, version, classifier, type, libinfo, url, checksums);
		}
	}

	@Override
	public Set<Asset> parseAssetIndex(JSONObject json) throws JSONException {
		if (json == null) return null;

		JSONObject objects = json.getJSONObject("objects");
		Set<Asset> assets = new TreeSet<>(new Comparator<Asset>() {

			@Override
			public int compare(Asset o1, Asset o2) {
				return o1.getVirtualPath().compareTo(o2.getVirtualPath());
			}
		});
		for (Object rawVirtualPath : objects.keySet()) {
			String virtualPath = (String) rawVirtualPath;
			JSONObject object = objects.getJSONObject(virtualPath);
			String hash = object.getString("hash");
			int size = object.getInt("size");
			assets.add(new Asset(virtualPath, hash, size));
		}
		return Collections.unmodifiableSet(assets);
	}

	@Override
	public Version parseVersion(Stack<JSONObject> hierarchy, PlatformDescription platformDescription) throws JSONException {
		String version = hierarchy.get(0).getString("id");
		String root = hierarchy.peek().getString("id");

		String assets = "legacy";
		String mainClass = null;
		String launchArgs = null;
		String type = null;
		Map<String, Library> librariesMap = new TreeMap<>();
		Map<String, DownloadInfo> downloads = new TreeMap<>();
		AssetIndexInfo assetIndexInfo = null;

		JSONObject json;
		do {
			json = hierarchy.pop();

			assets = json.optString("assets", assets);
			mainClass = json.optString("mainClass", mainClass);
			launchArgs = json.optString("minecraftArguments", launchArgs);
			type = json.optString("type", type);

			Set<Library> currentLibraries = parseLibraries(json.optJSONArray("libraries"), platformDescription);
			if (currentLibraries != null) {
				for (Library library : currentLibraries) {
					StringBuilder libId = new StringBuilder();
					libId.append(library.getGroupId()).append(':')
							.append(library.getArtifactId());
					if (library.getClassifier() != null)
						libId.append(':').append(library.getClassifier());
					librariesMap.put(libId.toString(), library);
				}
			}

			Map<String, DownloadInfo> currentDownloads = parseDownloads(json.optJSONObject("downloads"));
			if (currentDownloads != null)
				downloads.putAll(currentDownloads);

			JSONObject jsonAssetIndexInfo = json.optJSONObject("assetIndex");
			if (jsonAssetIndexInfo != null)
				assetIndexInfo = parseAssetIndexInfo(jsonAssetIndexInfo);

		} while (!hierarchy.isEmpty());

		if (mainClass == null)
			throw new JSONException("Missing mainClass");
		if (launchArgs == null)
			throw new JSONException("Missing minecraftArguments");

		Set<Library> libraries = new LinkedHashSet<>(librariesMap.values());

		return new Version(version,
				type,
				mainClass,
				assets,
				launchArgs,
				root,
				Collections.unmodifiableSet(libraries),
				assets.equals("legacy"),
				assetIndexInfo,
				Collections.unmodifiableMap(downloads));
	}

	@Override
	public boolean checkAllowed(JSONArray rules, PlatformDescription platformDescription) throws JSONException {
		// by default it's allowed
		if (rules == null || rules.length() == 0) {
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

				if (platformDescription.getPlatform().name().equalsIgnoreCase(name)) {
					if (version == null || platformDescription.getVersion().matches(version)) {
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

	private String[] parseChecksums(JSONArray json) throws JSONException {
		if (json == null) return null;

		String[] checksums = new String[json.length()];
		for (int i = 0; i < json.length(); i++) {
			checksums[i] = json.getString(i);
		}
		return checksums;
	}

	private String parseNativeClassifier(JSONObject natives, PlatformDescription platform) throws JSONException {
		if (natives == null) return null;
		String classifier = natives.optString(platform.getPlatform().name().toLowerCase(), null);
		if (classifier != null) {
			classifier = classifier.replaceAll("\\Q${arch}", platform.getArch());
		}
		return classifier;
	}

	private Set<String> parseExtractExcludes(JSONObject json) throws JSONException {
		if (json == null) return null;
		JSONArray elements = json.optJSONArray("exclude");
		if (elements == null) return null;

		Set<String> excludes = new LinkedHashSet<>();
		for (Object element : elements) {
			excludes.add((String) element);
		}
		return Collections.unmodifiableSet(excludes);
	}

	private LibraryInfo parseLibraryDownloads(JSONObject json, String classifier) throws JSONException {
		if (json == null) return null;

		JSONObject artifact;
		if (classifier == null) {
			artifact = json.optJSONObject("artifact");
		} else {
			JSONObject classifiers = json.optJSONObject("classifiers");
			if (classifiers == null) return null;
			artifact = classifiers.optJSONObject(classifier);
		}
		if (artifact == null) return null;
		return parseLibraryInfo(artifact);
	}

	private Set<Library> parseLibraries(JSONArray json, PlatformDescription platform) throws JSONException {
		if (json == null) return null;
		Set<Library> libraries = new HashSet<>();
		for (Object element : json) {
			Library library = parseLibrary((JSONObject) element, platform);
			if (library != null) {
				libraries.add(library);
			}
		}
		return libraries;
	}

	private Map<String, DownloadInfo> parseDownloads(JSONObject json) throws JSONException {
		if (json == null) return null;
		Map<String, DownloadInfo> downloads = new HashMap<>();
		for (String key : json.keySet()) {
			downloads.put(key, parseDownloadInfo(json.getJSONObject(key)));
		}
		return downloads;
	}

}
