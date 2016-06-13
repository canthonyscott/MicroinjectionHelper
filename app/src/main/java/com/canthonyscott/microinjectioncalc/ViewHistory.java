package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;

public class ViewHistory extends AppCompatActivity {

    private  HistoryAdapter adapter;
    private ArrayList<HistoryItem> historyArray = new ArrayList<>();
    private Context context;
    private GetNetworkResource getNetworkResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getNetworkResource = new GetNetworkResource(getApplicationContext(), "get_inj_history.php");

        // get cookie manager for proper cookie requests
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        final ListView listViewMO = (ListView) findViewById(R.id.historyList);
        adapter = new HistoryAdapter(this, R.layout.listview_history_item, historyArray);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // download the oligos if connected to the database as reported by the main activity check
        if((prefs.getString("connectedToNetwork", "0")).equals("1")){
            // TODO: 6/13/2016 CONNECT AND DOWNLOAD HISOTRY
        } else{
            Toast.makeText(ViewHistory.this, "Not connected to network, You shouldn't be here", Toast.LENGTH_SHORT).show();
        }



    }

}
