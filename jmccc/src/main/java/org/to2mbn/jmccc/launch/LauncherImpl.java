package org.to2mbn.jmccc.launch;

import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.option.WindowSize;
import org.to2mbn.jmccc.util.FileUtils;
import org.to2mbn.jmccc.util.Platform;
import org.to2mbn.jmccc.util.UUIDUtils;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Native;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class LauncherImpl implements Launcher {

    private boolean nativeFastCheck = false;
    private boolean printDebugCommandline = false;
    private boolean useDaemonThreads = false;

    public LauncherImpl() {
    }

    @Override
    public Process launch(LaunchOption option) throws LaunchException {
        return launch(option, null);
    }

    @Override
    public Process launch(LaunchOption option, ProcessListener listener) throws LaunchException {
        return launch(generateLaunchArgs(option), listener);
    }

    public void setNativeFastCheck(boolean nativeFastCheck) {
        this.nativeFastCheck = nativeFastCheck;
    }

    public void setPrintDebugCommandline(boolean printDebugCommandline) {
        this.printDebugCommandline = printDebugCommandline;
    }

    public void setUseDaemonThreads(boolean useDaemonThreads) {
        this.useDaemonThreads = useDaemonThreads;
    }

    private Process launch(LaunchArgument arg, ProcessListener listener) throws LaunchException {
        String[] commandline = arg.generateCommandline();
        if (printDebugCommandline) {
            printDebugCommandline(commandline);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commandline);
        processBuilder.directory(arg.getLaunchOption().getRuntimeDirectory().getRoot());

        Process process;
        try {
            process = processBuilder.start();
        } catch (SecurityException | IOException e) {
            throw new LaunchException("Couldn't start process", e);
        }

        if (listener == null) {
            startStreamPumps(process);
        } else {
            startStreamLoggers(process, listener, useDaemonThreads);
        }

        return process;
    }

    @Override
    public LaunchArgument generateLaunchArgs(LaunchOption option) throws LaunchException {
        Objects.requireNonNull(option);

        if (option.getJavaEnvironment() == null) {
            throw new IllegalArgumentException("No JavaEnvironment is specified");
        }

        MinecraftDirectory mcdir = option.getMinecraftDirectory();
        Version version = option.getVersion();

        // check libraries
        Set<Library> missing = version.getMissingLibraries(mcdir);
        if (!missing.isEmpty()) {
            throw new MissingDependenciesException(missing);
        }

        Set<File> javaLibraries = new LinkedHashSet<>();
        File nativesDir = mcdir.getNatives(version);
        for (Library library : version.getLibraries()) {
            File libraryFile = mcdir.getLibrary(library);
            if (library instanceof Native) {
                try {
                    decompressZipWithExcludes(libraryFile, nativesDir, ((Native) library).getExtractExcludes());
                } catch (IOException e) {
                    throw new LaunchException("Couldn't uncompress " + libraryFile, e);
                }
            } else {
                javaLibraries.add(libraryFile);
            }
        }
        javaLibraries.add(mcdir.getVersionJar(version));
        javaLibraries.addAll(option.extraClasspath());

        if (version.isLegacy()) {
            try {
                buildLegacyAssets(mcdir, version);
            } catch (IOException e) {
                throw new LaunchException("Couldn't build virtual assets", e);
            }
        }

        AuthInfo auth = option.getAuthenticator().auth();

        Map<String, String> tokens = new HashMap<>();
        String token = auth.getToken();
        String assetsDir = (version.isLegacy() ? mcdir.getVirtualLegacyAssets() : mcdir.getAssets()).getAbsolutePath();
        tokens.put("assets_root", assetsDir);
        tokens.put("game_assets", assetsDir);
        tokens.put("auth_access_token", token);
        tokens.put("auth_session", token);
        tokens.put("auth_player_name", auth.getUsername());
        tokens.put("auth_uuid", UUIDUtils.unsign(auth.getUUID()));
        tokens.put("user_type", auth.getUserType());
        tokens.put("user_properties", new JSONObject(auth.getProperties()).toString());
        tokens.put("version_name", version.getVersion());
        tokens.put("assets_index_name", version.getAssets());
        tokens.put("game_directory", option.getRuntimeDirectory().getAbsolutePath());
        tokens.put("natives_directory", nativesDir.getAbsolutePath());
        tokens.put("library_directory", mcdir.getLibraries().getAbsolutePath());
        tokens.put("classpath_separator", Platform.getPathSeparator());
        tokens.put("auth_xuid", auth.getXboxUserId());

        String type = version.getType();
        if (type != null) {
            tokens.put("version_type", type);
        }

        WindowSize windowSize = option.getWindowSize();
        if (windowSize != null) {
            tokens.put("resolution_width", Integer.toString(windowSize.getWidth()));
            tokens.put("resolution_height", Integer.toString(windowSize.getHeight()));
        }

        return new LaunchArgument(option, tokens, javaLibraries, nativesDir);
    }

    private void buildLegacyAssets(MinecraftDirectory mcdir, Version version) throws IOException {
        Set<Asset> assets = Versions.resolveAssets(mcdir, version);
        if (assets != null)
            for (Asset asset : assets)
                FileUtils.copyFile(mcdir.getAsset(asset), mcdir.getVirtualAsset(asset));
    }

    private void decompressZipWithExcludes(File zip, File outputDir, Set<String> excludes) throws IOException {
        if (!outputDir.exists())
            outputDir.mkdirs();

        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip.toPath()))) {
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
                        if (!nativeFastCheck) {
                            try (InputStream targetin = new BufferedInputStream(Files.newInputStream(outFile.toPath()))) {
                                for (int i = 0; i < len; i++) {
                                    if (buf[i] != (byte) targetin.read()) {
                                        match = false;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // different length
                        match = false;
                    }

                    if (!match) {
                        if (entry.isDirectory()) {
                            outFile.mkdir();//Fix extract directory as file
                        } else {
                            try (OutputStream out = Files.newOutputStream(outFile.toPath())) {
                                out.write(buf, 0, len);
                            }
                        }
                    }
                }

                in.closeEntry();
            }
        }

    }

    private void printDebugCommandline(String[] commandline) {
        StringBuilder sb = new StringBuilder();
        sb.append("jmccc:\n");
        for (String arg : commandline) {
            sb.append(arg).append('\n');
        }
        System.err.println(sb);
    }

    private void startStreamPumps(Process process) {
        startThread("stdout-pump", true, new StreamPump(process.getInputStream()));
        startThread("stderr-pump", true, new StreamPump(process.getErrorStream()));
    }

    private void startStreamLoggers(Process process, ProcessListener listener, boolean daemon) {
        startThread("stdout-logger", daemon, new StreamLogger(listener, false, process.getInputStream()));
        startThread("stderr-logger", daemon, new StreamLogger(listener, true, process.getErrorStream()));
        startThread("exit-waiter", daemon, new ExitWaiter(process, listener));
    }

    private void startThread(String name, boolean daemon, Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setName(name);
        t.setDaemon(daemon);
        t.start();
    }

}
