package org.to2mbn.jmccc.mcdownloader.download.tasks;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A download session is used to handle received data and write them to the
 * download target.
 * <p>
 * A session will be created when the download task begins, and be destroyed
 * when the download completes or fails. Once the download session created, one
 * of the following methods {@link #completed()}, {@link #failed()} must be
 * invoked.
 *
 * @param <T> the type of result
 * @author yushijinhun
 */
public interface DownloadSession<T> {

    /**
     * Calls when receives a part of data.
     *
     * @param data the data
     * @throws IOException if an I/O error occurs
     */
    void receiveData(ByteBuffer data) throws IOException;

    /**
     * Calls when all the data has been received successfully.
     * <p>
     * Notes for implementation: In this method you should close the opened
     * resources.
     *
     * @return the result
     * @throws Exception if an I/O error occurs
     */
    T completed() throws Exception;

    /**
     * Calls when an error occurs during downloading.
     * <p>
     * Notes for implementation: In this method you should close the opened
     * resources.
     *
     * @throws Exception if an I/O error occurs
     */
    void failed() throws Exception;

}
