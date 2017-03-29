package com.snlu.snluapp.item;

import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2016-11-14.
 */

public class RoomItem {
    private String number;
    private String title;
    private String chief;
    private String isStart;
    private String startedDocumentNumber;

    public RoomItem() {
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChief() {
        return chief;
    }

    public void setChief(String chief) {
        this.chief = chief;
    }

    public String getIsStart() {
        return isStart;
    }

    public void setIsStart(String isStart) {
        this.isStart = isStart;
    }

    public String getStartedDocumentNumber() {
        return startedDocumentNumber;
    }

    public void setStartedDocumentNumber(String startedDocumentNumber) {
        this.startedDocumentNumber = startedDocumentNumber;
    }
}
