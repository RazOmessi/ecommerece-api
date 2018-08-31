package com.openu.apis.cache;

/**
 * An interface that describes how to load data to DataCache.
 * @param <K> The data key.
 * @param <V> The data value.
 */
public interface ICacheLoader<K,V> {
    V load(K key);
}
