package com.snlu.snluapp.item;

/**
 * Created by Hong Tae Joon on 2016-11-14.
 */

public class DocumentItem {
    private String number;
    private String date;

    public DocumentItem() {
    }

    public DocumentItem(String number, String date) {
        this.number = number;
        this.date = date;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public String getDate() {
        return date;
    }
}
