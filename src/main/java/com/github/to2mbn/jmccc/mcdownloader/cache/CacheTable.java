package com.github.to2mbn.jmccc.mcdownloader.cache;

public interface CacheTable<K, V> {

	Cache<V> get(K key);

}
