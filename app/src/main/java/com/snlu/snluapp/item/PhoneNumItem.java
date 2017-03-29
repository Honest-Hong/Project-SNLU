package com.snlu.snluapp.item;

/**
 * Created by Garden on 2016-11-25.
 */

public class PhoneNumItem {
    private String phoneNum;
    private String name;
    private boolean selected;

    public PhoneNumItem(String phoneNum, String name, boolean selected) {
        this.phoneNum = phoneNum;
        this.name = name;
        this.selected = selected;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
