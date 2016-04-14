package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

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
 * Created by Anthony on 2/12/2016.
 * This class will be used to log user activity to the database
 * It will return no data and will not display anything to the user.
 * It will contain different constructors to send different data to the database.
 * These constructors should set up their own hashmap which will be passed to a method to send the
 * request to the database.
 * There will also need to be a flag to send to the php script to inform it if it is an oligo injection
 * if it is an oligo injection it will send another SQL query to write to another table
 */
public class LogHistory extends AsyncTask <Void, Void, Void> {
    private HashMap <String, String> paramData = new HashMap<>();
    private String uniqueID;
    private String actionToLog;
    private GetNetworkResource getNetworkResource;

    public LogHistory(Context context, String actionToLog) {
        String uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("LogHistory","uniqueID: " + uniqueID);
        this.actionToLog = actionToLog;
        getNetworkResource = new GetNetworkResource(context, "logHistory.php");

        paramData.put("historyType", "event");
        paramData.put("uniqueID", uniqueID);
        paramData.put("action", actionToLog);

    }

    public LogHistory(Context context, String type, String reagent, String concentration, String mm, String pumps, String nanoliters, String picograms, String dilution1, String dilution2){
        getNetworkResource = new GetNetworkResource(context, "logHistory.php");
        String uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        paramData.put("historyType", "injection");
        paramData.put("uniqueID", uniqueID);
        paramData.put("type", type);
        paramData.put("mm", mm);
        paramData.put("pumps", pumps);
        paramData.put("nanoliters", nanoliters);
        paramData.put("picograms", picograms);
        paramData.put("concentration", concentration);
        paramData.put("reagent", reagent);
        paramData.put("dilution1", dilution1);
        paramData.put("dilution2", dilution2);

    }

    @Override
    protected Void doInBackground(Void... params) {
        sendHttpRequest();
        return null;
    }

    private void sendHttpRequest() {
        URL url;
        HttpsURLConnection conn;
        StringBuilder result;
        StringBuilder sbParams;
        String paramsString;
        String charset = "UTF-8";
        DataOutputStream wr;

        // get cookie manager for proper cookie requests
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        // build the POST data to send
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
            return;
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
            Log.d("LogHistory","server response: " + result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

    }

}
