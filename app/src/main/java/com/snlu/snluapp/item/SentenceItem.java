package com.snlu.snluapp.item;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hong Tae Joon on 2016-11-16.
 */

public class SentenceItem {
    private String speakerPhoneNumber;
    private String speakerName;
    private String sentence;
    private String speakTime;

    public SentenceItem() {
    }

    public static SentenceItem make(JSONObject jsonObject) {
        SentenceItem item = new SentenceItem();
        try {
            item.setSpeakerPhoneNumber(jsonObject.getString("speaker"));
            item.setSpeakerName(jsonObject.getString("name"));
            item.setSpeakTime(jsonObject.getString("speakTime"));
            item.setSentence(jsonObject.getString("sentence"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return item;
    }

    public static SentenceItem make(Intent intent) {
        SentenceItem item = new SentenceItem();
        item.setSpeakerPhoneNumber(intent.getStringExtra("speakerPhoneNumber"));
        item.setSpeakerName(intent.getStringExtra("speakerName"));
        item.setSentence(intent.getStringExtra("sentence"));
        item.setSpeakTime(intent.getStringExtra("speakTime"));
        return item;
    }

    public String getSpeakerPhoneNumber() {
        return speakerPhoneNumber;
    }

    public void setSpeakerPhoneNumber(String speakerPhoneNumber) {
        this.speakerPhoneNumber = speakerPhoneNumber;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public void setSpeakTime(String speakTime) {
        this.speakTime = speakTime;
    }

    public String getSpeakerName() {

        return speakerName;
    }

    public String getSentence() {
        return sentence;
    }

    public String getSpeakTime() {
        return speakTime;
    }
}
