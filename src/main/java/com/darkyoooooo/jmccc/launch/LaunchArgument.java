package com.darkyoooooo.jmccc.launch;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.darkyoooooo.jmccc.util.OsTypes;
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.Version;

public class LaunchArgument {
    private LaunchOption launchOption;
    private String argTemplet, mainClass, nativePath;
    private List<String> libraries, advArgs;
    private Map<String, String> tokens;
    private int maxMemory, minMemory;
    private boolean enableCGC;
    private ServerInfo serverInfo;
    private WindowSize windowSize;

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
        this.serverInfo = launchOption.getServerInfo();
        this.windowSize = launchOption.getWindowSize();
        this.advArgs = advArgs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        OsTypes os = OsTypes.CURRENT();

        sb.append(Utils.getJavaPath()).append(' ');

        if (this.enableCGC) {
            sb.append("-Xincgc ");
        }
        if (this.minMemory > 0) {
            sb.append("-Xms").append(this.minMemory).append("M ");
        }
        if (this.maxMemory > 0) {
            sb.append("-Xmx").append(this.maxMemory).append("M ");
        } else {
            sb.append("-Xmx1024M ");
        }
        for (String adv : this.advArgs) {
            sb.append(adv).append(' ');
        }
        if (os == OsTypes.WINDOWS) {
            sb.append("-Djava.library.path=\"" + this.nativePath + "\" ");
        } else {
            sb.append("-Djava.library.path=" + this.nativePath + " ");
        }

        sb.append("-cp \"");
        for (String lib : this.libraries) {
            sb.append(lib).append(os.getPathSpearator());
        }
        Version ver = this.launchOption.getVersion();
        if (!ver.isInheritsForm()) {
            sb.append(String.format("%s.jar%s\" ", Utils.handlePath(ver.getPath() + "/" + ver.getId()), os.getPathSpearator()));
        } else {
            sb.append(String.format("%s.jar%s\" ", Utils.handlePath(String.format("%s/%s", ver.getParentInheritsPath(), ver.getParentInheritsFormName())), os.getPathSpearator()));
        }

        sb.append(this.mainClass).append(' ');
        sb.append(this.replaceLaunchArgs()).append(' ');

        if (this.serverInfo != null && this.serverInfo.getAddress() != null && !this.serverInfo.getAddress().equals("")) {
            sb.append("--server ").append(this.serverInfo.getAddress()).append(' ');
            sb.append("--port ").append(this.serverInfo.getPort() == 0 ? 25565 : this.serverInfo.getPort()).append(' ');
        }
        if (this.windowSize != null) {
            if (this.windowSize.isFullSize()) {
                sb.append("--fullscreen").append(' ');
            }
            if (this.windowSize.getHeight() > 0) {
                sb.append("--height " + this.windowSize.getHeight()).append(' ');
            }
            if (this.windowSize.getWidth() > 0) {
                sb.append("--width " + this.windowSize.getWidth()).append(' ');
            }
        }
        return sb.toString();
    }

    private String replaceLaunchArgs() {
        String arg = this.argTemplet;
        for (Entry<String, String> entry : this.tokens.entrySet()) {
            arg = arg.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return arg;
    }

    public LaunchOption getLaunchOption() {
        return this.launchOption;
    }

    public String getArgTemplet() {
        return this.argTemplet;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public String getNativePath() {
        return this.nativePath;
    }

    public List<String> getLibraries() {
        return this.libraries;
    }

    public List<String> getAdvArgs() {
        return this.advArgs;
    }

    public Map<String, String> getTokens() {
        return this.tokens;
    }

    public int getMaxMemory() {
        return this.maxMemory;
    }

    public int getMinMemory() {
        return this.minMemory;
    }

    public boolean isEnableCGC() {
        return this.enableCGC;
    }

    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    public WindowSize getWindowSize() {
        return this.windowSize;
    }
}
