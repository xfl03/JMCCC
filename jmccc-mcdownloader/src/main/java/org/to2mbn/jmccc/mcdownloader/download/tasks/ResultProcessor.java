package org.to2mbn.jmccc.mcdownloader.download.tasks;

public interface ResultProcessor<T, R> {

    R process(T arg) throws Exception;

}
