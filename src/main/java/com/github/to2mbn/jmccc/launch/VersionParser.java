package com.github.to2mbn.jmccc.launch;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.util.OsTypes;
import com.github.to2mbn.jmccc.util.Utils;
import com.github.to2mbn.jmccc.version.Library;
import com.github.to2mbn.jmccc.version.Version;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class VersionParser {

    public Version parse(MinecraftDirectory minecraftDir, String name) throws IOException, JsonParseException {
        String version;
        String assets;
        String mainClass;
        String launchArgs;
        Set<Library> libraries = new HashSet<>();
        File jar;

        JsonObject json = Utils.readJson(minecraftDir.getVersionJson(name)).getAsJsonObject();
        version = json.get("id").getAsString();
        assets = json.get("assets").getAsString();
        mainClass = json.get("mainClass").getAsString();
        launchArgs = json.get("minecraftArguments").getAsString();
        loadDepends(json.getAsJsonArray("libraries"), libraries);

        // used to handle Forge, Liteloader......
        if (json.has("inheritsFrom")) {
            String inheritsFrom;
            String inheritsJar;
            do {
                inheritsFrom = json.get("inheritsFrom").getAsString();
                inheritsJar = json.has("jar") ? json.get("jar").getAsString() : inheritsFrom;
                json = Utils.readJson(minecraftDir.getVersionJson(inheritsFrom, inheritsJar)).getAsJsonObject();
                loadDepends(json.getAsJsonArray("libraries"), libraries);
            } while (json.has("inheritsFrom"));
            jar = minecraftDir.getVersionJar(inheritsFrom, inheritsJar);
        } else {
            jar = minecraftDir.getVersionJar(name);
        }

        return new Version(version, mainClass, assets, launchArgs, jar, libraries);
    }

    private void loadDepends(JsonArray librariesList, Collection<Library> libraries) {
        for (JsonElement element : librariesList) {
            JsonObject library = element.getAsJsonObject();

            if (!IsAllow(library.get("rules"))) {
                continue;
            }

            String[] splited = library.get("name").getAsString().split(":", 3);
            String domain = splited[0];
            String name = splited[1];
            String version = splited[2];

            boolean isNative = library.has("natives");
            if (isNative) {
                String natives = resolveNatives(library.get("natives"));
                Set<String> excludes = resolveExtractExclude(library.get("extract"));
                libraries.add(new Library(domain, name, version, natives, excludes));
            } else {
                libraries.add(new Library(domain, name, version));
            }
        }
    }

    private boolean IsAllow(JsonElement rules) {
        // by default it's allow
        if (rules == null || rules.getAsJsonArray().size() == 0) {
            return true;
        }

        // else it's disallow by default
        boolean allow = false;
        for (JsonElement element : rules.getAsJsonArray()) {
            JsonObject rule = element.getAsJsonObject();

            boolean action = rule.get("action").getAsString().equals("allow");

            // apply by default
            boolean apply = true;

            if (rule.has("os")) {
                // don't apply by default if has os rule
                apply = false;

                JsonObject osRule = rule.getAsJsonObject("os");
                String name = osRule.get("name").getAsString();
                String version = osRule.has("version") ? osRule.get("version").getAsString() : null;

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

    private String resolveNatives(JsonElement nativesList) {
        if (nativesList == null) {
            return null;
        }

        JsonElement nativesElement = nativesList.getAsJsonObject().get(OsTypes.CURRENT.name().toLowerCase());
        if (nativesElement == null) {
            return null;
        } else {
            return nativesElement.getAsString().replaceAll("\\Q${arch}", System.getProperty("java.vm.name").contains("64") ? "64" : "32");
        }
    }

    private Set<String> resolveExtractExclude(JsonElement extract) {
        if (extract == null || !extract.getAsJsonObject().has("exclude")) {
            return null;
        }

        Set<String> excludes = new HashSet<>();
        for (JsonElement element : extract.getAsJsonObject().getAsJsonArray("exclude")) {
            excludes.add(element.getAsString());
        }
        return excludes;
    }

}
