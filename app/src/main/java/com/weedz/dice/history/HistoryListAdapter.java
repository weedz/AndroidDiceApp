package com.weedz.dice.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weedz.dice.R;

import java.util.ArrayList;

/**
 * Created by WeeDz on 2016-08-14.
 */
public class HistoryListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "HistoryListAdapter";

    private final Context context;
    private final ArrayList<String> values;


    public HistoryListAdapter(Context context, int resId, ArrayList<String> values) {
        super(context, resId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.history_row_layout, parent, false);

        TextView data = (TextView) rowView.findViewById(R.id.history_row_data);
        data.setText(values.get(position));

        return rowView;

    }
}
