package com.snlu.snluapp.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Hong Tae Joon on 2016-11-19.
 */

public class SNLUPermission {

    // 권한을 검사할 필요가 없는 경우 true 리턴
    /*
     activity - 부른 액티비티
     permission - 권한 이름
     requestCode - onActivityResult 요청 코드
     */
    public static boolean checkPermission(AppCompatActivity activity, String permission, int requestCode) {
        //권한이 없는 경우
        if(Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                //최초 거부를 선택하면 두번째부터 이벤트 발생 & 권한 획득이 필요한 이융를 설명
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                }
                //요청 팝업 선택시 onRequestPermissionsResult 이동
                activity.requestPermissions(new String[]{permission}, requestCode);
                SNLULog.v(permission + " false " + requestCode);
                return false;
            }
            //권한이 있는 경우
            else {
                SNLULog.v(permission + " true " + requestCode);
                return true;
            }
        }
        return true;
    }
}
