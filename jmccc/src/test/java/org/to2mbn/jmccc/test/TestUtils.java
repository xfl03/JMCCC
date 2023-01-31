package org.to2mbn.jmccc.test;

import java.util.*;
import java.util.Map.Entry;

public final class TestUtils {

    private TestUtils() {
    }

    @SafeVarargs
    public static <T> Set<T> hashSet(T... elements) {
        Set<T> set = new HashSet<>();
        for (T element : elements) {
            set.add(element);
        }
        return set;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> hashMap(Entry<K, V>... entries) {
        Map<K, V> map = new HashMap<>();
        for (Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K, V> Entry<K, V> entry(final K key, final V val) {
        return new Entry<K, V>() {

            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return val;
            }

            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode() {
                return Objects.hash(key, val);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj instanceof Entry) {
                    Entry<?, ?> another = (Entry<?, ?>) obj;
                    return Objects.deepEquals(key, another.getKey()) && Objects.deepEquals(val, another.getValue());
                }
                return false;
            }
        };
    }
}
