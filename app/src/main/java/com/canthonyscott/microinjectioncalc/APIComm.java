package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Anthony on 10/13/2016.
 */

public class APIComm {

    String charset = "UTF-8";

    public APIComm() {
    }

    public String makeHttpsRequestGET(String url, String token){

        String base_url = "https://injcalcapi.herokuapp.com";
        String specificUrl = url;
        String wholeUrl = base_url + specificUrl;
        HttpsURLConnection conn;
        StringBuilder result = null;

        // SEND POST REQUEST TO SERVER
        try{
            Log.d("APIComm", "whole url: " + wholeUrl);
            URL InjCalcAPI = new URL(wholeUrl);
            conn = (HttpsURLConnection) InjCalcAPI.openConnection();
            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", charset);
            conn.setRequestProperty("Authorization", token);
            conn.connect();

        } catch (Exception e){
            e.printStackTrace();
            return "failed";
        }

        // RECIEVE THE RESPONSE FROM THE SERVER
        try{
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                result.append(line);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        try{
            int responseCode = conn.getResponseCode();
            Log.d("APIComm", "Response Code: " + responseCode);
            if (responseCode != 200)
                return "failed";
        } catch (IOException e){
            e.printStackTrace();
        }
        conn.disconnect();
        return result.toString();
    }



    public String makeHttpsRequestPOST(String url, HashMap<String, String> params, Boolean useToken, Context context){

        String base_url = "https://injcalcapi-staging.herokuapp.com/api/";
        String specificUrl = url;
        String wholeUrl = base_url + specificUrl;
        HttpsURLConnection conn;
        StringBuilder result = null;

        // SEND POST REQUEST TO SERVER
        try{
            Log.d("APIComm", "whole url: " + wholeUrl);
            URL InjCalcAPI = new URL(wholeUrl);
            conn = (HttpsURLConnection) InjCalcAPI.openConnection();
            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", charset);
            if(useToken){
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String token = sharedPreferences.getString("auth_token", "LOGOUT");
                conn.setRequestProperty("Authorization", token);
            }
            conn.setDoOutput(true);
            conn.connect();
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            String paramsString = paramsToString(params);
            wr.writeBytes(paramsString);
            wr.flush();
            wr.close();

        } catch (Exception e){
            e.printStackTrace();
            return "failed";
        }

        // RECIEVE THE RESPONSE FROM THE SERVER
        try{
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                result.append(line);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        try{
            int responseCode = conn.getResponseCode();
            Log.d("APIComm", "Response Code: " + responseCode);
        } catch (IOException e){
            e.printStackTrace();
        }
        conn.disconnect();
        try{
            Log.d("APIComm", "response: " + result.toString());
            return result.toString();
        } catch (NullPointerException e){
            return "failed";
        }
    }

    public String makeHttpsRequestDELETE(String url, String token, Morpholino oligo){

        String base_url = "https://injcalcapi.herokuapp.com/api/";
        String specificUrl = url;
        String idToDelete = Integer.toString(oligo.getId());
        String wholeUrl = base_url + specificUrl + idToDelete + "/";
        Log.d("APIComm", "whole url: " + wholeUrl);
        HttpsURLConnection conn;
        StringBuilder result = null;

        // SEND POST REQUEST TO SERVER
        try{
            Log.d("APIComm", "whole url: " + wholeUrl);
            URL InjCalcAPI = new URL(wholeUrl);
            conn = (HttpsURLConnection) InjCalcAPI.openConnection();
            conn.setRequestMethod("DELETE");
//            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", charset);
            conn.setRequestProperty("Authorization", token);
            conn.connect();

        } catch (Exception e){
            e.printStackTrace();
            return "failed";
        }

        // RECIEVE THE RESPONSE FROM THE SERVER
        try{
            int responseCode = conn.getResponseCode();
            Log.d("APIComm", "Response Code: " + responseCode);
            if (responseCode == 204){
                return "success";
            } else {
                return "failed";
            }
        }catch (IOException e){
            e.printStackTrace();
            return "failed";
        }
    }


    private String paramsToString(HashMap<String,String> params){
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()){
            try{
                if (i != 0){
                    sb.append("&");
                }
                sb.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), charset));
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            i++;
        }

        return sb.toString();
    }



}
