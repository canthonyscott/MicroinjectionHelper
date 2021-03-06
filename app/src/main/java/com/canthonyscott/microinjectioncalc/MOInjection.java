package com.canthonyscott.microinjectioncalc;

import android.app.ProgressDialog;
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
import android.support.v4.content.ContextCompat;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private final static String primaryColor = "#4CAF50";

    // some variables needed for broad scope
    private Context context;
    private SharedPreferences sharedPreferences;
    private boolean showingShared = false;

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

        final EditText numberOfPumps = (EditText) findViewById(R.id.numberOfPumps);
        final EditText numberOfMillimeters = (EditText) findViewById(R.id.numberOfMillimeters);
        final EditText dilution1 = (EditText) findViewById(R.id.dilution1);
        final EditText dilution2 = (EditText) findViewById(R.id.dilution2);
        final TextView injectionVolumeResult = (TextView) findViewById(R.id.injectionVolume);
        final TextView injectionQuantityResult = (TextView) findViewById(R.id.injectionQuantity);
        final TextView molarityUI = (TextView) findViewById(R.id.stockMolarity);
        final LinearLayout oligoLayout = (LinearLayout) findViewById(R.id.oligVolumeLayout);
        final LinearLayout totalLayout = (LinearLayout) findViewById(R.id.totalVolumeLayout);
        context = getApplicationContext();

        // Get the stock oligo concentration from the user preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        oligoConcentration = Float.parseFloat(sharedPreferences.getString("oligoMolarity", "1"));
        String concText = "Concentration: " + oligoConcentration.toString() + " mM";
        molarityUI.setText(concText);
        // convert to mM concentration to M concentration
        oligoConcentration = oligoConcentration/1000;




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

        //Set onSelectListener to change layout colors
        dilution1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(dilution1, dilution2, numberOfPumps, numberOfMillimeters);
                int color = ContextCompat.getColor(context, R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });
        dilution2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(dilution1, dilution2, numberOfPumps, numberOfMillimeters);
                int color = ContextCompat.getColor(context, R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });
        numberOfPumps.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(dilution1, dilution2, numberOfPumps, numberOfMillimeters);
                int color = ContextCompat.getColor(context, R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });
        numberOfMillimeters.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(dilution1, dilution2, numberOfPumps, numberOfMillimeters);
                int color = ContextCompat.getColor(context, R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });

        //Set onClickListener for Calculate button
        Button calculate = (Button) findViewById(R.id.calculate);
        assert calculate != null;

        calculate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // minimize the soft keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(numberOfPumps.getWindowToken(),0);

            }
        });

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clear the colors of the layouts
                resetLayoutColors(dilution1, dilution2, numberOfPumps, numberOfMillimeters);
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
                // convert injection quantity into picograms, needed for consistent DB storage
                Double injectionQuantityPg = injectionQuantity * 1000;
                injectionQuantityPg = Double.parseDouble(new DecimalFormat("#####.##").format(injectionQuantityPg));

                // round the results and send to UI
                injectionVolume = Double.parseDouble(new DecimalFormat("#####.##").format(injectionVolume));
                injectionVolumeResult.setText(injectionVolume.toString());
                injectionQuantity = Double.parseDouble(new DecimalFormat("#####.##").format(injectionQuantity));
                injectionQuantityResult.setText(injectionQuantity.toString());

            }
        });

    }

    private void resetLayoutColors(EditText oligoVol, EditText TotalVol, EditText pumps, EditText mm){
        oligoVol.setBackgroundColor(Color.WHITE);
        TotalVol.setBackgroundColor(Color.WHITE);
        pumps.setBackgroundColor(Color.WHITE);
        mm.setBackgroundColor(Color.WHITE);
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
            // Downloaded the Shared Database if logged in
            DownloadOligos downloadOligos = new DownloadOligos();
            downloadOligos.execute();

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
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MOInjection.this);
            pDialog.setMessage("Connecting...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            String token = sharedPreferences.getString("auth_token", "LOGOUT");
            APIComm InjCalcAPI = new APIComm();
            String response = InjCalcAPI.makeHttpsRequestGET("/oligos/", token);

            try {
                jsonArray = new JSONArray(response);
                parseArray(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
                return "failed";
            }

            return "victory";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            if (s.equals("failed")){
                Toast.makeText(context, "You must be logged in. Redirecting...", Toast.LENGTH_LONG).show();
               //  send to log-in screen
                finish();
                startActivity(new Intent(context, Login.class));
            } else if (s.equals("victory")){
                showingShared = true;
            }
            try {
                // sort the downloaded array list alphabetically
                Collections.sort(moList, new Comparator<Morpholino>() {
                    @Override
                    public int compare(Morpholino lhs, Morpholino rhs) {
                        return lhs.toString().compareToIgnoreCase(rhs.toString());
                    }
                });
                adapter.notifyDataSetChanged();
                selectedMO = moList.get(0);
                Toast.makeText(context, "Shared Oligos Successfully Loaded", Toast.LENGTH_SHORT).show();
            } catch (IndexOutOfBoundsException e){
                Toast.makeText(MOInjection.this, "You don't have any shared oligos yet.", Toast.LENGTH_SHORT).show();
            }

        }


        private void parseArray(JSONArray jsonArray){
            moList.clear();
            for (int i = 0; i < jsonArray.length(); i++){
                try {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String name = obj.getString("gene");
                    Double molWt = Double.parseDouble(obj.getString("molecular_weight"));

                    moList.add(new Morpholino(molWt,name,-1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }





}
