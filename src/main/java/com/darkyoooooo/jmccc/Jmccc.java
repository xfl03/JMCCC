package com.darkyoooooo.jmccc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.Library;
import com.darkyoooooo.jmccc.version.Version;
import com.google.gson.JsonSyntaxException;

public class Jmccc {

    public Jmccc() {
    }

    /**
     * Launches the game with the given option.
     * <p>
     * If we fail to launch the game, we will throw a {@link LaunchException}.
     * 
     * @param option the launching option
     * @return the launching result
     * @throws LaunchException if we fail to launch the game
     * @throws NullPointerException if <code>option==null</code>
     * @see LaunchResult
     * @see LaunchException
     */
    public LaunchResult launch(LaunchOption option) throws LaunchException {
        Objects.requireNonNull(option);
        return launch(generateLaunchArgs(option), null);
    }

    /**
     * Launches the game with the given option. If <code>listener!=null</code>, JMCCC will create a
     * {@link GameProcessMonitor} to monitor the game process. The logs will be reported to the given listener.
     * Else if <code>listener==null</code>, this method won't create a monitor.
     * <p>
     * The monitor threads won't stop until the game process stops, or {@link GameProcessMonitor#shutdown()} has been
     * called. Also, the jvm won't exit automatically because the monitor threads are not daemon.<br>
     * If we fail to launch the game, we will throw a {@link LaunchException}.
     * 
     * @param option the launching option
     * @param listener the game listener to receive logs from the game
     * @return the launching result
     * @throws LaunchException if we fail to launch the game
     * @throws NullPointerException if <code>option==null</code>
     * @see LaunchResult
     * @see GameProcessMonitor
     * @see IGameListener
     * @see GameProcessMonitor#shutdown()
     * @see LaunchException
     */
    public LaunchResult launch(LaunchOption option, IGameListener listener) throws LaunchException {
        Objects.requireNonNull(option);
        return launch(generateLaunchArgs(option), listener);
    }

    /**
     * Gets the Version object of the given version in the given .minecraft dir.
     * 
     * @param version the version name
     * @return the Version object, null if <code>version==null</code>, or the version does not exist
     * @throws IOException if an I/O exception has occurred during resolving version
     * @throws JsonSyntaxException if an JSON syntax exception has occurred during resolving version json
     * @throws NullPointerException if <code>minecraftDir==null</code>
     * @see Version
     * @see Jmccc#getVersions(File)
     */
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

    /**
     * Gets the names of the versions in the given .minecraft dir.
     * <p>
     * This method returns a non-threaded safe, unordered set.
     * 
     * @param minecraftDir the .minecraft dir
     * @return a set of the names of the versions in the given .minecraft dir
     * @throws NullPointerException if <code>minecraftDir==null</code>
     * @see Jmccc#getVersion(File, String)
     */
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

        AuthInfo authInfo = option.getAuthenticator().get();
        if (authInfo.getError() != null && !authInfo.getError().isEmpty()) {
            throw new LoginException(authInfo.getError());
        } else {
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
            tokens.put("auth_access_token", authInfo.getAccessToken());
            tokens.put("auth_session", authInfo.getAccessToken());
            tokens.put("auth_player_name", authInfo.getDisplayName());
            tokens.put("version_name", option.getVersion().getVersion());
            tokens.put("game_directory", ".");
            tokens.put("assets_root", "assets");
            tokens.put("assets_index_name", option.getVersion().getAssets());
            tokens.put("auth_uuid", authInfo.getUuid());
            tokens.put("user_type", authInfo.getUserType());
            tokens.put("user_properties", authInfo.getProperties());
            return new LaunchArgument(option, tokens, option.getExtraArguments(), Utils.isCGCSupported(), javaLibraries, nativesDir);
        }
    }

    private File getLibraryFile(Library library, LaunchOption option) {
        return new File(option.getEnvironmentOption().getMinecraftDir(), library.getPath());
    }

    private File getNativesDir(LaunchOption option) {
        return new File(option.getEnvironmentOption().getMinecraftDir(), "natives");
    }

}
