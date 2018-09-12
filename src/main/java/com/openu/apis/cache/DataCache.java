package com.openu.apis.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

public class DataCache<K, V> {
    private ICacheLoader<K, V> _loader;
    private Map<K, V> _cache;
    private Queue<K> _keys;

    private int _maxSize;

    /**
     * DataCache represent a generic cache to store key-value data in memory.
     * @param maxSize The maximum size of the cache.
     * @param loader Loader class to load new data to the cache.
     */
    public DataCache(int maxSize, ICacheLoader<K, V> loader){
        if(maxSize <= 0){
            throw new IllegalArgumentException("Cache max size must be grater than zero.");
        }

        this._loader = loader;
        this._cache = new HashMap<K, V>();
        this._keys = new ConcurrentLinkedQueue<K>();
        this._maxSize = maxSize;
    }

    /**
     * Retrieve value from the cache by key.
     * @param key The key to retrieve the value from.
     * @return the value from the cache.
     */
    public V getValue(K key) {
        if (_cache.containsKey(key)){
            return _cache.get(key);
        }

        V value = _loader.load(key);
        if(value != null){
            if(_keys.size() == _maxSize){
                synchronized (this) {
                    _cache.remove(_keys.poll());
                }
            }

            _keys.add(key);
            _cache.put(key, value);

            return value;
        }

        return null;
    }

    public void reload(K key){
        if (!_cache.containsKey(key)){
            //let the load function insert the key to the queue
            _cache.get(key);
        } else {
            V value = _loader.load(key);
            synchronized (this) {
                if(_cache.containsKey(key)){
                    _cache.put(key, value);
                }
            }
        }
    }
}
