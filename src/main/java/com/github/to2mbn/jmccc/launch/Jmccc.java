package com.github.to2mbn.jmccc.launch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.json.JSONException;
import com.github.to2mbn.jmccc.Launcher;
import com.github.to2mbn.jmccc.auth.AuthResult;
import com.github.to2mbn.jmccc.exec.DaemonStreamPumpMonitor;
import com.github.to2mbn.jmccc.exec.LoggingMonitor;
import com.github.to2mbn.jmccc.exec.GameProcessListener;
import com.github.to2mbn.jmccc.exec.ProcessMonitor;
import com.github.to2mbn.jmccc.option.LaunchOption;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.util.Utils;
import com.github.to2mbn.jmccc.version.Library;
import com.github.to2mbn.jmccc.version.Native;
import com.github.to2mbn.jmccc.version.Version;

public class Jmccc implements Launcher {

    /**
     * Gets a Launcher object with no extended identity.
     * 
     * @return the launcher
     * @see Jmccc#getLauncher(String)
     * @see Launcher#setReport(boolean)
     * @see Reporter
     */
    public static Launcher getLauncher() {
        return getLauncher(null);
    }

    /**
     * Gets a Launcher object with the given extended identity.
     * <p>
     * If <code>extendedIdentity==null</code>, this launcher won't have any extended identity.<br>
     * The extended identity is used to identity the caller of JMCCC, default to null. If you want to help us do the
     * statistics better, please set this to the name and the version of your launcher.
     * 
     * @param extendedIdentity the extended identity
     * @return the launcher
     * @see Jmccc#getLauncher()
     * @see Launcher#setReport(boolean)
     * @see Reporter
     */
    public static Launcher getLauncher(String extendedIdentity) {
        return new Jmccc(extendedIdentity);
    }

    private Reporter reporter;
    private VersionParser versionParser = new VersionParser();
    private boolean reportmode = true;

    private Jmccc(String extendedIdentity) {
        reporter = new Reporter(extendedIdentity);
    }

    @Override
    public LaunchResult launch(LaunchOption option) throws LaunchException {
        Objects.requireNonNull(option);
        return launch(option, null);
    }

    @Override
    public LaunchResult launch(LaunchOption option, GameProcessListener listener) throws LaunchException {
        Objects.requireNonNull(option);
        if (reportmode) {
            return launchWithReport(option, listener);
        } else {
            return launchWithoutReport(option, listener);
        }
    }

    @Override
    public Version getVersion(MinecraftDirectory minecraftDir, String version) throws JSONException, IOException {
        Objects.requireNonNull(minecraftDir);
        if (version == null) {
            return null;
        }

        if (doesVersionExists(minecraftDir, version)) {
            return versionParser.parse(minecraftDir, version);
        } else {
            return null;
        }
    }

    @Override
    public Set<String> getVersions(MinecraftDirectory minecraftDir) {
        Objects.requireNonNull(minecraftDir);

        Set<String> versions = new HashSet<>();
        for (File file : minecraftDir.getVersions().listFiles()) {
            if (file.isDirectory() && doesVersionExists(minecraftDir, file.getName())) {
                versions.add(file.getName());
            }
        }
        return versions;
    }

    @Override
    public void setReport(boolean on) {
        reportmode = on;
    }

    public LaunchResult launchWithoutReport(LaunchOption option, GameProcessListener listener) throws LaunchException {
        return launch(generateLaunchArgs(option), listener);
    }

    public LaunchResult launchWithReport(LaunchOption option, GameProcessListener listener) throws LaunchException {
        try {
            LaunchResult result = launchWithoutReport(option, listener);
            reporter.asyncLaunchSuccessfully(option, result);
            return result;
        } catch (Throwable e) {
            reporter.asyncLaunchUnsuccessfully(option, e);
            throw e;
        }
    }

    private boolean doesVersionExists(MinecraftDirectory minecraftDir, String version) {
        return minecraftDir.getVersionJson(version).isFile();
    }

    private LaunchResult launch(LaunchArgument arg, GameProcessListener listener) throws LaunchException {
        Process process;

        ProcessBuilder processBuilder = new ProcessBuilder(arg.generateCommandline());
        processBuilder.directory(arg.getLaunchOption().getMinecraftDirectory().getRoot());

        try {
            process = processBuilder.start();
        } catch (SecurityException | IOException e) {
            throw new LaunchException("Failed to start process", e);
        }

        ProcessMonitor monitor;
        if (listener == null) {
            monitor = new DaemonStreamPumpMonitor(process);
        } else {
            monitor = new LoggingMonitor(process, listener);
        }
        monitor.start();

        return new LaunchResult(monitor, process);
    }

    private LaunchArgument generateLaunchArgs(LaunchOption option) throws LaunchException {
        // check libraries
        Set<Library> missing = option.getVersion().getMissingLibraries(option.getMinecraftDirectory());
        if (!missing.isEmpty()) {
            throw new MissingDependenciesException(missing.toString());
        }

        Set<File> javaLibraries = new HashSet<>();
        File nativesDir = option.getMinecraftDirectory().getNatives(option.getVersion().getVersion());
        for (Library library : option.getVersion().getLibraries()) {
            File libraryFile = new File(option.getMinecraftDirectory().getLibraries(), library.getPath());
            if (library instanceof Native) {
                try {
                    Utils.uncompressZipWithExcludes(libraryFile, nativesDir, ((Native) library).getExtractExcludes());
                } catch (IOException e) {
                    throw new UncompressException("Failed to uncompress " + libraryFile, e);
                }
            } else {
                javaLibraries.add(libraryFile);
            }
        }

        AuthResult auth = option.getAuthenticator().auth();

        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("auth_access_token", auth.getToken());
        tokens.put("auth_session", auth.getToken());
        tokens.put("auth_player_name", auth.getUsername());
        tokens.put("version_name", option.getVersion().getVersion());
        tokens.put("game_directory", ".");
        tokens.put("assets_root", "assets");
        tokens.put("assets_index_name", option.getVersion().getAssets());
        tokens.put("auth_uuid", auth.getUUID());
        tokens.put("user_type", auth.getUserType());
        tokens.put("user_properties", auth.getProperties());
        return new LaunchArgument(option, tokens, option.getExtraArguments(), javaLibraries, nativesDir);
    }

}
