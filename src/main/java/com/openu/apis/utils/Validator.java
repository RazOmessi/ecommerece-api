package com.openu.apis.utils;

public class Validator {
    public static boolean isValidEmailAddress(String email) {
        String checker = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(checker);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
