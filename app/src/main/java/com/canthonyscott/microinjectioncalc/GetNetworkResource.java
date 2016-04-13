package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Anthony on 2/2/2016.
 * Will get the saved cookie from the Sharedprefs if it exists
 * includes a method to return the cookie as a string to send out with SetRequestProperty("Cookie",...)
 */
public class GetNetworkResource {

    private String cookie;
    private String cookieToSend;
    private final String cookiePart1 = "rememberMe=";

    private String url;

    private Context context;

//    public GetNetworkResource(Context context){
//        this.context = context;
//        // get saved cookie from sharedprefs, if it exists
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        cookie = prefs.getString("cookie","a:1");
//        // append cookie to send
//        cookieToSend = cookiePart1 + cookie;
//        Log.d("GetSavedLoginCookie", cookieToSend);
//    }

    // this method will get the correct URL to connect to using provided php file in the constructor
    // this saves refactoring the shit out of the code when needing to change the server address
    public GetNetworkResource(Context context, String phpFile){
        this.context = context;
        // get saved cookie from sharedprefs, if it exists
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        cookie = prefs.getString("cookie","a:1");
        // append cookie to send
        cookieToSend = cookiePart1 + cookie;
        Log.d("GetSavedLoginCookie", cookieToSend);

        String serverAddress = prefs.getString("serverAddressDomain", "canthonyscott.tk:1106");
        url = "https://" + serverAddress + "/" + phpFile;
        Log.d("GetNetworkResource", "url: " + url);
    }



    // return the cookie to send
    public String getCookieToSend() {
        return cookieToSend;
    }

    public String getUrl(){
        return url;
    }

    // this method will hopefully return an SSLContext that trusts my self-signed cert
    public HttpsURLConnection getSSLUrlConnection () throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // Load in CRT file from input stream
        // crt file was generated on my home-brew apache2 webserver
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = new BufferedInputStream(context.getResources().getAssets().open("cert_copy.crt"));
        Certificate ca = null;
        try {
            ca = cf.generateCertificate(caInput);
            Log.d("GetNetworkResource", "ca=" + ((X509Certificate) ca).getSubjectDN());
        } catch (CertificateException e) {
            e.printStackTrace();
        } finally {
            caInput.close();
        }

        // create keystore containing CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null,null);
        keyStore.setCertificateEntry("ca", ca);

        //create trustmanager that trusts the CA in our keystore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // create an SSLContext that uses this TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        URL url = new URL(getUrl());
        HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
        urlConnection.setSSLSocketFactory(context.getSocketFactory());

        return urlConnection;
    }
}
