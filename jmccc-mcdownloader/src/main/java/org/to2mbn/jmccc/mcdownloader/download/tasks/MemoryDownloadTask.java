package org.to2mbn.jmccc.mcdownloader.download.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * A download task which stores data in memory.
 *
 * @author yushijinhun
 */
public class MemoryDownloadTask extends DownloadTask<byte[]> {

    /**
     * Constructs a MemoryDownloadTask.
     *
     * @param uri the uri of resource to download
     * @throws NullPointerException     if <code>uri==null</code>
     * @throws IllegalArgumentException if <code>uri</code> is not in a valid
     *                                  URI format
     */
    public MemoryDownloadTask(String uri) {
        super(uri);
    }

    /**
     * Constructs a MemoryDownloadTask.
     *
     * @param uri the uri of resource to download
     * @throws NullPointerException if <code>uri==null</code>
     */
    public MemoryDownloadTask(URI uri) {
        super(uri);
    }

    @Override
    public DownloadSession<byte[]> createSession(final long length) throws IOException {
        return new DownloadSession<byte[]>() {

            private ByteArrayOutputStream out = new ByteArrayOutputStream(length == -1 ? 8192 : (int) length);
            private WritableByteChannel channel = Channels.newChannel(out);

            @Override
            public void receiveData(ByteBuffer data) throws IOException {
                channel.write(data);
            }

            @Override
            public void failed() throws IOException {
                close();
            }

            @Override
            public byte[] completed() throws IOException {
                byte[] data = out.toByteArray();
                close();
                return data;
            }

            private void close() {
                channel = null;
                out = null;
            }
        };
    }

    @Override
    public DownloadSession<byte[]> createSession() throws IOException {
        return createSession(8192);
    }

}
