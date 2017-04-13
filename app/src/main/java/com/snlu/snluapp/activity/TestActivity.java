package com.snlu.snluapp.activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.snlu.snluapp.R;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        requestSpeech();
    }

    private void requestSpeech() {
        String key = "AIzaSyAoHunlmq6WezotdFL_MeBBsOQi9-aYHPI";
        JSONObject json = new JSONObject();
        JSONObject config = new JSONObject();
        JSONObject audio = new JSONObject();
        try {
            config.put("encoding", "LINEAR16");
            config.put("sampleRate", 16000);
            InputStream in = getRawFile();
            if(in == null) {
                Log.v("requestSpeech", "null");
                return;
            }
            audio.put("content", getBase64String(in));
            json.put("config", config);
            json.put("audio", audio);
            SNLUVolley.getInstance(this).requestSpeech("https://speech.googleapis.com/v1beta1/speech:syncrecognize?key=" + key, json, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        JSONArray alternatives = results.getJSONObject(0).getJSONArray("alternatives");
                        String transcript = alternatives.getJSONObject(0).getString("transcript");
                        Log.v("transcript", transcript);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getBase64String(@NonNull InputStream in) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        try {
            while ( (n = in.read(buf)) != -1)
                baos.write(buf, 0, n);
            byte[] audioBytes = baos.toByteArray();
            return Base64.encodeToString(audioBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Nullable
    private InputStream getRawFile() {
        return getResources().openRawResource(R.raw.test);
    }
}
