package org.to2mbn.jmccc.mcdownloader.provider.forge;

import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.cache.CacheNames;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.*;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.FileUtils;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ForgeDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {

    public static final String FORGE_GROUP_ID = "net.minecraftforge";
    public static final String FORGE_ARTIFACT_ID = "forge";
    public static final String FORGE_OLD_ARTIFACT_ID = "minecraftforge";
    public static final String CLASSIFIER_INSTALLER = "installer";
    public static final String CLASSIFIER_UNIVERSAL = "universal";
    public static final String MINECRAFT_MAINCLASS = "net.minecraft.client.Minecraft";

    private static final String[] UNIVERSAL_TYPES = new String[]{"jar", "zip"};

    private final ForgeDownloadSource source;

    private MinecraftDownloadProvider upstreamProvider;

    public ForgeDownloadProvider() {
        this(new DefaultForgeDownloadSource());
    }

    public ForgeDownloadProvider(ForgeDownloadSource source) {
        if (source == null) {
            source = new DefaultForgeDownloadSource();
        }
        this.source = source;
    }

    public CombinedDownloadTask<ForgeVersionList> forgeVersionList() {
        return CombinedDownloadTask.all(
                new MemoryDownloadTask(source.getForgeMetadataUrl())
                        .andThen(new JsonDecoder())
                        .cacheable()
                        .cachePool(CacheNames.FORGE_VERSION_META),
                new MemoryDownloadTask(source.getForgePromotionUrl())
                        .andThen(new JsonDecoder())
                        .cacheable()
                        .cachePool(CacheNames.FORGE_VERSION_PROMO)
        ).andThen(it -> ForgeVersionList.fromJson((JSONObject) it[0], (JSONObject) it[1]));
    }

    @Override
    public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, String version) {
        final ResolvedForgeVersion forgeInfo = ResolvedForgeVersion.resolve(version);

        if (forgeInfo != null) {
            // for old forge versions
            return forgeVersion(forgeInfo.getForgeVersion())
                    .andThenDownload(forge -> CombinedDownloadTask.any(
                            installerTask(forge.getMavenVersion())
                                    .andThen(new InstallProfileProcessor(mcdir)),
                            upstreamProvider.gameVersionJson(mcdir, forge.getMinecraftVersion())
                                    .andThen(superversion -> createForgeVersionJson(mcdir, forge))
                                    .andThen(new VersionJsonInstaller(mcdir))));
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
                        .andThenDownload(version -> universalTask(version.getMavenVersion(), mcdir.getLibrary(library)));
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
                    .andThenDownload(forge -> downloadSuperVersion(mcdir, forge.getMinecraftVersion()));
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
                    .andThenDownload(superVersion -> forgeVersion(forgeInfo.getForgeVersion())
                            .andThenDownload(forge -> universalTask(forge.getMavenVersion(), universalFile)
                                    .andThenReturn(superVersion)))
                    .andThen(superVersion -> {
                        mergeJar(mcdir.getVersionJar(superVersion), universalFile, targetJar);
                        return null;
                    });
        } else {
            // copy its superversion's jar
            // remove META-INF
            return baseTask.andThen(superVersion -> {
                purgeMetaInf(mcdir.getVersionJar(superVersion), targetJar);
                return null;
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
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(parent.toPath()));
             ZipInputStream universalIn = new ZipInputStream(Files.newInputStream(universal.toPath()));
             ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(target.toPath()))) {
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
                if (isNotMetaInfEntry(entry) && !universalEntries.contains(entry.getName())) {
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
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(src.toPath()));
             ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(target.toPath()))) {
            ZipEntry entry;
            byte[] buf = new byte[8192];
            int read;
            while ((entry = in.getNextEntry()) != null) {
                if (isNotMetaInfEntry(entry)) {
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
                .andThen(versionList -> {
                    ForgeVersion forge = versionList.get(forgeVersion);
                    if (forge == null) {
                        throw new IllegalArgumentException("Forge version not found: " + forgeVersion);
                    }
                    return forge;
                });
    }

    private boolean isNotMetaInfEntry(ZipEntry entry) {
        return !entry.getName().startsWith("META-INF/");
    }

    private CombinedDownloadTask<Version> downloadSuperVersion(final MinecraftDirectory mcdir, String version) {
        return upstreamProvider.gameVersionJson(mcdir, version)
                .andThenDownload(resolvedMcversion -> {
                    final Version superversion = Versions.resolveVersion(mcdir, resolvedMcversion);
                    return upstreamProvider.gameJar(mcdir, superversion).andThenReturn(superversion);
                });
    }

}
