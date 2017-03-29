package com.snlu.snluapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.snlu.snluapp.R;
import com.snlu.snluapp.activity.RoomActivity;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hong Tae Joon on 2016-11-11.
 */

public class CreateRoomDialog extends Dialog {
    public CreateRoomDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_create_room);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        findViewById(R.id.dialog_create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자의 전화번호
                String phoneNumber = LoginInformation.getUserItem().getPhoneNumber();

                // 회의방 제목
                EditText editTitle = (EditText)findViewById(R.id.dialog_create_edit_title);
                String title = editTitle.getText().toString();

                // 방생성 요청
                if(title.equals("")) Toast.makeText(getContext(), "방 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                else requestCreateRoom(phoneNumber, title);
            }
        });
    }

    private void requestCreateRoom(String phoneNumber, String title) {
        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", phoneNumber);
            json.put("title", title);
            SNLUVolley.getInstance(getContext()).post("roomAdd", json, responseListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SNLUVolley.OnResponseListener responseListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String result = response.getString("result");
                if(result.equals("0")) {
                }
                dismiss();
            } catch (JSONException e) {
            }
        }
    };
}
