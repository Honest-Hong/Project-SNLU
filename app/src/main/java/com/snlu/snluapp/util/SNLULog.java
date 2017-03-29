package com.snlu.snluapp.util;

import android.util.Log;

/**
 * Created by Hong Tae Joon on 2016-11-27.
 */

public class SNLULog {
    public static final String TAG = "SNLU_LOG";
    public static void v(String log) {
        if(log!=null) Log.v(TAG, log);
    }
}
