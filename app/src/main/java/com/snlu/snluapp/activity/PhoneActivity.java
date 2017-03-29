package com.snlu.snluapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.snlu.snluapp.R;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUPermission;
import com.snlu.snluapp.util.SNLUSharedPreferences;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import static java.lang.Integer.parseInt;

public class PhoneActivity extends AppCompatActivity {
    private static final int REQUEST_SEND_SMS_PERMISSION = 100;
    private static final int REQUEST_READ_PHONE_STATE_PERMISSION = 101;
    Random mRand;
    Button btn;
    int mResult;
    Context context;
    EditText putPhone;
    String phoneNum;
    TextView textTimer;
    private long startTime=2*1000*60;
    private long interval=1*1000;
    private boolean timerHasStarted=false;
    EditText putCertify;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        mRand = new Random();
        btn = (Button) findViewById(R.id.phone_button);
        putPhone = (EditText) findViewById(R.id.phone_putPhone);

        if(SNLUPermission.checkPermission(this, Manifest.permission.READ_PHONE_STATE, REQUEST_READ_PHONE_STATE_PERMISSION)) getPhoneNumber();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn.getText().equals("확인")) {
                    Certify();
                } else {
                    requestDuplicateNumber();
                }
            }
        });
        textTimer = (TextView) findViewById(R.id.phone_text_time);

        textTimer.setText((startTime / 1000)/60 + "분"+(startTime/1000)%60+"초");
    }

    private void requestDuplicateNumber() {
        try {
            JSONObject json = new JSONObject();
            json.put("phoneNumber", putPhone.getText().toString());
            final Intent intent = new Intent(PhoneActivity.this,InfoUserActivity.class);
            SNLUVolley.getInstance(this).post("isDuplicate", json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String count = response.getString("result");
                        if(count.equals("0")) {
                            if(SNLUPermission.checkPermission(PhoneActivity.this, Manifest.permission.SEND_SMS, REQUEST_SEND_SMS_PERMISSION)) sendSMS();
                            countDownTimer = new PhoneActivity.MyCountDownTimer(startTime, interval);
                            if(timerHasStarted==false) {
                                countDownTimer.start();

                                timerHasStarted = true;
                            }
                            btn.setText("확인");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void Certify() {
        putCertify = (EditText) findViewById(R.id.phone_putCertify);
        if (putCertify.getText().toString().equals("")) {
            Toast.makeText(PhoneActivity.this, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            int number = parseInt(putCertify.getText().toString());
            if (mResult == number) {
                countDownTimer.cancel();
                timerHasStarted = false;

                Toast.makeText(PhoneActivity.this, "인증되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PhoneActivity.this, InfoUserActivity.class);
                intent.putExtra("phoneNumber", putPhone.getText().toString());
                startActivity(intent);
                countDownTimer.cancel();
                finish();
            } else {
                Toast.makeText(PhoneActivity.this, "인증번호와 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                btn.setText("인증번호 전송");
                countDownTimer.cancel();
                return;
            }
        }
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long statTime,long interval){
            super(statTime,interval);
        }
        public void onFinish(){
            Toast.makeText(PhoneActivity.this,"시간이 초과되었습니다.",Toast.LENGTH_SHORT).show();
            countDownTimer.cancel();
            btn.setText("인증번호 전송");
        }
        public void onTick(long millisUntiFinished){
            textTimer.setText("남은시간 : "+(millisUntiFinished/1000)/60+"분"+(millisUntiFinished/1000)%60+"초");
            Log.d("tick",millisUntiFinished/1000+"");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 권한 요청 결과
        if(requestCode == REQUEST_SEND_SMS_PERMISSION) {
            // 승인인 경우 문자 전송
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) sendSMS();
        } else if(requestCode == REQUEST_READ_PHONE_STATE_PERMISSION) {
            // 승인인 경우 번호 받아오기
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) getPhoneNumber();
        }
    }

    private void getPhoneNumber() {
        TelephonyManager telephonyManager= (TelephonyManager)getSystemService(context.TELEPHONY_SERVICE);
        phoneNum = telephonyManager.getLine1Number();
        if(phoneNum != null) {
            phoneNum = "0" + phoneNum.substring(3);
        } else {
            SNLUSharedPreferences.put(this, "logined", "Y");
            SNLUSharedPreferences.put(this, "user_phone_number", "01055966432");
            SNLUSharedPreferences.put(this, "user_name", "홍태준");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        putPhone.setText(phoneNum);
    }
    private void sendSMS() {
        mResult = mRand.nextInt(8999) + 1000;
        __sendSMS(phoneNum, mResult);
    }
    private void __sendSMS(String phoneNumber,int message){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber,null,"인증번호는 : "+ message+"입니다.",null,null);
    }
}
