package com.geniusnine.android.marathijokes;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clockbyte.admobadapter.ViewWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AndriodDev8 on 20-02-2017.
 * this is adapter to connect data to content activity
 */

public class ContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MarathiJokesContent> items = new ArrayList<>();


    private Context context;

    public ContentAdapter(Context context, ArrayList<MarathiJokesContent> items) {
        this.items = items;
        this.context = context;
    }

    @Override
    public ViewWrapper<ContentHolder> onCreateViewHolder(ViewGroup parent, int viewType){
        return new ViewWrapper<ContentHolder>(new ContentHolder(context));
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position){
        ContentHolder contentHolder = (ContentHolder)viewHolder.itemView;
        MarathiJokesContent item = items.get(position);
        contentHolder.bind(item.getContent());
    }


    @Override
    public int getItemCount(){
        return  items.size();
    }


}
