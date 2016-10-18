package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.CookieManager;
import java.net.URL;
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

        oligoToAdd.put("gene",oligoName);
        oligoToAdd.put("molecular_weight", Double.toString(molecularWeight));

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // get a uniqueID to identify android devices
        uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        oligoToAdd.put("added_by", uniqueID);

    }

    @Override
    protected String doInBackground(String... params) {

        String token = sharedPreferences.getString("auth_token", "LOGOUT");
        APIComm connect = new APIComm();
        String rawJsonData = connect.makeHttpsRequestPOST("/oligos/", oligoToAdd, true, this.context);
        return rawJsonData;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s.equalsIgnoreCase("failed")){
            Toast.makeText(context, "Failed to add to shared database", Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.has("pk")){
                Toast.makeText(context, "Successfully added to the shared database", Toast.LENGTH_SHORT).show();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

}
