package com.snlu.snluapp.activity;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.snlu.snluapp.R;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.item.UserItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CreateRoomActivity extends AppCompatActivity implements View.OnClickListener{
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private EditText editTitle;
    private TextView buttonCreate;
    private InviteItem searchItem;
    private UserItem userItem;
    private int roomNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        editTitle = (EditText)findViewById(R.id.edit_title);
        buttonCreate = (TextView)findViewById(R.id.button_create);

        buttonCreate.setOnClickListener(this);
        findViewById(R.id.button_search).setOnClickListener(this);
        findViewById(R.id.button).setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        ArrayList<InviteItem> inviteItems = new ArrayList<>();
        roomNumber = getIntent().getIntExtra("roomNumber", 0);
        userItem = LoginInformation.getUserItem();
        if(roomNumber != 0) {
            getSupportActionBar().setTitle("회의자 추가");
            buttonCreate.setText("회의자 추가하기");
            editTitle.setEnabled(false);
            editTitle.setText(getIntent().getStringExtra("roomTitle"));
            requestUserList(roomNumber);
            findViewById(R.id.edit_search).requestFocus();
        } else {
            getSupportActionBar().setTitle("회의방 생성");
            buttonCreate.setText("회의방 생성하기");
            inviteItems.add(new InviteItem(userItem.getPhoneNumber(), userItem.getName(), false));
        }
        adapter = new ListAdapter(inviteItems);
        recyclerView.setAdapter(adapter);

        ImageView button = (ImageView)findViewById(R.id.button);
        button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_plus));
        setSearchResult(null);
    }

    private void setSearchResult(InviteItem item) {
        searchItem = item;
        ImageView imageView = (ImageView)findViewById(R.id.image_view);
        TextView textView = (TextView)findViewById(R.id.text_view);
        ImageView button = (ImageView)findViewById(R.id.button);
        if(item == null) {
            imageView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            textView.setText(item.userId + "\t\t"+ item.userName);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_create:
                if(roomNumber == 0) requestCreateRoom(editTitle.getText().toString(), userItem.getPhoneNumber());
                else {
                    ArrayList<InviteItem> inviteItems = new ArrayList<>();
                    for(int i=0; i<adapter.data.size(); i++) if(adapter.data.get(i).canDelete) inviteItems.add(adapter.data.get(i));
                    requestInvite(roomNumber, inviteItems);
                }
                break;
            case R.id.button_search:
                EditText editText = (EditText)findViewById(R.id.edit_search);
                requestSearch(editText.getText().toString());
                break;
            case R.id.button:
                // 회의자를 리사이클러 뷰에 추가하는 과정
                boolean isExisted = false;
                for(int i=0; i<adapter.data.size(); i++) {
                    if(adapter.data.get(i).userId.equals(searchItem.userId)) {
                        isExisted = true;
                        break;
                    }
                }
                if(isExisted) Snackbar.make(getWindow().getDecorView().getRootView(), "이미 회의자로 등록되어있습니다.", 2000).show();
                else adapter.addItem(searchItem);
                setSearchResult(null);
                break;
        }
    }

    private void requestSearch(String userId) {
        JSONObject json = new JSONObject();
        try {
            json.put("phoneNumber", userId);
            SNLUVolley.getInstance(this).post("confirmUser", json, onResultSearch);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SNLUVolley.OnResponseListener onResultSearch = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                int result = response.getInt("result");
                if(result == 0) {
                    JSONArray data = response.getJSONArray("data");
                    JSONObject user = data.getJSONObject(0);
                    setSearchResult(new InviteItem(user.getString("phoneNumber"), user.getString("name"), true));
                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "존재하지 않는 번호 또는 이메일입니다.", 2000).show();
                    setSearchResult(null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void requestCreateRoom(String title, String phoneNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("phoneNumber", phoneNumber);
            SNLUVolley.getInstance(this).post("roomAdd", json, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int result = response.getInt("result");
                        if(result == 0) {
                            ArrayList<InviteItem> inviteItems = new ArrayList<>();
                            for(int i=0; i<adapter.data.size(); i++)
                                if(adapter.data.get(i).canDelete) inviteItems.add(adapter.data.get(i));
                            requestInvite(response.getInt("roomNumber"), inviteItems);
                        } else {
                            Snackbar.make(getWindow().getDecorView().getRootView(), "방을 생성하는데 실패하였습니다.", 2000).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestInvite(final int roomNumber, ArrayList<InviteItem> inviteItems) {
        try {
            JSONObject json = new JSONObject();
            json.put("roomNumber",roomNumber);
            String str = "[";
            for (int i = 0; i < inviteItems.size(); i++) {
                if(i!=0) str += ",";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("phoneNumber", inviteItems.get(i).userId.replace("-", ""));
                str += jsonObject.toString();
            }
            str += "]";
            JSONArray jsonArray = new JSONArray(str);
            json.put("phoneNumbers", jsonArray);
            SNLUVolley.getInstance(this).post("userAdd", json, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    if(CreateRoomActivity.this.roomNumber == 0) {
                        Intent intent = new Intent(CreateRoomActivity.this, RoomActivity.class);
                        intent.putExtra("roomNumber", roomNumber + "");
                        intent.putExtra("roomTitle", editTitle.getText().toString());
                        intent.putExtra("roomChief", userItem.getPhoneNumber());
                        startActivity(intent);
                    } else {
                        setResult(RESULT_OK);
                    }
                    finish();
                }
            });
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestUserList(int roomNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("roomNumber", roomNumber);
            SNLUVolley.getInstance(this).post("userList", json, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String result = response.getString("result");
                        if(result.equals("0")) {
                            ArrayList<InviteItem> inviteItems = new ArrayList<>();
                            JSONArray array = response.getJSONArray("data");
                            for(int i=0; i<array.length(); i++)
                                adapter.addItem(new InviteItem(
                                        array.getJSONObject(i).getString("phoneNumber"),
                                        array.getJSONObject(i).getString("name"),
                                        false));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class ListAdapter extends RecyclerView.Adapter implements View.OnClickListener {
        private ArrayList<InviteItem> data;

        public ListAdapter(ArrayList<InviteItem> data) {
            this.data = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.item_invite, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder vh = (ViewHolder)holder;
            String text = data.get(position).userId + "\t\t" + data.get(position).userName;
            if(data.get(position).userId.equals(userItem.getPhoneNumber())) text += "(방장)";
            vh.textView.setText(text);
            vh.button.setTag(position);
            vh.button.setOnClickListener(this);
            if(data.get(position).canDelete) vh.button.setVisibility(View.VISIBLE);
            else vh.button.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public void onClick(View v) {
            int pos = (int)v.getTag();
            data.remove(pos);
            notifyItemRemoved(pos);
        }

        public void addItem(InviteItem item) {
            data.add(item);
            notifyItemInserted(data.size() - 1);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;
            public ImageView button;

            public ViewHolder(View view) {
                super(view);
                textView = (TextView)view.findViewById(R.id.text_view);
                button = (ImageView)view.findViewById(R.id.button);
                button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_delete));
            }
        }
    }

    class InviteItem {
        public String userId, userName;
        public boolean canDelete;
        public InviteItem(String userId, String userName, boolean canDelete) {
            this.userId = userId;
            this.userName = userName;
            this.canDelete = canDelete;
        }
    }
}
