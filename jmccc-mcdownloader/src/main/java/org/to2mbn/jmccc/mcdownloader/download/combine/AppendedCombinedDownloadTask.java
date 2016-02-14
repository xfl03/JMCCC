package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;

class AppendedCombinedDownloadTask<R, S> extends CombinedDownloadTask<S> {

	ResultProcessor<R, S> processor;
	CombinedDownloadTask<R> proxied;

	AppendedCombinedDownloadTask(CombinedDownloadTask<R> proxied, ResultProcessor<R, S> processor) {
		this.proxied = proxied;
		this.processor = processor;
	}

	@Override
	public void execute(CombinedDownloadContext<S> context) throws Exception {
		proxied.execute(new AppendedCombinedDownloadContext<R, S>(processor, context));
	}

}