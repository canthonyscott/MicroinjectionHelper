package com.canthonyscott.microinjectioncalc;

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
 * Created by Anthony on 1/17/2016.
 * Adapted from "http://danielnugent.blogspot.com/2015/06/updated-jsonparser-with.html"
 */
public class JSONParser {

    String charset = "UTF-8";
    HttpsURLConnection conn;
    DataOutputStream wr;
    StringBuilder result;
    URL urlObj;
    StringBuilder sbParams;
    String parmsString;
    GetNetworkResource getNetworkResource;

    public JSONParser (GetNetworkResource getNetworkResource){
        this.getNetworkResource = getNetworkResource;

    }

    public String makeHttpRequest(String url, String method, HashMap<String,String> params){


        sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()){
            try {
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

        if (method.equals("POST")){
            // request method is post
            try{
                urlObj = new URL(url);
                conn = getNetworkResource.getSSLUrlConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Charset", charset);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.connect();
                parmsString = sbParams.toString();

                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(parmsString);
                wr.flush();
                wr.close();

            } catch (Exception e){
//                e.printStackTrace();
                return "failed";

            }
        }
        else if (method.equals("GET")){
            // request method is get
            if (sbParams.length()!=0){
                url += "?" + sbParams.toString();
            }

            try{
                urlObj = new URL(url);
                conn = getNetworkResource.getSSLUrlConnection();
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Charset", charset);
                conn.setConnectTimeout(15000);
                conn.connect();

            } catch (Exception e){
//                e.printStackTrace();
                return "failed";
            }

        }

        try{
            // Receive the response from the server
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null){
                result.append(line);
            }

            Log.d("JSON Parser", "result: " + result.toString());
        } catch (IOException e){
            e.printStackTrace();
        }
        try {
            int responseCode = conn.getResponseCode();
            Log.d("JSONParser","Response code: " + responseCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.disconnect();

        return result.toString();

    }
}
