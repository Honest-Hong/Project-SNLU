package com.snlu.snluapp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUPermission;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DocumentActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{
    private DocumentItem documentItem;
    private ArrayList<SentenceItem> sentenceItems;
    private LinearLayout paper;
    private FloatingActionButton fab;
    private LinearLayout linearDocument, linearStatistic, linearSummary;
    private long downloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        documentItem = new DocumentItem();
        documentItem.setNumber(getIntent().getStringExtra("documentNumber"));
        documentItem.setDate(getIntent().getStringExtra("documentDate"));
        getSupportActionBar().setTitle(documentItem.getDate());
        loadDocumentInformation(documentItem.getNumber());

        // 임의의 데이터
        paper =  (LinearLayout)findViewById(R.id.document_paper);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        linearDocument = (LinearLayout)findViewById(R.id.linear_document);
        linearStatistic = (LinearLayout)findViewById(R.id.linear_statistic);
        linearSummary = (LinearLayout)findViewById(R.id.linear_summary);

        fab.setOnClickListener(this);
        findViewById(R.id.fab_document).setOnClickListener(this);
        findViewById(R.id.fab_statistic).setOnClickListener(this);
        findViewById(R.id.fab_summary).setOnClickListener(this);

        registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        SNLUPermission.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i=0; i<permissions.length; i++) {
            if(grantResults[i] == PackageManager.PERMISSION_DENIED)
                finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.fab:
                if(fab.getRotation() != 45) {
                    fab.animate().rotation(45f);
                    linearSummary.animate().translationX(0);
                    linearStatistic.animate().translationX(0);
                    linearDocument.animate().translationX(0);
                } else {
                    fab.animate().rotation(0f);
                    float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
                    linearSummary.animate().translationX(dp);
                    linearStatistic.animate().translationX(dp);
                    linearDocument.animate().translationX(dp);
                }
                break;
            case R.id.fab_document:
                Snackbar.make(getWindow().getDecorView().getRootView(), "문서를 다운로드합니다.", 3000).show();
                downloadFile(1);
                break;
            case R.id.fab_statistic:
                Intent intentStatistic = new Intent(this, StatistcActivity.class);
                intentStatistic.putExtra("documentNumber", documentItem.getNumber());
                startActivity(intentStatistic);
                break;
            case R.id.fab_summary:
                Intent intentSummary = new Intent(this, SummaryActivity.class);
                intentSummary.putExtra("documentNumber", documentItem.getNumber());
                startActivity(intentSummary);
                break;
        }
    }

    private void downloadFile(int type) {
        String fileName = "아이편회_회의록_" + documentItem.getDate();
        switch(type) {
            case 1: fileName += ".txt"; break;
            case 2: fileName += ".doc"; break;
            case 3: fileName += ".hwp"; break;
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://52.78.92.129:8000/downloadDocument?documentNumber=" + documentItem.getNumber() + "&documentType=" + type))
                .setAllowedOverRoaming(false)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                .setTitle(fileName)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setDescription("문서를 다운로드 중입니다.");

        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        downloadId = downloadManager.enqueue(request);
    }

    private void showFile(String file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/text";
        if(file.endsWith(".doc")) {
            type = "application/doc";
        } else if(file.endsWith(".pdf")) {
            type = "application/pdf";
        }
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

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            // make a query
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);

            // check the status
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                // when download completed
                int statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusColumn)) {
                    return;
                }

                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String downloadedPackageUriString = cursor.getString(uriIndex);
                Snackbar.make(getWindow().getDecorView().getRootView(), "다운로드가 완료되었습니다.", 3000).show();
                String file = Uri.decode(downloadedPackageUriString);
                showFile(file);
            }else{
                // when canceled
                return;
            }
        }
    };

    @Override
    public boolean onLongClick(View v) {
        int position = (int)v.getTag();
        v.findViewById(R.id.item_sentence_sentence).setVisibility(View.GONE);
        v.findViewById(R.id.edit_sentence).setVisibility(View.VISIBLE);
        return false;
    }

    private void loadDocumentInformation(String documentNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", documentNumber);
            SNLUVolley.getInstance(DocumentActivity.this).post("showDocument", json, requestDocumentListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addSentence(SentenceItem item) {
        sentenceItems.add(item);
        View viewSentence = LayoutInflater.from(DocumentActivity.this).inflate(R.layout.item_sentence, null);
        TextView textName = (TextView)viewSentence.findViewById(R.id.item_sentence_name);
        textName.setText(item.getSpeakerName());
        TextView textSentence = (TextView)viewSentence.findViewById(R.id.item_sentence_sentence);
        textSentence.setText(item.getSentence());
        EditText editSentence = (EditText)viewSentence.findViewById(R.id.edit_sentence);
        editSentence.setText(item.getSentence());
        viewSentence.setOnLongClickListener(this);
        viewSentence.setTag(sentenceItems.size() - 1);
        paper.addView(viewSentence);
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
                        SentenceItem item = new SentenceItem();
                        item.setSpeakerName(array.getJSONObject(i).getString("name"));
                        item.setSpeakTime(array.getJSONObject(i).getString("speakTime"));
                        item.setSentence(array.getJSONObject(i).getString("sentence"));
                        addSentence(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
