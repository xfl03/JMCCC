package com.darkyoooooo.jmccc.version;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import com.darkyoooooo.jmccc.util.OsTypes;
import com.darkyoooooo.jmccc.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class Version {

    private static File getVersionJsonFile(File versionsDir, String version) {
        return new File(new File(versionsDir, version), version + ".json");
    }

    private static File getVersionJarFile(File versionsDir, String version) {
        return new File(new File(versionsDir, version), version + ".jar");
    }

    private File minecraftDir;
    private String version;
    private String mainClass;
    private String assets;
    private String launchArgs;
    private File jar;

    private Set<Library> libraries = new HashSet<>();

    public Version(File minecraftDir, String name) throws JsonSyntaxException, IOException {
        this.minecraftDir = minecraftDir;

        File versionsDir = new File(minecraftDir, "versions");
        JsonObject json = Utils.readJson(getVersionJsonFile(versionsDir, name)).getAsJsonObject();

        version = json.get("id").getAsString();
        assets = json.get("assets").getAsString();
        mainClass = json.get("mainClass").getAsString();
        launchArgs = json.get("minecraftArguments").getAsString();
        loadDepends(json.getAsJsonArray("libraries"));

        // used to handle Forge, Liteloader......
        if (json.has("inheritsFrom")) {
            String inheritsFrom;
            String inheritsJar;
            do {
                inheritsFrom = json.get("inheritsFrom").getAsString();
                inheritsJar = json.has("jar") ? json.get("jar").getAsString() : inheritsFrom;
                json = Utils.readJson(getVersionJsonFile(versionsDir, inheritsFrom)).getAsJsonObject();
                loadDepends(json.getAsJsonArray("libraries"));
            } while (json.has("inheritsFrom"));
            jar = new File(new File(versionsDir, inheritsFrom), inheritsJar + ".jar");
        } else {
            jar = getVersionJarFile(versionsDir, name);
        }
    }

    /**
     * Checks the libraries and returns a set of the missing libraries.
     * <p>
     * If there's no missing library, this method will return a empty set. This method returns a non-threaded safe,
     * unordered set.
     * 
     * @return a set of missing libraries
     */
    public Set<Library> findMissingLibraries() {
        Set<Library> missing = new HashSet<>();
        for (Library library : libraries) {
            if (!new File(minecraftDir, library.getPath()).exists()) {
                missing.add(library);
            }
        }
        return missing;
    }

    private void loadDepends(JsonArray librariesList) {
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

                if (OsTypes.CURRENT().name().equalsIgnoreCase(name)) {
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

        JsonElement nativesElement = nativesList.getAsJsonObject().get(OsTypes.CURRENT().name().toLowerCase());
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

    public String getVersion() {
        return version;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getAssets() {
        return assets;
    }

    public String getLaunchArgs() {
        return launchArgs;
    }

    public File getJar() {
        return jar;
    }

    public Set<Library> getLibraries() {
        return libraries;
    }

}
