package com.geniusnine.android.marathijokes;

/**
 * Created by AndriodDev8 on 20-02-2017.
 */

public class MarathiJokesContent {//Change the parameter

    @com.google.gson.annotations.SerializedName("id")
    private String id ;
    @com.google.gson.annotations.SerializedName("category")
    private String category;
    @com.google.gson.annotations.SerializedName("content")
    private String content;

    public MarathiJokesContent() {//Change the parameter
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
