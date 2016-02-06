package org.to2mbn.jmccc.launch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.exec.DaemonStreamPumpMonitor;
import org.to2mbn.jmccc.exec.GameProcessListener;
import org.to2mbn.jmccc.exec.LoggingMonitor;
import org.to2mbn.jmccc.exec.ProcessMonitor;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Native;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

public class Jmccc implements Launcher {

	/**
	 * Gets a launcher.
	 * 
	 * @return the launcher
	 */
	public static Launcher getLauncher() {
		return new Jmccc();
	}

	private Jmccc() {
	}

	@Override
	public LaunchResult launch(LaunchOption option) throws LaunchException {
		return launch(option, null);
	}

	@Override
	public LaunchResult launch(LaunchOption option, GameProcessListener listener) throws LaunchException {
		return launch(generateLaunchArgs(option), listener);
	}

	private LaunchResult launch(LaunchArgument arg, GameProcessListener listener) throws LaunchException {
		Process process;

		ProcessBuilder processBuilder = new ProcessBuilder(arg.generateCommandline());
		processBuilder.directory(arg.getLaunchOption().getGameDirectory().getRoot());

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
		Objects.requireNonNull(option);
		MinecraftDirectory mcdir = option.getMinecraftDirectory();
		Version version = option.getVersion();

		// check libraries
		Set<Library> missing = version.getMissingLibraries(mcdir);
		if (!missing.isEmpty()) {
			throw new MissingDependenciesException(missing.toString());
		}

		Set<File> javaLibraries = new HashSet<>();
		File nativesDir = mcdir.getNatives(version.getVersion());
		for (Library library : version.getLibraries()) {
			File libraryFile = new File(mcdir.getLibraries(), library.getPath());
			if (library instanceof Native) {
				try {
					uncompressZipWithExcludes(libraryFile, nativesDir, ((Native) library).getExtractExcludes());
				} catch (IOException e) {
					throw new LaunchException("Failed to uncompress " + libraryFile, e);
				}
			} else {
				javaLibraries.add(libraryFile);
			}
		}

		if (version.isLegacy()) {
			try {
				buildLegacyAssets(mcdir, version);
			} catch (IOException e) {
				throw new LaunchException("Failed to build virtual assets", e);
			}
		}

		AuthInfo auth = option.getAuthenticator().auth();

		Map<String, String> tokens = new HashMap<String, String>();
		String token = auth.getToken();
		String assetsDir = version.isLegacy() ? mcdir.getVirtualLegacyAssets().toString() : mcdir.getAssets().toString();
		tokens.put("assets_root", assetsDir);
		tokens.put("game_assets", assetsDir);
		tokens.put("auth_access_token", token);
		tokens.put("auth_session", token);
		tokens.put("auth_player_name", auth.getUsername());
		tokens.put("auth_uuid", auth.getUUID());
		tokens.put("user_type", auth.getUserType());
		tokens.put("user_properties", auth.getProperties());
		tokens.put("version_name", version.getVersion());
		tokens.put("assets_index_name", version.getAssets());
		tokens.put("game_directory", option.getGameDirectory().toString());

		String type = version.getType();
		if (type != null) {
			tokens.put("version_type", type);
		}

		return new LaunchArgument(option, tokens, option.getExtraArguments(), javaLibraries, nativesDir);
	}

	private void buildLegacyAssets(MinecraftDirectory mcdir, Version version) throws IOException {
		Set<Asset> assets = Versions.resolveAssets(mcdir, version.getAssets());
		if (assets != null) {
			for (Asset asset : assets) {
				copyFile(new File(mcdir.getAssetObjects(), asset.getPath()), new File(mcdir.getVirtualLegacyAssets(), asset.getVirtualPath()));
			}
		}
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

	private void copyFile(File src, File target) throws IOException {
		File parent = target.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}

		try (FileInputStream in = new FileInputStream(src); FileOutputStream out = new FileOutputStream(target)) {
			FileChannel chin = in.getChannel();
			FileChannel chout = out.getChannel();
			chin.transferTo(0, chin.size(), chout);
		}
	}

}
