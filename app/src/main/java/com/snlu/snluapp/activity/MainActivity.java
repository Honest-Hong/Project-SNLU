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
//        saveSummary();
//        loadSummary();
//        downloadFile();
    }

    private void saveSummary() {
        JSONObject param = new JSONObject();
        try {
            param.put("division", "전산");
            param.put("writer", "홍태준");
            param.put("roomNumber", 2);
            param.put("documentNumber", 2);
            param.put("subject", "프로젝트 실무");
            JSONArray content = new JSONArray();
            {
                JSONObject con = new JSONObject();
                con.put("item", "아이디어");
                JSONArray sentences = new JSONArray();
                JSONObject sentence1 = new JSONObject();
                sentence1.put("sentence", "윤수환 아이디어");
                sentences.put(sentence1);
                JSONObject sentence2 = new JSONObject();
                sentence2.put("sentence", "홍태준 아이디어");
                sentences.put(sentence2);
                JSONObject sentence3 = new JSONObject();
                sentence3.put("sentence", "강동우 아이디어");
                sentences.put(sentence3);
                con.put("sentences", sentences);
                content.put(con);
            }
            {
                JSONObject con = new JSONObject();
                con.put("item", "윤수환 아이디어 반응");
                JSONArray sentences = new JSONArray();
                JSONObject sentence1 = new JSONObject();
                sentence1.put("sentence", "윤수환 INU UCF");
                sentences.put(sentence1);
                con.put("sentences", sentences);
                content.put(con);
            }
            {
                JSONObject con = new JSONObject();
                con.put("item", "홍태준 아이디어 반응");
                JSONArray sentences = new JSONArray();
                JSONObject sentence1 = new JSONObject();
                sentence1.put("sentence", "최승식 교수님");
                sentences.put(sentence1);
                JSONObject sentence2 = new JSONObject();
                sentence2.put("sentence", "엽떡 맛있다");
                sentences.put(sentence2);
                con.put("sentences", sentences);
                content.put(con);
            }
            {
                JSONObject con = new JSONObject();
                con.put("item", "강동우 아이디어 반응");
                JSONArray sentences = new JSONArray();
                JSONObject sentence1 = new JSONObject();
                sentence1.put("sentence", "에디트 텍스트 극혐");
                sentences.put(sentence1);
                JSONObject sentence2 = new JSONObject();
                sentence2.put("sentence", "파워에이드 짱짱");
                sentences.put(sentence2);
                con.put("sentences", sentences);
                content.put(con);
            }

            param.put("content", content.toString());
            SNLUVolley.getInstance(this).post("saveSummary", param, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int result = response.getInt("result");
                        if(result == 0) {

                        } else {
                            Toast.makeText(MainActivity.this, "요약 저장 실패", Toast.LENGTH_SHORT).show();
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
//
//    private void loadSummary() {
//        JSONObject param = new JSONObject();
//        try {
//            param.put("documentNumber", 2);
//            SNLUVolley.getInstance(this).post("loadSummary", param, new SNLUVolley.OnResponseListener() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    try {
//                        String division = response.getString("division");
//                        if(division != null) {
//
//                        } else {
//                            Toast.makeText(MainActivity.this, "요약 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

//    private long downloadId = 0;
//    private void downloadFile() {
//        String fileName = "회의 요약본.doc";
//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(SNLUVolley.BASE_URL + "downloadSummary?documentNumber=" + 2))
//                .setAllowedOverRoaming(false)
//                .setTitle(fileName)
//                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
//                .setDescription("문서를 다운로드 중입니다.");
//
//        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
//        downloadId = downloadManager.enqueue(request);
//    }
//
//    private void showFile(String file) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        String type = "application/doc";
//        intent.setDataAndType(Uri.parse(file), type);
//        try {
//            startActivity(intent);
//        } catch (ActivityNotFoundException e) {
//            Snackbar.makeContent(getWindow().getDecorView().getRootView(), "파일을 실행시킬 수 있는 프로그램이 없습니다.", 2000).show();
//        }
//    }
//
//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // check whether the download-id is mine
//            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
//            if (id != downloadId) {
//                // not our download id, ignore
//                return;
//            }
//
//            final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//
//            // makeContent a query
//            final DownloadManager.Query query = new DownloadManager.Query();
//            query.setFilterById(id);
//
//            Cursor cursor = downloadManager.query(query);
//            if (cursor.moveToFirst()) {
//                // when download completed
//                int statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
//                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusColumn)) {
//                    return;
//                }
//
//                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
//                String downloadedPackageUriString = cursor.getString(uriIndex);
//                String file = Uri.decode(downloadedPackageUriString);
//                showFile(file);
//            }
//        }
//    };
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(broadcastReceiver);
//    }

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
