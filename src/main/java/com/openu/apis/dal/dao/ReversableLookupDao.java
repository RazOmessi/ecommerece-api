package com.openu.apis.dal.dao;

import com.openu.apis.beans.LookupTableBean;
import com.openu.apis.dal.IDal;

public class ReversableLookupDao<K, V> extends LookupDao<K, V> {
    private LookupDao<V, K> _reversedDao;

    public ReversableLookupDao(final IDal dal, final LookupTableBean table) {
        super(dal, table);

        this._reversedDao = new LookupDao<V, K>(dal, table);
    }

    public K getReversedLookup(V value){
        return this._reversedDao.getLookup(value);
    }

}
