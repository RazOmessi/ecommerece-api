package com.openu.apis.lookups;

import com.openu.apis.beans.LookupTableBean;

import java.util.HashMap;
import java.util.Map;

public class LookupsFactory {
    private static Map<String, LookupTableBean> confmap = new HashMap<String, LookupTableBean> (){};
    static {
        confmap.put("lkpUserRoles", new LookupTableBean("e-commerce", "user_roles", "id", "name", 1000));
        confmap.put("lkpVendor", new LookupTableBean("e-commerce", "vendors", "id", "name", 1000));
        confmap.put("lkpOrderStatuses", new LookupTableBean("e-commerce", "order_statuses", "id", "name", 1000));
        confmap.put("lkpCategory", new LookupTableBean("e-commerce", "categories", "id", "name", 1000));
    }

    public static LookupTableBean fromConfigs(String lookupName){
        return confmap.get(lookupName);
    }


}
