package com.openu.apis.dal.dao;

import com.openu.apis.beans.LookupTableBean;
import com.openu.apis.dal.IDal;

public class ReversableLookupDao<K, V> extends LookupDao<K, V> {
    private LookupDao<V, K> _reversedDao;

    public ReversableLookupDao(final IDal dal, final LookupTableBean table) {
        this(dal, table, true);
    }

    public ReversableLookupDao(final IDal dal, final LookupTableBean table, boolean isStatic) {
        super(dal, table, true);

        LookupTableBean reversedTable = new LookupTableBean(table.getSchemaName(), table.getTableName(), table.getValueColumn(), table.getKeyColumn(), table.getMaxCacheSize());
        this._reversedDao = new LookupDao<V, K>(dal, reversedTable, isStatic);
    }

    public K getReversedLookup(V value){
        return this._reversedDao.getLookup(value);
    }

}
