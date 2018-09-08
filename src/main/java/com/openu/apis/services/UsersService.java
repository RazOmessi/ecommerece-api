package com.openu.apis.services;

import com.openu.apis.beans.UserBean;
import com.openu.apis.utils.Validator;

import java.util.HashSet;
import java.util.Set;

public class UsersService {
    public static final int MIN_FIRST_NAME_LEN = 2, MAX_FIRST_NAME_LEN = 20;

    public static Set<String> validateUser(UserBean user){
        Set<String> errors = new HashSet<>();
        if(!Validator.isValidEmailAddress(user.getEmail())){
            errors.add("Invalid E-Mail");
        }

        if(user.getFirstName().length() < MIN_FIRST_NAME_LEN || user.getFirstName().length() > MAX_FIRST_NAME_LEN){
            errors.add(String.format("First name length must be between %s - %s", MIN_FIRST_NAME_LEN, MAX_FIRST_NAME_LEN));
        }

        return errors;
    }
}
