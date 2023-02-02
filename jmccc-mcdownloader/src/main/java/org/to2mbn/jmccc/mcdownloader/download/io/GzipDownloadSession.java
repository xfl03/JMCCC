package org.to2mbn.jmccc.mcdownloader.download.io;

import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

public class GzipDownloadSession<T> implements DownloadSession<T> {

    private final DownloadSession<T> underlying;
    private ByteArrayOutputStream out;
    private WritableByteChannel channel;

    public GzipDownloadSession(DownloadSession<T> underlying) {
        Objects.requireNonNull(underlying);
        this.underlying = underlying;
        out = new ByteArrayOutputStream();
        channel = Channels.newChannel(out);
    }

    @Override
    public void receiveData(ByteBuffer data) throws IOException {
        channel.write(data);
    }

    @Override
    public void failed() throws Exception {
        close();
        underlying.failed();
    }

    @Override
    public T completed() throws Exception {
        try (GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(out.toByteArray()))) {
            byte[] buf = new byte[8192];
            ByteBuffer bb = ByteBuffer.wrap(buf);
            int read;
            while ((read = gzin.read(buf)) != -1) {
                ((java.nio.Buffer) bb).position(0);
                ((java.nio.Buffer) bb).limit(read);
                underlying.receiveData(bb);
            }
        } catch (Throwable e) {
            underlying.failed();
            throw e;
        } finally {
            close();
        }
        return underlying.completed();
    }

    private void close() {
        channel = null;
        out = null;
    }

}
