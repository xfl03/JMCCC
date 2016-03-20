package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.AbstractMinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.ExtendedDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.InstallProfileProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.FileUtils;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

public class ForgeDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {

	private MinecraftDownloadProvider upstreamProvider;

	public CombinedDownloadTask<ForgeVersionList> forgeVersionList() {
		return CombinedDownloadTask.single(new MemoryDownloadTask(forgeVersionListUrl()).andThen(new ResultProcessor<byte[], ForgeVersionList>() {

			@Override
			public ForgeVersionList process(byte[] arg) throws IOException {
				return ForgeVersionList.fromJson(new JSONObject(new String(arg, "UTF-8")));
			}
		}));
	}

	@Override
	public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, String version) {
		final ResolvedForgeVersion forgeVersion = ResolvedForgeVersion.resolve(version);

		if (forgeVersion != null) {
			return forgeVersion(forgeVersion).andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<String>>() {

				@Override
				public CombinedDownloadTask<String> process(ForgeVersion arg) throws Exception {
					return installerTask(arg).andThen(new InstallProfileProcessor(mcdir));
				}
			});
		}

		return null;
	}

	@Override
	public CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, final Library library) {
		if ("net.minecraftforge".equals(library.getDomain())) {
			final String libraryVersion = library.getVersion();
			final File target = mcdir.getLibrary(library);

			if ("forge".equals(library.getName())) {
				final ResolvedForgeVersion forgeVersion = ResolvedForgeVersion.resolveShort(libraryVersion);
				if (forgeVersion == null) {
					throw new IllegalArgumentException("Not in a valid forge library version format");
				}
				return universalTask(forgeVersion.getForgeVersion(), target);
			} else if ("minecraftforge".equals(library.getName())) {
				return universalTask(libraryVersion, target);
			}
		}
		return null;
	}

	@Override
	public CombinedDownloadTask<Void> gameJar(final MinecraftDirectory mcdir, final Version version) {
		ResolvedForgeVersion forgeVersion = ResolvedForgeVersion.resolve(version.getRoot());
		if (forgeVersion == null) {
			return null;
		}

		final String mcversion = forgeVersion.getMinecraftVersion();
		return upstreamProvider.gameVersionJson(mcdir, mcversion).andThenDownload(new ResultProcessor<String, CombinedDownloadTask<Version>>() {

			@Override
			public CombinedDownloadTask<Version> process(String resolvedMcversion) throws Exception {
				final Version superversion = Versions.resolveVersion(mcdir, resolvedMcversion);
				return upstreamProvider.gameJar(mcdir, superversion).andThen(new ResultProcessor<Void, Version>() {

					@Override
					public Version process(Void arg) throws Exception {
						return superversion;
					}
				});
			}
		}).andThenCall(new ResultProcessor<Version, Callable<Void>>() {

			@Override
			public Callable<Void> process(final Version superversion) throws Exception {
				return new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						File target = mcdir.getVersionJar(version);
						FileUtils.prepareWrite(target);
						try (ZipInputStream in = new ZipInputStream(new FileInputStream(mcdir.getVersionJar(superversion)));
								ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));) {
							ZipEntry entry;
							byte[] buf = new byte[8192];
							int read;
							while ((entry = in.getNextEntry()) != null) {
								if (!entry.getName().startsWith("META-INF/")) {
									out.putNextEntry(entry);
									while ((read = in.read(buf)) != -1) {
										out.write(buf, 0, read);
									}
									out.closeEntry();
								}
								in.closeEntry();
							}
						}
						return null;
					}
				};
			}
		});
	}

	@Override
	public void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider) {
		this.upstreamProvider = upstreamProvider;
	}

	protected boolean decompressUniversalFromInstaller() {
		return false;
	}

	protected String forgeVersionListUrl() {
		return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
	}

	protected CombinedDownloadTask<byte[]> installerTask(ForgeVersion version) {
		String downloadname = resolveForgeDownloadName(version);
		return CombinedDownloadTask.single(new MemoryDownloadTask("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + downloadname + "/forge-" + downloadname + "-installer.jar"));
	}

	protected CombinedDownloadTask<Void> directUniversalTask(ForgeVersion version, File target) {
		String downloadname = resolveForgeDownloadName(version);
		return CombinedDownloadTask.any(
				new FileDownloadTask("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + downloadname + "/forge-" + downloadname + "-universal.jar", target),
				new FileDownloadTask("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + downloadname + "/forge-" + downloadname + "-universal.zip", target));
	}

	private CombinedDownloadTask<Void> universalTask(ForgeVersion version, File target) {
		if (decompressUniversalFromInstaller()) {
			return installerTask(version).andThen(new UniversalDecompressor(target, version));
		} else {
			return directUniversalTask(version, target);
		}
	}

	private CombinedDownloadTask<Void> universalTask(String forgeVersion, final File target) {
		return forgeVersion(forgeVersion).andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<Void>>() {

			@Override
			public CombinedDownloadTask<Void> process(ForgeVersion forge) throws Exception {
				return universalTask(forge, target);
			}
		});
	}

	private String resolveForgeDownloadName(ForgeVersion forge) {
		String downloadname = forge.getMinecraftVersion() + "-" + forge.getForgeVersion();
		String branch = forge.getBranch();
		if (branch != null) {
			downloadname += "-" + branch;
		}
		return downloadname;
	}

	private CombinedDownloadTask<ForgeVersion> forgeVersion(final String ver) {
		return forgeVersionList().andThen(new ResultProcessor<ForgeVersionList, ForgeVersion>() {

			@Override
			public ForgeVersion process(ForgeVersionList versionList) throws Exception {
				ForgeVersion forge = versionList.get(ver);
				if (forge == null) {
					throw new IllegalArgumentException("Forge version not found: " + ver);
				}
				return forge;
			}
		});
	}

	private CombinedDownloadTask<ForgeVersion> forgeVersion(ResolvedForgeVersion ver) {
		return forgeVersion(ver.getForgeVersion());
	}

}
