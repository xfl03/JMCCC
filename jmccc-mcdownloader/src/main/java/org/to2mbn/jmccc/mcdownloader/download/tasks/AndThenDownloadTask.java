package org.to2mbn.jmccc.mcdownloader.download.tasks;

class AndThenDownloadTask<SRC, DEST> extends DownloadTaskDecorator<SRC, DEST> {

    private ResultProcessor<SRC, DEST> processor;

    public AndThenDownloadTask(ResultProcessor<SRC, DEST> processor, DownloadTask<SRC> delegated) {
        super(delegated);
        this.processor = processor;
    }

    @Override
    protected DownloadSession<DEST> createSessionDelegate(DownloadSession<SRC> toDelegate) {
        return new AndThenDownloadSession<>(processor, toDelegate);
    }

}