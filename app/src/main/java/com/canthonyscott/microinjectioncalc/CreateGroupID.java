package com.canthonyscott.microinjectioncalc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CreateGroupID extends AppCompatActivity {

    JSONObject jsonObject = null;
    String rawJsonData;
    HashMap<String, String> paramData = new HashMap<>();

    EditText username;
    EditText password;
    EditText confirmPassword;
    TextView resultUI;

    String user;
    String pass;
    String confirm;

    private GetNetworkResource getNetworkResource;

    // json node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_id);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getNetworkResource = new GetNetworkResource(getApplicationContext(), "ADD_USER.php");


        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);
        Button addUserToDB = (Button) findViewById(R.id.addUserBtn);
        resultUI = (TextView) findViewById(R.id.resultUI);

        addUserToDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();
                confirm = confirmPassword.getText().toString();

                if (user.equals("") || pass.equals("") || confirm.equals("")) {
                    Toast.makeText(CreateGroupID.this, "Please Enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                AddUserToDB dbRequest = new AddUserToDB();
                dbRequest.execute();
            }
        });
    }

    private class AddUserToDB extends AsyncTask<String, String, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CreateGroupID.this);
            pDialog.setMessage("Adding User...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            paramData.put("username", user);
            paramData.put("password", pass);
            paramData.put("confirm", confirm);

            JSONParser jsonParser = new JSONParser(getNetworkResource);
            rawJsonData = jsonParser.makeHttpRequest(getNetworkResource.getUrl(), "POST", paramData);
            Log.d("AddUSer", "Raw JSON data: " + rawJsonData);

            return rawJsonData;
        }

        @Override
        protected void onPostExecute(String s) {
            String status = null;
            super.onPostExecute(s);
            pDialog.dismiss();
            try {
                jsonObject = new JSONObject(s);
                Log.d("AddUser", "JSONObject to String: " + jsonObject.toString());
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("AddUser", "Failed to convert into JSON Object");
            }
            showResultToUI();
            clearScreen();
            // finish this activity and close the screen IF status is 1, else leave it
            if (status.equals("1")){
                finish();
                startActivity(new Intent(getApplicationContext(), Login.class));
            }

        }
        private void showResultToUI() {
            String messageString = null;
            try {
                messageString = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // send to resultUI
            resultUI.setText(messageString);
        }

        private void clearScreen() {
            final String CLEAR = "";
            username.setText(CLEAR);
            password.setText(CLEAR);
            confirmPassword.setText(CLEAR);
        }
    }
}