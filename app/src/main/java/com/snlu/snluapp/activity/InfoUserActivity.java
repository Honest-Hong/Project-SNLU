package com.snlu.snluapp.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.snlu.snluapp.R;
import com.snlu.snluapp.util.SNLUSharedPreferences;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONException;
import org.json.JSONObject;

import static com.snlu.snluapp.R.id.certify_putName;

public class InfoUserActivity extends AppCompatActivity {

    Button btn;
    EditText Name;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certify);

        btn=(Button)findViewById(R.id.certify_button);
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        Name = (EditText)findViewById(R.id.certify_putName);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("phoneNumber", phoneNumber);
                    json.put("name", Name.getText().toString());
                   SNLUVolley.getInstance(InfoUserActivity.this).post("join", json, new Response.Listener<JSONObject>() {
                        public void onResponse(JSONObject response) {
                            SNLUSharedPreferences.put(InfoUserActivity.this, "logined", "Y");
                            SNLUSharedPreferences.put(InfoUserActivity.this, "user_phone_number", phoneNumber);
                            SNLUSharedPreferences.put(InfoUserActivity.this, "user_name", Name.getText().toString());
                            Intent intent = new Intent(InfoUserActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    Log.d("이름",Name.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
