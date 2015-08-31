package com.darkyoooooo.jmccc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.darkyoooooo.jmccc.auth.AuthInfo;
import com.darkyoooooo.jmccc.ext.GameProcessMonitor;
import com.darkyoooooo.jmccc.ext.Reporter;
import com.darkyoooooo.jmccc.launch.ErrorType;
import com.darkyoooooo.jmccc.launch.LaunchArgument;
import com.darkyoooooo.jmccc.launch.LaunchOption;
import com.darkyoooooo.jmccc.launch.LaunchResult;
import com.darkyoooooo.jmccc.launch.MonitorOption;
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
    private LaunchResult launchResult = null;

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

    public LaunchResult launchGame(LaunchArgument arg) {
        LaunchResult result = this.rLaunchGame(arg);
        Reporter.report(this, arg, result);
        return result;
    }

    private LaunchResult rLaunchGame(LaunchArgument arg) {
        if (this.launchResult != null) {
            return this.launchResult;
        }
        if (this.hasMissingLibraries() || this.hasMissingNatives()) {
            return this.launchResult = LaunchResult.launchUnsuccessfully(ErrorType.DEPENDS_MISSING_ERROR, "Library文件或Native文件缺失");
        }
        try {
            Process process = Runtime.getRuntime().exec(arg.toString(), null, new File(this.baseOptions.getGameRoot()));

            GameProcessMonitor monitor = null;
            MonitorOption monitorOption = arg.getLaunchOption().getMonitorOption();
            if (monitorOption != null) {
                monitor = new GameProcessMonitor(process, monitorOption.isDaemon(), monitorOption.getListeners());
                monitor.monitor();
            }

            return this.launchResult = LaunchResult.launchSuccessfully(monitor);
        } catch (Exception e) {
            return this.launchResult = LaunchResult.launchUnsuccessfully(ErrorType.HANDLE_ERROR, "启动游戏进程时出错", e);
        }
    }

    public LaunchArgument generateLaunchArgs(LaunchOption option) {
        return this.generateLaunchArgs(option, null);
    }

    public LaunchArgument generateLaunchArgs(LaunchOption option, AuthInfo authInfo) {
        this.resetAdvArgs();
        this.launchResult = null; // 防止第一次启动失败后再次启动时出现的各种奇怪问题
        authInfo = authInfo == null ? option.getAuthenticator().get() : authInfo;
        if (authInfo.getError() != null && !authInfo.getError().isEmpty()) { // 检测登录是否有效
            this.launchResult = LaunchResult.launchUnsuccessfully(ErrorType.BAD_LOGIN, authInfo.getError());
            return null;
        } else {
            for (String path : Utils.resolveRealNativePaths(this, option.getVersion().getNatives())) {
                try {
                    Utils.uncompressZipFile(new File(path), this.baseOptions.getGameRoot() + "/natives");
                } catch (Exception e) {
                    this.launchResult = LaunchResult.launchUnsuccessfully(ErrorType.UNCOMPRESS_ERROR, String.format("解压\'%s\'时出现错误", path), e);
                    return null;
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
            return new LaunchArgument(option, tokens, ADV_ARGS, !System.getProperty("java.version").contains("1.9."), Utils.resolveRealLibPaths(this, option.getVersion().getLibraries()),
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
