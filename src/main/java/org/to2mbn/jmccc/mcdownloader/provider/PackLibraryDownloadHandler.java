package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.File;
import java.net.URI;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import com.github.to2mbn.jmccc.version.Library;

public class PackLibraryDownloadHandler implements LibraryDownloadHandler {

	@Override
	public DownloadTask<Object> createDownloadTask(File target, Library library, URI libraryUri) {
		return new MemoryDownloadTask(libraryUri).andThen(new PackProcessor(target));
	}

}
