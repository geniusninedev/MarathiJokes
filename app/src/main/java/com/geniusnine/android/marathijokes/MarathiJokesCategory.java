package com.geniusnine.android.marathijokes;

/**
 * Created by AndriodDev8 on 27-02-2017.
 */

public class MarathiJokesCategory {

    @com.google.gson.annotations.SerializedName("category")
    private String category;
    @com.google.gson.annotations.SerializedName("id")
    private  String id;
    public  MarathiJokesCategory(){

    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
