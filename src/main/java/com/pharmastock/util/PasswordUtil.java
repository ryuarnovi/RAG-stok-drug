package com.pharmastock.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordUtil {

    private static final int BCRYPT_COST = 12;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
    }

    public static boolean verify(String password, String hash) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
        return result.verified;
    }
}
