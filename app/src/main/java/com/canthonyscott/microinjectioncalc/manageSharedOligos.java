package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class manageSharedOligos extends AppCompatActivity {
    private OligoAdapter adapter;
    private ArrayList<Morpholino> moList = new ArrayList();
    private Context context;
    private Morpholino selectedMO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_shared_oligos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();


        final ListView sharedListViewMO = (ListView) findViewById(R.id.sharedListViewMO);
        adapter = new OligoAdapter(this, R.layout.listview_item_row_mo, moList);
        sharedListViewMO.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        sharedListViewMO.setAdapter(adapter);

        // get the selected MO from the checked listview item
        sharedListViewMO.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("CLICKED", "Position: " + position);
                adapter.setStoredSelectedItem(position);
                selectedMO = (Morpholino) sharedListViewMO.getItemAtPosition(position);
                Log.d("CLICKED", "SelectedMO: " + selectedMO.toString());
                Log.d("CLICKED", "database ID: " + selectedMO.getId());
                adapter.notifyDataSetChanged();
            }
        });

        DownloadOnlyMyOligos downloadOnlyMyOligos = new DownloadOnlyMyOligos();
        downloadOnlyMyOligos.execute();




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_remove_shared, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        if (id == R.id.deleteMO) {
            DeleteOligoFromNetwork deleteOligo = new DeleteOligoFromNetwork(selectedMO);
            deleteOligo.execute();
        }
        
        return super.onOptionsItemSelected(item);
    }



    private class DownloadOnlyMyOligos extends AsyncTask<String, Void, String>{
        URL url;
        HttpsURLConnection conn;
        StringBuilder result;
        JSONObject jsonObject;
        JSONArray jsonArray;
        HashMap<String,String> paramData = new HashMap<>();
        StringBuilder sbParams;
        String paramsString;
        String charset = "UTF-8";
        DataOutputStream wr;

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("failed")){
                Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
                return;
            }
            try{
                JSONArray jsonArray = new JSONArray(s);
                parseJsonData(jsonArray);
                adapter.notifyDataSetChanged();
            } catch (JSONException e){
                e.printStackTrace();
            }


        }

        @Override
        protected String doInBackground(String... params) {
            String uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            paramData.put("uniqueID", uniqueID);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String token = prefs.getString("auth_token", "LOGOUT");

            APIComm connect = new APIComm();
            String rawJsonData = connect.makeHttpsRequestGET("/oligos/", token);
            return rawJsonData;

        }



        private void parseJsonData(JSONArray array) throws JSONException {
            moList.clear();
            for (int i = 0; i < array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                String name = obj.getString("gene");
                Double mw = Double.parseDouble(obj.getString("molecular_weight"));
                int id = Integer.parseInt(obj.getString("pk"));

                Morpholino temp = new Morpholino(mw, name, id);
                moList.add(temp);
            }

        }
    }

    public class DeleteOligoFromNetwork extends AsyncTask<String, Void, String>{
        private Morpholino oligo;

        // constructor that takes the database ID of the shared oligo
        public DeleteOligoFromNetwork(Morpholino oligo) {
            this.oligo = oligo;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equalsIgnoreCase("success")){
                adapter.remove(oligo);
                Toast.makeText(context, "Oligo deleted from shared database", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            APIComm connect = new APIComm();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String token = prefs.getString("auth_token", "LOGOUT");

            return connect.makeHttpsRequestDELETE("/oligos/", token, oligo);
        }

    }


}
