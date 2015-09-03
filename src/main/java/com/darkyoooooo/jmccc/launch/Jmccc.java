package com.darkyoooooo.jmccc.launch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.darkyoooooo.jmccc.Launcher;
import com.darkyoooooo.jmccc.auth.AuthResult;
import com.darkyoooooo.jmccc.ext.GameProcessMonitor;
import com.darkyoooooo.jmccc.ext.IGameListener;
import com.darkyoooooo.jmccc.option.LaunchOption;
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.Library;
import com.darkyoooooo.jmccc.version.Version;
import com.google.gson.JsonSyntaxException;

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
     * @return the launcher
     * @see Jmccc#getLauncher()
     * @see Launcher#setReport(boolean)
     * @see Reporter
     */
    public static Launcher getLauncher(String extendedIdentity) {
        return new Jmccc(extendedIdentity);
    }

    private Jmccc(String extendedIdentity) {
        // TODO creates a reporter with the given extended identity
    }

    @Override
    public LaunchResult launch(LaunchOption option) throws LaunchException {
        Objects.requireNonNull(option);
        return launch(generateLaunchArgs(option), null);
    }

    @Override
    public LaunchResult launch(LaunchOption option, IGameListener listener) throws LaunchException {
        Objects.requireNonNull(option);
        return launch(generateLaunchArgs(option), listener);
    }

    @Override
    public Version getVersion(File minecraftDir, String version) throws JsonSyntaxException, IOException {
        Objects.requireNonNull(minecraftDir);
        if (version == null) {
            return null;
        }

        if (doesVersionExists(minecraftDir, version)) {
            return new Version(minecraftDir, version);
        } else {
            return null;
        }
    }

    @Override
    public Set<String> getVersions(File minecraftDir) {
        Objects.requireNonNull(minecraftDir);

        Set<String> versions = new HashSet<>();
        for (File file : new File(minecraftDir, "versions").listFiles()) {
            if (file.isDirectory() && doesVersionExists(minecraftDir, file.getName())) {
                versions.add(file.getName());
            }
        }
        return versions;
    }

    @Override
    public void setReport(boolean on) {
        // TODO sets the report mode to on or off
    }

    private boolean doesVersionExists(File minecraftDir, String version) {
        File versionsDir = new File(minecraftDir, "versions");
        File versionDir = new File(versionsDir, version);
        File versionJsonFile = new File(versionDir, version + ".json");
        return versionJsonFile.isFile();
    }

    private LaunchResult launch(LaunchArgument arg, IGameListener listener) throws LaunchException {
        Process process;
        try {
            process = Runtime.getRuntime().exec(arg.toString(), null, arg.getLaunchOption().getEnvironmentOption().getMinecraftDir());
        } catch (SecurityException | IOException e) {
            throw new LaunchException("Failed to start process", e);
        }

        GameProcessMonitor monitor = null;
        if (listener != null) {
            monitor = new GameProcessMonitor(process, listener);
            monitor.monitor();
        }

        return new LaunchResult(monitor, process);
    }

    private LaunchArgument generateLaunchArgs(LaunchOption option) throws LaunchException {
        // check libraries
        Set<Library> missing = option.getVersion().findMissingLibraries();
        if (!missing.isEmpty()) {
            throw new MissingDependenciesException(missing.toString());
        }

        AuthResult auth = option.getAuthenticator().auth();

        Set<File> javaLibraries = new HashSet<>();
        File nativesDir = getNativesDir(option);
        for (Library library : option.getVersion().getLibraries()) {
            File libraryFile = getLibraryFile(library, option);
            if (library.isNatives()) {
                try {
                    Utils.uncompressZipWithExcludes(libraryFile, nativesDir, library.getExtractExcludes());
                } catch (IOException e) {
                    throw new UncompressException("Failed to uncompress " + libraryFile, e);
                }
            } else {
                javaLibraries.add(libraryFile);
            }
        }

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
        return new LaunchArgument(option, tokens, option.getExtraArguments(), Utils.isCGCSupported(), javaLibraries, nativesDir);
    }

    private File getLibraryFile(Library library, LaunchOption option) {
        return new File(option.getEnvironmentOption().getMinecraftDir(), library.getPath());
    }

    private File getNativesDir(LaunchOption option) {
        return new File(option.getEnvironmentOption().getMinecraftDir(), "natives");
    }

}
