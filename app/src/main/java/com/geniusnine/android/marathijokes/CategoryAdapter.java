package com.geniusnine.android.marathijokes;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class CategoryAdapter extends ArrayAdapter<MarathiJokesCategory> {
    Context mContext;

    int mLayoutResourceId;

    public CategoryAdapter(Context context, int resource){
        super(context, resource);
        mContext = context;
        mLayoutResourceId = resource;

    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View row = convertView;
        final MarathiJokesCategory currentItem = getItem(position);
        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(R.layout.row_list_category, parent, false);

        }
        row.setTag(currentItem);

        final TextView textViewCategory = (TextView)row.findViewById(R.id.textViewCategories);
        textViewCategory.setText(currentItem.getCategory());

        final ImageView imageViewCategory = (ImageView) row.findViewById(R.id.imageViewCategory);
        Picasso.with(mContext).load(currentItem.getImagelink()).transform(new CircleTransform()).into(imageViewCategory);

        return row;

    }
}
