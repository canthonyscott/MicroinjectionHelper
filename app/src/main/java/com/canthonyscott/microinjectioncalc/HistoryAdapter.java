package com.canthonyscott.microinjectioncalc;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Anthony on 6/13/2016.
 */
public class HistoryAdapter extends ArrayAdapter<HistoryItem> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<HistoryItem> list;

    public HistoryAdapter(Context context, int resource, ArrayList<HistoryItem> list) {
        super(context, resource, list);
        this.context = context;
        this.layoutResourceId = resource;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        HistoryHolder holder = null;

        if (row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new HistoryHolder();
            holder.reagent = (TextView) row.findViewById(R.id.history_reagent);
            holder.nanoliters = (TextView) row.findViewById(R.id.history_nl);
            holder.pigograms = (TextView) row.findViewById(R.id.history_pg);
            holder.pigograms = (TextView) row.findViewById(R.id.history_date);

            row.setTag(holder);
        } else{
            holder = (HistoryHolder) row.getTag();
        }

        HistoryItem historyItem = list.get(position);
        holder.reagent.setText(historyItem.getReagent());
        holder.nanoliters.setText(historyItem.getNanoliters());
        holder.pigograms.setText(historyItem.getPicograms());
        holder.pigograms.setText(historyItem.getDate());

        return  row;
    }
}

class HistoryHolder
{
    TextView reagent;
    TextView nanoliters;
    TextView pigograms;
    TextView date;
}

