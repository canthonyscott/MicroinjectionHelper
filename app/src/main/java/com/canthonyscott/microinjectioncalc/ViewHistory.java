package com.canthonyscott.microinjectioncalc;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.util.ArrayList;

public class ViewHistory extends AppCompatActivity {

    private  HistoryAdapter adapter;
    private ArrayList<HistoryItem> historyArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView listViewMO = (ListView) findViewById(R.id.historyList);
        adapter = new HistoryAdapter(this, R.layout.listview_history_item, historyArray);


    }

}
