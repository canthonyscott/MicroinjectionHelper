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

public class manageSharedOligos extends AppCompatActivity {
    private OligoAdapter adapter;
    private ArrayList<Morpholino> moList = new ArrayList();
    private Context context;
    private Morpholino selectedMO;
    private GetNetworkResource getNetworkResource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_shared_oligos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getNetworkResource = new GetNetworkResource(getApplicationContext(), "downloadOnlyMyOligos.php");

        // get cookie manager for proper cookie requests
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

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



        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // download the oligos if connected to the database as reported by the main activity check
        if((prefs.getString("connectedToNetwork", "0")).equals("1")){
             DownloadOnlyMyOligos download = new DownloadOnlyMyOligos();
            download.execute();
        } else{
            Toast.makeText(manageSharedOligos.this, "Not connected to network, You shouldn't be here", Toast.LENGTH_SHORT).show();
        }



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
            DeleteOligoFromNetwork deleteOligo = new DeleteOligoFromNetwork(selectedMO.getId(),context);
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
            } else if (s.equals("victory")){
                Toast.makeText(context, "Downloaded Your Shared Oligos", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();

            }


        }

        @Override
        protected String doInBackground(String... params) {
            String uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            paramData.put("uniqueID", uniqueID);
            String response = sendHttpRequest();
            Log.d("manageSharedOligos", "http response: " + response);

            try {
                jsonObject = new JSONObject(response);
                if (jsonObject.getString("status").equals("0")){
                    return "failed";
                } else {
                    jsonArray = jsonObject.getJSONArray("oligos");
                    parseJsonData(jsonArray);
                    return "victory";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "failed";
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
            moList.clear();
            for (int i = 0; i < array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                String name = obj.getString("OligoName");
                Double mw = Double.parseDouble(obj.getString("MolecularWeight"));
                int id = Integer.parseInt(obj.getString("ID"));

                Morpholino temp = new Morpholino(mw, name, id);
                moList.add(temp);
            }

        }
    }

    public class DeleteOligoFromNetwork extends AsyncTask<String, Void, String>{
        private Integer id;
        private Context context;
        private SharedPreferences sharedPreferences;
        private CookieManager cookieManager;
        private String url;
        private String cookie;
        private StringBuilder sbParams;
        private final String charset = "UTF-8";
        private URL urlObj;
        private HttpsURLConnection conn;
        private String paramsString;
        private DataOutputStream wr;
        private StringBuilder result;
        private HashMap<String,String> oligoToDelete = new HashMap<>();
        private GetNetworkResource getNetworkResource;

        // constructor that takes the database ID of the shared oligo
        public DeleteOligoFromNetwork(Integer id, Context context) {
            this.id = id;
            this.context = context;
            getNetworkResource = new GetNetworkResource(getApplicationContext(), "deleteOligo.php");

            // work to do on construction
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//            String serverAddress = sharedPreferences.getString("serverAddressDomain", "173.29.130.159:1106");
//            url = "http://" + serverAddress + "/deleteOligo.php";


            cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);

            oligoToDelete.put("oligoID", id.toString());

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("victory")){
                moList.remove(selectedMO);
                adapter.notifyDataSetChanged();
                LogHistory logHistory = new LogHistory(getApplicationContext(), "DeletedSharedOligo");
                logHistory.execute();
            }
            if (s.equals("failed")){
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if(sendHttpRequest(getNetworkResource.getUrl(), oligoToDelete)){
                    return "victory";
                } else {
                    return "failed";

                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "JSONException";
            }
        }

        private boolean sendHttpRequest(String url, HashMap<String,String> params) throws JSONException {
            // generate the POST string to send to server
            sbParams = new StringBuilder();
            int i = 0;
            for (String key : params.keySet()){
                try{
                    if (i != 0){
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=")
                            .append(URLEncoder.encode(params.get(key), charset));
                } catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                i++;
            }

            // send the POST request
            try {
                urlObj = new URL(url);
                conn = getNetworkResource.getSSLUrlConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Charset", charset);
                conn.setRequestProperty("Cookie", getNetworkResource.getCookieToSend());
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.connect();
                paramsString = sbParams.toString();

                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(paramsString);
                wr.flush();
                wr.close();


            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            // recieve the server response
            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    result.append(line);
                }
                Log.d("DeleteOligoFromNetwork", "server resposne: " + result.toString());

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            conn.disconnect();
            JSONObject jsonObject = new JSONObject(result.toString());
            if (jsonObject.getString("status").equals("1")){
                return true;
            } else {
                return false;
            }

        }
    }


}
