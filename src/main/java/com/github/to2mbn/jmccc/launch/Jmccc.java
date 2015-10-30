package com.github.to2mbn.jmccc.launch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONException;
import com.github.to2mbn.jmccc.Launcher;
import com.github.to2mbn.jmccc.auth.AuthResult;
import com.github.to2mbn.jmccc.exec.DaemonStreamPumpMonitor;
import com.github.to2mbn.jmccc.exec.LoggingMonitor;
import com.github.to2mbn.jmccc.exec.GameProcessListener;
import com.github.to2mbn.jmccc.exec.ProcessMonitor;
import com.github.to2mbn.jmccc.option.LaunchOption;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Library;
import com.github.to2mbn.jmccc.version.Native;
import com.github.to2mbn.jmccc.version.Version;

public class Jmccc implements Launcher {

    /**
     * Gets a launcher.
     * 
     * @return the launcher
     */
    public static Launcher getLauncher() {
        return new Jmccc();
    }

    private VersionParser versionParser = new VersionParser();

    @Override
    public LaunchResult launch(LaunchOption option) throws LaunchException {
        Objects.requireNonNull(option);
        return launch(option, null);
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

        // null if the 'versions' dir not exists
        File[] subdirs = minecraftDir.getVersions().listFiles();
        if (subdirs != null) {
            for (File file : subdirs) {
                if (file.isDirectory() && doesVersionExists(minecraftDir, file.getName())) {
                    versions.add(file.getName());
                }
            }
        }
        return versions;
    }

    @Override
    public LaunchResult launch(LaunchOption option, GameProcessListener listener) throws LaunchException {
        return launch(generateLaunchArgs(option), listener);
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
                    uncompressZipWithExcludes(libraryFile, nativesDir, ((Native) library).getExtractExcludes());
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

    private void uncompressZipWithExcludes(File zip, File outputDir, Set<String> excludes) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            byte[] buf = null;

            while ((entry = in.getNextEntry()) != null) {
                boolean excluded = false; // true if the file is in excludes list
                if (excludes != null) {
                    for (String exclude : excludes) {
                        if (entry.getName().startsWith(exclude)) {
                            excluded = true;
                            break;
                        }
                    }
                }

                if (!excluded) {
                    // 1 unused byte for sentinel
                    if (buf == null || buf.length < entry.getSize() - 1) {
                        buf = new byte[(int) entry.getSize() + 1];
                    }
                    int len = 0;
                    int read;
                    // read the zipped data fully
                    while ((read = in.read(buf, len, buf.length - len)) != -1) {
                        if (read == 0) {
                            // reach the sentinel
                            throw new IOException("actual length and entry length mismatch");
                        }
                        len += read;
                    }

                    File outFile = new File(outputDir, entry.getName());
                    boolean match; // true if two files are the same
                    if (outFile.isFile() && outFile.length() == entry.getSize()) {
                        // same length, check the content
                        match = true;
                        try (InputStream targetin = new BufferedInputStream(new FileInputStream(outFile))) {
                            for (int i = 0; i < len; i++) {
                                if (buf[i] != (byte) targetin.read()) {
                                    match = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        // different length
                        match = false;
                    }

                    if (!match) {
                        try (OutputStream out = new FileOutputStream(outFile)) {
                            out.write(buf, 0, len);
                        }
                    }
                }

                in.closeEntry();
            }
        }

    }

}
