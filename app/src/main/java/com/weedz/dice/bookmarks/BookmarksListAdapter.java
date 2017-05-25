package com.weedz.dice.bookmarks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weedz.dice.R;

import java.util.ArrayList;

/**
 * Created by WeeDz on 2016-08-12.
 */
public class BookmarksListAdapter extends ArrayAdapter<String> {

    private static final String TAG = "BookmarksListAdapter";

    private final ArrayList<String> name;
    private final ArrayList<ArrayList<Integer>> nr;
    private final ArrayList<ArrayList<Integer>> sides;

    private static class ViewHolder {
        TextView name, data;
    }

    public BookmarksListAdapter(Context context, int resId, ArrayList<String> values, ArrayList<String> name, ArrayList<ArrayList<Integer>> sidesList, ArrayList<ArrayList<Integer>> nrList) {
        super(context, resId, values);
        this.name = name;
        nr = nrList;
        sides = sidesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nr.get(position).size(); i++) {
            sb.append(nr.get(position).get(i)).append("d").
                    append(sides.get(position).get(i)).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
        }

        ViewHolder holder = (ViewHolder)convertView.getTag();
        holder.name.setText(name.get(position));
        holder.data.setText(sb.toString());

        return convertView;
    }

    private View newView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.bookmarks_row_layout, null);

        ViewHolder holder = new ViewHolder();
        holder.name = (TextView)rowView.findViewById(R.id.bookmarks_row_save_name);
        holder.data = (TextView)rowView.findViewById(R.id.bookmarks_row_save_data);

        rowView.setTag(holder);

        return rowView;
    }
}
