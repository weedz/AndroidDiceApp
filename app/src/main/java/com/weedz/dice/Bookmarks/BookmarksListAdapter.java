package com.weedz.dice.Bookmarks;

import android.content.Context;
import android.util.Log;
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

    private final Context context;
    private final ArrayList<String> values;
    private final ArrayList<ArrayList<Integer>> nr;
    private final ArrayList<ArrayList<Integer>> sides;


    public BookmarksListAdapter(Context context, int resId, ArrayList<String> values, ArrayList<ArrayList<Integer>> sidesList, ArrayList<ArrayList<Integer>> nrList) {
        super(context, resId, values);
        this.context = context;
        this.values = values;
        nr = nrList;
        sides = sidesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.bookmarks_row_layout, parent, false);

        TextView saveName = (TextView) rowView.findViewById(R.id.bookmarks_row_save_name);
        TextView saveData = (TextView) rowView.findViewById(R.id.bookmarks_row_save_data);
        saveName.setText(values.get(position));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nr.get(position).size(); i++) {
            sb.append(nr.get(position).get(i)).append("d").
                    append(sides.get(position).get(i)).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
        }

        saveData.setText(sb.toString());

        return rowView;
    }



}
