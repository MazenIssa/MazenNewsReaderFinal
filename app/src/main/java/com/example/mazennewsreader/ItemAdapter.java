package com.example.mazennewsreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class ItemAdapter extends ArrayAdapter<News> {
    private final Context context;

    public ItemAdapter(Context context, int textViewResourceId, List<News> items) {
        super(context, textViewResourceId, items);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_layout, null);
        }

        News item = getItem(position);
        if (item != null) {
            TextView title = view.findViewById(R.id.title);
            TextView desc = view.findViewById(R.id.desc);
            title.setText(item.getTitle());
            desc.setText(item.getDescription());
        }

        return view;
    }
}
