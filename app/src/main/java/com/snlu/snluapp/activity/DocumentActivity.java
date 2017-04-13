package com.snlu.snluapp.activity;

import android.Manifest;
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
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUPermission;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DocumentActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{
    private DocumentItem documentItem;
    private ArrayList<SentenceItem> sentenceItems;
    private ArrayList<SentenceItem> searchItems;
    private LinearLayout paper;
    private FloatingActionButton fab;
    private LinearLayout linearPdf, linearWord, linearStatistic, linearSummary;
    private long downloadId;
    private SearchView searchView;
    private MenuItem menuSave, menuCancel;
    private int editedPosition = -1;

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
        linearPdf = (LinearLayout)findViewById(R.id.linear_pdf);
        linearWord = (LinearLayout)findViewById(R.id.linear_word);
        linearStatistic = (LinearLayout)findViewById(R.id.linear_statistic);
        linearSummary = (LinearLayout)findViewById(R.id.linear_summary);

        fab.setOnClickListener(this);
        findViewById(R.id.fab_pdf).setOnClickListener(this);
        findViewById(R.id.fab_statistic).setOnClickListener(this);
        findViewById(R.id.fab_summary).setOnClickListener(this);
        findViewById(R.id.fab_word).setOnClickListener(this);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_document, menu);
        menuSave = menu.findItem(R.id.menu_save);
        menuCancel = menu.findItem(R.id.menu_cancel);

        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("검색할 내용을 입력하세요.");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")) {
                    createPaper(sentenceItems, false);
                } else {
                    ArrayList<SentenceItem> temp = new ArrayList<>();
                    searchItems = new ArrayList<>();
                    for(int i=0; i<sentenceItems.size(); i++) {
                        if(sentenceItems.get(i).getSentence().contains(newText)) {
                            searchItems.add(sentenceItems.get(i));
                            SentenceItem item = new SentenceItem();
                            item.setSentence(sentenceItems.get(i).getSentence().replace(newText, "<font color='red'>" + newText + "</font>"));
                            item.setSpeakTime(sentenceItems.get(i).getSpeakTime());
                            item.setSpeakerPhoneNumber(sentenceItems.get(i).getSpeakerPhoneNumber());
                            item.setSpeakerName(sentenceItems.get(i).getSpeakerName());
                            temp.add(item);
                        }
                    }
                    createPaper(temp, true);
                }
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_save:
                if(editedPosition != -1) {
                    SentenceItem sentenceItem = sentenceItems.get(editedPosition);
                    View parent = paper.getChildAt(editedPosition);
                    EditText editText = (EditText)parent.findViewById(R.id.edit_text);
                    sentenceItem.setSentence(editText.getText().toString());
                    TextView textView = (TextView)parent.findViewById(R.id.text_sentence);
                    textView.setText(sentenceItem.getSentence());
                    requestSentenceSave(sentenceItem);
                    setEditedMode(editedPosition, false);
                }
                showEditMenu(false);
                return true;
            case R.id.menu_cancel:
                if(editedPosition != -1) setEditedMode(editedPosition, false);
                showEditMenu(false);
                return true;
            default:
                return true;
        }
    }

    private void requestSentenceSave(SentenceItem item) {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", documentItem.getNumber());
            json.put("speakTime", item.getSpeakTime());
            json.put("sentence", item.getSentence());
            SNLUVolley.getInstance(this).post("modifySentence", json, onSaveListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SNLUVolley.OnResponseListener onSaveListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                if(response.getString("result").equals("0")) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "문장이 수정되었습니다.", 2000).show();
                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "오류가 발생하였습니다.", 2000).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 수정모드일때 아닐때 메뉴를 숨기고 보여주는 함수.
    private void showEditMenu(boolean show) {
        if(show) {
            menuCancel.setVisible(true);
            menuSave.setVisible(true);
            searchView.setVisibility(View.GONE);
            fab.hide();
        } else {
            menuCancel.setVisible(false);
            menuSave.setVisible(false);
            searchView.setVisibility(View.VISIBLE);
            fab.show();
        }
    }

    // 해당 문장을 수정모드일때 아닐때 변환해주는 함수.
    private void setEditedMode(int position, boolean edited) {
        View view = paper.getChildAt(position);
        TextView textView = (TextView)view.findViewById(R.id.text_sentence);
        EditText editText = (EditText)view.findViewById(R.id.edit_text);
        if(edited) {
            editedPosition = position;
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
        } else {
            editedPosition = -1;
            textView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(!searchView.isIconified()) {
            searchView.setIconified(true);
            createPaper(sentenceItems, false);
        } else if (editedPosition != -1) {
            setEditedMode(editedPosition, false);
            showEditMenu(false);
        } else {
            super.onBackPressed();
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
                    linearPdf.animate().translationX(0);
                    linearWord.animate().translationX(0);
                } else {
                    fab.animate().rotation(0f);
                    float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
                    linearSummary.animate().translationX(dp);
                    linearStatistic.animate().translationX(dp);
                    linearPdf.animate().translationX(dp);
                    linearWord.animate().translationX(dp);
                }
                break;
            case R.id.fab_pdf:
                Snackbar.make(getWindow().getDecorView().getRootView(), "pdf파일을 다운로드합니다.", 2000).show();
                downloadFile(3);
                break;
            case R.id.fab_word:
                Snackbar.make(getWindow().getDecorView().getRootView(), "word파일을 다운로드합니다.", 2000).show();
                downloadFile(2);
                break;
            case R.id.fab_statistic:
                Intent intentStatistic = new Intent(this, StatisticActivity.class);
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
            case 2: fileName += ".doc"; break;
            case 3: fileName += ".pdf"; break;
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://52.78.92.129:8000/downloadDocument?documentNumber=" + documentItem.getNumber() + "&documentType=" + type))
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

            final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            // make a query
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
    public boolean onLongClick(View v) {
        if(editedPosition != -1) setEditedMode(editedPosition, false);
        setEditedMode((int)v.getTag(), true);
        showEditMenu(true);
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

    private void createPaper(ArrayList<SentenceItem> items, boolean highlight) {
        paper.removeAllViews();
        for(int i=0; i<items.size(); i++) {
            SentenceItem item = items.get(i);
            View viewSentence = LayoutInflater.from(DocumentActivity.this).inflate(R.layout.item_sentence, null);
            TextView textName = (TextView) viewSentence.findViewById(R.id.text_name);
            textName.setText(item.getSpeakerName() + ":");
            TextView textSentence = (TextView) viewSentence.findViewById(R.id.text_sentence);
            EditText editSentence = (EditText) viewSentence.findViewById(R.id.edit_text);
            if (highlight) {
                textSentence.setText(Html.fromHtml(item.getSentence()));
                editSentence.setText(searchItems.get(i).getSentence());
            }
            else {
                textSentence.setText(item.getSentence());
                editSentence.setText(item.getSentence());
            }
            TextView textTime = (TextView) viewSentence.findViewById(R.id.text_time);
            String time = item.getSpeakTime();
            textTime.setText(String.format("%s시 %s분 %s초", time.substring(11, 13), time.substring(14, 16), time.substring(17, 19)));
            viewSentence.setOnLongClickListener(this);
            viewSentence.setTag(i);
            paper.addView(viewSentence);
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
                        SentenceItem item = new SentenceItem();
                        item.setSpeakerName(array.getJSONObject(i).getString("name"));
                        item.setSpeakTime(array.getJSONObject(i).getString("speakTime"));
                        item.setSentence(array.getJSONObject(i).getString("sentence"));
                        sentenceItems.add(item);
                    }
                    createPaper(sentenceItems, false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
