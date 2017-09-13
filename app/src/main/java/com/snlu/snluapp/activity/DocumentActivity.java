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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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
import android.widget.ImageView;
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
import com.snlu.snluapp.util.SNLUSharedPreferences;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DocumentActivity extends AppCompatActivity implements OnEditListener{
    private String roomNumber;
    private DocumentItem documentItem;
    @BindView(R.id.text_title) TextView textTitle;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.fab_word) FloatingActionButton fabWord;
    @BindView(R.id.fab_pdf) FloatingActionButton fabPDF;
    @BindView(R.id.fab_statistic) FloatingActionButton fabStatistic;
    @BindView(R.id.fab_summary) FloatingActionButton fabSummary;
    @BindView(R.id.text_word) TextView textWord;
    @BindView(R.id.text_pdf) TextView textPDF;
    @BindView(R.id.text_statistic) TextView textStatistic;
    @BindView(R.id.text_summary) TextView textSummary;
    @BindView(R.id.button_restart) View viewRestart;
    @BindView(R.id.button_save) View buttonSave;
    @BindView(R.id.button_cancel) View buttonCancel;
    @BindView(R.id.button_edit) View buttonEdit;
    @BindView(R.id.button_search) ImageView buttonSearch;
    @BindView(R.id.edit_search) EditText editSearch;
    @BindView(R.id.image_cloud) ImageView imageCloud;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    private SentencesDetailAdapter adapter;
    private long downloadId;
    private boolean isChief = false;
    private boolean searching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        ButterKnife.bind(this);

        documentItem = new DocumentItem();
        documentItem.setTitle(getIntent().getStringExtra("documentTitle"));
        documentItem.setNumber(getIntent().getStringExtra("documentNumber"));
        documentItem.setDate(getIntent().getStringExtra("documentDate"));
        roomNumber = getIntent().getStringExtra("roomNumber");
        isChief = getIntent().getBooleanExtra("isChief", false);
        loadDocumentInformation(documentItem.getNumber());

        textTitle.setText(documentItem.getTitle());

        if(isChief) {
            viewRestart.setVisibility(View.VISIBLE);
            buttonEdit.setVisibility(View.VISIBLE);
        } else {
            viewRestart.setVisibility(View.GONE);
            buttonEdit.setVisibility(View.GONE);
        }

        // 회의 내용 리사이클러뷰 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SentencesDetailAdapter(this, this, isChief, SNLUSharedPreferences.get(this, "user_phone_number"), getIntent().getStringExtra("managerId"));
        recyclerView.setAdapter(adapter);

        SNLUPermission.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 100);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.setSearchKeyword(s.toString());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i=0; i<permissions.length; i++) {
            if(grantResults[i] == PackageManager.PERMISSION_DENIED)
                finish();
        }
    }

    @OnClick({R.id.button_back, R.id.button_edit, R.id.button_save, R.id.button_cancel})
    public void onMenuClick(View v) {
        switch(v.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.button_edit:
                SNLUInputDialog.newInstance(
                        "회의록의 제목을 입력하세요",
                         documentItem.getTitle(),
                        new SNLUInputDialog.OnConfirmListener() {
                            @Override
                            public void onConfirm(String text) {
                                requestEdit(text);
                            }
                        }
                ).show(getSupportFragmentManager(), null);
                break;
            case R.id.button_save:
                if(adapter.getEditedPosition() != -1) {
                    SentenceItem sentenceItem = adapter.getItem(adapter.getEditedPosition());
                    sentenceItem.setSentence(adapter.getEditedText());
                    adapter.edit();
                    requestSentenceSave(sentenceItem);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                showEditMenu(false);
                break;
            case R.id.button_cancel:
                adapter.returnEditedPosition();
                showEditMenu(false);
                break;
        }
    }

    @OnClick(R.id.button_search)
    public void doSearch() {
        if(searching) {
            buttonSearch.setImageResource(R.drawable.ic_search_white);
            textTitle.setVisibility(View.VISIBLE);
            editSearch.setVisibility(View.GONE);
        } else {
            buttonSearch.setImageResource(R.drawable.ic_clear_white_24dp);
            textTitle.setVisibility(View.GONE);
            editSearch.setVisibility(View.VISIBLE);
            editSearch.setText("");
            adapter.setSearchKeyword("");
        }
        searching = !searching;
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
                            textTitle.setText(title);
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
            buttonCancel.setVisibility(View.VISIBLE);
            buttonSave.setVisibility(View.VISIBLE);
            if(isChief) buttonEdit.setVisibility(View.GONE);
            if(fab.getRotation() == 45)
                hideFabs();
            fab.hide();
        } else {
            buttonCancel.setVisibility(View.GONE);
            buttonSave.setVisibility(View.GONE);
            if(isChief) buttonEdit.setVisibility(View.VISIBLE);
            fab.show();
        }
    }

    @Override
    public void onBackPressed() {
        if(fab.getRotation() == 45) {
            hideFabs();
        } else if (adapter.getEditedPosition() != -1) {
            adapter.returnEditedPosition();
            showEditMenu(false);
        } else if(searching) {
            doSearch();
        } else {
            super.onBackPressed();
        }
    }

    @OnClick({R.id.fab, R.id.fab_word, R.id.fab_pdf, R.id.fab_statistic, R.id.fab_summary})
    public void onFabClick(View v) {
        switch(v.getId()) {
            case R.id.fab:
                if(fab.getRotation() != 45) {
                    showFabs();
                } else {
                    hideFabs();
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

    @OnClick(R.id.button_restart)
    public void doRestart() {
        SNLUAlertDialog.newInstance(
                "알림",
                "회의를 다시 시작 하시겠습니까?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestResume();
                        dialog.dismiss();
                    }
                },
                null
        ).show(getSupportFragmentManager(), null);
    }

    public void showFabs() {
        fab.animate().rotation(45f);
        fabWord.show();
        fabPDF.show();
        fabStatistic.show();
        fabSummary.show();
        textWord.setVisibility(View.VISIBLE);
        textPDF.setVisibility(View.VISIBLE);
        textStatistic.setVisibility(View.VISIBLE);
        textSummary.setVisibility(View.VISIBLE);
        imageCloud.setVisibility(View.VISIBLE);
    }

    public void hideFabs() {
        fab.animate().rotation(0f);
        fabWord.hide();
        fabPDF.hide();
        fabStatistic.hide();
        fabSummary.hide();
        textWord.setVisibility(View.GONE);
        textPDF.setVisibility(View.GONE);
        textStatistic.setVisibility(View.GONE);
        textSummary.setVisibility(View.GONE);
        imageCloud.setVisibility(View.GONE);
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
                        item.setSpeakerPhoneNumber(array.getJSONObject(i).getString("speaker"));
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
