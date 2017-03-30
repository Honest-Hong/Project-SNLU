package com.snlu.snluapp.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.snlu.snluapp.R;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.dialog.CreateRoomDialog;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;
import com.snlu.snluapp.util.SNLUPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SearchView searchView;
    private ListView listView;
    private RoomAdapter roomAdapter;
    private ArrayList<RoomItem> roomItems;
    private ArrayList<RoomItem> searchItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("회의방 목록");

        LoginInformation.loadLoginInformation(this);
        SNLULog.v("token: " + LoginInformation.getToken(this));

        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateRoomDialog dialog = new CreateRoomDialog(MainActivity.this);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        loadRoomList();
                    }
                });
                dialog.show();
            }
        });

        listView = (ListView)findViewById(R.id.main_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomAdapter adapter = (RoomAdapter)parent.getAdapter();
                RoomItem data = (RoomItem)adapter.getItem(position);
                if(data.getIsStart().equals("0")) {
                    Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                    intent.putExtra("roomNumber", data.getNumber());
                    intent.putExtra("roomTitle", data.getTitle());
                    intent.putExtra("roomChief", data.getChief());
                    intent.putExtra("roomIsStart", data.getIsStart());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, ConferenceActivity.class);
                    intent.putExtra("documentNumber", data.getStartedDocumentNumber());
                    intent.putExtra("roomChief", data.getChief());
                    intent.putExtra("roomNumber", data.getNumber());
                    startActivity(intent);
                }
            }
        });
        roomAdapter = new RoomAdapter(this);
        listView.setAdapter(roomAdapter);

        requestRefreshToken();
        FirebaseMessaging.getInstance().subscribeToTopic("notice");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("방 제목 또는 방장의 이름을 입력하세요.");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchItems = new ArrayList<>();
                for(int i=0; i<roomItems.size(); i++) {
                    if (roomItems.get(i).getTitle().contains(newText)) {
                        searchItems.add(roomItems.get(i));
                    }
                }
                roomAdapter.setData(searchItems);

                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                roomAdapter.setData(roomItems);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRoomList();
    }

    private void loadRoomList() {
        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", LoginInformation.getUserItem().getPhoneNumber());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SNLUVolley.getInstance(MainActivity.this).post("roomList", json, new SNLUVolley.OnResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                String result = "";
                try {
                    result = response.getString("result");
                    if(result.equals("0")) {
                        JSONArray array = response.getJSONArray("data");
                        roomItems = new ArrayList<>();
                        for(int i=0; i<array.length(); i++) {
                            RoomItem item = new RoomItem();
                            item.setNumber(array.getJSONObject(i).getString("roomNumber"));
                            item.setTitle(array.getJSONObject(i).getString("title"));
                            item.setChief(array.getJSONObject(i).getString("chiefPhoneNumber"));
                            item.setIsStart(array.getJSONObject(i).getString("isStart"));
                            item.setStartedDocumentNumber(array.getJSONObject(i).getString("documentNumber"));
                            roomItems.add(item);
                        }
                        roomAdapter.setData(roomItems);
                    } else if (result.equals("1")) {
                        // 다른 에러
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class RoomAdapter extends BaseAdapter {
        Context context;
        ArrayList<RoomItem> data;

        public RoomAdapter(Context context) {
            this.context = context;
            data = new ArrayList<>();
        }

        @Override
        public int getCount() {
            if(data == null) return 0;
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null) convertView = LayoutInflater.from(context).inflate(R.layout.item_room, null);
            TextView text = (TextView)convertView.findViewById(R.id.item_room_text);
            text.setText(data.get(position).getTitle());
            return convertView;
        }

        public void setData(ArrayList<RoomItem> data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }

    private void requestRefreshToken() {
        LoginInformation.setToken(this);
        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", LoginInformation.getUserItem().getPhoneNumber());
            json.put("token", LoginInformation.getToken(this));
            SNLUVolley.getInstance(this).post("dong", json, requestRefreshTokenListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SNLUVolley.OnResponseListener requestRefreshTokenListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String result = response.getString("result");
                if(result.equals("0")) {
                    SNLULog.v("성공");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
