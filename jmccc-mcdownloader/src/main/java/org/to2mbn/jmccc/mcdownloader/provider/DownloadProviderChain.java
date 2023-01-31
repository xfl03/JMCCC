package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.util.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DownloadProviderChain implements Builder<MinecraftDownloadProvider> {

    protected MinecraftDownloadProvider baseProvider;
    protected List<MinecraftDownloadProvider> providers = new ArrayList<>();

    /*
     * Chain Model
     *
     * <Beginning(p1~p2)> <---------------Mid(p3~p6)------------------------->  <End(p7)>
     * p1======>p2========>p3=======================>p4=========>p5======>p6======>p7
     *  |  /|\   |  /|\     |               /||\      |    /||\  ............
     *  |___|    |___|     \|/               ||      \|/    ||   ............
     *                    p3.1(same as p1)   ||      p4.1   ||   ............
     *                      ||               ||       ||    ||   ............
     *                     \||/              ||      \||/   ||
     *                    p3.2(same as p2)   ||      p4.2   ||
     *                     \||/              ||      \||/   ||
     *                      ||_______________||       ||____||
     *                      |_________________|       |______|
     * ====>    parent is
     * ---->    upstream is
     *
     * In binary tree:
     *
     * Beginning:
     *                           *
     *                         /   \
     *                       p1 =>  *
     *                            /   \
     *                          p2 =>  ?(next)
     * =>    upstream is
     *
     * Full:
     *     beginning---------\
     *                        \--------------->*
     *                                        / \
     *                                       /   \
     *                                      /     \
     *                                     /       \
     *                                    /         \
     *                                   /           \
     *                                  /             \
     *                                p3 =>beginning-> *
     *                                                / \
     *                                               /   \
     *                                              /     \
     *                                             /       \
     *                                            /         \
     *                                           /           \
     *                                          /             \
     *                                        p4 =>beginning-> *
     *                                                        / \
     *                                                       /   \
     *                                                      /     \
     *                                                     /       \
     *                                                    /         \
     *                                                   /           \
     *                                                  /             \
     *                                                p5 =>beginning-> *
     *                                                                / \
     *                                                               /   \
     *                                                              /     \
     *                                                             /       \
     *                                                            /         \
     *                                                           /           \
     *                                                          /             \
     *                                                        p6 =>beginning-> p7
     * =>    upstream is
     * ->    the '?' refers to (see 'Beginning' above)
     *
     * In such a binary tree, all the left trees are leaves. All the right trees(except the right tree in the deepest level) are NOT leaves.
     * The leaves are DownloadProviders. When resolving download tasks, we first try the left tree, and then the right tree.
     */
    protected List<Builder<MinecraftDownloadProvider>> aheadProviders = new ArrayList<>();
    protected boolean useDownloadInfo = true;
    protected List<DownloadInfoProcessor> downloadInfoProcessor = new ArrayList<>();
    protected DownloadProviderChain() {
    }

    public static DownloadProviderChain create() {
        return new DownloadProviderChain();
    }

    public static MinecraftDownloadProvider buildDefault() {
        return create().build();
    }

    public DownloadProviderChain baseProvider(MinecraftDownloadProvider baseProvider) {
        this.baseProvider = baseProvider;
        return this;
    }

    public DownloadProviderChain addProvider(MinecraftDownloadProvider provider) {
        providers.add(Objects.requireNonNull(provider));
        return this;
    }

    public DownloadProviderChain addAheadProvider(Builder<MinecraftDownloadProvider> aheadProvider) {
        aheadProviders.add(Objects.requireNonNull(aheadProvider));
        return this;
    }

    public DownloadProviderChain useDownloadInfo(boolean useDownloadInfo) {
        this.useDownloadInfo = useDownloadInfo;
        return this;
    }

    public DownloadProviderChain addDownloadInfoProcessor(DownloadInfoProcessor processor) {
        this.downloadInfoProcessor.add(Objects.requireNonNull(processor));
        return this;
    }

    @Override
    public MinecraftDownloadProvider build() {
        MinecraftDownloadProvider right = this.baseProvider == null ? new MojangDownloadProvider() : this.baseProvider;
        for (MinecraftDownloadProvider left : providers) {
            if (left instanceof ExtendedDownloadProvider) {
                ((ExtendedDownloadProvider) left).setUpstreamProvider(withAheadProvider(right));
            }
            right = new DownloadProviderTree(left, right);
        }
        right = withAheadProvider(right);
        return right;
    }

    protected MinecraftDownloadProvider withAheadProvider(MinecraftDownloadProvider right) {
        List<MinecraftDownloadProvider> ahead = new ArrayList<>();
        for (Builder<MinecraftDownloadProvider> builder : aheadProviders) {
            ahead.add(Objects.requireNonNull(builder.build(), "Ahead provider builder [" + builder + "] returns null"));
        }
        if (useDownloadInfo) {
            ahead.add(new DownloadInfoProvider(new ArrayList<>(downloadInfoProcessor)));
        }

        for (MinecraftDownloadProvider left : ahead) {
            if (left instanceof ExtendedDownloadProvider) {
                ((ExtendedDownloadProvider) left).setUpstreamProvider(right);
            }
            right = new DownloadProviderTree(left, right);
        }
        return right;
    }

}
