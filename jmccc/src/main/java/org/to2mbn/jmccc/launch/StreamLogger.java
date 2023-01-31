package org.to2mbn.jmccc.launch;

import org.to2mbn.jmccc.util.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

class StreamLogger implements Runnable {

    private ProcessListener listener;
    private boolean isErr;
    private InputStream in;

    public StreamLogger(ProcessListener listener, boolean isErr, InputStream in) {
        this.listener = listener;
        this.isErr = isErr;
        this.in = in;
    }

    @Override
    public void run() {
        char[] eol = Platform.getLineSeparator().toCharArray();
        try {
            // no need for closing this
            // because we don't need to close the underlying stream
            Reader reader = new InputStreamReader(in, Platform.getEncoding());

            StringBuilder buffer = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                buffer.append((char) ch);

                if (buffer.length() >= eol.length) {
                    // check eol
                    boolean isEOL = true;
                    for (int i = 0; i < eol.length; i++) {
                        if (eol[i] != buffer.charAt(buffer.length() - eol.length + i)) {
                            isEOL = false;
                            break;
                        }
                    }

                    if (isEOL) {
                        buffer.delete(buffer.length() - eol.length, buffer.length());
                        String log = buffer.toString();
                        buffer.delete(0, buffer.length());
                        if (isErr) {
                            listener.onErrorLog(log);
                        } else {
                            listener.onLog(log);
                        }
                    }
                }

                if (Thread.interrupted()) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
