package com.snlu.snluapp.activity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.snlu.snluapp.R;
import com.snlu.snluapp.adapter.OnEditListener;
import com.snlu.snluapp.adapter.SentencesDetailAdapter;
import com.snlu.snluapp.dialog.SNLUAlertDialog;
import com.snlu.snluapp.dialog.SNLUInputDialog;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUPermission;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DocumentActivity extends AppCompatActivity implements View.OnClickListener, OnEditListener{
    private String roomNumber;
    private DocumentItem documentItem;
    private RecyclerView recyclerView;
    private SentencesDetailAdapter adapter;
    private FloatingActionButton fab;
    private LinearLayout linearResume, linearPdf, linearWord, linearStatistic, linearSummary;
    private long downloadId;
    private SearchView searchView;
    private MenuItem menuEdit, menuSave, menuCancel;
    private boolean isChief = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        documentItem = new DocumentItem();
        documentItem.setTitle(getIntent().getStringExtra("documentTitle"));
        documentItem.setNumber(getIntent().getStringExtra("documentNumber"));
        documentItem.setDate(getIntent().getStringExtra("documentDate"));
        roomNumber = getIntent().getStringExtra("roomNumber");
        isChief = getIntent().getBooleanExtra("isChief", false);
        getSupportActionBar().setTitle(documentItem.getTitle());
        loadDocumentInformation(documentItem.getNumber());

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        linearResume = (LinearLayout)findViewById(R.id.linear_resume);
        linearPdf = (LinearLayout)findViewById(R.id.linear_pdf);
        linearWord = (LinearLayout)findViewById(R.id.linear_word);
        linearStatistic = (LinearLayout)findViewById(R.id.linear_statistic);
        linearSummary = (LinearLayout)findViewById(R.id.linear_summary);

        // FAB 설정
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(this);
        findViewById(R.id.fab_resume).setOnClickListener(this);
        findViewById(R.id.fab_pdf).setOnClickListener(this);
        findViewById(R.id.fab_statistic).setOnClickListener(this);
        findViewById(R.id.fab_summary).setOnClickListener(this);
        findViewById(R.id.fab_word).setOnClickListener(this);
        if(isChief) linearResume.setVisibility(View.VISIBLE);
        else linearResume.setVisibility(View.GONE);

        // 회의 내용 리사이클러뷰 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SentencesDetailAdapter(this, this, isChief);
        recyclerView.setAdapter(adapter);

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
        menuEdit = menu.findItem(R.id.menu_edit);
        menuSave = menu.findItem(R.id.menu_save);
        menuCancel = menu.findItem(R.id.menu_cancel);

        if(isChief) menuEdit.setVisible(true);
        else menuEdit.setVisible(false);

        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("검색할 내용을 입력하세요.");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.setSearchKeyword(newText);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.setSearchKeyword("");
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_edit:
                SNLUInputDialog dialog = new SNLUInputDialog(this);
                dialog.setTitleText("회의록의 제목을 입력하세요")
                        .setContent(documentItem.getTitle())
                        .setOnConfirmListener(new SNLUInputDialog.OnConfirmListener() {
                            @Override
                            public void onConfirm(String text) {
                                requestEdit(text);
                            }
                        }).show();
                return true;
            case R.id.menu_save:
                if(adapter.getEditedPosition() != -1) {
                    SentenceItem sentenceItem = adapter.getItem(adapter.getEditedPosition());
                    sentenceItem.setSentence(adapter.getEditedText());
                    adapter.edit();
                    requestSentenceSave(sentenceItem);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                showEditMenu(false);
                return true;
            case R.id.menu_cancel:
                adapter.returnEditedPosition();
                showEditMenu(false);
                return true;
            default:
                return true;
        }
    }

    private void requestEdit(final String title) {
        try {
            JSONObject parameter = new JSONObject();
            parameter.put("title", title);
            parameter.put("roomNumber", roomNumber);
            parameter.put("documentNumber", documentItem.getNumber());
            SNLUVolley.getInstance(this).post("modifyDocumentName", parameter, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getInt("result") == 0) {
                            getSupportActionBar().setTitle(title);
                            documentItem.setTitle(title);
                        } else {
                            Toast.makeText(DocumentActivity.this, "수정 실패", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
            if(isChief) menuEdit.setVisible(false);
            fab.hide();
        } else {
            menuCancel.setVisible(false);
            menuSave.setVisible(false);
            searchView.setVisibility(View.VISIBLE);
            if(isChief) menuEdit.setVisible(true);
            fab.show();
        }
    }

    @Override
    public void onBackPressed() {
        if(fab.getRotation() == 45) {
            hideFabs();
        } else if(!searchView.isIconified()) {
            searchView.setIconified(true);
            adapter.setSearchKeyword("");
        } else if (adapter.getEditedPosition() != -1) {
            adapter.returnEditedPosition();
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
                    showFabs();
                } else {
                    hideFabs();
                }
                break;
            case R.id.fab_resume:
                SNLUAlertDialog dialog = new SNLUAlertDialog(this);
                dialog.setTitle("알림");
                dialog.setMessage("회의를 다시 시작 하시겠습니까?");
                dialog.setOnYesClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestResume();
                        dialog.dismiss();
                    }
                });
                dialog.show();
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

    public void showFabs() {
        fab.animate().rotation(45f);
        linearResume.animate().translationX(0);
        linearSummary.animate().translationX(0);
        linearStatistic.animate().translationX(0);
        linearPdf.animate().translationX(0);
        linearWord.animate().translationX(0);
    }

    public void hideFabs() {
        fab.animate().rotation(0f);
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
        linearResume.animate().translationX(dp);
        linearSummary.animate().translationX(dp);
        linearStatistic.animate().translationX(dp);
        linearPdf.animate().translationX(dp);
        linearWord.animate().translationX(dp);
    }

    private void requestResume() {
        try {
            JSONObject parameter = new JSONObject();
            parameter.put("roomNumber", roomNumber);
            parameter.put("documentNumber", documentItem.getNumber());
            SNLUVolley.getInstance(this).post("resume", parameter, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        SNLULog.v(response.toString());
                        String result = response.getString("result");
                        if(result.equals("0")) {
                            Intent intent = new Intent(DocumentActivity.this, ConferenceActivity.class);
                            intent.putExtra("documentNumber", documentItem.getNumber());
                            intent.putExtra("roomNumber", roomNumber);
                            intent.putExtra("roomChief", response.getJSONArray("data").getJSONObject(0).getString("chiefPhoneNumber"));
                            startActivity(intent);
                            finish();
                        }
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(int type) {
        String fileName = "썰록_회의록_" + documentItem.getTitle();
        switch(type) {
            case 2: fileName += ".doc"; break;
            case 3: fileName += ".pdf"; break;
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(SNLUVolley.BASE_URL + "downloadDocument?documentNumber=" + documentItem.getNumber() + "&documentType=" + type))
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

    private void loadDocumentInformation(String documentNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", documentNumber);
            SNLUVolley.getInstance(DocumentActivity.this).post("showDocument", json, requestDocumentListener);
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
                    adapter.clear();
                    for(int i=0; i<array.length(); i++) {
                        SentenceItem item = new SentenceItem();
                        item.setSpeakerName(array.getJSONObject(i).getString("name"));
                        item.setSpeakTime(array.getJSONObject(i).getString("speakTime"));
                        item.setSentence(array.getJSONObject(i).getString("sentence"));
                        adapter.addItem(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onEdit() {
        showEditMenu(true);
    }

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
