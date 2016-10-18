package com.canthonyscott.microinjectioncalc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private String user;
    private String pass;
    private final HashMap<String,String> paramData = new HashMap<>();
    private String rawJSONData;
    private TextView result;

    private SharedPreferences sharedPreferences;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = getApplicationContext();


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
            paramData.put("username", user);
            paramData.put("password", pass);

            APIComm connect = new APIComm();
            rawJSONData = connect.makeHttpsRequestPOST("/api-token-auth/", paramData, false, getApplicationContext());
            return rawJSONData;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            if (s.equalsIgnoreCase("failed")){
                String message = "Failed to login. Is your username and password correct?";
                result.setText(message);
                return;
            }
            Log.d("LogIN", "Result: " + rawJSONData);

            try {
                jsonObject = new JSONObject(rawJSONData);
                if (jsonObject.has("token")){
                    saveToken(jsonObject.getString("token"));
                    Log.d("Login", "Saved to token: " + jsonObject.getString("token"));
                    clearScreen();
                    finish();
                    startActivity(new Intent(context, MOInjection.class));
                } else {
                    Log.d("Login", "Failed to get Auth Token");
                }
                } catch (JSONException e){
                e.printStackTrace();
            }
            }


        private void clearScreen(){
            final EditText username = (EditText) findViewById(R.id.username);
            final EditText password = (EditText) findViewById(R.id.password);
            username.setText("");
            password.setText("");
        }

    }


    private void saveToken(String token){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String authToken = "Token " + token;
        editor.putString("auth_token", authToken);
        editor.commit();
}



}
