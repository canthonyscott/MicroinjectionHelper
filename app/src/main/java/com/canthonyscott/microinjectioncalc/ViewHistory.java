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
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
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

        final ListView listViewMO = (ListView) findViewById(R.id.historyList);
        adapter = new HistoryAdapter(this, R.layout.listview_history_item, historyArray);

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
            // TODO: 6/13/2016 PARSE THE RESPOSNE AND LOAD THE JSON ARRAY INTO A HISTORY ITEM ARRAY AND UPDATE ADAPTER 
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
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

    }

}
