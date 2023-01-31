package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.version.Library;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

class XZPackLibraryDownloadHandler implements LibraryDownloadHandler {

    @Override
    public DownloadTask<Void> createDownloadTask(File target, Library library, URI libraryUri) {
        return new MemoryDownloadTask(libraryUri).andThen(new ResultProcessor<byte[], byte[]>() {

            @Override
            public byte[] process(byte[] data) throws IOException {
                try (XZInputStream in = new XZInputStream(new ByteArrayInputStream(data)); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    return out.toByteArray();
                }
            }

        }).andThen(new PackProcessor(target));
    }

}
