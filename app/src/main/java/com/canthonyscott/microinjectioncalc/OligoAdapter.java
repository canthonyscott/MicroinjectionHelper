package com.canthonyscott.microinjectioncalc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Anthony on 1/7/2016.
 * Adapter to display gene name and molecular weight on the listview
 */
public class OligoAdapter extends ArrayAdapter<Morpholino> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Morpholino> list;
    private int storedSelectedItem = -1;

    public void setStoredSelectedItem(int storedSelectedItem) {
        this.storedSelectedItem = storedSelectedItem;
    }

    public OligoAdapter(Context context, int resource, ArrayList<Morpholino> list) {
        super(context, resource, list);
        this.context = context;
        this.layoutResourceId = resource;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        OligoHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new OligoHolder();
            holder.geneName = (TextView) row.findViewById(R.id.geneName);
            holder.molecularWeight = (CheckedTextView) row.findViewById(R.id.molecularWeight_adapter);
            row.setTag(holder);
        } else {
            holder = (OligoHolder) row.getTag();
        }
        if (position == storedSelectedItem){
            Log.d("Adapter", "posistion: " + position);
            Log.d("Adapter", "storedSelectedItem: " + storedSelectedItem);
            holder.molecularWeight.setChecked(true);

        } else{
            holder.molecularWeight.setChecked(false);
        }

        Morpholino morpholino = list.get(position);
        holder.geneName.setText(morpholino.toString());
        holder.molecularWeight.setText(String.valueOf(morpholino.getMolecularWeight()));

        return row;
    }

}

class OligoHolder
{
    TextView geneName;
    CheckedTextView molecularWeight;
}
