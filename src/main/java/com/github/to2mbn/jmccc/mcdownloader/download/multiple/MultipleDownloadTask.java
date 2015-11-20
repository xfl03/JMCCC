package com.github.to2mbn.jmccc.mcdownloader.download.multiple;

public interface MultipleDownloadTask<T> {

	void execute(MultipleDownloadContext<T> context) throws Exception;

}
