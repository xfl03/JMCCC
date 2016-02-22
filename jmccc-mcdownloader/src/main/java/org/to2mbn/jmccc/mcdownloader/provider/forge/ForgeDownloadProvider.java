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
import org.to2mbn.jmccc.mcdownloader.download.AbstractDownloadCallback;
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
		final ResolvedForgeVersion forgeVersion = ResolvedForgeVersion.resolve(version);
		if (forgeVersion == null) {
			throw new IllegalArgumentException("Not in a valid forge version format");
		}
		try {
			return CombinedDownloadTask.single(new MemoryDownloadTask(new URI(forgeInstallerUrl(forgeVersion))).andThen(new InstallProfileProcessor(mcdir)));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
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
				if (decompressUniversalFromInstaller()) {
					return universalFromInstaller(forgeVersion, target);
				} else {
					try {
						return CombinedDownloadTask.single(new FileDownloadTask(new URI(forgeUniversalUrl(forgeVersion)), target));
					} catch (URISyntaxException e) {
						// ignore
						e.printStackTrace();
					}
				}
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
											if (decompressUniversalFromInstaller()) {
												context.submit(universalFromInstaller(forgeVersion, target), new AbstractCombinedDownloadCallback<Void>() {

													@Override
													public void done(Void result) {
														context.done(null);
													}
												}, true);
											} else {
												downloadOldForgeUniversal(forgeVersion, target, context);
											}
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
			throw new IllegalArgumentException("Not in a valid forge version format");
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

	private CombinedDownloadTask<Void> universalFromInstaller(ResolvedForgeVersion version, File target) {
		try {
			return CombinedDownloadTask.single(new MemoryDownloadTask(new URI(forgeInstallerUrl(version))).andThen(new UniversalDecompressor(target, version)));
		} catch (URISyntaxException e) {
			// ignore
			e.printStackTrace();
		}
		return null;
	}

	private void downloadOldForgeUniversal(final ResolvedForgeVersion version, final File target, final CombinedDownloadContext<Void> context) throws InterruptedException, URISyntaxException {
		context.submit(new FileDownloadTask(new URI(forgeUniversalUrl(version)), target), new AbstractDownloadCallback<Void>() {

			@Override
			public void done(Void result) {
				context.done(null);
			}

			@Override
			public void failed(final Throwable e) {
				if (e instanceof IOException) {
					try {
						context.submit(new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								context.submit(new FileDownloadTask(new URI(forgeOldUniversalUrl(version)), target), new AbstractDownloadCallback<Void>() {

									@Override
									public void done(Void result) {
										context.done(null);
									}

									@Override
									public void failed(Throwable e1) {
										e1.addSuppressed(e);
										context.failed(e1);
									}

									@Override
									public void cancelled() {
										context.cancelled();
									}

								}, false);
								return null;
							}
						}, null, true);
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				} else {
					context.failed(e);
				}
			}

			@Override
			public void cancelled() {
				context.cancelled();
			}

		}, false);
	}

	protected String forgeVersionListUrl() {
		return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
	}

	protected String forgeInstallerUrl(ResolvedForgeVersion version) {
		return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-installer.jar";
	}
	
	protected String forgeUniversalUrl(ResolvedForgeVersion version) {
		return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-universal.jar";
	}

	protected String forgeOldUniversalUrl(ResolvedForgeVersion version) {
		return "http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-universal.zip";
	}

	protected boolean decompressUniversalFromInstaller(){
		return false;
	}

}
