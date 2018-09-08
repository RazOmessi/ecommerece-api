package com.openu.apis.lookups;

import com.openu.apis.dal.MySqlDal;
import com.openu.apis.dal.dao.LookupDao;
import com.openu.apis.dal.dao.ReversableLookupDao;

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

    private ReversableLookupDao<Integer, String> lkpVendor;
    private LookupDao<Integer, String> lkpOrderStatuses;
    private LookupDao<Integer, String> lkpCategory;
    private LookupDao<Integer, String> lkpUserRole;

    private Lookups() {
        lkpUserRole = new LookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpUserRoles"), false);
        lkpCategory = new LookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpCategory"));
        lkpOrderStatuses = new LookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpOrderStatuses"));
        lkpVendor = new ReversableLookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpVendor"), false);

    }

    public ReversableLookupDao<Integer, String> getLkpVendor() {
        return lkpVendor;
    }

    public LookupDao<Integer, String> getLkpOrderStatuses() {
        return lkpOrderStatuses;
    }

    public LookupDao<Integer, String> getLkpCategory() {
        return lkpCategory;
    }
    public LookupDao<Integer, String> getLkpUserRole() {
        return lkpUserRole;
    }
}