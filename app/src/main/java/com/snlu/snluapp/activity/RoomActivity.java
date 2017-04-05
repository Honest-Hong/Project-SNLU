package com.snlu.snluapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.snlu.snluapp.R;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.dialog.SNLUAlertDialog;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.item.UserItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RoomActivity extends AppCompatActivity {
    public static final int REQUEST_ADD_USER = 100;
    private RoomItem room;
    private boolean isChief;
    private int dividerColor;
    private int dividerHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // 회의방의 정보를 저장한다.
        room = new RoomItem();
        room.setNumber(getIntent().getStringExtra("roomNumber"));
        room.setTitle(getIntent().getStringExtra("roomTitle"));
        room.setChief(getIntent().getStringExtra("roomChief"));

        // 로그인 정보가 없는경우 (알림창으로 접속)
        if(LoginInformation.getUserItem() == null) LoginInformation.loadLoginInformation(this);

        // 현재 사용자가 방장인지를 저장한다.
        isChief = room.getChief().equals(LoginInformation.getUserItem().getPhoneNumber());

        // 회의방의 제목 지정
        getSupportActionBar().setTitle(room.getTitle());

        if(!isChief) {
            findViewById(R.id.room_start_new_conference).setVisibility(View.GONE);
        } else {
            // 새로운 회의 시작
            findViewById(R.id.room_start_new_conference).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SNLUAlertDialog dialog = new SNLUAlertDialog(RoomActivity.this);
                    dialog.setTitle("알림");
                    dialog.setMessage("새로운 회의를 시작하시겠습니까?");
                    dialog.setOnYesClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("roomNumber", room.getNumber());
                                SNLUVolley.getInstance(RoomActivity.this).post("start", json, requestStartListener);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });
        }

        dividerColor = ContextCompat.getColor(this, R.color.colorGrey);
        dividerHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
    }

    // 회의 시작 요청 결과 리스너
    private SNLUVolley.OnResponseListener requestStartListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                SNLULog.v(response.toString());
                String result = response.getString("result");
                if(result.equals("0")) {
                    requestDocumentList();
                    String documentNumber = response.getString("documentNumber");
                    Intent intent = new Intent(RoomActivity.this, ConferenceActivity.class);
                    intent.putExtra("documentNumber", documentNumber);
                    intent.putExtra("roomNumber", room.getNumber());
                    intent.putExtra("roomChief", room.getChief());
                    intent.putExtra("roomTitle", room.getTitle());
                    startActivity(intent);
                    finish();
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 유저 목록 요청
    private void requestUserList() {
        clearUserList();
        try {
            JSONObject json = new JSONObject();
            json.put("roomNumber", room.getNumber());
            SNLUVolley.getInstance(this).post("userList", json, requestUserListListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 유저 목록 요청 결과 리스너
    private SNLUVolley.OnResponseListener requestUserListListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String result = response.getString("result");
                SNLULog.v(response.toString());
                if(result.equals("0")) {
                    JSONArray array = response.getJSONArray("data");
                    ArrayList<UserItem> userItems = new ArrayList<UserItem>();
                    for(int i=0; i<array.length(); i++) {
                        UserItem item = new UserItem();
                        item.setPhoneNumber(array.getJSONObject(i).getString("phoneNumber"));
                        item.setName(array.getJSONObject(i).getString("name"));
                        userItems.add(item);
                    }
                    showUserList(userItems);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 회의록 목록 요청
    private void requestDocumentList() {
        clearDocumentList();
        try {
            JSONObject json = new JSONObject();
            json.put("roomNumber", room.getNumber());
            SNLUVolley.getInstance(this).post("documentList", json, requestDocumentListListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 회의록 목록 요청 결과 리스너
    private SNLUVolley.OnResponseListener requestDocumentListListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String result = response.getString("result");
                SNLULog.v(response.toString());
                if(result.equals("0")) {
                    JSONArray array = response.getJSONArray("data");
                    ArrayList<DocumentItem> documentItems = new ArrayList<>();
                    for(int i=0; i<array.length(); i++) {
                        DocumentItem item = new DocumentItem();
                        item.setNumber(array.getJSONObject(i).getString("documentNumber"));
                        item.setDate(array.getJSONObject(i).getString("date"));
                        documentItems.add(item);
                    }
                    showDocumentList(documentItems);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 유저 목록 생성
    private void showUserList(final ArrayList<UserItem> users) {
        // 사용자 목록에 띄우기
        LinearLayout listUser = (LinearLayout)findViewById(R.id.room_user_list);
        // 회의자 강퇴 버튼 클릭 리스너 정의
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer)(v.getTag());
                SNLUAlertDialog dialog = new SNLUAlertDialog(RoomActivity.this);
                dialog.setTitle("알림");
                dialog.setMessage("정말로 " + users.get(position).getName() + "님을 회의방에서 강퇴하시겠습니까?");
                dialog.setItem(users.get(position));
                dialog.setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SNLUAlertDialog snluAlertDialog = (SNLUAlertDialog)dialog;
                        UserItem item = (UserItem)snluAlertDialog.getItem();
                        requestDelUser(item.getPhoneNumber());
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        };
        // 뷰 생성
        for(int i=0; i<users.size(); i++) {
            if(i!=0) listUser.addView(createDivider(dividerColor, dividerHeight));
            View item = LayoutInflater.from(this).inflate(R.layout.item_user, null);
            TextView tvName = (TextView)item.findViewById(R.id.item_user_name);
            tvName.setText(users.get(i).getName());
            View del = item.findViewById(R.id.item_user_del);
            if(isChief && !users.get(i).getPhoneNumber().equals(LoginInformation.getUserItem().getPhoneNumber())) {
                del.setVisibility(View.VISIBLE);
                del.setTag(i);
                del.setOnClickListener(clickListener);
            } else {
                del.setVisibility(View.GONE);
            }
            listUser.addView(item);
        }
    }

    // 유저 목록 비우기
    private void clearUserList() {
        LinearLayout listUser = (LinearLayout)findViewById(R.id.room_user_list);
        listUser.removeAllViews();
    }

    // 회의록 목록 비우기
    private void clearDocumentList() {
        LinearLayout listDocument = (LinearLayout)findViewById(R.id.room_document_list);
        listDocument.removeAllViews();
    }

    // 회의록 목록 생성
    private void showDocumentList(final ArrayList<DocumentItem> documents) {
        // 회의록 목록에 띄우기
        LinearLayout listDocument = (LinearLayout)findViewById(R.id.room_document_list);
        for(int i=0; i<documents.size(); i++) {
            if(i!=0) listDocument.addView(createDivider(dividerColor, dividerHeight));
            View item = LayoutInflater.from(this).inflate(R.layout.item_document, null);
            TextView tvDate = (TextView)item.findViewById(R.id.item_document_date);
            tvDate.setText(documents.get(i).getDate());
            item.setTag(i);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int)v.getTag();
                    Intent intent = new Intent(RoomActivity.this, DocumentActivity.class);
                    intent.putExtra("documentDate", documents.get(position).getDate());
                    intent.putExtra("documentNumber", documents.get(position).getNumber());
                    startActivity(intent);
                }
            });
            listDocument.addView(item);
        }
    }

    // 회의자 강퇴 요청
    private void requestDelUser(String phoneNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("roomNumber", room.getNumber());
            json.put("phoneNumber", phoneNumber);
            SNLUVolley.getInstance(RoomActivity.this).post("userDel", json, requestDelUserListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 회의자 강퇴 요청 결과 리스너
    private SNLUVolley.OnResponseListener requestDelUserListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String result = response.getString("result");
                if(result.equals("0")) requestUserList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        requestUserList();
        requestDocumentList();
    }

    private View createDivider(int color, int height) {
        View view = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        view.setLayoutParams(params);
        view.setBackgroundColor(color);
        return view;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room, menu);
        if(!isChief) menu.getItem(R.id.menu_invite).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_invite:
                Intent intent = new Intent(RoomActivity.this, CreateRoomActivity.class);
                intent.putExtra("roomNumber", Integer.parseInt(room.getNumber()));
                intent.putExtra("roomTitle", room.getTitle());
                startActivityForResult(intent, REQUEST_ADD_USER);
                return true;
            default:
                return false;
        }
    }
}
