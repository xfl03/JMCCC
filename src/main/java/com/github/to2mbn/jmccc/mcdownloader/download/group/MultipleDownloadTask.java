package com.github.to2mbn.jmccc.mcdownloader.download.group;

public interface MultipleDownloadTask<T> {

	void execute(MultipleDownloadContext<T> context);

}
