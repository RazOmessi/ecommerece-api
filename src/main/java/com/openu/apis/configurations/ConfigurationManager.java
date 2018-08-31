package com.openu.apis.configurations;

public class ConfigurationManager {
    private static ConfigurationManager _instance;

    public static ConfigurationManager getInstance() {
        if(_instance != null){
            return _instance;
        }

        synchronized(ConfigurationManager.class) {
            if(_instance == null){
                _instance = new ConfigurationManager();
            }

            return _instance;
        }
    }

    private ConfigurationManager(){
        String filename = System.getProperty("ecommeerce.configurations");
        if(filename == null){
            //get file from resources
        } else {
            //get file from file path
        }

    }

}
