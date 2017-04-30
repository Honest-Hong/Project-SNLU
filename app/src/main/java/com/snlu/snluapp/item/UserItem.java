package com.snlu.snluapp.item;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Garden on 2016-11-25.
 */

public class UserItem {
    private String id;
    private String name;
    private String imagePath;
    private boolean selected;

    public UserItem() {
    }

    public UserItem(String id, String name, String imagePath, boolean selected) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
        this.selected = selected;
    }

    public UserItem(JSONObject json) throws JSONException {
        id = json.getString("phoneNumber");
        name = json.getString("name");
        imagePath = json.getString("imageurl");
        selected = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
