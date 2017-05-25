package com.weedz.dice.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weedz.dice.R;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by WeeDz on 2016-08-14.
 */
public class HistoryListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "HistoryListAdapter";

    private final Context context;
    private final ArrayList<String> values;
    private ArrayList<Long> timestamp;

    public HistoryListAdapter(Context context, int resId, ArrayList<String> values, ArrayList<Long> timestamp) {
        super(context, resId, values);
        this.context = context;
        this.values = values;
        this.timestamp = timestamp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.history_row_layout, parent, false);

        TextView data = (TextView) rowView.findViewById(R.id.history_row_data);
        TextView time = (TextView) rowView.findViewById(R.id.history_row_time);
        data.setText(values.get(position));
        time.setText(new Timestamp(timestamp.get(position)).toString());

        return rowView;

    }
}
