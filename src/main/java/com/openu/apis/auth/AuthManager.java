package com.openu.apis.auth;

public class AuthManager {
    private static AuthManager _instance;

    public static AuthManager getInstance() {
        if (_instance != null) {
            return _instance;
        }

        synchronized (AuthManager.class) {
            if (_instance == null) {
                _instance = new AuthManager();
            }

            return _instance;
        }
    }

    private AuthManager(){

    }



    //user_tokens(userId, token, createdTs)
}
