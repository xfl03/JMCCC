package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

import java.util.Set;

public interface MinecraftDownloadProvider {

    /**
     * Returns a version list download task.
     *
     * @return a minecraft version list
     */
    CombinedDownloadTask<RemoteVersionList> versionList();

    /**
     * Returns an asset index download task.
     * <p>
     * The asset index will also be saved to
     * <code>${mcdir}/indexes/${version.getAssets()}.json</code>.
     *
     * @param mcdir   the minecraft dir
     * @param version the minecraft version
     * @return the asset index
     */
    CombinedDownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, Version version);

    /**
     * Returns a game jar download task.
     * <p>
     * The jar is saved to
     * <code>${mcdir}/versions/${version.getVersion()}/${version.getVersion()}.jar</code>
     * . If the file already exists, this method will overwrite the file.
     *
     * @param mcdir   the minecraft dir
     * @param version the minecraft version
     * @return void
     */
    CombinedDownloadTask<Void> gameJar(MinecraftDirectory mcdir, Version version);

    /**
     * Returns a game version json download task.
     * <p>
     * The version json file will be saved to
     * <code>${mcdir}/versions/${version}/${version}.json</code>. If the file
     * already exists, this method will overwrite the file.
     *
     * @param mcdir   the minecraft dir
     * @param version the game version
     * @return the downloaded version's name
     */
    CombinedDownloadTask<String> gameVersionJson(MinecraftDirectory mcdir, String version);

    /**
     * Returns a library download task.
     * <p>
     * The library will be saved to
     * <code>${mcdir}/libraries/${library.getPath()}</code>. If the file already
     * exists, this method will overwrite it.
     *
     * @param mcdir   the minecraft dir
     * @param library the library to download
     * @return void
     */
    CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, Library library);

    /**
     * Returns an asset download task.
     * <p>
     * The asset will be saved to
     * <code>${mcdir}/assets/objects/${2-character-prefix of hash}/${hash}</code>
     * . If the file already exists, this method will overwrite it.
     *
     * @param mcdir the minecraft dir
     * @param asset the asset to download
     * @return void
     */
    CombinedDownloadTask<Void> asset(MinecraftDirectory mcdir, Asset asset);

}
