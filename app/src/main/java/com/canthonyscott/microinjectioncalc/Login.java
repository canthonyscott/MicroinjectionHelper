package com.canthonyscott.microinjectioncalc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.util.HashMap;

public class Login extends AppCompatActivity {

    private String user;
    private String pass;
    private final HashMap<String,String> paramData = new HashMap<>();
    private String rawJSONData;
    private TextView result;

    private SharedPreferences sharedPreferences;
    private CookieManager cookieManager;

    private GetNetworkResource getNetworkResource;

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = getApplicationContext();

        getNetworkResource = new GetNetworkResource(getApplicationContext(), "login.php");

        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);


        // generate URL to server resource
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        final EditText username = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);
        Button login = (Button) findViewById(R.id.addUserBtn);
        result = (TextView) findViewById(R.id.resultUI);
        Button createID = (Button) findViewById(R.id.createUser);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();
                serverLogIn login = new serverLogIn();
                login.execute();
            }
        });

        createID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish this activity and start create user activity
                finish();
                startActivity(new Intent(getApplicationContext(), CreateGroupID.class));
            }
        });

    }

    private class serverLogIn extends AsyncTask<String,Void,String> {

        JSONObject jsonObject;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Logging in...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            paramData.put("username", user);
            paramData.put("password", pass);
            paramData.put("uniqueID", uniqueID);

            JSONParser connect = new JSONParser(getNetworkResource);
            rawJSONData = connect.makeHttpRequest(getNetworkResource.getUrl(),"POST",paramData);
            return rawJSONData;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            if (s.equalsIgnoreCase("failed")){
                String message = "Could not connect to server";
                result.setText(message);
                return;
            }
            Log.d("LogIN", "Result: " + rawJSONData);
            try {
                jsonObject = new JSONObject(rawJSONData);
                //check if status failed
                if (jsonObject.getString("status").equalsIgnoreCase("0")){
                    Log.d("LogIN","Password does not match");
                    showResultToUI();
                    return;
                } else if (jsonObject.get("status").equals("1")){
                    String networkID = jsonObject.getString("userID");
                    showResultToUI();
                    clearScreen();
                    CookieStore store = cookieManager.getCookieStore();
                    saveCookie();
                    // save Database userID for future requets
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("networkID",networkID);
//                    editor.commit();
                    // close activity and reload MO injection
                    LogHistory logHistory = new LogHistory(getApplicationContext(), "Login");
                    logHistory.execute();
                    finish();
                    startActivity(new Intent(context, MOInjection.class));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void showResultToUI() throws JSONException {
            String messageString;
            messageString = jsonObject.getString("message");
            result.setText(messageString);
        }

        private void clearScreen(){
            final EditText username = (EditText) findViewById(R.id.username);
            final EditText password = (EditText) findViewById(R.id.password);
            username.setText("");
            password.setText("");
        }

    }

    // saved server provided cookie to sharedprefs
    private void saveCookie(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String wholeCookie = cookieManager.getCookieStore().getCookies().toString();
        String [] split = wholeCookie.split("=");
        String cookie = split[1];
        String cookieSub = cookie.substring(0, cookie.length()-1);
        Log.d("saveCookie", cookieSub);
        editor.putString("cookie", cookieSub);
        editor.commit();

    }



}
