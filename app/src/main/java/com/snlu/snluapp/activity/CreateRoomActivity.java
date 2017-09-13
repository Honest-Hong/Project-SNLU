package com.snlu.snluapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.snlu.snluapp.R;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.item.UserItem;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

public class CreateRoomActivity extends AppCompatActivity implements View.OnClickListener{
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private EditText editTitle;
    private TextView buttonCreate;
    private UserItem userItem, searchItem;
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        ArrayList<UserItem> userItems = new ArrayList<>();
        roomNumber = getIntent().getIntExtra("roomNumber", 0);
        userItem = LoginInformation.getUserItem();
        if(roomNumber != 0) {
//            getSupportActionBar().setTitle("회의자 추가");
            buttonCreate.setText("회의자 추가하기");
            editTitle.setEnabled(false);
            editTitle.setText(getIntent().getStringExtra("roomTitle"));
            requestUserList(roomNumber);
            findViewById(R.id.edit_search).requestFocus();
        } else {
//            getSupportActionBar().setTitle("회의방 생성");
            buttonCreate.setText("회의방 생성하기");
            userItems.add(userItem);
        }
        adapter = new ListAdapter(userItems);
        recyclerView.setAdapter(adapter);

        ImageView button = (ImageView)findViewById(R.id.button_manager);
        button.setOnClickListener(this);
        button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_plus));
        setSearchResult(null);
    }

    private void setSearchResult(final UserItem item) {
        searchItem = item;
        final ImageView imageView = (ImageView)findViewById(R.id.image_view);
        TextView textName = (TextView)findViewById(R.id.text_name);
        TextView textId = (TextView)findViewById(R.id.text_id);
        ImageView button = (ImageView)findViewById(R.id.button_manager);
        if(item == null) {
            imageView.setVisibility(View.INVISIBLE);
            textName.setVisibility(View.INVISIBLE);
            textId.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
        } else {
            item.setSelected(true);
            imageView.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(item.getImagePath());
                        final Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        imageView.setImageDrawable(ContextCompat.getDrawable(CreateRoomActivity.this, R.drawable.icon_user));
                    }
                }
            }).start();
            textName.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            textName.setText(item.getName());
            textId.setText("(" + item.getId() + ")");
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_create:
                if(roomNumber == 0) requestCreateRoom(editTitle.getText().toString(), userItem.getId());
                else {
                    ArrayList<UserItem> inviteItems = new ArrayList<>();
                    for(int i=0; i<adapter.data.size(); i++) if(adapter.data.get(i).isSelected()) inviteItems.add(adapter.data.get(i));
                    requestInvite(roomNumber, inviteItems);
                }
                break;
            case R.id.button_search:
                EditText editText = (EditText)findViewById(R.id.edit_search);
                requestSearch(editText.getText().toString());
                break;
            case R.id.button_manager:
                // 회의자를 리사이클러 뷰에 추가하는 과정
                boolean isExisted = false;
                for(int i=0; i<adapter.data.size(); i++) {
                    if(adapter.data.get(i).getId().equals(searchItem.getId())) {
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
                    setSearchResult(new UserItem(user));
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
                            ArrayList<UserItem> userItems = new ArrayList<>();
                            for(int i=0; i<adapter.data.size(); i++)
                                if(adapter.data.get(i).isSelected()) userItems.add(adapter.data.get(i));
                            requestInvite(response.getInt("roomNumber"), userItems);
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

    private void requestInvite(final int roomNumber, ArrayList<UserItem> userItems) {
        try {
            JSONObject json = new JSONObject();
            json.put("roomNumber",roomNumber);
            String str = "[";
            for (int i = 0; i < userItems.size(); i++) {
                if(i!=0) str += ",";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("phoneNumber", userItems.get(i).getId().replace("-", ""));
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
                        intent.putExtra("roomChief", userItem.getId());
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
                            JSONArray array = response.getJSONArray("data");
                            for(int i=0; i<array.length(); i++)
                                adapter.addItem(new UserItem(array.getJSONObject(i)));
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
        private ArrayList<UserItem> data;

        public ListAdapter(ArrayList<UserItem> data) {
            this.data = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.item_user, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder vh = (ViewHolder)holder;
            String text = data.get(position).getName();
            if(data.get(position).getId().equals(userItem.getId())) text += "(방장)";
            vh.textName.setText(text);
            vh.textId.setText("(" + data.get(position).getId() + ")");
            vh.button.setTag(position);
            vh.button.setOnClickListener(this);
            if(data.get(position).isSelected()) vh.button.setVisibility(View.VISIBLE);
            else vh.button.setVisibility(View.GONE);

            if(!data.get(position).getImagePath().equals("null")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(data.get(position).getImagePath());
                            final Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    vh.imageView.setImageBitmap(bitmap);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    vh.imageView.setImageDrawable(ContextCompat.getDrawable(CreateRoomActivity.this, R.drawable.icon_user));
                                }
                            });
                        }
                    }
                }).start();
            }
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

        public void addItem(UserItem item) {
            data.add(item);
            notifyItemInserted(data.size() - 1);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textName, textId;
            public ImageView button, imageView;

            public ViewHolder(View view) {
                super(view);
                textName = (TextView)view.findViewById(R.id.text_name);
                textId = (TextView)view.findViewById(R.id.text_id);
                button = (ImageView)view.findViewById(R.id.button_manager);
                button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_delete));
                imageView = (ImageView)view.findViewById(R.id.image_view);
            }
        }
    }
}
