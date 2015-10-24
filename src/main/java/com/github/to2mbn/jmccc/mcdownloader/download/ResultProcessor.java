package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;

public interface ResultProcessor<T, R> {

	R process(T arg) throws IOException;

}
