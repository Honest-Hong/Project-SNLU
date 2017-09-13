package com.snlu.snluapp.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.snlu.snluapp.R;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.item.UserItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.text_title) TextView textTitle;
    @BindView(R.id.button_search) ImageView buttonSearch;
    @BindView(R.id.edit_search) EditText editSearch;
    private RoomAdapter roomAdapter;
    private ArrayList<RoomItem> roomItems = new ArrayList<>();
    private HashMap<String, Integer> roomMap;
    private ArrayList<RoomItem> searchItems;
    private boolean searching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        textTitle.setText("회의방");

        LoginInformation.loadLoginInformation(this);
        SNLULog.v("token: " + LoginInformation.getToken(this));

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new RoomAdapter(this);
        recyclerView.setAdapter(roomAdapter);

        requestRefreshToken();
        FirebaseMessaging.getInstance().subscribeToTopic("notice");

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchItems = new ArrayList<>();
                for(RoomItem item : roomItems) {
                    if(item.getTitle().startsWith(s.toString())) {
                        searchItems.add(item);
                    }
                }
                roomAdapter.setItems(searchItems);
            }
        });
    }

    public void onClick(View v) {
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

    @OnClick(R.id.button_search)
    public void doSearch() {
        if(searching) {
            buttonSearch.setImageResource(R.drawable.ic_search_white);
            textTitle.setVisibility(View.VISIBLE);
            editSearch.setVisibility(View.GONE);
            roomAdapter.setItems(roomItems);
        } else {
            buttonSearch.setImageResource(R.drawable.ic_clear_white_24dp);
            textTitle.setVisibility(View.GONE);
            editSearch.setVisibility(View.VISIBLE);
            editSearch.setText("");
        }
        searching = !searching;
    }

    @OnClick({R.id.button_star, R.id.button_add, R.id.button_profile})
    public void onClickMenu(View v) {
        switch(v.getId()) {
            case R.id.button_star:
                break;
            case R.id.button_add:
                startActivity(new Intent(this, CreateRoomActivity.class));
                break;
            case R.id.button_profile:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(searching) {
            doSearch();
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
                        ArrayList<RoomItem> temp = new ArrayList<>();
                        roomMap = new HashMap<>();
                        JSONArray array = response.getJSONArray("data");
                        for(int i=0; i<array.length(); i++) {
                            RoomItem item = new RoomItem();
                            item.setNumber(array.getJSONObject(i).getString("roomNumber"));
                            item.setTitle(array.getJSONObject(i).getString("title"));
                            item.setChief(array.getJSONObject(i).getString("chiefPhoneNumber"));
                            item.setIsStart(array.getJSONObject(i).getString("isStart"));
                            item.setStartedDocumentNumber(array.getJSONObject(i).getString("documentNumber"));
                            item.setCount(array.getJSONObject(i).getInt("count"));
                            temp.add(item);
                            roomMap.put(item.getNumber(), i);
                        }
                        if(roomItems.size() != temp.size()) {
                            roomItems = temp;
                            roomAdapter.setItems(roomItems);
                        }
                        if(roomItems.size() == 0) {
                            findViewById(R.id.text_help).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.text_help).setVisibility(View.GONE);
                            requestUserList();
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

    // 유저 목록 요청
    private void requestUserList() {
        try {
            for(RoomItem item : roomItems) {
                JSONObject json = new JSONObject();
                json.put("roomNumber", item.getNumber());
                SNLUVolley.getInstance(this).post("userList", json, requestUserListListener);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 유저 목록 요청 결과 리스너
    private SNLUVolley.OnResponseListener requestUserListListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String roomNumber = response.getString("result");
                SNLULog.v(response.toString());
                JSONArray array = response.getJSONArray("data");
                ArrayList<UserItem> userItems = new ArrayList<>();
                for(int i=0; i<array.length(); i++)
                    userItems.add(new UserItem(array.getJSONObject(i)));
                int realIndex = roomMap.get(roomNumber);
                if(roomItems.get(realIndex).getUsers().size() != userItems.size()) {
                    roomItems.get(realIndex).setUsers(userItems);
                    roomAdapter.notifyItemChanged(realIndex);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    class RoomAdapter extends RecyclerView.Adapter {
        private Context context;
        private ArrayList<RoomItem> data;

        public RoomAdapter(Context context) {
            this.context = context;
            data = new ArrayList<>();
        }

        public void setItems(ArrayList<RoomItem> items) {
            data = items;
            notifyDataSetChanged();
        }

        public void removeItem(int position) {
            notifyItemRemoved(position);
            data.remove(position);
        }

        public RoomItem getItem(int position) {
            return data.get(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_room, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder vh = (ViewHolder)holder;
            vh.textTitle.setText(data.get(position).getTitle());
            vh.cardView.setTag(position);
            if(data.get(position).getIsStart().equals("1")) {
                vh.textProceed.setText("회의중");
                vh.imageState.setImageResource(R.drawable.ic_circle_green);
            }
            else {
                vh.textProceed.setText("회의종료");
                vh.imageState.setImageResource(R.drawable.ic_circle_red);
            }
            vh.textCount.setText(data.get(position).getCount() + "");
            String str = "";
            for(UserItem item : data.get(position).getUsers()) {
                str += item.getName() + ", ";
            }
            if(str.length() > 0) {
                vh.textMember.setText(str.substring(0, str.length() - 2));
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.card_view) CardView cardView;
            @BindView(R.id.text_title) TextView textTitle;
            @BindView(R.id.text_proceed) TextView textProceed;
            @BindView(R.id.text_member) TextView textMember;
            @BindView(R.id.text_amount) TextView textCount;
            @BindView(R.id.image_state) ImageView imageState;
            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                cardView.setOnClickListener(MainActivity.this);
                cardView.setOnLongClickListener(MainActivity.this);
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

    private int deletePos;
    @Override
    public boolean onLongClick(View v) {
        deletePos = (int)v.getTag();
        new AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage("정말로 " + roomItems.get(deletePos).getTitle() + "방을 삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject param = new JSONObject();
                        try {
                            param.put("roomNumber", roomItems.get(deletePos).getNumber());
                            SNLUVolley.getInstance(MainActivity.this).post("roomDelete", param, new SNLUVolley.OnResponseListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        int result = response.getInt("result");
                                        if(result == 0) {
                                            roomAdapter.removeItem(deletePos);
                                            Toast.makeText(MainActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        return true;
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
