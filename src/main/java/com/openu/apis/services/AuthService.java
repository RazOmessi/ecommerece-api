package com.openu.apis.services;


import org.apache.commons.codec.digest.DigestUtils;

public class AuthService {
    public static String hashPassword(String password){
        return DigestUtils.md5Hex(password).toUpperCase();
    }
}
