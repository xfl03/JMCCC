package com.github.to2mbn.jmccc.mcdownloader.cache;

public interface CacheTableSource<K, V> {

	V produce(K key) throws Exception;

}
