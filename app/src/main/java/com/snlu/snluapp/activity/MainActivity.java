package com.snlu.snluapp.activity;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
            Intent intent = new Intent(MainActivity.this, RoomActivity.class);
            intent.putExtra("roomNumber", data.getNumber());
            intent.putExtra("roomTitle", data.getTitle());
            intent.putExtra("roomChief", data.getChief());
            intent.putExtra("roomIsStart", data.getIsStart());
            intent.putExtra("documentNumber", data.getStartedDocumentNumber());
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("방 제목을 입력하세요.");
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
                        if(roomItems.size() == 0) {
                            findViewById(R.id.text_help).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.text_help).setVisibility(View.GONE);
                        }
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
            vh.textTitle.setText(roomItems.get(position).getTitle());
            vh.linearLayout.setTag(position);
            if(roomItems.get(position).getIsStart().equals("1")) {
                vh.textProceed.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorRedPink));
                vh.textProceed.setText("회의 진행중");
            }
            else {
                vh.textProceed.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorGrey));
                vh.textProceed.setText("회의 휴식중");
            }
        }

        @Override
        public int getItemCount() {
            return roomItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textTitle, textProceed;
            public LinearLayout linearLayout;
            public ViewHolder(View itemView) {
                super(itemView);
                textTitle = (TextView)itemView.findViewById(R.id.item_room_text);
                textProceed = (TextView)itemView.findViewById(R.id.text_proceed);
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
            json.put("imageurl", LoginInformation.getUserItem().getImagePath());
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
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
