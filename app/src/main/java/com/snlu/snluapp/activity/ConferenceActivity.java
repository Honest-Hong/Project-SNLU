package com.snlu.snluapp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.snlu.snluapp.R;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUMessageController;
import com.snlu.snluapp.util.SNLUPermission;
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

public class ConferenceActivity extends AppCompatActivity implements SensorEventListener, RecognitionListener {
    final static int REQUEST_RECORD_AUDIO = 101;
    private final int SNACK_BAR_TIME = 2000;

    // 음성인식 인텐트와 서비스
    private Intent recognizerIntent;
    private SpeechRecognizer recognizer;
    // 발언자 이름 텍스트 뷰
    private TextView textSpeaker;
    // 내화 내용
    private LinearLayout paper;
    // 발언이 가능한지를 판단하는 변수
    private boolean canSpeak = true;
    // 날짜 형식을 변환해줄 포멧
    private Timestamp timestamp;
    // 회의록 정보
    private DocumentItem documentItem;
    // 문장 정보
    private ArrayList<SentenceItem> sentenceItems;
    // 방 정보
    private RoomItem roomItem;
    // 근접센서 변수들
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private final static int SENSOR_SENSITIVITY = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);

        getSupportActionBar().setTitle("회의중");

        // 문장들을 뿌려주는 화면
        paper =  (LinearLayout)findViewById(R.id.conference_paper);
        // 발언하는 사람의 이름
        textSpeaker = (TextView)findViewById(R.id.conference_speaker_name);
        // 회의 종료 버튼
        View viewEnd = findViewById(R.id.conference_end);
        // 발언하기 센서
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        // 음성인식 인텐트
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

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
        if(!roomItem.getChief().equals(LoginInformation.getUserItem().getPhoneNumber())) viewEnd.setVisibility(GONE);
        else {
            viewEnd.setVisibility(View.VISIBLE);
            findViewById(R.id.conference_end).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestEndConference();
                }
            });
        }
    }

    // 발언 시작
    private void startListening() {
        requestSayStart(LoginInformation.getUserItem().getPhoneNumber());
        timestamp = new Timestamp(System.currentTimeMillis());

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(this);
        recognizer.startListening(recognizerIntent);
        textSpeaker.setText("음성인식이 시작되었습니다.");
//        Snackbar.make(getWindow().getDecorView().getRootView(), "음성인식이 시작되었습니다.", SNACK_BAR_TIME).show();
    }

    // 발언 중단
    private void stopListening() {
        if(recognizer != null) recognizer.stopListening();
    }

    // 음성 인식 내용 처리
    private void processSpeech(String sentence) {
        String speakerPhoneNumber = LoginInformation.getUserItem().getPhoneNumber();
        String speakerName = LoginInformation.getUserItem().getName();
        SentenceItem item = new SentenceItem();
        item.setSpeakerPhoneNumber(speakerPhoneNumber);
        item.setSpeakerName(speakerName);
        item.setSpeakTime(timestamp.toString());
        item.setSentence(sentence);
        requestSayEnd(item);
    }

    // 회의 내용 화면에 발언을 추가하는 함수
    private void addSentece(SentenceItem item, boolean focus) {
        sentenceItems.add(item);
        View viewSentence = LayoutInflater.from(ConferenceActivity.this).inflate(R.layout.item_sentence, null);
        TextView textName = (TextView)viewSentence.findViewById(R.id.item_sentence_name);
        textName.setText(item.getSpeakerName() + ":");
        TextView textSentence = (TextView)viewSentence.findViewById(R.id.item_sentence_sentence);
        textSentence.setText(item.getSentence());
        TextView textTime = (TextView)viewSentence.findViewById(R.id.item_sentence_time);
        String time = item.getSpeakTime();
        textTime.setText(String.format("%s시 %s분 %s초", time.substring(11,13), time.substring(14,16), time.substring(17,19)));
        paper.addView(viewSentence);
        if(focus) {
            // 최근에 발언한 문장이 보이도록 스크롤을 내려준다.
            scrollDown();
        }
    }

    // 회의 내용 화면의 스크롤을 아래로 내려주는 함수
    private void scrollDown() {
        final ScrollView scrollView = (ScrollView)findViewById(R.id.conference_scroll);
        scrollView.post(new Runnable() {

            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
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
                        if(!speakerPhoneNumber.equals(LoginInformation.getUserItem().getPhoneNumber())) {
                            // todo 다른사람이 말하고 있다고 표시해준다.
                            textSpeaker.setText(speakerName + "님이 발언중입니다.");
//                            Snackbar.make(getWindow().getDecorView().getRootView(), speakerName + "님이 발언중입니다.", SNACK_BAR_TIME).show();
                        }
                        // 발언이 시작됐으므로 발언권 off
                        canSpeak = false;
                    }
                    break;
                case "04":
                    // 발언 종료
                    roomNumber = intent.getStringExtra("roomNumber");
                    SNLULog.v("ConferenceActivity BroadcastReceiver code 04: " + roomNumber);
                    // 발언이 종료된 방의 번호와 현재 방 번호가 일치할 경우에만 처리해줌
                    if(roomNumber.equals(roomItem.getNumber())) {
                        // 문장을 대화 내용애 추가해줌
                        addSentece(SentenceItem.make(intent), true);

                        //  todo 이제 말할 수 있다고 표시해준다.
                        textSpeaker.setText("");
//                        Snackbar.make(getWindow().getDecorView().getRootView(), intent.getStringExtra("spekerName") + "님이 발언을 종료하셨습니다.", SNACK_BAR_TIME).show();
                        // 발언권을 활성화함
                        canSpeak = true;
                    }
                    break;
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if(event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                // start listening
                if(canSpeak) startListening();
                else {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "지금은 발언하실 수 없습니다.", SNACK_BAR_TIME).show();
                }
            } else {
                stopListening();
                Snackbar.make(getWindow().getDecorView().getRootView(), "음성인식을 종료합니다.", SNACK_BAR_TIME).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

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
                    Intent intent = new Intent(ConferenceActivity.this, RoomActivity.class);
                    intent.putExtra("roomNumber", roomItem.getNumber());
                    intent.putExtra("roomTitle", roomItem.getTitle());
                    intent.putExtra("roomChief", roomItem.getChief());
                    startActivity(intent);
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
                    sentenceItems = new ArrayList<>();
                    for(int i=0; i<array.length(); i++) addSentece(SentenceItem.make(array.getJSONObject(i)), false);
                    scrollDown();
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
        canSpeak = true;
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
    }
    @Override
    public void onResults(Bundle results) {
        canSpeak = true;
        ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(result != null) processSpeech(result.get(0));
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
        // 센서 등록
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 브로드 캐스트 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        // 센서 해제
        sensorManager.unregisterListener(this);
    }
}