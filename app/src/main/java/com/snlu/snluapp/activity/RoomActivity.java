package com.snlu.snluapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.snlu.snluapp.R;
import com.snlu.snluapp.adapter.DocumentsRecyclerAdapter;
import com.snlu.snluapp.adapter.OnItemClickListener;
import com.snlu.snluapp.adapter.UsersRecyclerAdapter;
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
    private RecyclerView recyclerViewUsers, recyclerViewDocuments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // 회의방의 정보를 저장한다.
        room = new RoomItem();
        room.setNumber(getIntent().getStringExtra("roomNumber") + "");
        room.setTitle(getIntent().getStringExtra("roomTitle"));
        room.setChief(getIntent().getStringExtra("roomChief"));

        // 로그인 정보가 없는경우 (알림창으로 접속)
        if(LoginInformation.getUserItem() == null) LoginInformation.loadLoginInformation(this);

        // 현재 사용자가 방장인지를 저장한다.
        isChief = room.getChief().equals(LoginInformation.getUserItem().getId());

        // 회의방의 제목 지정
        getSupportActionBar().setTitle(room.getTitle());

        if(!isChief) {
            findViewById(R.id.button_start).setVisibility(View.GONE);
            findViewById(R.id.linear_start).setVisibility(View.GONE);
        } else {
            // 새로운 회의 시작
            findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
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

        recyclerViewUsers = (RecyclerView)findViewById(R.id.recycler_view_users);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDocuments = (RecyclerView)findViewById(R.id.recycler_view_documents);
        recyclerViewDocuments.setLayoutManager(new LinearLayoutManager(this));
    }

    // 회의 시작 요청 결과 리스너
    private SNLUVolley.OnResponseListener requestStartListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                SNLULog.v(response.toString());
                String result = response.getString("result");
                if(result.equals("0")) {
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
                    ArrayList<UserItem> userItems = new ArrayList<>();
                    for(int i=0; i<array.length(); i++)
                        userItems.add(new UserItem(array.getJSONObject(i)));
                    for(int i=0; i<array.length(); i++)
                        if(userItems.get(i).getId().equals(room.getChief()))
                            userItems.get(i).setSelected(true);
                    recyclerViewUsers.setAdapter(new UsersRecyclerAdapter(RoomActivity.this, userItems, new OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            UsersRecyclerAdapter adapter = (UsersRecyclerAdapter)recyclerViewUsers.getAdapter();
                            SNLUAlertDialog dialog = new SNLUAlertDialog(RoomActivity.this);
                            dialog.setTitle("알림");
                            dialog.setMessage("정말로 " + adapter.getItem(position).getName() + "님을 회의방에서 강퇴하시겠습니까?");
                            dialog.setItem(adapter.getItem(position));
                            dialog.setOnYesClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SNLUAlertDialog snluAlertDialog = (SNLUAlertDialog)dialog;
                                    UserItem item = (UserItem)snluAlertDialog.getItem();
                                    requestDelUser(item.getId());
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                    }, isChief));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 회의록 목록 요청
    private void requestDocumentList() {
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
                        item.setTitle(array.getJSONObject(i).getString("title"));
                        documentItems.add(item);
                    }
                    recyclerViewDocuments.setAdapter(new DocumentsRecyclerAdapter(RoomActivity.this, documentItems, new OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            DocumentsRecyclerAdapter adapter = (DocumentsRecyclerAdapter)recyclerViewDocuments.getAdapter();
                            Intent intent = new Intent(RoomActivity.this, DocumentActivity.class);
                            intent.putExtra("documentDate", adapter.getItem(position).getDate());
                            intent.putExtra("documentNumber", adapter.getItem(position).getNumber());
                            intent.putExtra("documentTitle", adapter.getItem(position).getTitle());
                            intent.putExtra("roomNumber", room.getNumber());
                            startActivity(intent);
                        }
                    }));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room, menu);
        if(!isChief) menu.findItem(R.id.menu_invite).setVisible(false);
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
