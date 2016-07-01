package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.cache.CacheNames;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.AbstractMinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.ExtendedDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.JsonDecoder;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.VersionJsonInstaller;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.FileUtils;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;

public class ForgeDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {

	public static final String FORGE_GROUP_ID = "net.minecraftforge";
	public static final String FORGE_ARTIFACT_ID = "forge";
	public static final String FORGE_OLD_ARTIFACT_ID = "minecraftforge";
	public static final String CLASSIFIER_INSTALLER = "installer";
	public static final String CLASSIFIER_UNIVERSAL = "universal";
	public static final String MINECRAFT_MAINCLASS = "net.minecraft.client.Minecraft";

	private static final String[] UNIVERSAL_TYPES = new String[] { "jar", "zip" };

	private ForgeDownloadSource source;

	private MinecraftDownloadProvider upstreamProvider;

	public ForgeDownloadProvider() {
		this(new DefaultForgeDownloadSource());
	}

	public ForgeDownloadProvider(ForgeDownloadSource source) {
		this.source = Objects.requireNonNull(source);
	}

	public CombinedDownloadTask<ForgeVersionList> forgeVersionList() {
		return CombinedDownloadTask.single(
				new MemoryDownloadTask(source.getForgeVersionListUrl())
						.andThen(new JsonDecoder())
						.andThen(new ResultProcessor<JSONObject, ForgeVersionList>() {

							@Override
							public ForgeVersionList process(JSONObject json) throws IOException {
								return ForgeVersionList.fromJson(json);
							}
						})
						.cacheable()
						.cachePool(CacheNames.FORGE_VERSION_LIST));
	}

	@Override
	public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, String version) {
		final ResolvedForgeVersion forgeInfo = ResolvedForgeVersion.resolve(version);

		if (forgeInfo != null) {
			return forgeVersion(forgeInfo.getForgeVersion())
					.andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<String>>() {

						@Override
						public CombinedDownloadTask<String> process(final ForgeVersion forge) throws Exception {
							return CombinedDownloadTask.any(
									installerTask(forge.getMavenVersion())
											.andThen(new InstallProfileProcessor(mcdir)),
									upstreamProvider.gameVersionJson(mcdir, forge.getMinecraftVersion())
											.andThen(new ResultProcessor<String, JSONObject>() {

												// for old forge versions
												@Override
												public JSONObject process(String superversion) throws Exception {
													return createForgeVersionJson(mcdir, forge);
												}
											})
											.andThen(new VersionJsonInstaller(mcdir)));
						}
					});
		}

		return null;
	}

	@Override
	public CombinedDownloadTask<Void> library(final MinecraftDirectory mcdir, final Library library) {
		if (FORGE_GROUP_ID.equals(library.getGroupId())) {

			if (FORGE_ARTIFACT_ID.equals(library.getArtifactId())) {
				return universalTask(library.getVersion(), mcdir.getLibrary(library));

			} else if (FORGE_OLD_ARTIFACT_ID.equals(library.getArtifactId())) {
				return forgeVersion(library.getVersion())
						.andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<Void>>() {

							@Override
							public CombinedDownloadTask<Void> process(ForgeVersion version) throws Exception {
								return universalTask(version.getMavenVersion(), mcdir.getLibrary(library));
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

		boolean mergeJar = true;
		for (Library library : version.getLibraries()) {
			if (library.getGroupId().equals(FORGE_GROUP_ID)) {
				mergeJar = false;
				break;
			}
		}

		// downloads the super version
		CombinedDownloadTask<Version> baseTask;
		if (forgeInfo.getMinecraftVersion() == null) {
			baseTask = forgeVersion(forgeInfo.getForgeVersion())
					.andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<Version>>() {

						@Override
						public CombinedDownloadTask<Version> process(ForgeVersion forge) throws Exception {
							return downloadSuperVersion(mcdir, forge.getMinecraftVersion());
						}
					});
		} else {
			baseTask = downloadSuperVersion(mcdir, forgeInfo.getMinecraftVersion());
		}

		final File targetJar = mcdir.getVersionJar(version);

		if (mergeJar) {
			// downloads the universal
			// copy its superversion's jar
			// remove META-INF
			// copy universal into the jar
			final File universalFile = mcdir.getLibrary(new Library("net.minecraftforge", "minecraftforge", forgeInfo.getForgeVersion()));
			return baseTask
					.andThenDownload(new ResultProcessor<Version, CombinedDownloadTask<Version>>() {

						@Override
						public CombinedDownloadTask<Version> process(final Version superVersion) throws Exception {
							return forgeVersion(forgeInfo.getForgeVersion())
									.andThenDownload(new ResultProcessor<ForgeVersion, CombinedDownloadTask<Version>>() {

										@Override
										public CombinedDownloadTask<Version> process(ForgeVersion forge) throws Exception {
											return universalTask(forge.getMavenVersion(), universalFile)
													.andThenReturn(superVersion);
										}
									});
						}
					})
					.andThen(new ResultProcessor<Version, Void>() {

						@Override
						public Void process(final Version superVersion) throws Exception {
							mergeJar(mcdir.getVersionJar(superVersion), universalFile, targetJar);
							return null;
						}
					});
		} else {
			// copy its superversion's jar
			// remove META-INF
			return baseTask.andThen(new ResultProcessor<Version, Void>() {

				@Override
				public Void process(final Version superVersion) throws Exception {
					purgeMetaInf(mcdir.getVersionJar(superVersion), targetJar);
					return null;
				}
			});
		}
	}

	@Override
	public void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider) {
		this.upstreamProvider = upstreamProvider;
	}

	protected CombinedDownloadTask<byte[]> installerTask(String m2Version) {
		Library lib = new Library(FORGE_GROUP_ID, FORGE_ARTIFACT_ID, m2Version, CLASSIFIER_INSTALLER, "jar");
		return CombinedDownloadTask.single(
				new MemoryDownloadTask(source.getForgeMavenRepositoryUrl() + lib.getPath())
						.cacheable()
						.cachePool(CacheNames.FORGE_INSTALLER));
	}

	protected CombinedDownloadTask<Void> universalTask(String m2Version, File target) {
		String[] types = UNIVERSAL_TYPES;

		@SuppressWarnings("unchecked")
		CombinedDownloadTask<Void>[] tasks = new CombinedDownloadTask[types.length + 1];
		tasks[0] = installerTask(m2Version)
				.andThen(new UniversalDecompressor(target, m2Version));

		for (int i = 0; i < types.length; i++) {
			Library lib = new Library(FORGE_GROUP_ID, FORGE_ARTIFACT_ID, m2Version, CLASSIFIER_UNIVERSAL, types[i]);
			tasks[i + 1] = CombinedDownloadTask.single(
					new FileDownloadTask(source.getForgeMavenRepositoryUrl() + lib.getPath(), target)
							.cachePool(CacheNames.FORGE_UNIVERSAL));
		}

		return CombinedDownloadTask.any(tasks);
	}

	protected JSONObject createForgeVersionJson(MinecraftDirectory mcdir, ForgeVersion forgeVersion) throws IOException, JSONException {
		JSONObject versionjson = IOUtils.toJson(mcdir.getVersionJson(forgeVersion.getMinecraftVersion()));

		versionjson.remove("downloads");
		versionjson.remove("assets");
		versionjson.remove("assetIndex");
		versionjson.put("id", forgeVersion.getVersionName());
		versionjson.put("mainClass", MINECRAFT_MAINCLASS);
		return versionjson;
	}

	protected void mergeJar(File parent, File universal, File target) throws IOException {
		FileUtils.prepareWrite(target);
		try (ZipInputStream in = new ZipInputStream(new FileInputStream(parent));
				ZipInputStream universalIn = new ZipInputStream(new FileInputStream(universal));
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
				if (!isMetaInfEntry(entry) && !universalEntries.contains(entry.getName())) {
					out.putNextEntry(entry);
					while ((read = in.read(buf)) != -1) {
						out.write(buf, 0, read);
					}
					out.closeEntry();
				}
				in.closeEntry();
			}
		}
	}

	protected void purgeMetaInf(File src, File target) throws IOException {
		FileUtils.prepareWrite(target);
		try (ZipInputStream in = new ZipInputStream(new FileInputStream(src));
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));) {
			ZipEntry entry;
			byte[] buf = new byte[8192];
			int read;
			while ((entry = in.getNextEntry()) != null) {
				if (!isMetaInfEntry(entry)) {
					out.putNextEntry(entry);
					while ((read = in.read(buf)) != -1) {
						out.write(buf, 0, read);
					}
					out.closeEntry();
				}
				in.closeEntry();
			}
		}
	}

	private CombinedDownloadTask<ForgeVersion> forgeVersion(final String forgeVersion) {
		return forgeVersionList()
				.andThen(new ResultProcessor<ForgeVersionList, ForgeVersion>() {

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

	private boolean isMetaInfEntry(ZipEntry entry) {
		return entry.getName().startsWith("META-INF/");
	}

	private CombinedDownloadTask<Version> downloadSuperVersion(final MinecraftDirectory mcdir, String version) {
		return upstreamProvider.gameVersionJson(mcdir, version)
				.andThenDownload(new ResultProcessor<String, CombinedDownloadTask<Version>>() {

					@Override
					public CombinedDownloadTask<Version> process(String resolvedMcversion) throws Exception {
						final Version superversion = Versions.resolveVersion(mcdir, resolvedMcversion);
						return upstreamProvider.gameJar(mcdir, superversion).andThenReturn(superversion);
					}
				});
	}

}
