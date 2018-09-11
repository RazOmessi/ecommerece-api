package com.openu.apis.auth;

import com.openu.apis.beans.UserAuthBean;
import com.openu.apis.beans.UserBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.dao.AuthDao;
import com.openu.apis.dal.dao.UserDao;
import com.openu.apis.exceptions.GetTokenException;
import com.openu.apis.exceptions.InsertTokenException;
import com.openu.apis.exceptions.RefreshTokenException;
import com.openu.apis.utils.Roles;

import java.sql.Timestamp;
import java.util.UUID;

public class AuthManager {
    //todo: move to conf
    private static final long TOKEN_LIFE_TIME = 30 * 60 * 1000;

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

    private DataCache<String, UserAuthBean> _cache;

    private AuthManager(){
        _cache = new DataCache<>(1000, new ICacheLoader<String, UserAuthBean>() {
            @Override
            public UserAuthBean load(String key) {
                try {
                    return AuthDao.getInstance().getAuthByToken(key);
                } catch (GetTokenException e){
                    //todo: add logger
                    return null;
                }
            }
        });
    }

    public String generateToken(int userId){
        String token = UUID.randomUUID().toString();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try {
            AuthDao.getInstance().insertToken(new UserAuthBean(userId, token, timestamp));
        } catch (InsertTokenException e){
            throw new RuntimeException(String.format("Error generating token: %s", e.getMessage()));
        }

        return token;
    }

    public boolean isAuthenticate(String token, Roles role) {
        return isAuthenticate(token, role, null);
    }

    public int getUserId(String token) {
        return _cache.getValue(token).getUserId();
    }

    public boolean isAuthenticate(String token, Roles role, Integer id){
        UserAuthBean auth = _cache.getValue(token);

        if(auth != null){
            UserBean user = UserDao.getInstance().getUserById(auth.getUserId());
            long now = System.currentTimeMillis();

            if(auth.getTimestamp().after(new Timestamp(now - TOKEN_LIFE_TIME)) && Roles.valueOf(user.getRoleId()) == role && (id == null || user.getId() == id)){
                auth.setTimestamp(new Timestamp(now));
                try{
                    AuthDao.getInstance().refreshToken(auth);
                } catch (RefreshTokenException e){
                    //todo: add logger
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        }

        return false;
    }
}
