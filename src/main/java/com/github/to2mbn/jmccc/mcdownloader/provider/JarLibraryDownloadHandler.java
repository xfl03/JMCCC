package com.github.to2mbn.jmccc.mcdownloader.provider;

import java.io.File;
import java.net.URI;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import com.github.to2mbn.jmccc.version.Library;

public class JarLibraryDownloadHandler implements LibraryDownloadHandler {

	@Override
	public DownloadTask<Object> createDownloadTask(File target, Library library, URI libraryUri) {
		return new FileDownloadTask(libraryUri, target);
	}

}
