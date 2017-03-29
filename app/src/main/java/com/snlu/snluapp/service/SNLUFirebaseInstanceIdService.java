package com.snlu.snluapp.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.util.SNLULog;

public class SNLUFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SNLULog.v("Refreshed token: " + refreshedToken);
        LoginInformation.setToken(getApplicationContext());
    }
}
