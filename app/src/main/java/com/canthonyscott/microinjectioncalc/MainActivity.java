package com.canthonyscott.microinjectioncalc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    private TextView statusResult;
    private SharedPreferences prefs;
    private TextView loggedInAs;
    private String serverMessage;
    private String lastServerMessage;
    private GetNetworkResource getNetworkResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getNetworkResource = new GetNetworkResource(getApplicationContext(), "connection_check.php");

        // set version name on UI
        TextView versionNameMain = (TextView) findViewById(R.id.versionNameMain);
        versionNameMain.setText(BuildConfig.VERSION_NAME);

        // get instance of cookieManager for proper cookie sending
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        statusResult = (TextView) findViewById(R.id.statusResult);
        loggedInAs = (TextView) findViewById(R.id.loggedInAs);
        // See if the one-time popup has been observed
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // do some cleanup of unneeded resources from previous versions


        // load up the last saved server message, this will be used to compare to received server
        // messages to decide if it needs to be displayed or not
        lastServerMessage = prefs.getString("lastServerMessage", "none");


        //Check if useNetwork switch is on or off
        boolean networkSwitch = prefs.getBoolean("useNetworkDatabase", true);
        if (networkSwitch) {
            // get server address to use
            CheckConnectionToDB checkConnectionToDB = new CheckConnectionToDB();
            checkConnectionToDB.execute();
        } else {
            // change shared prefs to false
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("connectedToNetwork", "0");
            editor.commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settingsLogin) {
            startActivity(new Intent(this, Login.class));
            return true;
        }
        if (id == R.id.settingsLogout) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("cookie", "a:1");
            editor.commit();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // methods for handling MainActivity Button clicks
    public void openMOActivity(View view) {
        Intent intent = new Intent(this, MOInjection.class);
        startActivity(intent);
    }

    public void openRNAActivity(View view) {
        Intent intent = new Intent(this, RNAInjection.class);
        startActivity(intent);
    }

    public void savedOligos(View view) {
        Intent intent = new Intent(this, RemoveMO.class);
        startActivity(intent);
    }

    public void calcRNAMix(View view) {
        startActivity(new Intent(this, MakeAnInjectionMix.class));
    }


    public class displayServerMessage extends DialogFragment {
        // this class displays a one-time dialog message to alert users about the heavy code updates
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Server Message");
            builder.setMessage(serverMessage);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing, dismiss the message
                }
            });
            builder.setNeutralButton("Don't show again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // save this message to allow user to opt-out of seeing it again
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("lastServerMessage", serverMessage);
                    editor.commit();
                }
            });
            return builder.create();
        }

    }

    // method to display dialog to user
    private void showDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment newFragment = new displayServerMessage();
        newFragment.show(fm, "Tag");
    }

    private class CheckConnectionToDB extends AsyncTask<String, Void, String> {

        private static final String LOG_TAG = "checkConnectionToDB";

        final String charset = "UTF-8";
        URL urlObj;
//        HttpURLConnection conn;
        HttpsURLConnection conn;
        StringBuilder result;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            statusResult.setText("Checking....");
        }

        @Override
        protected String doInBackground(String... params) {
            return sendHttpRequest(getNetworkResource.getUrl());
        }

        @Override
        protected void onPostExecute(String s) {
            JSONObject jsonObject;
            super.onPostExecute(s);
            try {
                jsonObject = new JSONObject(s);
                serverMessage = jsonObject.getString("serverMessage");
                if (serverMessage.equals("You are connected to a testing DB")){
                    // TODO: 4/14/2016 change color of action bars
                    ActionBar actionbar = getSupportActionBar();
                    actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));

                }
                if (jsonObject.getString("status").equalsIgnoreCase("1")) {
                    statusResult.setText("Connected to Network!");
                    statusResult.setTextColor(Color.parseColor("#29a329"));
                    loggedInAs.setText(jsonObject.getString("username"));
                    loggedInAs.setTextColor(Color.parseColor("#0000b3"));
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("connectedToNetwork", "1");
                    editor.commit();
                    /* Only try to show the server message if the server says it has one to display
                     * If the user has said don't show again, dont show it
                     * However, if the message is new, show it to the user
                     */
                    if (jsonObject.getString("serverMessageStatus").equals("1") && (!serverMessage.equals(lastServerMessage))) {
                        showDialog();
                    }
                }

            } catch (JSONException e) {
//                e.printStackTrace();
                statusResult.setText("Failed to Connect");
                statusResult.setTextColor(Color.parseColor("#b30000"));
                loggedInAs.setText("Disconnected");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("connectedToNetwork", "0");
                editor.commit();
            }


        }

        private String sendHttpRequest(String url) {

            DataOutputStream wr;

            // put unique ID into hashmap to send to server
            String uniqueID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            HashMap<String,String> paramData = new HashMap<>();
            paramData.put("uniqueID", uniqueID);

            // create POST message to send
            StringBuilder sbParams = new StringBuilder();
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
            String paramString = sbParams.toString();

            try {
                conn = getNetworkResource.getSSLUrlConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Charset", charset);
                conn.setRequestProperty("Cookie", getNetworkResource.getCookieToSend());
                conn.setReadTimeout(3000);
                conn.setConnectTimeout(5000);
                conn.connect();
                // send POST output stream
                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(paramString);
                wr.flush();
                wr.close();

            } catch (Exception e) {
                e.printStackTrace();
                return "failed";
            }
            // receive the server response
            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                Log.d(LOG_TAG, "server resposne: " + result.toString());

            } catch (IOException e) {
                e.printStackTrace();
                return "failed";
            }
            conn.disconnect();
            return result.toString();
        }
    }
}
