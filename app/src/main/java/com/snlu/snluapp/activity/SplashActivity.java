package com.snlu.snluapp.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.snlu.snluapp.R;
import com.snlu.snluapp.util.SNLUSharedPreferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(userFristLogin()) {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        },1500);
    }

    private boolean userFristLogin() {
        String str = SNLUSharedPreferences.get(SplashActivity.this, "logined");
        if(str.equals("Y")) {
            return false;
        } else {
            return true;
        }
    }
}
