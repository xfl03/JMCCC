package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.cache.CacheNames;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadContext;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.*;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.jmccc.version.Library;

import java.io.IOException;
import java.util.Set;

public class LiteloaderDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {

    public static final String LITELOADER_GROUP_ID = "com.mumfrey";
    public static final String LITELOADER_ARTIFACT_ID = "liteloader";
    public static final String LITELOADER_TWEAK_CLASS = "com.mumfrey.liteloader.launch.LiteLoaderTweaker";
    public static final String LITELOADER_REPO_URL = "http://dl.liteloader.com/versions/";

    public static final String LAUNCH_WRAPPER_GROUP_ID = "net.minecraft";
    public static final String LAUNCH_WRAPPER_ARTIFACT_ID = "launchwrapper";
    public static final String LAUNCH_WRAPPER_LOWEST_VERSION = "1.7";
    public static final String LAUNCH_WRAPPER_MAINCLASS = "net.minecraft.launchwrapper.Launch";

    private final LiteloaderDownloadSource source;
    private boolean upgradeLaunchWrapper = true;
    private String lowestLaunchWrapperVersion = LAUNCH_WRAPPER_LOWEST_VERSION;

    private final VersionComparator versionComparator = new VersionComparator();
    private MinecraftDownloadProvider upstreamProvider;

    public LiteloaderDownloadProvider() {
        this(new DefaultLiteloaderDownloadSource());
    }

    public LiteloaderDownloadProvider(LiteloaderDownloadSource source) {
        if (source == null) {
            source = new DefaultLiteloaderDownloadSource();
        }
        this.source = source;
    }

    public CombinedDownloadTask<LiteloaderVersionList> liteloaderVersionList() {
        return CombinedDownloadTask.single(new MemoryDownloadTask(source.getLiteloaderManifestUrl())
                .andThen(new JsonDecoder())
                .andThen(LiteloaderVersionList::fromJson)
                .cacheable()
                .cachePool(CacheNames.LITELOADER_VERSION_LIST));
    }

    @Override
    public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, String version) {
        final ResolvedLiteloaderVersion liteloaderInfo = ResolvedLiteloaderVersion.resolve(version);
        if (liteloaderInfo == null) {
            return null;
        }

        // lookup LiteloaderVersion
        // create version json
        return upstreamProvider.gameVersionJson(mcdir, liteloaderInfo.getSuperVersion())
                .andThenDownload(superVersion -> liteloaderVersionList()
                        .andThen(versionList -> {
                            String mcversion = liteloaderInfo.getMinecraftVersion();
                            LiteloaderVersion genericLiteloader = versionList.getLatest(mcversion);
                            if (genericLiteloader == null) {
                                genericLiteloader = versionList.getSnapshot(mcversion);
                            }

                            if (genericLiteloader == null) {
                                throw new IllegalArgumentException("Liteloader version not found: " + liteloaderInfo);
                            }
                            return genericLiteloader.customize(superVersion);
                        }))
                .andThenDownload(liteloader -> {
                    if (liteloader.getLiteloaderVersion().endsWith("-SNAPSHOT")) {
                        // it's a snapshot

                        return source.liteloaderSnapshotVersionJson(liteloader)
                                .andThen(json -> processSnapshotLiteloaderVersion(mcdir, json, liteloader))
                                .andThen(new VersionJsonInstaller(mcdir))
                                .cachePool(CacheNames.LITELOADER_VERSION_JSON);
                    } else {
                        // it's a release
                        return new CombinedDownloadTask<String>() {

                            @Override
                            public void execute(CombinedDownloadContext<String> context) throws Exception {
                                context.done(new VersionJsonInstaller(mcdir).process(createLiteloaderVersion(mcdir, liteloader)));
                            }
                        };
                    }
                });
    }

    @Override
    public CombinedDownloadTask<Void> library(final MinecraftDirectory mcdir, final Library library) {
        final String groupId = library.getGroupId();
        final String artifactId = library.getArtifactId();
        final String version = library.getVersion();

        if (LITELOADER_GROUP_ID.equals(groupId) && LITELOADER_ARTIFACT_ID.equals(artifactId)) {
            if (library.isSnapshotArtifact()) {
                return liteloaderVersionList()
                        .andThenDownload(versionList -> {
                            LiteloaderVersion liteloader = versionList.getSnapshot(
                                    version.substring(0, version.length() - "-SNAPSHOT".length()) // the minecraft version
                            );
                            if (liteloader != null) {
                                final String repo = liteloader.getRepoUrl();
                                if (repo != null) {
                                    return MavenRepositories.snapshotPostfix(groupId, artifactId, version, repo)
                                            .andThenDownload(postfix -> {
                                                Library libToDownload = new Library(groupId, artifactId, version, "release", library.getType());
                                                return CombinedDownloadTask.single(
                                                        new FileDownloadTask(repo + libToDownload.getPath(postfix), mcdir.getLibrary(library))
                                                                .cacheable()
                                                                .cachePool(CacheNames.LIBRARY));
                                            });
                                }
                            }
                            return upstreamProvider.library(mcdir, library);
                        });
            }
        }
        return null;
    }

    @Override
    public void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider) {
        this.upstreamProvider = upstreamProvider;
    }

    public boolean isUpgradeLaunchWrapper() {
        return upgradeLaunchWrapper;
    }

    public void setUpgradeLaunchWrapper(boolean upgradeLaunchWrapper) {
        this.upgradeLaunchWrapper = upgradeLaunchWrapper;
    }

    public String getLowestLaunchWrapperVersion() {
        return lowestLaunchWrapperVersion;
    }

    public void setLowestLaunchWrapperVersion(String lowestLaunchWrapperVersion) {
        this.lowestLaunchWrapperVersion = lowestLaunchWrapperVersion;
    }

    protected JSONObject createLiteloaderVersion(MinecraftDirectory mcdir, LiteloaderVersion liteloader) throws IOException {
        String superVersion = liteloader.getSuperVersion();
        String minecraftVersion = liteloader.getMinecraftVersion();
        String repoUrl = liteloader.getRepoUrl();
        String tweakClass = liteloader.getTweakClass();
        Set<JSONObject> liteloaderLibraries = liteloader.getLibraries();

        JSONObject versionJson = IOUtils.toJson(mcdir.getVersionJson(superVersion));

        String minecraftArguments = String.format("%s --tweakClass %s", versionJson.getString("minecraftArguments"),
                tweakClass == null ? LITELOADER_TWEAK_CLASS : tweakClass);
        JSONArray libraries = new JSONArray();
        JSONObject liteloaderLibrary = new JSONObject();
        liteloaderLibrary.put("name", String.format("%s:%s:%s", LITELOADER_GROUP_ID, LITELOADER_ARTIFACT_ID, minecraftVersion));
        liteloaderLibrary.put("url", repoUrl == null ? LITELOADER_REPO_URL : repoUrl);
        libraries.put(liteloaderLibrary);

        if (liteloaderLibraries != null) {
            for (JSONObject library : liteloaderLibraries) {

                if (upgradeLaunchWrapper) {
                    String name = library.optString("name", null);
                    if (name != null) {
                        String launchwrapperPrefix = LAUNCH_WRAPPER_GROUP_ID + ":" + LAUNCH_WRAPPER_ARTIFACT_ID + ":";
                        if (lowestLaunchWrapperVersion != null && name.startsWith(launchwrapperPrefix)) {
                            String actualVersion = name.substring(launchwrapperPrefix.length());
                            if (versionComparator.compare(actualVersion, lowestLaunchWrapperVersion) < 0) {
                                library.put("name", launchwrapperPrefix + lowestLaunchWrapperVersion);
                            }
                        }
                    }
                }

                libraries.put(library);
            }
        }

        versionJson.put("inheritsFrom", superVersion);
        versionJson.put("minecraftArguments", minecraftArguments);
        versionJson.put("mainClass", LAUNCH_WRAPPER_MAINCLASS);
        versionJson.put("id", generateLiteloaderVersionName(liteloader));
        versionJson.put("libraries", libraries);
        versionJson.remove("downloads");
        versionJson.remove("assets");
        versionJson.remove("assetIndex");
        return versionJson;
    }

    protected JSONObject processSnapshotLiteloaderVersion(MinecraftDirectory mcdir, JSONObject versionJson, LiteloaderVersion liteloader) throws IOException {
        versionJson.put("inheritsFrom", liteloader.getSuperVersion());
        versionJson.put("id", generateLiteloaderVersionName(liteloader));

        final String TWEAK_CLASS_ARG_PREFIX = "--tweakClass ";
        String minecraftArguments = versionJson.getString("minecraftArguments");
        int tweakArgIdx = minecraftArguments.lastIndexOf(TWEAK_CLASS_ARG_PREFIX);
        String tweakClass = tweakArgIdx == -1
                ? LITELOADER_TWEAK_CLASS
                : minecraftArguments.substring(tweakArgIdx + TWEAK_CLASS_ARG_PREFIX.length());

        JSONObject superVersionJson = IOUtils.toJson(mcdir.getVersionJson(liteloader.getSuperVersion()));
        String superMinecraftArguments = superVersionJson.getString("minecraftArguments");

        versionJson.put("minecraftArguments", String.format("%s --tweakClass %s", superMinecraftArguments, tweakClass));

        return versionJson;
    }

    protected String generateLiteloaderVersionName(LiteloaderVersion liteloader) {
        return String.format("%s-LiteLoader%s", liteloader.getSuperVersion(), liteloader.getMinecraftVersion());
    }

}
