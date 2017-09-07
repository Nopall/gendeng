package com.softdev.weekimessenger.Configuration;
public class Config {

    // Notification Type
    public static final int PUSH_TYPE_GROUP = 1;
    public static final int PUSH_TYPE_USER = 2;

    // Response / Error codes
    public static final int USER_ALREADY_EXISTS = 32;
    public static final int USER_INVALID = 31;
    public static final int PASSWORD_INCORRECT = 28;
    public static final int ACCOUNT_DISABLED = 27;
    public static final int UNKNOWN_ERROR = 404;
    public static final int EMAIL_INVALID = 35;

    // Notification Config
    public static final boolean isSingleLineNotification = true;
    public static final int NOTIFICATION_ID = 100;

    // Client Config
    public static final String KEY_NOTIFICATIONS = "kNotifications";
    public static final String GCM_UPDATED = "99";
    public static final String FRIENDS_UPDATED = "98";
    public static final String INBOX_UPDATE = "97";

    // Server Config
    public static final String BASE_URI = "https://backendplan.com";
    public static final String REGISTER = BASE_URI + "/user/register";
    public static final String LOGIN = BASE_URI + "/user/login";
    public static final String CONVERSATIONS = BASE_URI + "/users/messages/";
    public static final String PRIVATE_MESSAGE = BASE_URI + "/user/message/";
    public static final String GROUP_MESSAGE = BASE_URI + "/group/message/";
    public static final String USER_INFO = BASE_URI + "/user/info/";
    public static final String USERS_DIRECTORY = BASE_URI + "/users/directory/";
    public static final String GROUP_CREATE = BASE_URI + "/groups/create";
    public static final String GROUP_UPDATE = BASE_URI + "/group/update/";
    public static final String GROUP_INFO = BASE_URI + "/group/info/";
    public static final String GCM_UPDATE = BASE_URI + "/user/";
    public static final String DELIVERY_UPDATE = BASE_URI + "/update/message";
    public static final String USER_UPDATE = BASE_URI + "/user/update/";
    public static final String USER_UPDATE_ICON = BASE_URI + "/user/update/icon";
    public static final String GROUP_CONFIG = GROUP_UPDATE + "group-addmember";
}
