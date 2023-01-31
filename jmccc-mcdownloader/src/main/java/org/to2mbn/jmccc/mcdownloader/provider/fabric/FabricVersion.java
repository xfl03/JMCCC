package org.to2mbn.jmccc.mcdownloader.provider.fabric;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FabricVersion {
    private final String minecraftVersion;
    private final String fabricLoaderVersion;
    private final String loaderName;

    public FabricVersion(String minecraftVersion, String fabricLoaderVersion, String loaderName) {
        this.minecraftVersion = minecraftVersion;
        this.fabricLoaderVersion = fabricLoaderVersion;
        this.loaderName = loaderName;
    }

    private static final Pattern PATTERN = Pattern.compile("^([\\w.\\-]+)-loader-([\\w.\\-]+)-([\\w.\\-]+)$");

    public static FabricVersion resolve(String loaderName, String version) {
        Matcher m = PATTERN.matcher(version);
        if (m.matches()) {
            //Check loader name
            if (!loaderName.equals(m.group(1))) {
                return null;
            }
            return new FabricVersion(m.group(3), m.group(2), m.group(1));
        }
        return null;
    }

    public String getVersionName() {
        return loaderName + "-loader-" + fabricLoaderVersion + "-" + minecraftVersion;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getFabricLoaderVersion() {
        return fabricLoaderVersion;
    }
}
