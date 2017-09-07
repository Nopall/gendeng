package com.softdev.weekimessenger.Configuration;

import android.content.Context;
import android.content.SharedPreferences;

public class DataStorage {
    private String TAG = DataStorage.class.getSimpleName();
    static SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Context mContext;

    public DataStorage(Context context) {
        this.mContext = context;
        prefs = mContext.getSharedPreferences("com.weeki-messenger", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void addNotification(String notification) {
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(Config.KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return prefs.getString(Config.KEY_NOTIFICATIONS, null);
    }

    public String getString(String key, String defValue){
        return prefs.getString(key,defValue);
    }

    public Boolean getBoolean(String key, boolean defValue){ return prefs.getBoolean(key, defValue); }

    public Integer getInt(String key, Integer defValue){
        return prefs.getInt(key, defValue);
    }

    public void setString(String key, String value){
        prefs.edit().putString(key, value).apply();
    }

    public void setInt(String key, Integer value){
        prefs.edit().putInt(key, value).apply();
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
