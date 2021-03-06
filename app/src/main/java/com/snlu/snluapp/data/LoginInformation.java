package com.snlu.snluapp.data;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.snlu.snluapp.item.UserItem;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUSharedPreferences;

import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2016-11-18.
 */

public class LoginInformation {
    private static UserItem userItem;

    public static void loadLoginInformation(Context context) {
        // 로그인 정보 초기화
        String userPhoneNumber = SNLUSharedPreferences.get(context, "user_phone_number");
        String userName = SNLUSharedPreferences.get(context, "user_name");
        String userImagePath = SNLUSharedPreferences.get(context, "user_image_path");
        UserItem userItem = new UserItem(userPhoneNumber, userName, userImagePath, false);
        setUserItem(userItem);
    }

    public static UserItem getUserItem() {
        return userItem;
    }

    public static void setUserItem(UserItem userItem) {
        LoginInformation.userItem = userItem;
    }

    public static void setToken(Context context) {
        SNLULog.v("token : " + FirebaseInstanceId.getInstance().getToken());
        SNLUSharedPreferences.put(context, "token", FirebaseInstanceId.getInstance().getToken());
    }

    public static String getToken(Context context) {
        return SNLUSharedPreferences.get(context, "token");
    }
}
