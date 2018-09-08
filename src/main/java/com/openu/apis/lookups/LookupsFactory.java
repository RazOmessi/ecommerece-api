package com.openu.apis.lookups;

import com.openu.apis.beans.LookupTableBean;
import com.openu.apis.configurations.ConfigurationManager;

import java.util.HashMap;
import java.util.Map;

public class LookupsFactory {

    public static LookupTableBean fromConfigs(String lookupName){
        return ConfigurationManager.getInstance().getTableConfigs(lookupName);
    }

}
