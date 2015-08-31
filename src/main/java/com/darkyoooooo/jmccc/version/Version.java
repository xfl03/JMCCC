package com.darkyoooooo.jmccc.version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.darkyoooooo.jmccc.util.OsTypes;
import com.darkyoooooo.jmccc.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class Version {

    private static File getVersionJsonFile(File versionsDir, String version) {
        return new File(new File(versionsDir, version), version + ".json");
    }

    private static File getVersionJarFile(File versionsDir, String version) {
        return new File(new File(versionsDir, version), version + ".jar");
    }

    private String version;
    private String mainClass;
    private String assets;
    private String launchArgs;
    private File jar;

    private List<Library> libraries = new ArrayList<>();
    private List<Native> natives = new ArrayList<>();

    public Version(File versionsDir, String name) throws JsonSyntaxException, IOException {
        JsonObject json = Utils.readJson(getVersionJsonFile(versionsDir, name)).getAsJsonObject();

        version = json.get("id").getAsString();
        assets = json.get("assets").getAsString();
        mainClass = json.get("mainClass").getAsString();
        launchArgs = json.get("minecraftArguments").getAsString();
        loadDepends(json);

        // used to handle Forge, Liteloader......
        if (json.has("inheritsFrom")) {
            String inheritsFrom;
            String inheritsJar;
            do {
                inheritsFrom = json.get("inheritsFrom").getAsString();
                inheritsJar = json.has("jar") ? json.get("jar").getAsString() : inheritsFrom;
                json = Utils.readJson(getVersionJsonFile(versionsDir, inheritsFrom)).getAsJsonObject();
                loadDepends(json);
            } while (json.has("inheritsFrom"));
            jar = new File(new File(versionsDir, inheritsFrom), inheritsJar + ".jar");
        } else {
            jar = getVersionJarFile(versionsDir, name);
        }
    }

    private void loadDepends(JsonObject obj) {
        JsonArray libs = obj.get("libraries").getAsJsonArray();
        for (int i = 0; i < libs.size(); ++i) {
            obj = libs.get(i).getAsJsonObject();
            String[] info = obj.get("name").getAsString().split(":");
            if (!obj.has("natives")) {
                if (obj.has("rules") && !checkRules(obj.get("rules").getAsJsonArray())) {
                    continue;
                } else {
                    libraries.add(new Library(info[0], info[1], info[2], obj.has("serverreq") ? obj.get("serverreq").getAsBoolean() : true, obj.has("clientreq") ? obj.get("clientreq").getAsBoolean() : true));
                }
            } else {
                String suffix = obj.get("natives").getAsJsonObject().get(OsTypes.CURRENT().toString().toLowerCase()).getAsString();
                natives.add(new Native(info[0], info[1], info[2], suffix.contains("${arch}") ? suffix.replaceAll("\\Q${arch}", System.getProperty("java.vm.name").contains("64") ? "64" : "32") : suffix, obj.has("rules") ? checkRules(obj.get("rules").getAsJsonArray()) : true));
            }
        }
    }

    private boolean checkRules(JsonArray array) {
        boolean flag = false;
        for (int i = 0; i < array.size(); ++i) {
            JsonObject obj = array.get(i).getAsJsonObject();
            if (obj.has("action")) {
                if (obj.get("action").getAsString().contentEquals("allow")) {
                    if (!obj.has("os")) {
                        flag = true;
                    } else {
                        flag = obj.get("os").getAsJsonObject().get("name").getAsString().contains(OsTypes.CURRENT().toString().toLowerCase());
                    }
                }
                if (obj.get("action").getAsString().contentEquals("disallow") && obj.has("os")) {
                    return !obj.get("os").getAsJsonObject().get("name").getAsString().contains(OsTypes.CURRENT().toString().toLowerCase());
                }
            }
        }
        return flag;
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

    public List<Library> getLibraries() {
        return libraries;
    }

    public List<Native> getNatives() {
        return natives;
    }
}
