package com.canthonyscott.microinjectioncalc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView statusResult;
    private SharedPreferences prefs;
    private TextView loggedInAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d("MainActivity", "Stored auth_token: " + prefs.getString("auth_token", "LOGOUT"));

        // do some cleanup of unneeded resources from previous versions

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settingsLogin) {
            startActivity(new Intent(this, Login.class));
            return true;
        }
        if (id == R.id.settingsLogout) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("auth_token", "LOGOUT");
            editor.commit();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // methods for handling MainActivity Button clicks
    public void openMOActivity(View view) {
        Intent intent = new Intent(this, MOInjection.class);
        startActivity(intent);
    }

    public void openRNAActivity(View view) {
        Intent intent = new Intent(this, RNAInjection.class);
        startActivity(intent);
    }

    public void savedOligos(View view) {
        Intent intent = new Intent(this, RemoveMO.class);
        startActivity(intent);
    }

    public void calcRNAMix(View view) {
        startActivity(new Intent(this, MakeAnInjectionMix.class));
    }

//    public void injHistView(View view){
//        startActivity(new Intent(this, ViewHistory.class));
//
//    }


}
