package com.vtp.vipo.seller.common.constants;

public  final class AuthConstant {
    private AuthConstant() {
        throw new IllegalStateException(UTITLITY_CLASS_ERROR);
    }

    public static final String UTITLITY_CLASS_ERROR = "Utility class!!!";

    public static final String EMAIL_EXIST = "Email already exists in the system!";

    public static final String PHONE_EXIST = "Phone number already exists in the system!";

    public static final String PHONE_NOT_EXIST = "The phone number does not exist in the system";

    public static final String PASSWORD_NOT_EXIST = "The phone number does not exist in the system";

    public static final String INCORRECT_PASSWORD = "Incorrect password";

}
