package org.to2mbn.jmccc.mcdownloader.download.tasks;

import java.io.IOException;
import java.nio.ByteBuffer;

class AndThenDownloadSession<R, S> implements DownloadSession<S> {

    private ResultProcessor<R, S> processor;
    private DownloadSession<R> delegated;

    public AndThenDownloadSession(ResultProcessor<R, S> processor, DownloadSession<R> delegated) {
        this.processor = processor;
        this.delegated = delegated;
    }

    @Override
    public void receiveData(ByteBuffer data) throws IOException {
        delegated.receiveData(data);
    }

    @Override
    public S completed() throws Exception {
        return processor.process(delegated.completed());
    }

    @Override
    public void failed() throws Exception {
        delegated.failed();
    }

}