package com.darkyoooooo.jmccc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.darkyoooooo.jmccc.auth.AuthInfo;
import com.darkyoooooo.jmccc.ext.GameProcessMonitor;
import com.darkyoooooo.jmccc.ext.IGameListener;
import com.darkyoooooo.jmccc.launch.LaunchArgument;
import com.darkyoooooo.jmccc.launch.LaunchException;
import com.darkyoooooo.jmccc.launch.LaunchOption;
import com.darkyoooooo.jmccc.launch.LaunchResult;
import com.darkyoooooo.jmccc.launch.LoginException;
import com.darkyoooooo.jmccc.launch.MissingDependenciesException;
import com.darkyoooooo.jmccc.launch.UncompressException;
import com.darkyoooooo.jmccc.util.EnvironmentOption;
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.Library;
import com.darkyoooooo.jmccc.version.Native;

public class Jmccc {

    private EnvironmentOption baseOptions;

    public Jmccc(EnvironmentOption baseOptions) {
        this.baseOptions = baseOptions;
    }

    // ADV_ARGS.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
    // ADV_ARGS.add("-Dfml.ignorePatchDiscrepancies=true");

    public LaunchResult launch(LaunchOption option) throws LaunchException {
        return launch(generateLaunchArgs(option), null);
    }

    public LaunchResult launch(LaunchArgument arg) throws LaunchException {
        return launch(arg, null);
    }

    public LaunchResult launch(LaunchOption option, IGameListener listener) throws LaunchException {
        return launch(generateLaunchArgs(option), null);
    }

    public LaunchResult launch(LaunchArgument arg, IGameListener listener) throws LaunchException {
        Objects.requireNonNull(arg);

        Process process;
        try {
            process = Runtime.getRuntime().exec(arg.toString(), null, new File(baseOptions.getGameRoot()));
        } catch (IOException e) {
            throw new LaunchException(e);
        }

        GameProcessMonitor monitor = null;
        if (listener != null) {
            monitor = new GameProcessMonitor(process, listener);
            monitor.monitor();
        }

        return new LaunchResult(monitor, process);
    }

    public LaunchArgument generateLaunchArgs(LaunchOption option) throws LaunchException {
        AuthInfo authInfo = option.getAuthenticator().get();
        if (authInfo.getError() != null && !authInfo.getError().isEmpty()) {
            throw new LoginException(authInfo.getError());
        } else {
            for (String path : Utils.resolveRealNativePaths(this, option.getVersion().getNatives())) {
                try {
                    Utils.uncompressZipFile(new File(path), this.baseOptions.getGameRoot() + "/natives");
                } catch (IOException e) {
                    throw new UncompressException(String.format("解压\'%s\'时出现错误", path), e);
                }
            }
            Map<String, String> tokens = new HashMap<String, String>();
            tokens.put("auth_access_token", authInfo.getAccessToken());
            tokens.put("auth_session", authInfo.getAccessToken());
            tokens.put("auth_player_name", authInfo.getDisplayName());
            tokens.put("version_name", option.getVersion().getId());
            tokens.put("game_directory", ".");
            tokens.put("assets_root", "assets");
            tokens.put("assets_index_name", option.getVersion().getAssets());
            tokens.put("auth_uuid", authInfo.getUuid());
            tokens.put("user_type", authInfo.getUserType());
            tokens.put("user_properties", authInfo.getProperties());
            return new LaunchArgument(option, tokens, option.getExtraArguments(), Utils.isCGCSupported(), Utils.resolveRealLibPaths(this, option.getVersion().getLibraries()),
                    Utils.handlePath(this.baseOptions.getGameRoot() + "/natives"));
        }
    }

    public List<String> resolveRealLibPaths(List<Library> list) throws MissingDependenciesException {
        List<String> realPaths = new ArrayList<String>();
        for (Library lib : list) {
            String path = Utils.handlePath(String.format("%s/libraries/%s/%s/%s/%s-%s.jar", jmccc.getBaseOptions().getGameRoot(), lib.getDomain().replace(".", "/"), lib.getName(), lib.getVersion(),
                    lib.getName(), lib.getVersion()));
            realPaths.add(path);
            if (!new File(path).exists()) {
                jmccc.MISSING_LIBRARIES.add(lib);
            }
        }
        return realPaths;
    }

    public static List<String> resolveRealNativePaths(Jmccc jmccc, List<Native> list) {
        List<String> realPaths = new ArrayList<String>();
        for (Native nat : list) {
            if (!nat.isAllowed()) {
                continue;
            }
            String path = Utils.handlePath(String.format("%s/libraries/%s/%s/%s/%s-%s-%s.jar", jmccc.getBaseOptions().getGameRoot(), nat.getDomain().replace(".", "/"), nat.getName(),
                    nat.getVersion(), nat.getName(), nat.getVersion(), nat.getSuffix()));
            realPaths.add(path);
            if (!new File(path).exists()) {
                jmccc.MISSING_NATIVES.add(nat);
            }
        }
        return realPaths;
    }

    public EnvironmentOption getBaseOptions() {
        return this.baseOptions;
    }

}
