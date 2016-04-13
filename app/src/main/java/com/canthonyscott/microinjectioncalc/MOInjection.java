package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.net.ssl.HttpsURLConnection;

public class MOInjection extends AppCompatActivity{

    //Create the MO Array List and a selected MO holder
    private final ArrayList<Morpholino> moList = new ArrayList<>();
    private Morpholino selectedMO;
    private Float oligoConcentration;

    //declare adapter here to give scope of the adapted to the AsyncTask Process
    private ArrayAdapter<Morpholino> adapter;

    // color used for the fab below
    private final static String primaryColor = "#2196F3";

    // some variables needed for broad scope
    private String serverAddress = null;
    private String URL_DOWNLOAD_OLIGOS = null;
    private Context context;
    private SharedPreferences sharedPreferences;
    private boolean networkConnected = false;
    private boolean showingShared = false;

    private GetNetworkResource getNetworkResource;

    // refresh everything if the activity is returned to using the back button
    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moinjection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getNetworkResource = new GetNetworkResource(getApplicationContext(), "downloadOligos.php");

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);


        final EditText numberOfPumps = (EditText) findViewById(R.id.numberOfPumps);
        final EditText numberOfMillimeters = (EditText) findViewById(R.id.numberOfMillimeters);
        final EditText dilution1 = (EditText) findViewById(R.id.dilution1);
        final EditText dilution2 = (EditText) findViewById(R.id.dilution2);
        final TextView injectionVolumeResult = (TextView) findViewById(R.id.injectionVolume);
        final TextView injectionQuantityResult = (TextView) findViewById(R.id.injectionQuantity);
        final TextView molarityUI = (TextView) findViewById(R.id.stockMolarity);
        context = getApplicationContext();

        // Get the stock oligo concentration from the user preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        oligoConcentration = Float.parseFloat(sharedPreferences.getString("oligoMolarity", "1"));
        molarityUI.setText(oligoConcentration.toString());
        // convert to mM concentration to M concentration
        oligoConcentration = oligoConcentration/1000;

        //get URL from sharedprefs
//        serverAddress = sharedPreferences.getString("serverAddressDomain","canthonyscott.tk:1106");
//        URL_DOWNLOAD_OLIGOS = "http://" + serverAddress + "/downloadOligos.php";



        // check if network is currently connected
        if(sharedPreferences.getString("connectedToNetwork", "0").equals("1")){
            networkConnected = true;
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(primaryColor)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddMorpholino.class);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Query the database in the Background using AsyncTask
            // onPostExecute sends completed notification to array adapter
        final DatabaseToArray databaseToArray = new DatabaseToArray();
        databaseToArray.execute();


        //Link code to spinner View
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Set the array adapter to go into the ArrayList of saved MOs and populate the spinner dropdown list
        adapter = new ArrayAdapter<> (this, android.R.layout.simple_spinner_item, moList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // get the selected MO from the spinner dropdown and place it into holding MO object
            // listener updates as user switches items
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMO = (Morpholino) spinner.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(MOInjection.this, "Your MO List is empty. You should fix that. Average molecular weight used", Toast.LENGTH_LONG).show();
                selectedMO = new Morpholino(8400.0, "Average");
                moList.add(selectedMO);
                adapter.notifyDataSetChanged();

            }
        });

        //Set onClickListener for Calculate button
        Button calculate = (Button) findViewById(R.id.calculate);
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // minimize the soft keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(numberOfPumps.getWindowToken(),0);


                // declare variables here for scope
                Double injectionVolume;
                Double dilution;

                // create local variables for the pumps and mm inputs
                // try,catch to prevent crash from user failing to provide values

                try {
                    // check to make sure user is not dividing by zero
                    if(dilution2.getText().toString().equalsIgnoreCase("0") || numberOfPumps.getText().toString().equalsIgnoreCase("0")){
                        Toast.makeText(MOInjection.this, "These values cannot be 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dilution = Double.parseDouble(dilution1.getText().toString())/Double.parseDouble(dilution2.getText().toString());
                    injectionVolume = ((Double.parseDouble(numberOfMillimeters.getText().toString()))*1000)/((Double.parseDouble(numberOfPumps.getText().toString()))*32);
                } catch (NumberFormatException e) {
                    Log.e("Input_Error", "User likely failed to input their own values, asking them to retry");
                    Toast.makeText(MOInjection.this, "Please enter your own values ", Toast.LENGTH_LONG).show();
                    return;
                }

                // Grab MO object selected by user from the spinner
                if (spinner.getSelectedItem() == null){
                    Toast.makeText(MOInjection.this, "Your MO List is empty. You should fix that. Average molecular weight used", Toast.LENGTH_LONG).show();
                    selectedMO = new Morpholino(8400.0, "Average");
                    moList.add(selectedMO);
                    adapter.notifyDataSetChanged();
                }

                // Extract molecular weight double
                Double molecularWeight = selectedMO.getMolecularWeight();
                // get the concentration by dividing the molecular weight of the oligo by its stock concentration
                Double injectionConcentration = molecularWeight * oligoConcentration;
                // calculate the injection quantity and round to 2 decimal places
                Double injectionQuantity;
                injectionQuantity = injectionVolume * injectionConcentration * dilution;

                // round the results and send to UI
                injectionVolume = Double.parseDouble(new DecimalFormat("#####.##").format(injectionVolume));
                injectionVolumeResult.setText(injectionVolume.toString());
                injectionQuantity = Double.parseDouble(new DecimalFormat("#####.##").format(injectionQuantity));
                injectionQuantityResult.setText(injectionQuantity.toString());

                LogHistory logHistory = new LogHistory(getApplicationContext(), "MicroInjection", dilution1.getText().toString(), dilution2.getText().toString(), numberOfMillimeters.getText().toString(),selectedMO.getGene(),numberOfPumps.getText().toString(),injectionQuantity.toString());
                logHistory.execute();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mo_injection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.downloadOligos) {
            if(networkConnected){
                // Downloaded the Shared Database if logged in
                DownloadOligos downloadOligos = new DownloadOligos();
                downloadOligos.execute();
            } else {
                Toast.makeText(MOInjection.this, "Not connected to server", Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.saveToLocalDB){
            if (showingShared == true){
                String gene = selectedMO.toString();
                Double mw = selectedMO.getMolecularWeight();
                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.addToMOTable(gene,mw,db);
                db.close();
                dbHelper.close();
                Toast.makeText(MOInjection.this, "Saved " + gene + " to your local database", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MOInjection.this, "You must download shared oligos first", Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.deleteMO) {
            deleteSelectedMO(selectedMO);
            adapter.notifyDataSetChanged();
        }
        if (id == R.id.logout){
            logout();
        }
        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteSelectedMO(Morpholino selectedMO){
        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        int id = selectedMO.getId();
        int removed = helper.removeFromMOTable(db, id);
        db.close();
        helper.close();
        if (removed == 0){
            Toast.makeText(MOInjection.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        } else if (removed > 0){
            Toast.makeText(MOInjection.this, "Removed MO from the database", Toast.LENGTH_SHORT).show();
            moList.remove(selectedMO);
        }
    }

    private void logout(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cookie", "a:1");
        editor.commit();
    }



    public class DatabaseToArray extends AsyncTask<Void, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(Void... params) {
            databaseToArrayMO();
            return moList;
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {
            super.onPostExecute(arrayList);
            // sort the array list alphabetically
            Collections.sort(moList, new Comparator<Morpholino>() {
                @Override
                public int compare(Morpholino lhs, Morpholino rhs) {
                    return lhs.toString().compareToIgnoreCase(rhs.toString());
                }
            });

            adapter.notifyDataSetChanged();
            Log.d("AsyncTask", arrayList.toString());
        }

        private void databaseToArrayMO(){

            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor c = helper.getAllRowsFromMOTable(db);
            c.moveToFirst();

            for (int i = 0; i < c.getCount(); i ++){
                String gene = c.getString(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_GENE));
                Double mw = c.getDouble(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_MOLWT));
                Integer id = c.getInt(c.getColumnIndex((FeedReaderContract.FeedEntry._ID)));
                moList.add(new Morpholino(mw,gene, id));
                c.moveToNext();
            }
            helper.close();

        }

    }

    public class DownloadOligos extends AsyncTask<String, Void, String>{

        URL url;
        HttpsURLConnection conn;
        StringBuilder result;
        JSONObject jsonObject;
        JSONArray jsonArray;

        @Override
        protected String doInBackground(String... params) {

            String response = sendHttpRequest();
            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("status").equalsIgnoreCase("0")){
                    return "failed";
                } else {
                    jsonArray = jsonObject.getJSONArray("oligos");
                    parseArray(jsonArray);
                    return "victory";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "failed";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("failed")){
                Toast.makeText(context, "You must be logged in. Redirecting...", Toast.LENGTH_LONG).show();
               //  send to log-in screen
                finish();
                startActivity(new Intent(context, Login.class));
            } else if (s.equals("victory")){
                Toast.makeText(context, "Shared Oligos Successfully Loaded", Toast.LENGTH_SHORT).show();
                showingShared = true;
            }
            // sort the downloaded array list alphabetically
            Collections.sort(moList, new Comparator<Morpholino>() {
                @Override
                public int compare(Morpholino lhs, Morpholino rhs) {
                    return lhs.toString().compareToIgnoreCase(rhs.toString());
                }
            });
            adapter.notifyDataSetChanged();
            selectedMO = moList.get(0);

        }

        private String sendHttpRequest(){


            // connect
            try {
                url = new URL(getNetworkResource.getUrl());
                conn = getNetworkResource.getSSLUrlConnection();
                conn.setDoInput(true);
                conn.setConnectTimeout(15000);
                conn.setRequestProperty("Cookie", getNetworkResource.getCookieToSend());
                conn.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // receive response
            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                result = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    result.append(line);
                }
                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            } finally {
                conn.disconnect();
            }

        }

        private void parseArray(JSONArray jsonArray){
            moList.clear();
            for (int i = 0; i < jsonArray.length(); i++){
                try {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String name = obj.getString("OligoName");
                    Double molWt = Double.parseDouble(obj.getString("MolecularWeight"));

                    moList.add(new Morpholino(molWt,name,-1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }





}
