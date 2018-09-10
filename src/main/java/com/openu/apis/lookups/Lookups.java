package com.openu.apis.lookups;

import com.openu.apis.dal.MySqlDal;
import com.openu.apis.dal.dao.LookupDao;
import com.openu.apis.dal.dao.ReversableLookupDao;
import com.openu.apis.dal.dao.UpdateLookupDao;

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
    private ReversableLookupDao<Integer, String> lkpCategory;
    private ReversableLookupDao<Integer, String> lkpUserRole;
    private UpdateLookupDao<Integer, String> lkpProductImages;

    private Lookups() {
        lkpUserRole = new ReversableLookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpUserRoles"), true);
        lkpCategory = new ReversableLookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpCategory"), true);
        lkpOrderStatuses = new LookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpOrderStatuses"), true);
        lkpVendor = new ReversableLookupDao<Integer, String>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpVendor"), false);
        lkpProductImages = new UpdateLookupDao<>(MySqlDal.getInstance(), LookupsFactory.fromConfigs("lkpProductImages"));
    }

    public ReversableLookupDao<Integer, String> getLkpVendor() {
        return lkpVendor;
    }

    public LookupDao<Integer, String> getLkpOrderStatuses() {
        return lkpOrderStatuses;
    }

    public ReversableLookupDao<Integer, String> getLkpCategory() {
        return lkpCategory;
    }
    public ReversableLookupDao<Integer, String> getLkpUserRole() {
        return lkpUserRole;
    }

    public UpdateLookupDao<Integer, String> getLkpProductImages() { return lkpProductImages; }
}
