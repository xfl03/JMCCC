package org.to2mbn.jmccc.mcdownloader.provider.libraries;

import java.io.File;
import java.net.URI;
import org.to2mbn.jmccc.mcdownloader.download.task.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.task.FileDownloadTask;
import org.to2mbn.jmccc.version.Library;

public class JarLibraryDownloadHandler implements LibraryDownloadHandler {

	@Override
	public DownloadTask<Void> createDownloadTask(File target, Library library, URI libraryUri) {
		return new FileDownloadTask(libraryUri, target);
	}

}
