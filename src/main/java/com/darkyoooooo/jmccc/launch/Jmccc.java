package com.darkyoooooo.jmccc.launch;

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
import com.darkyoooooo.jmccc.util.BaseOptions;
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.util.VersionsHandler;
import com.darkyoooooo.jmccc.version.Library;
import com.darkyoooooo.jmccc.version.Native;

public class Jmccc {
    public static final List<String> ADV_ARGS = new ArrayList<String>();
    public static final List<Library> MISSING_LIBRARIES = new ArrayList<Library>();
    public static final List<Native> MISSING_NATIVES = new ArrayList<Native>();

    private final BaseOptions baseOptions;
    private final VersionsHandler versionsHandler;

    public Jmccc(BaseOptions baseOptions) {
        this.baseOptions = baseOptions;
        this.versionsHandler = new VersionsHandler(this.baseOptions.getGameRoot());
    }

    public void resetAdvArgs() {
        if (!ADV_ARGS.isEmpty()) {
            ADV_ARGS.clear();
        }
        ADV_ARGS.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
        ADV_ARGS.add("-Dfml.ignorePatchDiscrepancies=true");
    }

    public boolean hasMissingLibraries() {
        return !MISSING_LIBRARIES.isEmpty();
    }

    public boolean hasMissingNatives() {
        return !MISSING_NATIVES.isEmpty();
    }

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

        if (hasMissingLibraries() || hasMissingNatives()) {
            throw new MissingDependenciesException("Library文件或Native文件缺失");
        }

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
        this.resetAdvArgs();
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
            return new LaunchArgument(option, tokens, ADV_ARGS, Utils.isCGCSupported(), Utils.resolveRealLibPaths(this, option.getVersion().getLibraries()),
                    Utils.handlePath(this.baseOptions.getGameRoot() + "/natives"));
        }
    }

    public BaseOptions getBaseOptions() {
        return this.baseOptions;
    }

    public VersionsHandler getVersionsHandler() {
        return this.versionsHandler;
    }
}
