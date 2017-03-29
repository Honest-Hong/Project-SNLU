package com.snlu.snluapp.item;

/**
 * Created by Hong Tae Joon on 2016-11-14.
 */

public class UserItem {
    private String phoneNumber;
    private String name;

    public UserItem() {
    }

    public UserItem(String phoneNumber, String name) {
        this.phoneNumber = phoneNumber;
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {

        return phoneNumber;
    }

    public String getName() {
        return name;
    }
}
