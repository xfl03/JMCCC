package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.AbstractCombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadContext;
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
		try {
			return CombinedDownloadTask.single(new MemoryDownloadTask(new URI(forgeVersionListUrl())).andThen(new ResultProcessor<byte[], ForgeVersionList>() {

				@Override
				public ForgeVersionList process(byte[] arg) throws IOException {
					return ForgeVersionList.fromJson(new JSONObject(new String(arg, "UTF-8")));
				}
			}));
		} catch (URISyntaxException e) {
			throw new IllegalStateException("unable to convert to URI", e);
		}
	}

	@Override
	public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, final String version) {
		ResolvedForgeVersion forgeVersion = ResolvedForgeVersion.resolve(version);

		if (forgeVersion != null)
			return installerTask(forgeVersion).andThen(new InstallProfileProcessor(mcdir));

		return null;
	}

	@Override
	public CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, final Library library) {
		if ("net.minecraftforge".equals(library.getDomain())) {
			final String libraryVersion = library.getVersion();
			final File target = new File(mcdir.getLibraries(), library.getPath());

			if ("forge".equals(library.getName())) {
				ResolvedForgeVersion forgeVersion = ResolvedForgeVersion.resolveShort(libraryVersion);
				if (forgeVersion == null) {
					throw new IllegalArgumentException("Not in a valid forge library version format");
				}
					return universalTask(forgeVersion, target);
			} else if ("minecraftforge".equals(library.getName())) {

				return new CombinedDownloadTask<Void>() {

					@Override
					public void execute(final CombinedDownloadContext<Void> context) throws Exception {
						context.submit(forgeVersionList(), new AbstractCombinedDownloadCallback<ForgeVersionList>() {

							@Override
							public void done(final ForgeVersionList forgeVersionList) {
								try {
									context.submit(new Callable<Void>() {

										@Override
										public Void call() throws Exception {
											ForgeVersion forgeVersionInfo = forgeVersionList.get(libraryVersion);
											if (forgeVersionInfo == null) {
												throw new IllegalArgumentException("forge version " + libraryVersion + " not found");
											}

											ResolvedForgeVersion forgeVersion = new ResolvedForgeVersion(forgeVersionInfo);
											context.submit(universalTask(forgeVersion, target), new AbstractCombinedDownloadCallback<Void>() {

												@Override
												public void done(Void result) {
													context.done(null);
												}
											}, true);

											return null;
										}
									}, null, true);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
								}
							}
						}, true);
					}
				};
			}
		}
		return null;
	}

	@Override
	public CombinedDownloadTask<Void> gameJar(final MinecraftDirectory mcdir, final Version version) {
		ResolvedForgeVersion forgeVersion = ResolvedForgeVersion.resolve(version.getVersion());
		if (forgeVersion == null) {
			return null;
		}

		final String mcversion = forgeVersion.getMinecraftVersion();
		return new CombinedDownloadTask<Void>() {

			@Override
			public void execute(final CombinedDownloadContext<Void> context) throws Exception {
				context.submit(upstreamProvider.gameVersionJson(mcdir, mcversion), new AbstractCombinedDownloadCallback<String>() {

					@Override
					public void done(final String resolvedMcversion) {
						try {
							context.submit(new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									Version superversion = Versions.resolveVersion(mcdir, resolvedMcversion);
									context.submit(upstreamProvider.gameJar(mcdir, superversion).andThen(new ResultProcessor<Void, Void>() {

										@Override
										public Void process(Void arg) throws Exception {
											File target = mcdir.getVersionJar(version.getRoot());
											FileUtils.prepareWrite(target);
											try (ZipInputStream in = new ZipInputStream(new FileInputStream(mcdir.getVersionJar(resolvedMcversion)));
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
									}), new AbstractCombinedDownloadCallback<Void>() {

										@Override
										public void done(Void result) {
											context.done(null);
										}
									}, true);
									return null;
								}
							}, null, true);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}, true);
			}
		};
	}

	@Override
	public void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider) {
		this.upstreamProvider = upstreamProvider;
	}

	private CombinedDownloadTask<byte[]> installerTask(ResolvedForgeVersion version) {
		String[] urls = forgeInstallerUrls(version);
		@SuppressWarnings("unchecked")
		DownloadTask<byte[]>[] tasks = new DownloadTask[urls.length];
		try {
			for (int i = 0; i < urls.length; i++) {
				tasks[i] = new MemoryDownloadTask(new URI(urls[i]));

			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}

		return CombinedDownloadTask.any(tasks);
	}

	private CombinedDownloadTask<Void> universalTask(ResolvedForgeVersion version, File target) {
		if (decompressUniversalFromInstaller()) {
			return universalFromInstaller(version, target);

		} else {
			String[] urls = forgeUniversalUrls(version);
			@SuppressWarnings("unchecked")
			CombinedDownloadTask<Void>[] tasks = new CombinedDownloadTask[urls.length + 1];
			try {
				for (int i = 0; i < urls.length; i++) {
					tasks[i] = CombinedDownloadTask.single(new FileDownloadTask(new URI(urls[i]), target));
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return null;
			}
			tasks[tasks.length - 1] = universalFromInstaller(version, target);

			return CombinedDownloadTask.any(tasks);
		}
	}

	private CombinedDownloadTask<Void> universalFromInstaller(ResolvedForgeVersion version, File target) {
		UniversalDecompressor processor = new UniversalDecompressor(target, version);
		return installerTask(version).andThen(processor);
	}

	protected String forgeVersionListUrl() {
		return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
	}

	protected String[] forgeInstallerUrls(ResolvedForgeVersion version) {
		return new String[] {
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-installer.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-" + version.getMinecraftVersion() + "/forge-" + version + "-" + version.getMinecraftVersion() + "-installer.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-prerelease/forge-" + version + "-prerelease-installer.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-new/forge-" + version + "-new-installer.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-1710ls/forge-" + version + "-1710ls-installer.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-mc172/forge-" + version + "-mc172-installer.jar"
		};
	}

	protected String[] forgeUniversalUrls(ResolvedForgeVersion version) {
		return new String[] {
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-universal.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-universal.zip",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-" + version.getMinecraftVersion() + "/forge-" + version + "-" + version.getMinecraftVersion() + "-universal.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-prerelease/forge-" + version + "-prerelease-universal.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-new/forge-" + version + "-new-universal.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-1710ls/forge-" + version + "-1710ls-universal.jar",
				"http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "-mc172/forge-" + version + "-mc172-universal.jar"
		};
	}

	protected boolean decompressUniversalFromInstaller() {
		return false;
	}

}
