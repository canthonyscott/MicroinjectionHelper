package com.canthonyscott.microinjectioncalc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

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

        ListView listViewMO = (ListView) findViewById(R.id.historyList);
        adapter = new HistoryAdapter(this, R.layout.listview_history_item, historyArray);
        View header = getLayoutInflater().inflate(R.layout.listview_history_header, null);
        listViewMO.addHeaderView(header);

        listViewMO.setAdapter(adapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // download the oligos if connected to the database as reported by the main activity check
        if((prefs.getString("connectedToNetwork", "0")).equals("1")){
            DownloadHistory dh = new DownloadHistory();
            dh.execute();
        } else{
            Toast.makeText(ViewHistory.this, "Not connected to network, You shouldn't be here", Toast.LENGTH_SHORT).show();
        }



    }

    private class DownloadHistory extends AsyncTask<String, Void, String>{
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
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ViewHistory.this);
            pDialog.setMessage("Downloading History...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            String uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            paramData.put("uniqueID", uniqueID);
            String response = sendHttpRequest();
            try{
                jsonObject = new JSONObject(response);
                if(jsonObject.getString("status").equals("1")){
                    jsonArray = jsonObject.getJSONArray("InjHistory");
                    parseJsonData(jsonArray);
                    return "victory";
                } else {
                    return "failed";
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            if (s.equals("failed")){
                Toast.makeText(context, "You are not logged into the server", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "History Loaded", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                // loop for testing parsed array
                for(HistoryItem item : historyArray){
                    Log.d("ViewHistory","array item: " + item.getReagent());

                }

            }
        }

        private String sendHttpRequest(){

            sbParams = new StringBuilder();

            int i = 0;
            for (String key : paramData.keySet()){
                try {
                    if (i != 0){
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=")
                            .append(URLEncoder.encode(paramData.get(key), charset));
                } catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                i++;
            }

            // connect
            try {
                url = new URL(getNetworkResource.getUrl());
                conn = getNetworkResource.getSSLUrlConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Charset", charset);
                paramsString = sbParams.toString();
                conn.setRequestProperty("Cookie", getNetworkResource.getCookieToSend());
                conn.connect();

                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(paramsString);
                wr.flush();
                wr.close();

            } catch (Exception e) {
                e.printStackTrace();
                return "failed";
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
                return "failed";
            } finally {
                conn.disconnect();
            }

        }

        private void parseJsonData(JSONArray array) throws JSONException {
            historyArray.clear();
            for (int i = 0; i < array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                String reagent = obj.getString("Reagent");
                String nanoliters = obj.getString("nl");
                String pigograms = obj.getString("pg");
                String date = obj.getString("date");

                HistoryItem temp = new HistoryItem(date, reagent, nanoliters, pigograms);
                historyArray.add(temp);
            }
        }


    }

}
