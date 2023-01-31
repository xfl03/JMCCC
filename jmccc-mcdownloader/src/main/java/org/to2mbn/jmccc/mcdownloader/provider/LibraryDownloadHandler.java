package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.version.Library;

import java.io.File;
import java.net.URI;

/**
 * Creates a download task for a game library.
 * <p>
 * Each <code>LibraryDownloadHandler</code> handles one kind of libraries. For example, JarLibraryDownloadHandler
 * handles the libraries ending with '.jar', PackLibraryDownloadHandler handles the libraries ending with '.jar.pack',
 * XZPackLibraryDownloadHandler handles the libraries ending with '.jar.pack.xz'.
 *
 * @author yushijinhun
 */
public interface LibraryDownloadHandler {

    DownloadTask<Void> createDownloadTask(File target, Library library, URI libraryUri);

}
