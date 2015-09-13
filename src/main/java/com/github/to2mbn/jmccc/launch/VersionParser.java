package com.github.to2mbn.jmccc.launch;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.util.OsTypes;
import com.github.to2mbn.jmccc.util.Utils;
import com.github.to2mbn.jmccc.version.Library;
import com.github.to2mbn.jmccc.version.Native;
import com.github.to2mbn.jmccc.version.Version;

public class VersionParser {

    public Version parse(MinecraftDirectory minecraftDir, String name) throws IOException, JSONException {
        Set<Library> libraries = new HashSet<>();

        JSONObject json = Utils.readJson(minecraftDir.getVersionJson(name));
        String version = json.getString("id");
        String assets = json.getString("assets");
        String mainClass = json.getString("mainClass");
        String launchArgs = json.getString("minecraftArguments");
        loadDepends(json.getJSONArray("libraries"), libraries);

        String jarPath;

        // used to handle Forge, Liteloader......
        if (json.has("inheritsFrom")) {
            String inheritsFrom;
            String inheritsJar;
            do {
                inheritsFrom = json.getString("inheritsFrom");
                inheritsJar = json.has("jar") ? json.getString("jar") : inheritsFrom;
                json = Utils.readJson(minecraftDir.getVersionJson(inheritsFrom, inheritsJar));
                loadDepends(json.getJSONArray("libraries"), libraries);
            } while (json.has("inheritsFrom"));
            jarPath = getVersionJarPath(inheritsFrom, inheritsJar);
        } else {
            jarPath = getVersionJarPath(name, version);
        }

        return new Version(version, mainClass, assets, launchArgs, jarPath, libraries);
    }

    private void loadDepends(JSONArray librariesList, Collection<Library> libraries) {
        for (int i = 0; i < librariesList.length(); i++) {
            JSONObject library = librariesList.getJSONObject(i);

            if (library.has("rules") && !IsAllow(library.getJSONArray("rules"))) {
                continue;
            }

            String[] splited = library.getString("name").split(":", 3);
            String domain = splited[0];
            String name = splited[1];
            String version = splited[2];

            String url = library.has("url") ? library.getString("url") : null;
            Map<String, String> checksums = library.has("checksums") ? resolveChecksums(library.getJSONArray("checksums")) : null;

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

    private boolean IsAllow(JSONArray rules) {
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

                if (OsTypes.CURRENT.name().equalsIgnoreCase(name)) {
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
        String archName = OsTypes.CURRENT.name().toLowerCase();

        if (natives.has(archName)) {
            return natives.getString(archName).replaceAll("\\Q${arch}", System.getProperty("java.vm.name").contains("64") ? "64" : "32");
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

    private Map<String, String> resolveChecksums(JSONArray sumarray) {
        Map<String, String> checksums = new HashMap<>();
        for (int i = 0; i < sumarray.length(); i++) {
            String algorithm = getHashAlgorithmByChecksumIndex(i);
            if (algorithm == null) {
                continue;
            }
            checksums.put(algorithm, sumarray.getString(i));
        }
        return checksums;
    }

    private String getHashAlgorithmByChecksumIndex(int index) {
        switch (index) {
            case 0:
                return "SHA";

            case 1:
                return "SHA-1";

            default:
                return null;
        }
    }
}
