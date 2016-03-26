package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
		}).cacheable());
	}

	@Override
	public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, String version) {
		final ResolvedForgeVersion forgeInfo = ResolvedForgeVersion.resolve(version);

		if (forgeInfo != null) {
			return forgeVersion(forgeInfo.getForgeVersion()).andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<String>>() {

				@Override
				public CombinedDownloadTask<String> process(final ForgeVersion forgeVersion) throws Exception {
					return CombinedDownloadTask.any(installer(resolveFullVersion(forgeVersion)).andThen(new InstallProfileProcessor(mcdir)),
							upstreamProvider.gameVersionJson(mcdir, forgeVersion.getMinecraftVersion()).andThen(new ResultProcessor<String, String>() {

						// for old forge versions
						@Override
						public String process(String superversion) throws Exception {
							return createForgeVersionJson(mcdir, forgeVersion);
						}
					}));
				}
			});
		}

		return null;
	}

	@Override
	public CombinedDownloadTask<Void> library(final MinecraftDirectory mcdir, final Library library) {
		if ("net.minecraftforge".equals(library.getDomain())) {
			if ("forge".equals(library.getName())) {
				return universalTask(library.getVersion(), mcdir.getLibrary(library));
			} else if ("minecraftforge".equals(library.getName())) {
				return forgeVersion(library.getVersion()).andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<Void>>() {

					@Override
					public CombinedDownloadTask<Void> process(ForgeVersion version) throws Exception {
						return universalTask(resolveFullVersion(version), mcdir.getLibrary(library));
					}
				});
			}
		}
		return null;
	}

	@Override
	public CombinedDownloadTask<Void> gameJar(final MinecraftDirectory mcdir, final Version version) {
		final ResolvedForgeVersion forgeInfo = ResolvedForgeVersion.resolve(version.getRoot());
		if (forgeInfo == null) {
			return null;
		}

		// the very old forge is
		// the forge versions that uses merging jar to install(before 1.5.2)
		boolean isVeryOldForge = true;
		for (Library library : version.getLibraries()) {
			if (library.getDomain().equals("net.minecraftforge")) {
				isVeryOldForge = false;
				break;
			}
		}

		CombinedDownloadTask<Version> baseTask = upstreamProvider.gameVersionJson(mcdir, forgeInfo.getMinecraftVersion()).andThenDownload(new ResultProcessor<String, CombinedDownloadTask<Version>>() {

			@Override
			public CombinedDownloadTask<Version> process(String resolvedMcversion) throws Exception {
				final Version superversion = Versions.resolveVersion(mcdir, resolvedMcversion);
				return upstreamProvider.gameJar(mcdir, superversion).andThenReturn(superversion);
			}
		});

		if (isVeryOldForge) {
			final File universalFile = mcdir.getLibrary(new Library("net.minecraftforge", "minecraftforge", forgeInfo.getForgeVersion(), null));
			return baseTask.andThenDownload(new ResultProcessor<Version, CombinedDownloadTask<Version>>() {

				@Override
				public CombinedDownloadTask<Version> process(final Version superVersion) throws Exception {
					return forgeVersion(forgeInfo.getForgeVersion()).andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<Version>>() {

						@Override
						public CombinedDownloadTask<Version> process(ForgeVersion forgeVersion) throws Exception {
							return universal(resolveFullVersion(forgeVersion), universalFile).andThenReturn(superVersion);
						}
					});
				}
			}).andThenCall(new ResultProcessor<Version, Callable<Void>>() {

				@Override
				public Callable<Void> process(final Version superVersion) throws Exception {
					return new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							File target = mcdir.getVersionJar(version);
							FileUtils.prepareWrite(target);
							try (ZipInputStream in = new ZipInputStream(new FileInputStream(mcdir.getVersionJar(superVersion)));
									ZipInputStream universalIn = new ZipInputStream(new FileInputStream(universalFile));
									ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));) {
								ZipEntry entry;
								byte[] buf = new byte[8192];
								int read;

								Set<String> universalEntries = new HashSet<>();

								while ((entry = universalIn.getNextEntry()) != null) {
									universalEntries.add(entry.getName());
									out.putNextEntry(entry);
									while ((read = universalIn.read(buf)) != -1) {
										out.write(buf, 0, read);
									}
									out.closeEntry();
									universalIn.closeEntry();
								}

								while ((entry = in.getNextEntry()) != null) {
									if (!entry.getName().startsWith("META-INF/") && !universalEntries.contains(entry.getName())) {
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
		} else {
			return baseTask.andThenCall(new ResultProcessor<Version, Callable<Void>>() {

				@Override
				public Callable<Void> process(final Version superVersion) throws Exception {
					return new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							File target = mcdir.getVersionJar(version);
							FileUtils.prepareWrite(target);
							try (ZipInputStream in = new ZipInputStream(new FileInputStream(mcdir.getVersionJar(superVersion)));
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

	protected CombinedDownloadTask<byte[]> installer(String fullVersion) {
		return CombinedDownloadTask.single(new MemoryDownloadTask("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + fullVersion + "/forge-" + fullVersion + "-installer.jar")
				.cacheable());
	}

	protected CombinedDownloadTask<Void> universal(String fullVersion, File target) {
		return CombinedDownloadTask.any(
				new FileDownloadTask("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + fullVersion + "/forge-" + fullVersion + "-universal.jar", target),
				new FileDownloadTask("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + fullVersion + "/forge-" + fullVersion + "-universal.zip", target));
	}

	private CombinedDownloadTask<Void> universalTask(String fullVersion, File target) {
		if (decompressUniversalFromInstaller()) {
			return installer(fullVersion).andThen(new UniversalDecompressor(target, fullVersion));
		} else {
			return universal(fullVersion, target);
		}
	}

	private String resolveFullVersion(ForgeVersion forge) {
		String downloadname = forge.getMinecraftVersion() + "-" + forge.getForgeVersion();
		String branch = forge.getBranch();
		if (branch != null) {
			downloadname += "-" + branch;
		}
		return downloadname;
	}

	private CombinedDownloadTask<ForgeVersion> forgeVersion(final String forgeVersion) {
		return forgeVersionList().andThen(new ResultProcessor<ForgeVersionList, ForgeVersion>() {

			@Override
			public ForgeVersion process(ForgeVersionList versionList) throws Exception {
				ForgeVersion forge = versionList.get(forgeVersion);
				if (forge == null) {
					throw new IllegalArgumentException("Forge version not found: " + forgeVersion);
				}
				return forge;
			}
		});
	}

	private String createForgeVersionJson(MinecraftDirectory mcdir, ForgeVersion forgeVersion) throws IOException, JSONException {
		String versionId = forgeVersion.getVersionName();

		JSONObject versionjson;
		try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(mcdir.getVersionJson(forgeVersion.getMinecraftVersion()))), "UTF-8")) {
			versionjson = new JSONObject(new JSONTokener(reader));
		}

		versionjson.remove("downloads");
		versionjson.remove("assets");
		versionjson.remove("assetIndex");
		versionjson.put("id", versionId);
		versionjson.put("mainClass", "net.minecraft.client.Minecraft");

		File target = mcdir.getVersionJson(versionId);
		FileUtils.prepareWrite(target);
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(target)), "UTF-8")) {
			writer.write(versionjson.toString(4));
		}
		return versionId;
	}

}
