package com.snlu.snluapp.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.snlu.snluapp.GlideApp;
import com.snlu.snluapp.R;
import com.snlu.snluapp.util.SNLUSharedPreferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imageBackground = (ImageView) findViewById(R.id.image_background);
        GlideApp.with(this)
                .load(R.drawable.background_splash)
                .centerCrop()
                .into(imageBackground);

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
