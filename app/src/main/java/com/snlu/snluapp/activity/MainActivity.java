package com.snlu.snluapp.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.snlu.snluapp.R;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SearchView searchView;
    private RecyclerView recyclerView;
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

        findViewById(R.id.button_add).setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new RoomAdapter(this);
        recyclerView.setAdapter(roomAdapter);

        requestRefreshToken();
        FirebaseMessaging.getInstance().subscribeToTopic("notice");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_add)
            startActivity(new Intent(MainActivity.this, CreateRoomActivity.class));
        else {
            int position = (int)v.getTag();
            RoomItem data = roomAdapter.getItem(position);
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
                roomAdapter.setItems(searchItems);

                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                roomAdapter.setItems(roomItems);
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
            json.put("phoneNumber", LoginInformation.getUserItem().getId());
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
                        roomItems = new ArrayList<>();
                        JSONArray array = response.getJSONArray("data");
                        for(int i=0; i<array.length(); i++) {
                            RoomItem item = new RoomItem();
                            item.setNumber(array.getJSONObject(i).getString("roomNumber"));
                            item.setTitle(array.getJSONObject(i).getString("title"));
                            item.setChief(array.getJSONObject(i).getString("chiefPhoneNumber"));
                            item.setIsStart(array.getJSONObject(i).getString("isStart"));
                            item.setStartedDocumentNumber(array.getJSONObject(i).getString("documentNumber"));
                            roomItems.add(item);
                        }
                        roomAdapter.setItems(roomItems);
                    } else if (result.equals("1")) {
                        // 다른 에러
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class RoomAdapter extends RecyclerView.Adapter {
        private Context context;
        private ArrayList<RoomItem> roomItems;

        public RoomAdapter(Context context) {
            this.context = context;
            roomItems = new ArrayList<>();
        }

        public void setItems(ArrayList<RoomItem> items) {
            this.roomItems = items;
            notifyDataSetChanged();
        }

        public RoomItem getItem(int position) {
            return roomItems.get(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_room, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder vh = (ViewHolder)holder;
            vh.textView.setText(roomItems.get(position).getTitle());
            vh.linearLayout.setTag(position);
        }

        @Override
        public int getItemCount() {
            return roomItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;
            public LinearLayout linearLayout;
            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView)itemView.findViewById(R.id.item_room_text);
                linearLayout = (LinearLayout)itemView.findViewById(R.id.linear_layout);
                linearLayout.setOnClickListener(MainActivity.this);
            }
        }
    }

    private void requestRefreshToken() {
        LoginInformation.setToken(this);
        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", LoginInformation.getUserItem().getId());
            json.put("token", LoginInformation.getToken(this));
            json.put("imgaeurl", LoginInformation.getUserItem().getImagePath());
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
