package com.github.to2mbn.jmccc.mcdownloader.cache;

public interface CacheSource<T> {

	T produce() throws Exception;

}
