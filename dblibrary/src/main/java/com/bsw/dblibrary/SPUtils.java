package com.bsw.dblibrary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

/**
 * SharedPreferences工具类
 *
 * @author bsw
 */
public class SPUtils {
    private SharedPreferences preferences = null;
    private static SPUtils utils;

    private SPUtils(Context context) {
        initPreferences(context);
    }

    public static SPUtils getInstance(Context context) {
        if (utils == null) {
            utils = new SPUtils(context);
        }
        return utils;
    }

    private void initPreferences(Context context) {
        if (preferences == null) {
            preferences = context.getApplicationContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        }
    }

    public SPUtils put(String key, String value) {
        preferences.edit().putString(key, value).apply();
        return utils;
    }

    public SPUtils put(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
        return utils;
    }

    public SPUtils put(String key, int value) {
        preferences.edit().putInt(key, value).apply();
        return utils;
    }

    public SPUtils put(String key, Activity activity) {
        preferences.edit().putString(key, activity.getClass().getName()).apply();
        return utils;
    }

    public SPUtils put(String key, Class clazz) {
        preferences.edit().putString(key, clazz.getName()).apply();
        return utils;
    }

    public SPUtils put(String key, float value) {
        preferences.edit().putFloat(key, value).apply();
        return utils;
    }

    public SPUtils put(String key, long value) {
        preferences.edit().putLong(key, value).apply();
        return utils;
    }

    public SPUtils put(String key, Set<String> values) {
        preferences.edit().putStringSet(key, values).apply();
        return utils;
    }

    public String getString(String key, String defaultString) {
        return preferences.getString(key, defaultString);
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultBoolean) {
        return preferences.getBoolean(key, defaultBoolean);
    }

    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public int getInt(String key, int defaultInt) {
        return preferences.getInt(key, defaultInt);
    }

    public float getFloat(String key, float defaultFloat) {
        return preferences.getFloat(key, defaultFloat);
    }

    public Class getClass(String key, Class defaultClass) throws ClassNotFoundException {
        // 存储Class实际存储的就是String，所以这里需要先提取String，再转为Class
        return Class.forName(preferences.getString(key, defaultClass.getName()));
    }

    public float getFloat(String key) {
        return preferences.getFloat(key, 0);
    }

    public long getLong(String key) {
        return preferences.getLong(key, 0);
    }

    public long getLong(String key, long defaultLong) {
        return preferences.getLong(key, defaultLong);
    }

    public Set<String> getStringSet(String key) {
        return preferences.getStringSet(key, null);
    }

    public Set<String> getStringSet(String key, Set<String> defaultStringSet) {
        return preferences.getStringSet(key, defaultStringSet);
    }

    public Map<String, ?> getStringSet() {
        return preferences.getAll();
    }

    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
