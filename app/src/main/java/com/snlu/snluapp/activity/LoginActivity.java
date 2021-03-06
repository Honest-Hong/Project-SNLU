package com.snlu.snluapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.KakaoParameterException;
import com.kakao.util.exception.KakaoException;
import com.snlu.snluapp.GlideApp;
import com.snlu.snluapp.R;
import com.snlu.snluapp.util.SNLUPermission;
import com.snlu.snluapp.util.SNLUSharedPreferences;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, ISessionCallback, FacebookCallback<LoginResult> {
    private final static int REQUEST_READ_PHONE_STATE_PERMISSION = 100;
    private CallbackManager facebookCallbackManager;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        ImageView imageBackground = (ImageView) findViewById(R.id.image_background);
        GlideApp.with(this)
                .load(R.drawable.background_splash)
                .centerCrop()
                .into(imageBackground);

        //getAppKeyHash();
        Session.getCurrentSession().addCallback(this);
        Session.getCurrentSession().checkAndImplicitOpen();
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager, this);

        if(Profile.getCurrentProfile() != null) startMainActivity();
        if(SNLUPermission.checkPermission(this, Manifest.permission.READ_PHONE_STATE, REQUEST_READ_PHONE_STATE_PERMISSION)) getPhoneNumber();
    }

    private void getPhoneNumber() {
        TelephonyManager telephonyManager= (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        TextView textPhone = (TextView)findViewById(R.id.text_phone);

        phoneNumber = telephonyManager.getLine1Number();
        if(phoneNumber != null) {
            if(phoneNumber.charAt(0) == '+')
                phoneNumber = "0" + phoneNumber.substring(3);
            String str = phoneNumber.substring(0,3) + "-" + phoneNumber.substring(3, 7) + "-" + phoneNumber.substring(7, 11);
            textPhone.setText(str);
        } else {
            textPhone.setText("존재하지 않음");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 권한 요청 결과
        if(requestCode == REQUEST_READ_PHONE_STATE_PERMISSION) {
            // 승인인 경우 번호 받아오기
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) getPhoneNumber();
        }
    }

    // 카카오 로그인 처리
    @Override
    public void onSessionOpened() {
        //Log.v("Kakao", "onSessionOpened");
        requestKaKaoInformation();
    }

    @Override
    public void onSessionOpenFailed(KakaoException exception) {
        Toast.makeText(LoginActivity.this, "onSessionOpenFailed: " + exception, Toast.LENGTH_LONG).show();
        //Log.e("Kakao", "onSessionOpenFailed: " + exception);
    }

    private void requestKaKaoInformation() {
        List<String> propertyKeys = new ArrayList<>();
        propertyKeys.add("kaccount_email");
        propertyKeys.add("nickname");
        propertyKeys.add("thumbnail_image");

        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Toast.makeText(LoginActivity.this, "onSessionClosed: " + errorResult.getErrorMessage(), Toast.LENGTH_LONG).show();
                Log.e("Kakao", "onSessionClosed: " + errorResult.getErrorMessage());
            }

            @Override
            public void onNotSignedUp() {
                Toast.makeText(LoginActivity.this, "onNotSignedUp", Toast.LENGTH_LONG).show();
                Log.e("Kakao", "onNotSignedUp");
            }

            @Override
            public void onSuccess(UserProfile result) {
                String userName = result.getNickname();
                String userEmail = result.getEmail();
                String userImage = result.getThumbnailImagePath();
                if(phoneNumber == null) requestSignUp(userEmail, userName, userImage);
                else requestSignUp(phoneNumber, userName, userImage);
            }
        }, propertyKeys, false);
    }
    // 카카오 로그인 처리

    // 페이스북 로그인 처리
//    private ProfileTracker mProfileTracker;
    @Override
    public void onSuccess(LoginResult loginResult) {
        Profile.fetchProfileForCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String userName = object.getString("name");
                    String userEmail = object.getString("email");
                    JSONObject picture = object.getJSONObject("picture");
                    String userImage = picture.getJSONObject("data").getString("url");
                    if(phoneNumber == null) requestSignUp(userEmail, userName, userImage);
                    else requestSignUp(phoneNumber, userName, userImage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle arguments = new Bundle();
        arguments.putString("fields", "name,email,picture");
        request.setParameters(arguments);
        request.executeAsync();
    }

    @Override
    public void onCancel() {
        Log.e("Facebook", "onCancel");
    }

    @Override
    public void onError(FacebookException error) {
        Toast.makeText(LoginActivity.this, "onError: " + error.toString() , Toast.LENGTH_LONG).show();
        Log.e("Facebook", "onError: " + error.toString());
    }
    // 페이스북 로그인 처리

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) return;
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void requestSignUp(final String phoneNumber, final String name, final String image) {
        //Log.d("SignUp", String.format("userName(%s), userEmail(%s), userImage(%s)", name, phoneNumber, image));
        try {
            JSONObject json = new JSONObject();
            json.put("phoneNumber", phoneNumber);
            json.put("name", name);
            json.put("imageurl", image);
            SNLUVolley.getInstance(this).post("join", json, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        int result = response.getInt("result");
                        if(result == 0) {
                            Toast.makeText(LoginActivity.this, "환영합니다 " + name + "님!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "또 뵙네요 " + name + "님!", Toast.LENGTH_LONG).show();
                        }
                        storeLoginInform(phoneNumber, name, image);
                        startMainActivity();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void storeLoginInform(String phoneNumber, String name, String image) {
        SNLUSharedPreferences.put(LoginActivity.this, "logined", "Y");
        SNLUSharedPreferences.put(LoginActivity.this, "user_phone_number", phoneNumber);
        SNLUSharedPreferences.put(LoginActivity.this, "user_name", name);
        SNLUSharedPreferences.put(LoginActivity.this, "user_image_path", image);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick({R.id.button_kakao, R.id.button_facebook})
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_kakao:
                Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, this);
                break;
            case R.id.button_facebook:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
                break;
        }
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Toast.makeText(this, something, Toast.LENGTH_LONG).show();
                Log.d("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }
}
