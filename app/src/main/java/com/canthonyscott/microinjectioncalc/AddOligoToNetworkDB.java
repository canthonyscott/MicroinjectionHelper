package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

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
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Anthony on 1/23/2016.
 * This will add an entered Oligo into the Database
 * Currently this works while no user authentication is employed.
 * Baby steps lol
 */
class AddOligoToNetworkDB extends AsyncTask<String, Void, String>{
    private static final String LOG_TAG = "AddOligoNetworkConn";
    private String oligoName;
    private double molecularWeight;
    private Context context;
    private String uniqueID;
    private String cookie;
    private SharedPreferences sharedPreferences;
    private CookieManager cookieManager;


    private StringBuilder sbParams;
    private final String charset = "UTF-8";
    private URL urlObj;
    private HttpsURLConnection conn;
    private String paramsString;
    private DataOutputStream wr;
    private StringBuilder result;
    GetNetworkResource getNetworkResource;



    private HashMap<String,String> oligoToAdd = new HashMap<>();

    public AddOligoToNetworkDB(double molecularWeight, String oligoName, Context context) {
        this.molecularWeight = molecularWeight;
        this.oligoName = oligoName;
        this.context = context;
        getNetworkResource = new GetNetworkResource(context, "add_oligo.php");

        oligoToAdd.put("oligoName",oligoName);
        oligoToAdd.put("molWt", Double.toString(molecularWeight));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // get a uniqueID to identify android devices
        uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        oligoToAdd.put("uniqueId", uniqueID);


        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);


    }

    @Override
    protected String doInBackground(String... params) {
        try {
            if(sendHttpRequest(getNetworkResource.getUrl(), oligoToAdd)){
                return "Successfully added to network database";
            }
            else{
                return "Failed to add to network database";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "Failed to Read JSON Data";
        }

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
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
                        .append(URLEncoder.encode(params.get(key),charset));
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
            String cookieSend = getNetworkResource.getCookieToSend();
            Log.d("MOInjection", "cookieSend: " + cookieSend);
            conn.setRequestProperty("Cookie", cookieSend);
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
            Log.d(LOG_TAG, "server resposne: " + result.toString());

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
