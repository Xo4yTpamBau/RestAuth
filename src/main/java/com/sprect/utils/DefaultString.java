package com.sprect.utils;

import org.springframework.stereotype.Component;

@Component
public class DefaultString {

    public static final String PATTERN_PHONE = "^375(25|29|33|44)[0-9]{7}$";
    public static final String PATTERN_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
            "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static final String PATTERN_FIRST_NAME = "^([a-zA-Z]{2,})";
    public static final String PATTERN_LAST_NAME = "^([a-zA-Z]{1,}'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]{1,})?)";
    public static final String PATTERN_PASSWORD = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

    public static final String FAILED_VALIDATE_EMAIL = "The email address is specified incorrectly";
    public static final String FAILED_VALIDATE_USERNAME = "Username must be between 3 and 18 characters long";
    public static final String FAILED_VALIDATE_PHONE = "The phone is specified incorrectly";
    public static final String FAILED_VALIDATE_FIRST_NAME = "The first name are incorrectly specified";
    public static final String FAILED_VALIDATE_LAST_NAME = "The last name are incorrectly specified";
    public static final String FAILED_VALIDATE_PASSWORD =
            "The password must consist of 8 characters and contain at least one digit, one uppercase and one lowercase";
    public static final String WRONG_OLD_PASSWORD = "The old password is incorrect";

    public static final String EMAIL_BUSY = "This email is busy";
    public static final String PHONE_BUSY = "This phone is busy";
    public static final String USERNAME_IS_BUSY = "This username is busy";
    public static final String USERNAME_BUSY = "This username is busy";

    public static final String EXAMPLE_PHONE = "{\"phone\" : \"375251234567\"}";
    public static final String EXAMPLE_EMAIL = "{\"email\" : \"user@mail.com\"}";
    public static final String EXAMPLE_USERNAME = "{\"username\" : \"user\"}";
    public static final String EXAMPLE_SIGNIN = "{" +
            "\"username\" : \"user\"," +
            "\"password\" : \"1234Qwer\"" +
            "}";
    public static final String EXAMPLE_UPDATE_TOKENS = "{" +
            "\"accessToken\" : \"header.body.JWS\",\n" +
            "\"refreshToken\" : \"header.body.JWS\"" +
            "}";
    public static final String EXAMPLE_FILE = "{\"file\":\"string\"}";
    public static final String EXAMPLE_RESET_PASSWORD_THROUGH_EMAIL = "{\"password\" : \"1234Qwer\"}";
    public static final String EXAMPLE_PROFILE_DESCRIPTION = "{\"profileDescription\" : \"Bla... Bla... Bla...\"}";
    public static final String EXAMPLE_RESET_PASSWORD = "{" +
            "\"oldPassword\" : \"1234Qwer\", \n" +
            "\"newPassword\" : \"1234Qwer\"" +
            "}";

    public static final String DEFAULT_USER_ROLE = "USER";
    public static final String BLOCKED_USER_TRY_AUTH =
            "Suspicious activity has been detected from your account. Your account is temporarily blocked";
    public static final String BAD_CREDENTIALS = "Bad credentials";
    public static final String NOT_CONFIRM_EMAIL = "User is disabled";
    public static final String USER_BLOCKED = "User account is locked";
    public static final String USER_ALREADY_REGISTRATION = "Such a user is already registered";
    public static final String USER_NOT_FOUND = "A user with this name was not found";
    public static final String BLOCKER_USER_TRY_AUTH =
            "Suspicious activity has been detected from your account. Your account is temporarily blocked";
    public static final String RESPONSE_SUCCESS_SAVE_AVATAR = "{\"UrlAvatar\":\"link\"}";
    public static final String USER = "" +
            "\"idUser\":%s," +
            "\"username\":\"%s\"," +
            "\"email\":\"%s\"," +
            "\"phone\":\"%s\"," +
            "\"firstName\":\"test\"," +
            "\"lastName\":\"test\"," +
            "\"registrationDate\":%s," +
            "\"profileDescription\":null," +
            "\"urlAvatar\":%s";
    public static final String RESPONSE_ERROR =
            "\"status\":%d," +
                    "\"error\":\"%s\"";

    public static final String ACCESS_INVALID = "Access token is invalid";
    public static final String ACCESS_EXPIRED = "Access token is expired";
    public static final String CONFIRM_EXPIRED = "confirmationEmailToken is expired";
    public static final String CONFIRM_INVALID = "confirmationEmailToken is invalid";
    public static final String ACCESS_NOT_EXPIRED = "The access token has not expired";
    public static final String REFRESH_EXPIRED = "Refresh token is expired";
    public static final String REFRESH_INVALID = "Refresh token is invalid";
    public static final String TOKEN_BLACK_LIST = "Token is blacklisted";

    public static final String REGISTRATION_EXCEPTION = "Such a user is already registered";
}
