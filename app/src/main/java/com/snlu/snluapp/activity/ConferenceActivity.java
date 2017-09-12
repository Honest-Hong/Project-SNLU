package com.snlu.snluapp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.snlu.snluapp.R;
import com.snlu.snluapp.adapter.SentencesAdapter;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUMessageController;
import com.snlu.snluapp.util.SNLUPermission;
import com.snlu.snluapp.util.SNLUSharedPreferences;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;

import static android.view.View.GONE;

/**
 * Created by Hong Tae Joon on 2017-03-20.
 */

public class ConferenceActivity extends AppCompatActivity implements RecognitionListener, View.OnClickListener {
    final static int REQUEST_RECORD_AUDIO = 101;

    // 음성인식 인텐트와 서비스
    private Intent recognizerIntent;
    private SpeechRecognizer recognizer;
    // 발언자 이름 텍스트 뷰
    private TextView textSpeaker;
    // 내화 내용
    private RecyclerView recyclerView;
    private SentencesAdapter adapter;
    // 날짜 형식을 변환해줄 포멧
    private Timestamp timestamp;
    // 회의록 정보
    private DocumentItem documentItem;
    // 방 정보
    private RoomItem roomItem;
    // 발언하기 버튼
    private TextView buttonSay;
    private boolean toggleSay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);

        getSupportActionBar().setTitle("회의중");

        // 문장들을 뿌려주는 화면
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SentencesAdapter(this, new ArrayList<SentenceItem>(), SNLUSharedPreferences.get(this, "user_phone_number"));
        recyclerView.setAdapter(adapter);
        // 발언하는 사람의 이름
        textSpeaker = (TextView)findViewById(R.id.conference_speaker_name);
        // 음성인식 인텐트
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);

        // 회의방의 정보 저장하기
        roomItem = new RoomItem();
        roomItem.setNumber(getIntent().getStringExtra("roomNumber"));
        roomItem.setChief(getIntent().getStringExtra("roomChief"));

        // 회의록의 정보 저장하기
        documentItem = new DocumentItem();
        documentItem.setNumber(getIntent().getStringExtra("documentNumber"));
        requestDocumentInformation(documentItem.getNumber());

        // 오디오 녹음 권한 요청
        SNLUPermission.checkPermission(this, Manifest.permission.RECORD_AUDIO, REQUEST_RECORD_AUDIO);

        // 회의방의 방장인 경우 회의 종료버튼이 보인다.
        View viewEnd = findViewById(R.id.button_end);
        if(!roomItem.getChief().equals(LoginInformation.getUserItem().getId())) viewEnd.setVisibility(GONE);
        else {
            viewEnd.setVisibility(View.VISIBLE);
            viewEnd.setOnClickListener(this);
        }

        buttonSay = (TextView)findViewById(R.id.button_say);
        buttonSay.setOnClickListener(this);
        toggleSay = true;
    }

    private void setButtonSayMode(int mode) {
        switch(mode) {
            case 0: // 발언 가능
                buttonSay.setEnabled(true);
                buttonSay.setText("발언하기");
                toggleSay = true;
                break;
            case 1: // 발언 중단
                buttonSay.setEnabled(true);
                buttonSay.setText("발언중단하기");
                toggleSay = false;
                break;
            case 2: // 발언 불가
                buttonSay.setEnabled(false);
                buttonSay.setText("발언할 수 없음");
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_say:
                if(toggleSay) {
                    startListening();
                    setButtonSayMode(1);
                } else {
                    stopListening();
                    setButtonSayMode(0);
                }
                break;
            case R.id.button_end:
                requestEndConference();
                break;
        }
    }

    // 발언 시작
    private void startListening() {
        requestSayStart(LoginInformation.getUserItem().getId());
        timestamp = new Timestamp(System.currentTimeMillis());

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(this);
        recognizer.startListening(recognizerIntent);
        textSpeaker.setText("음성인식이 시작되었습니다.");
    }

    // 발언 중단
    private void stopListening() {
        if(recognizer != null) recognizer.stopListening();
    }

    // 음성 인식 내용 처리
    private void processSpeech(String sentence) {
        String speakerPhoneNumber = LoginInformation.getUserItem().getId();
        String speakerName = LoginInformation.getUserItem().getName();
        SentenceItem item = new SentenceItem();
        item.setSpeakerPhoneNumber(speakerPhoneNumber);
        item.setSpeakerName(speakerName);
        item.setSpeakTime(timestamp.toString());
        item.setSentence(sentence);
        requestSayEnd(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_RECORD_AUDIO:
                if(resultCode != RESULT_OK) {
                    Toast.makeText(this, "녹음 권한이 없으면 회의를 진행할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }
        }
    }

    // 브로드 캐스트 신호를 수신하는 리시버
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String code = intent.getStringExtra("code");
            String roomNumber;
            switch(code) {
                case "01":
                    // 회의 시작 (필요 없음)
                    break;
                case "02":
                    // 회의 종료
                    roomNumber = intent.getStringExtra("roomNumber");
                    // 회의가 종료된 방의 번호가 현재 방 번호와 일치하면 화면 닫기
                    if(roomNumber.equals(roomItem.getNumber())) {
                        Toast.makeText(context, "회의가 종료되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case "03":
                    // 발언 시작
                    roomNumber = intent.getStringExtra("roomNumber");
                    String speakerPhoneNumber = intent.getStringExtra("speakerPhoneNumber");
                    String speakerName = intent.getStringExtra("speakerName");
                    // 서버로부터 전달받은 방 번호가 일치하면 처리해줌
                    if(roomNumber.equals(roomItem.getNumber())) {
                        // 다른 사람이 말하고 있는 경우에만 표시해줌
                        if(!speakerPhoneNumber.equals(LoginInformation.getUserItem().getId())) {
                            textSpeaker.setText(speakerName + "님이 발언중입니다.");
                            setButtonSayMode(2);
                        }
                    }
                    break;
                case "04":
                    // 발언 종료
                    roomNumber = intent.getStringExtra("roomNumber");
                    SNLULog.v("ConferenceActivity BroadcastReceiver code 04: " + roomNumber);
                    // 발언이 종료된 방의 번호와 현재 방 번호가 일치할 경우에만 처리해줌
                    if(roomNumber.equals(roomItem.getNumber())) {
                        // 문장을 대화 내용애 추가해줌
                        adapter.addItem(SentenceItem.make(intent));
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                        textSpeaker.setText("");
                        setButtonSayMode(0);
                    }
                    break;
            }
        }
    };

    // 회의 종료 요청
    private void requestEndConference() {
        try {
            JSONObject json = new JSONObject();
            json.put("roomNumber", roomItem.getNumber());
            SNLUVolley.getInstance(ConferenceActivity.this).post("end", json, requestEndConferenceListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // 회의 종료 요청 결과
    private SNLUVolley.OnResponseListener requestEndConferenceListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String result = response.getString("result");
                if(result.equals("0")) {
//                    Intent intent = new Intent(ConferenceActivity.this, RoomActivity.class);
//                    intent.putExtra("roomNumber", roomItem.getNumber());
//                    intent.putExtra("roomTitle", roomItem.getTitle());
//                    intent.putExtra("roomChief", roomItem.getChief());
//                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 발언 시작 요청
    private void requestSayStart(String phoneNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("roomNumber", roomItem.getNumber());
            json.put("documentNumber", documentItem.getNumber());
            json.put("speaker", phoneNumber);
            SNLUVolley.getInstance(this).post("sayStart", json, requestSayStartListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // 발언 시작 요청 결과
    private SNLUVolley.OnResponseListener requestSayStartListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                SNLULog.v(response.toString());
                String result = response.getString("result");
                if(result.equals("0")) {
                    // 발언 시작 성공
                } else {
                    // 발언 시작 실패
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 발언 종료 요청
    private void requestSayEnd(SentenceItem item) {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", documentItem.getNumber());
            json.put("speaker", item.getSpeakerPhoneNumber());
            json.put("speakTime", item.getSpeakTime());
            json.put("sentence", item.getSentence());
            json.put("roomNumber", roomItem.getNumber());
            SNLUVolley.getInstance(this).post("sayEnd", json, requestSayEndListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // 발언 종료 요청 결과
    private SNLUVolley.OnResponseListener requestSayEndListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                SNLULog.v(response.toString());
                String result = response.getString("result");
                if(result.equals("0")) {
                    // 문장 전송 성공
                } else {
                    // 문장 전송 실패
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 회의록 정보 요청
    private void requestDocumentInformation(String documentNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", documentNumber);
            SNLUVolley.getInstance(ConferenceActivity.this).post("showDocument",json, requestDocumentListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // 회의록 정보 요청 결과
    private SNLUVolley.OnResponseListener requestDocumentListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                SNLULog.v(response.toString());
                String result = response.getString("result");
                if (result.equals("0")) {
                    JSONArray array = response.getJSONArray("data");
                    for(int i=0; i<array.length(); i++) adapter.addItem(SentenceItem.make(array.getJSONObject(i)));
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //
    // 음성인식 리스너 함수들...
    //
    // onError로 에러 처리를하고
    // onResults에서 음성인식 결과를 처리함
    //

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.v("MIC_TEST", "#onReadyForSpeech");
    }
    @Override
    public void onBeginningOfSpeech() {
        Log.v("MIC_TEST", "#onBeginningOfSpeech");
    }
    @Override
    public void onRmsChanged(float rmsdB) {
    }
    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.v("MIC_TEST", "#onBufferReceived");
    }
    @Override
    public void onEndOfSpeech() {
        Log.v("MIC_TEST", "#onEndOfSpeech");
    }
    @Override
    public void onError(int error) {
//        canSpeak = true;
        switch (error) {
            case SpeechRecognizer.ERROR_NO_MATCH:
                Toast.makeText(this, "일치하는 문장이 없습니다.", Toast.LENGTH_SHORT).show();
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                Toast.makeText(this, "필요한 권한이 승인되지 않았습니다.", Toast.LENGTH_SHORT).show();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                Toast.makeText(this, "이미 인식중입니다.", Toast.LENGTH_SHORT).show();
                break;
        }
        setButtonSayMode(0);
    }
    @Override
    public void onResults(Bundle results) {
        ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        //float scores[] = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
        if(result != null) {
            processSpeech(result.get(0));
        }
        setButtonSayMode(0);
    }
    @Override
    public void onPartialResults(Bundle partialResults) {
    }
    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 브로드 캐스트를 등록
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(SNLUMessageController.BROADCAST_END_CONFERENCE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(SNLUMessageController.BROADCAST_START_SPEAK));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(SNLUMessageController.BROADCAST_END_SPEAK));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 브로드 캐스트 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}