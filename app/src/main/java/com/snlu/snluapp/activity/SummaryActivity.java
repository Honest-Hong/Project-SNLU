package com.snlu.snluapp.activity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
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
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.snlu.snluapp.R;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.item.SummaryContentItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SummaryActivity extends AppCompatActivity implements View.OnClickListener {
    private DocumentItem documentItem;
    private int roomNumber;
    private ArrayList<SentenceItem> sentenceItems;
    private ProgressDialog loagindDialog; // 로딩화면
    private LinearLayout linearContent;
    private ArrayList<SummaryContentItem> contentItems;
    private ScrollView scrollView;
    private ListView listViewSentence;
    private EditText editDate, editDivision, editWriter, editSubject;
    private TextView textToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        ButterKnife.bind(this);

        documentItem = new DocumentItem();
        documentItem.setNumber(getIntent().getStringExtra("documentNumber"));
        listViewSentence = (ListView)findViewById(R.id.summary_sentence);
        listViewSentence.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                ClipData copy = ClipData.newPlainText("text", sentenceItems.get(position).getSentence());
                copy.addItem(new ClipData.Item(sentenceItems.get(position).getSpeakerName()));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(copy, shadowBuilder, v, 0);
                return false;
            }
        });
        loadDocumentInformation(documentItem.getNumber());
        createThreadAndDialog(); // 로딩만들기

        contentItems = new ArrayList<>();
        linearContent = (LinearLayout)findViewById(R.id.linear_content);
        scrollView = (ScrollView)findViewById(R.id.scroll_view);
        makeContent(linearContent, contentItems);
        findViewById(R.id.button_add_folder).setOnClickListener(this);
        editDate = (EditText)findViewById(R.id.summary_time);
        editWriter = (EditText)findViewById(R.id.summary_writer);
        editDivision = (EditText)findViewById(R.id.summary_department);
        editSubject = (EditText)findViewById(R.id.summary_topic);
        textToggle = (TextView)findViewById(R.id.text_toggle);
        textToggle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_add_folder:
                addFolder(linearContent, contentItems);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
                break;
            case R.id.text_toggle:
                if(listViewSentence.getVisibility() == View.VISIBLE) {
                    listViewSentence.setVisibility(View.GONE);
                    textToggle.setText("▲ 보이기");
                }
                else {
                    listViewSentence.setVisibility(View.VISIBLE);
                    textToggle.setText("▼ 숨기기");
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(listViewSentence.getVisibility() == View.VISIBLE) {
            listViewSentence.setVisibility(View.GONE);
            textToggle.setText("▲ 보이기");
        } else
            super.onBackPressed();
    }

    @OnClick({R.id.button_save, R.id.button_download})
    public void onMenuClick(View v) {
        switch(v.getId()) {
            case R.id.button_save:
                saveSummary();
                break;
            case R.id.button_download:
                downloadFile();
                break;
        }
    }

    void createThreadAndDialog() { //로딩만들기
        loagindDialog = ProgressDialog.show(this, "단어 목록",
                "불러오는 중.....", true, false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            }
        });
        thread.start();
    }

    private void loadDocumentInformation(String documentNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", documentNumber);
            SNLUVolley.getInstance(SummaryActivity.this).post("showDocument", json, requestDocumentListener);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SNLUVolley.OnResponseListener requestDocumentListener = new SNLUVolley.OnResponseListener() {

        @Override
        public void onResponse(JSONObject response) {
            try {
                SNLULog.v(response.toString());
                String result = response.getString("result");
                if (result.equals("0")) {
                    JSONArray array = response.getJSONArray("data");
                    sentenceItems = new ArrayList<>();
                    for(int i=0; i<array.length(); i++) {
                        SentenceItem item2 = new SentenceItem();
                        item2.setSentence(array.getJSONObject(i).getString("sentence"));
                        item2.setSpeakerName(array.getJSONObject(i).getString("name"));
                        sentenceItems.add(item2);
                    }
                    listViewSentence.setAdapter(new SummaryActivity.SentenceAdapter(getApplicationContext(), sentenceItems));
                    JSONObject document = response.getJSONObject("document");
                    documentItem.setDate(document.getString("date"));
                    documentItem.setTitle(document.getString("title"));
                    roomNumber = document.getInt("roomNumber");
                    editDate.setText(document.getString("date"));
                    editWriter.setText(LoginInformation.getUserItem().getName());
                    loadSummary();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    class SentenceAdapter extends BaseAdapter {
        Context context;
        ArrayList<SentenceItem> data;
        public SentenceAdapter(Context context, ArrayList<SentenceItem> data) {
            this.context = context;
            this.data = data;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null) convertView = LayoutInflater.from(context).inflate(R.layout.item_summary_sentence, null);
            TextView text = (TextView)convertView.findViewById(R.id.item_summary_sentence);
            text.setText(data.get(position).getSpeakerName()+":"+data.get(position).getSentence());
            convertView.setTag(data.get(position));
            return convertView;
        }
    }

    public void makeContent(ViewGroup parent, ArrayList<SummaryContentItem> contentItems) {
        for(int i=0; i<contentItems.size(); i++) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_summary_content, parent, false);
            EditText editName = (EditText)v.findViewById(R.id.edit_name);
            editName.setText(contentItems.get(i).getName());
            LinearLayout linearLayout = (LinearLayout)v.findViewById(R.id.parent);
            makeSentences(linearLayout, contentItems.get(i).getSentenceItems());
            editName.setTag(linearLayout);
            editName.setOnDragListener(onDragListener);
            View buttonDelete = v.findViewById(R.id.button_delete);
            buttonDelete.setTag(v);
            buttonDelete.setOnClickListener(onFolderDeleteListener);
            parent.addView(v);
        }
    }

    private View.OnClickListener onFolderDeleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parent = (View)v.getTag();
            linearContent.removeView(parent);
        }
    };

    public void addFolder(ViewGroup parent, ArrayList<SummaryContentItem> contentItems) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_summary_content, parent, false);
        EditText editName = (EditText)v.findViewById(R.id.edit_name);
        editName.setText("새 폴더");
        LinearLayout linearLayout = (LinearLayout)v.findViewById(R.id.parent);
        editName.setTag(linearLayout);
        editName.setOnDragListener(onDragListener);
        View buttonDelete = v.findViewById(R.id.button_delete);
        buttonDelete.setTag(v);
        buttonDelete.setOnClickListener(onFolderDeleteListener);
        parent.addView(v);
        contentItems.add(new SummaryContentItem("새 폴더", new ArrayList<SentenceItem>()));
    }

    public void makeSentences(ViewGroup parent, ArrayList<SentenceItem> sentenceItems) {
        for(int i=0; i<sentenceItems.size(); i++) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_summary_content_sentence, parent, false);
            TextView textName = (TextView)v.findViewById(R.id.text_name);
            textName.setText(sentenceItems.get(i).getSpeakerName());
            TextView textSentence = (TextView)v.findViewById(R.id.text_sentence);
            textSentence.setText(sentenceItems.get(i).getSentence());
            parent.addView(v);
        }
    }

    public void addSentence(ViewGroup parent, SentenceItem sentenceItem) {
        if(sentenceItem == null)
            return;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_summary_content_sentence, parent, false);
        TextView textName = (TextView)v.findViewById(R.id.text_name);
        textName.setText(sentenceItem.getSpeakerName());
        TextView textSentence = (TextView)v.findViewById(R.id.text_sentence);
        textSentence.setText(sentenceItem.getSentence());
        parent.addView(v);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private View.OnDragListener onDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            ViewGroup parent = (ViewGroup)v.getTag();
            EditText editName = (EditText)v;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    editName.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorRedPink));
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    editName.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorPink));
                    break;
                case DragEvent.ACTION_DROP:
                    ClipData clipData = event.getClipData();
                    SentenceItem item = new SentenceItem();
                    item.setSentence(clipData.getItemAt(0).getText().toString());
                    item.setSpeakerName(clipData.getItemAt(1).getText().toString());
                    addSentence(parent, item);
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    editName.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorPink));
                default:
                    break;
            }
            return true;
        }
    };

    private ProgressDialog saveDialog;
    private void saveSummary() {
        saveDialog = ProgressDialog.show(this, "", "저장중입니다..", true, false);
        JSONObject param = new JSONObject();
        try {
            param.put("division", editDivision.getText().toString());
            param.put("writer", editWriter.getText().toString());
            param.put("roomNumber", roomNumber);
            param.put("documentNumber", documentItem.getNumber());
            param.put("subject", editSubject.getText().toString());
            JSONArray content = new JSONArray();
            LinearLayout linearContent = (LinearLayout)findViewById(R.id.linear_content);
            for(int i=0; i<linearContent.getChildCount(); i++) {
                JSONObject con = new JSONObject();
                EditText editName = (EditText)linearContent.getChildAt(i).findViewById(R.id.edit_name);
                con.put("item", editName.getText().toString());
                LinearLayout linearLayout = (LinearLayout)linearContent.getChildAt(i).findViewById(R.id.parent);
                JSONArray sentences = new JSONArray();
                for(int j=0; j<linearLayout.getChildCount(); j++) {
                    JSONObject sentence = new JSONObject();
                    TextView textName = (TextView)linearLayout.getChildAt(j).findViewById(R.id.text_name);
                    TextView textSentence = (TextView)linearLayout.getChildAt(j).findViewById(R.id.text_sentence);
                    sentence.put("sentence", textName.getText() + ":" + textSentence.getText());
                    sentences.put(sentence);
                }
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
                            Toast.makeText(SummaryActivity.this, "요약 저장 완료", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SummaryActivity.this, "요약 저장 실패", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    saveDialog.dismiss();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadSummary() {
        JSONObject param = new JSONObject();
        try {
            param.put("documentNumber", documentItem.getNumber());
            SNLUVolley.getInstance(this).post("loadSummary", param, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int result = response.getInt("result");
                        if (result == 0) {
                            JSONObject data = response.getJSONObject("data");
                            String division = data.getString("division");
                            String writer = data.getString("writer");
                            String subject = data.getString("subject");
                            contentItems = new ArrayList<>();
                            JSONArray content = new JSONArray(data.getString("content"));
                            for(int i=0; i<content.length(); i++) {
                                SummaryContentItem contentItem = new SummaryContentItem();
                                contentItem.setName(content.getJSONObject(i).getString("item"));
                                JSONArray sentences = content.getJSONObject(i).getJSONArray("sentences");
                                ArrayList<SentenceItem> sentenceItems = new ArrayList<>();
                                for(int j=0; j<sentences.length(); j++) {
                                    SentenceItem item = new SentenceItem();
                                    String text = sentences.getJSONObject(j).getString("sentence");
                                    String[] split = text.split(":");
                                    item.setSpeakerName(split[0]);
                                    item.setSentence(split[1]);
                                    sentenceItems.add(item);
                                }
                                contentItem.setSentenceItems(sentenceItems);
                                contentItems.add(contentItem);
                            }
                            editDivision.setText(division);
                            editWriter.setText(writer);
                            editSubject.setText(subject);
                            makeContent(linearContent, contentItems);
                        } else {
                        }
                        loagindDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private long downloadId = 0;
    private void downloadFile() {
        String fileName = "썰록_요약본_" + documentItem.getTitle() + ".doc";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(SNLUVolley.BASE_URL + "downloadSummary?documentNumber=" + documentItem.getNumber()))
                .setAllowedOverRoaming(false)
                .setTitle(fileName)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setDescription("문서를 다운로드 중입니다.");

        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        downloadId = downloadManager.enqueue(request);
    }

    private void showFile(String file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/doc";
        intent.setDataAndType(Uri.parse(file), type);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(getWindow().getDecorView().getRootView(), "파일을 실행시킬 수 있는 프로그램이 없습니다.", 2000).show();
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // check whether the download-id is mine
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != downloadId) {
                // not our download id, ignore
                return;
            }

            final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            // makeContent a query
            final DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);

            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                // when download completed
                int statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusColumn)) {
                    return;
                }

                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String downloadedPackageUriString = cursor.getString(uriIndex);
                String file = Uri.decode(downloadedPackageUriString);
                showFile(file);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
