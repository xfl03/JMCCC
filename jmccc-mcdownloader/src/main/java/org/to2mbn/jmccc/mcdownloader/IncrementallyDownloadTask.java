package org.to2mbn.jmccc.mcdownloader;

import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadContext;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.ChecksumUtils;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.DownloadInfo;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

class IncrementallyDownloadTask extends CombinedDownloadTask<Version> {

    private MinecraftDirectory mcdir;
    private String version;
    private MinecraftDownloadProvider provider;
    private boolean checkLibrariesHash;
    private boolean checkAssetsHash;
    private boolean updateSnapshots;
    private AssetOption assetOption;

    private Set<String> handledVersions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private String resolvedVersion;

    public IncrementallyDownloadTask(MinecraftDownloadProvider downloadProvider, MinecraftDirectory mcdir, String version, boolean checkLibrariesHash, boolean checkAssetsHash, boolean updateSnapshots, AssetOption assetOption) {
        Objects.requireNonNull(mcdir);
        Objects.requireNonNull(version);
        Objects.requireNonNull(downloadProvider);
        this.mcdir = mcdir;
        this.version = version;
        this.provider = downloadProvider;
        this.checkLibrariesHash = checkLibrariesHash;
        this.checkAssetsHash = checkAssetsHash;
        this.updateSnapshots = updateSnapshots;
        this.assetOption = assetOption;
    }

    @Override
    public void execute(final CombinedDownloadContext<Version> context) throws Exception {
        handledVersions.clear();
        resolvedVersion = null;

        handleVersionJson(version, context, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                if (resolvedVersion == null) {
                    resolvedVersion = version;
                }

                final Version versionModel = Versions.resolveVersion(mcdir, resolvedVersion);

                if (mcdir.getAssetIndex(versionModel).exists()) {
                    downloadAssets(context, Versions.resolveAssets(mcdir, versionModel));

                } else {
                    context.submit(provider.assetsIndex(mcdir, versionModel), new CallbackAdapter<Set<Asset>>() {

                        @Override
                        public void done(final Set<Asset> result) {
                            try {
                                context.submit(new Callable<Void>() {

                                    @Override
                                    public Void call() throws Exception {
                                        downloadAssets(context, result);
                                        return null;
                                    }
                                }, null, true);
                            } catch (InterruptedException e) {
                                context.cancelled();
                            }
                        }

                    }, true);
                }

                if (!mcdir.getVersionJar(versionModel).exists()) {
                    context.submit(provider.gameJar(mcdir, versionModel), null, true);
                }

                downloadLibraries(context, versionModel);

                context.awaitAllTasks(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        context.done(versionModel);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    private void handleVersionJson(final String currentVersion, final CombinedDownloadContext<Version> context, final Callable<Void> callback) throws Exception {
        if (mcdir.getVersionJson(currentVersion).exists()) {
            JSONObject versionjson = IOUtils.toJson(mcdir.getVersionJson(currentVersion));
            String inheritsFrom = versionjson.optString("inheritsFrom", null);
            handledVersions.add(currentVersion);
            if (inheritsFrom == null) {
                // end node
                callback.call();
            } else {
                // intermediate node
                if (handledVersions.contains(inheritsFrom)) {
                    throw new IllegalStateException("loop inherits from: " + currentVersion + " to " + inheritsFrom);
                }
                handleVersionJson(inheritsFrom, context, callback);
            }
        } else {
            context.submit(provider.gameVersionJson(mcdir, currentVersion), new CallbackAdapter<String>() {

                @Override
                public void done(final String currentResolvedVersion) {
                    try {
                        context.submit(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                if (version.equals(currentVersion)) {
                                    resolvedVersion = currentResolvedVersion;
                                }

                                handleVersionJson(currentResolvedVersion, context, callback);
                                return null;
                            }
                        }, null, true);
                    } catch (InterruptedException e) {
                        context.cancelled();
                    }
                }

            }, true);
        }
    }

    private void downloadAssets(final CombinedDownloadContext<Version> context, Set<Asset> assets) throws InterruptedException {
        if (assets == null || assetOption == AssetOption.SKIP_ASSETS)
            return;

        Map<String, Asset> hashMapping = new HashMap<>();
        for (Asset asset : assets) {
            // put the assets into a map
            // to remove the elements which has the same hash
            hashMapping.put(asset.getHash(), asset);
        }

        final boolean fatal = assetOption == AssetOption.FORCIBLY_DOWNLOAD;

        if (checkAssetsHash)
            for (final Asset asset : hashMapping.values())
                context.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        if (!asset.isValid(mcdir))
                            context.submit(provider.asset(mcdir, asset), null, fatal);

                        return null;
                    }
                }, null, false);

        else
            for (Asset asset : hashMapping.values())
                if (!mcdir.getAsset(asset).isFile())
                    context.submit(provider.asset(mcdir, asset), null, fatal);
    }

    private void downloadLibraries(final CombinedDownloadContext<?> context, Version version) throws InterruptedException {
        if (checkLibrariesHash)
            for (final Library library : version.getLibraries())
                context.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        if (needDownload(mcdir.getLibrary(library), library.getDownloadInfo()))
                            downloadLibrary(context, library, true);
                        else
                            checkAndUpdate(context, library);

                        return null;
                    }
                }, null, true);

        else {
            Set<Library> missing = version.getMissingLibraries(mcdir);
            for (Library library : missing)
                downloadLibrary(context, library, true);

            Set<Library> existing = new HashSet<>(version.getLibraries());
            existing.removeAll(missing);
            for (Library library : existing)
                checkAndUpdate(context, library);
        }
    }

    private void downloadLibrary(CombinedDownloadContext<?> context, Library library, boolean fatal) throws InterruptedException {
        context.submit(provider.library(mcdir, library), null, fatal);
    }

    private boolean needDownload(File target, DownloadInfo info) throws NoSuchAlgorithmException, IOException {
        if (info == null)
            return !target.isFile();
        else
            return !ChecksumUtils.verify(target, info.getChecksum(), "SHA-1", info.getSize());
    }

    private void checkAndUpdate(final CombinedDownloadContext<?> context, final Library lib) throws InterruptedException {
        if (updateSnapshots && lib.isSnapshotArtifact()) {
            final Library sha1lib = new Library(lib.getGroupId(), lib.getArtifactId(), lib.getVersion(), lib.getClassifier(), "jar.sha1");
            context.submit(provider.library(mcdir, sha1lib), new CallbackAdapter<Void>() {

                @Override
                public void done(Void result) {
                    try {
                        context.submit(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                String sha1 = IOUtils.toString(mcdir.getLibrary(sha1lib)).trim();
                                if (!ChecksumUtils.verify(mcdir.getLibrary(lib), sha1, "SHA-1")) {
                                    downloadLibrary(context, lib, false);
                                }
                                return null;
                            }
                        }, null, false);
                    } catch (InterruptedException e) {
                    }
                }
            }, false);
        }
    }

}
