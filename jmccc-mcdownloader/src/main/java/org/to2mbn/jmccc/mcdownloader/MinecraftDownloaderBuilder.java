package org.to2mbn.jmccc.mcdownloader;

import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloader;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.util.Builder;
import org.to2mbn.jmccc.util.Builders;

import java.util.Objects;

public class MinecraftDownloaderBuilder implements Builder<MinecraftDownloader> {

    protected final Builder<? extends Downloader> underlying;
    protected Builder<MinecraftDownloadProvider> providerChain;

    protected MinecraftDownloaderBuilder(Builder<? extends Downloader> underlying) {
        this.underlying = underlying;
    }

    public static MinecraftDownloaderBuilder create(Builder<? extends Downloader> underlying) {
        return new MinecraftDownloaderBuilder(underlying);
    }

    public static MinecraftDownloaderBuilder create() {
        return create(CombinedDownloaderBuilder.create());
    }

    public static MinecraftDownloader buildDefault(Builder<? extends Downloader> underlying) {
        return new MinecraftDownloaderBuilder(underlying).build();
    }

    public static MinecraftDownloader buildDefault() {
        return buildDefault(CombinedDownloaderBuilder.create());
    }

    public MinecraftDownloaderBuilder providerChain(Builder<MinecraftDownloadProvider> providerChain) {
        this.providerChain = providerChain;
        return this;
    }

    @Override
    public MinecraftDownloader build() {
        MinecraftDownloadProvider provider = providerChain == null
                ? DownloadProviderChain.buildDefault()
                : Objects.requireNonNull(providerChain.build(), "providerChain builder returns null");

        CombinedDownloader combinedDownloader = null;
        Downloader underlying = null;
        try {
            underlying = Objects.requireNonNull(this.underlying.build(), "Underlying downloader builder returns null");

            if (underlying instanceof CombinedDownloader) {
                combinedDownloader = (CombinedDownloader) underlying;
            } else {
                combinedDownloader = CombinedDownloaderBuilder.buildDefault(Builders.of(underlying));
            }

            return new MinecraftDownloaderImpl(combinedDownloader, provider);
        } catch (Throwable e) {
            if (combinedDownloader != null) {
                try {
                    combinedDownloader.shutdown();
                } catch (Throwable e1) {
                    e.addSuppressed(e1);
                }
            }
            if (underlying != null) {
                try {
                    underlying.shutdown();
                } catch (Throwable e1) {
                    e.addSuppressed(e1);
                }
            }
            throw e;
        }
    }

}
