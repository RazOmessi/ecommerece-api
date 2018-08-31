package com.openu.apis.lookups;

import com.openu.apis.dal.MySqlDal;
import com.openu.apis.dal.dao.LookupDao;

public class Lookups {
    private static Lookups _instance;

    public static Lookups getInstance() {
        if(_instance != null){
            return _instance;
        }

        synchronized(Lookups.class) {
            if(_instance == null){
                _instance = new Lookups();
            }

            return _instance;
        }
    }

    private LookupDao<Integer, String> lkpVendor;
    private LookupDao<Integer, String> lkpOrderStatuses;

    private Lookups() {
        lkpVendor = new LookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpVendor"));
        lkpOrderStatuses = new LookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpOrderStatuses"));
    }

    public LookupDao<Integer, String> getLkpVendor() {
        return lkpVendor;
    }

    public LookupDao<Integer, String> getLkpOrderStatuses() {
        return lkpOrderStatuses;
    }
}
