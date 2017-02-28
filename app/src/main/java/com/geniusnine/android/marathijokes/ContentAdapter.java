package com.geniusnine.android.marathijokes;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AndriodDev8 on 20-02-2017.
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    public List<MarathiJokesContent> mContent;


    private Context mContext;

    public ContentAdapter(Context mContext,List<MarathiJokesContent> mContent) {
        this.mContent = new ArrayList<>();
        this.mContext = mContext;
    }

    @Override
    public ContentAdapter.ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contentView = inflater.inflate(R.layout.row_list_content, parent, false);

        // Return a new holder instance
        ContentViewHolder viewHolder = new ContentViewHolder(contentView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ContentAdapter.ContentViewHolder holder, int position) {
        MarathiJokesContent Contents = mContent.get(position);
        TextView textView = holder.textViewContent;
        textView.setText(Contents.getContent());
    }

    @Override
    public int getItemCount() {
        return mContent.size();
    }

    public class ContentViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewContent;

        public ContentViewHolder(View itemView) {
            super(itemView);

            textViewContent = (TextView) itemView.findViewById(R.id.textViewContent);


         
        }
    }
}
