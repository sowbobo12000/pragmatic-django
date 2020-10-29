package com.fresconews.fresco.v2.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fresconews.fresco.R;

import java.util.List;

public class SpinnerItemAdapter extends ArrayAdapter<String> {
    private int resource;

    public SpinnerItemAdapter(Context context, int resource, int textViewResource, List<String> objects) {
        super(context, resource, textViewResource, objects);

        this.resource = resource;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        SpinnerViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new SpinnerViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(resource, parent, false);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.spinner_item_text_view);
            viewHolder.nameTextView.setGravity(Gravity.CENTER | Gravity.LEFT | Gravity.START);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (SpinnerViewHolder) convertView.getTag();
        }

        String string = getItem(position);
        viewHolder.nameTextView.setText(string);

        return convertView;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        SpinnerViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new SpinnerViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(resource, parent, false);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.spinner_item_text_view);
            viewHolder.nameTextView.setPadding(0, 0, 0, 0);
            viewHolder.nameTextView.setGravity(Gravity.CENTER | Gravity.LEFT | Gravity.START);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (SpinnerViewHolder) convertView.getTag();
        }

        String string = getItem(position);
        viewHolder.nameTextView.setText(string);

        return convertView;
    }

    private static class SpinnerViewHolder {
        TextView nameTextView;
    }
}