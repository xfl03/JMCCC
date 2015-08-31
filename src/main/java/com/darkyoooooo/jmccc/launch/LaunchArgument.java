package com.darkyoooooo.jmccc.launch;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.darkyoooooo.jmccc.util.OsTypes;
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.Version;

/**
 * Used to generate launching command line.
 */
public class LaunchArgument {
    private LaunchOption launchOption;
    private String argTemplet, mainClass, nativePath;
    private List<String> libraries, advArgs;
    private Map<String, String> tokens;
    private int maxMemory, minMemory;
    private boolean enableCGC;

    public LaunchArgument(LaunchOption launchOption, Map<String, String> tokens, List<String> advArgs, boolean enableCGC, List<String> libraries, String nativesPath) {
        this.launchOption = launchOption;
        this.argTemplet = launchOption.getVersion().getLaunchArgs();
        this.mainClass = launchOption.getVersion().getMainClass();
        this.libraries = libraries;
        this.maxMemory = launchOption.getMaxMemory();
        this.minMemory = launchOption.getMinMemory();
        this.enableCGC = enableCGC;
        this.nativePath = nativesPath;
        this.tokens = tokens;
        this.advArgs = advArgs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        OsTypes os = OsTypes.CURRENT();

        sb.append(Utils.getJavaPath()).append(' ');

        if (enableCGC) {
            sb.append("-Xincgc ");
        }
        if (minMemory > 0) {
            sb.append("-Xms").append(minMemory).append("M ");
        }
        if (maxMemory > 0) {
            sb.append("-Xmx").append(maxMemory).append("M ");
        } else {
            sb.append("-Xmx1024M ");
        }
        for (String adv : advArgs) {
            sb.append(adv).append(' ');
        }
        if (os == OsTypes.WINDOWS) {
            sb.append("-Djava.library.path=\"" + nativePath + "\" ");
        } else {
            sb.append("-Djava.library.path=" + nativePath + " ");
        }

        sb.append("-cp \"");
        for (String lib : libraries) {
            sb.append(lib).append(os.getPathSpearator());
        }
        Version ver = launchOption.getVersion();
        if (!ver.isInheritsForm()) {
            sb.append(String.format("%s.jar%s\" ", Utils.handlePath(ver.getPath() + "/" + ver.getId()), os.getPathSpearator()));
        } else {
            sb.append(String.format("%s.jar%s\" ", Utils.handlePath(String.format("%s/%s", ver.getParentInheritsPath(), ver.getParentInheritsFormName())), os.getPathSpearator()));
        }

        sb.append(mainClass).append(' ');
        sb.append(replaceLaunchArgs()).append(' ');

        if (launchOption.getServerInfo() != null && launchOption.getServerInfo().getAddress() != null && !launchOption.getServerInfo().getAddress().equals("")) {
            sb.append("--server ").append(launchOption.getServerInfo().getAddress()).append(' ');
            sb.append("--port ").append(launchOption.getServerInfo().getPort() == 0 ? 25565 : launchOption.getServerInfo().getPort()).append(' ');
        }
        if (launchOption.getWindowSize() != null) {
            if (launchOption.getWindowSize().isFullSize()) {
                sb.append("--fullscreen").append(' ');
            }
            if (launchOption.getWindowSize().getHeight() > 0) {
                sb.append("--height " + launchOption.getWindowSize().getHeight()).append(' ');
            }
            if (launchOption.getWindowSize().getWidth() > 0) {
                sb.append("--width " + launchOption.getWindowSize().getWidth()).append(' ');
            }
        }
        return sb.toString();
    }

    private String replaceLaunchArgs() {
        String arg = argTemplet;
        for (Entry<String, String> entry : tokens.entrySet()) {
            arg = arg.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return arg;
    }

    public LaunchOption getLaunchOption() {
        return launchOption;
    }

    public String getArgTemplet() {
        return argTemplet;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getNativePath() {
        return nativePath;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public List<String> getAdvArgs() {
        return advArgs;
    }

    public Map<String, String> getTokens() {
        return tokens;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public int getMinMemory() {
        return minMemory;
    }

    public boolean isEnableCGC() {
        return enableCGC;
    }

}
