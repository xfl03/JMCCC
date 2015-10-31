package com.github.to2mbn.jmccc.mcdownloader.cache;

public interface Cache<T> {

	T get() throws Exception;

	void update() throws Exception;

}
