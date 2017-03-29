package com.snlu.snluapp.service;

import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.snlu.snluapp.data.LoginInformation;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUMessageController;

import java.util.Map;

public class SNLUFirebaseMessagingService extends FirebaseMessagingService {
    private LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        SNLULog.v("From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            SNLULog.v("Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Map<String, String> data = remoteMessage.getData();
            SNLULog.v("Message Notification : { title:" + title + ", body:" + body + ", data:" + data.toString() + "}");

            RoomItem roomItem = new RoomItem();
            SentenceItem sentenceItem = new SentenceItem();
            switch(data.get("code")) {
                case "01":
                    // 회의 시작
                    roomItem.setNumber(data.get("roomNumber"));
                    roomItem.setChief(data.get("roomChief"));
                    roomItem.setStartedDocumentNumber(data.get("documentNumber"));
                    SNLUMessageController.startConference(getApplicationContext(), title, roomItem);
                    break;
                case "02":
                    // 회의 종료
                    SNLUMessageController.notifyEndConference(localBroadcastManager, data.get("roomNumber"));
                    break;
                case "03":
                    // 발언 시작
                    SNLUMessageController.notifyStartSpeak(localBroadcastManager, data.get("roomNumber"), data.get("speakerPhoneNumber"), data.get("speakerName"));
                    break;
                case "04":
                    // 발언 종료
                    sentenceItem.setSpeakerPhoneNumber(data.get("speakerPhoneNumber"));
                    sentenceItem.setSpeakerName(data.get("speakerName"));
                    sentenceItem.setSentence(data.get("sentence"));
                    sentenceItem.setSpeakTime(data.get("speakTime"));
                    SNLUMessageController.notifyEndSpeak(localBroadcastManager, data.get("roomNumber"), sentenceItem);
                    break;
                case "05":
                    // 회의 초대
                    roomItem.setNumber(data.get("roomNumber"));
                    roomItem.setChief(data.get("roomChief"));
                    roomItem.setTitle(data.get("roomTitle"));
                    SNLUMessageController.visitedRoom(getApplicationContext(), title, roomItem);
            }
        }
    }
}