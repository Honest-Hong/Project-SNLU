package com.snlu.snluapp.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.snlu.snluapp.R;
import com.snlu.snluapp.activity.ConferenceActivity;
import com.snlu.snluapp.activity.RoomActivity;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.item.SentenceItem;

/**
 * Created by Hong Tae Joon on 2016-12-05.
 */

public class SNLUMessageController {
    public final static int NOTIFICATION_ID_ = 0x10000;
    public final static String BROADCAST_END_CONFERENCE = "com.snlu.snluapp.service.END_CONFERENCE";
    public final static String BROADCAST_START_SPEAK = "com.snlu.snluapp.service.START_SPEAK";
    public final static String BROADCAST_END_SPEAK = "com.snlu.snluapp.service.END_SPEAK";

    // 회의 시작 알림창 띄우기
    public static void startConference(Context context,String title, RoomItem roomItem) {
        SNLULog.v("startConference");
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setSmallIcon(R.drawable.icon_conference);
        builder.setTicker(title);
        builder.setAutoCancel(true);
        Intent intent = new Intent(context, ConferenceActivity.class);
        intent.putExtra("documentNumber", roomItem.getStartedDocumentNumber());
        intent.putExtra("roomChief", roomItem.getChief());
        intent.putExtra("roomNumber", roomItem.getNumber());
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notificationManager.notify(NOTIFICATION_ID_, builder.build());
    }

    // 회의 시작 알림창 띄우기
    public static void visitedRoom(Context context,String title, RoomItem roomItem) {
        SNLULog.v("visitedRoom");
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setSmallIcon(R.drawable.icon_add_user);
        builder.setTicker(title);
        builder.setAutoCancel(true);
        Intent intent = new Intent(context, RoomActivity.class);
        intent.putExtra("roomTitle", roomItem.getTitle());
        intent.putExtra("roomChief", roomItem.getChief());
        intent.putExtra("roomNumber", roomItem.getNumber());
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notificationManager.notify(NOTIFICATION_ID_, builder.build());
    }

    // 회의 종료 신호 보내기
    public static void notifyEndConference(LocalBroadcastManager localBroadcastManager, String roomNumber) {
        SNLULog.v("notifyEndConference");
        Intent intent = new Intent(BROADCAST_END_CONFERENCE);
        intent.putExtra("code", "02");
        intent.putExtra("roomNumber", roomNumber);
        localBroadcastManager.sendBroadcast(intent);
    }

    // 발언 시작 신호 보내기
    public static void notifyStartSpeak(LocalBroadcastManager localBroadcastManager, String roomNumber, String speakerPhoneNumber, String speakerName) {
        SNLULog.v("notifyStartSpeak");
        Intent intent = new Intent(BROADCAST_START_SPEAK);
        intent.putExtra("code", "03");
        intent.putExtra("roomNumber", roomNumber);
        intent.putExtra("speakerPhoneNumber", speakerPhoneNumber);
        intent.putExtra("speakerName", speakerName);
        localBroadcastManager.sendBroadcast(intent);
    }

    // 발언 종료 신호 보내기
    public static void notifyEndSpeak(LocalBroadcastManager localBroadcastManager, String roomNumber, SentenceItem item) {
        SNLULog.v("notifyEndSpeak");
        Intent intent = new Intent(BROADCAST_END_SPEAK);
        intent.putExtra("code", "04");
        intent.putExtra("roomNumber", roomNumber);
        intent.putExtra("speakerPhoneNumber", item.getSpeakerPhoneNumber());
        intent.putExtra("speakerName", item.getSpeakerName());
        intent.putExtra("sentence", item.getSentence());
        intent.putExtra("speakTime", item.getSpeakTime());
        localBroadcastManager.sendBroadcast(intent);
    }
}
