package com.snlu.snluapp.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Hong Tae Joon on 2016-11-19.
 */

public class SNLUSharedPreferences {
    public static String get(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences("SNLU", 0);
        return pref.getString(key, "");
    }

    public static void put(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences("SNLU", 0);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(key, value);
        edit.commit();
    }
}
