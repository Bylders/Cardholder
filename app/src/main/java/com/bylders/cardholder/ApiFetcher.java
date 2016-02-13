package com.bylders.cardholder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by darkryder on 13/2/16.
 */
public class ApiFetcher {
    public static final String API_URL = "http://steady-dagger-158651.nitrousapp.com:3000/api/v1/";

    public static String getSignedResponse(String url_, String api, boolean POST, HashMap<String, String> params) throws IOException
    {
        URL url;
        url_ = url_ + "?api_key=" + api;
        if(!POST && params != null && params.size() != 0){
            StringBuilder queryString = new StringBuilder(url_);
            for(String i: params.keySet()){
                queryString.append("&" + i + "=" + params.get(i));
            }
            url_ = queryString.toString();
        }
        url = new URL(url_);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        if (POST){
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            if (params != null && params.size() != 0){
                for(String i: params.keySet()){
                    urlConnection.setRequestProperty(i, params.get(i));
                }
            }
            urlConnection.setRequestProperty("api_key", api);
        }
        if(!POST) urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();
        if (inputStream == null) throw new IOException("InputStream was null");

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        while ((line = reader.readLine()) != null) {
            buffer.append(line + "\n");
        }
        if (buffer.length() == 0) return null;

        Log.v("NetworkCall", url_ + ": " + buffer.toString());

        return buffer.toString();
    }
}

class FetchSelfTask extends AsyncTask<String, Void, Contact>
{
    Context context;
    public FetchSelfTask setContext(Context context){this.context = context; return this;}
    @Override
    protected Contact doInBackground(String... strings) {
        if (context == null) {
            Log.d("FetchSelfTask", "Context not given");
            return null;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String api_token = sharedPreferences.getString("api_key", null);
        if (api_token == null) {
            Log.d("FetchSelfTask", "Api key not given");
            return null;
        }
        String response;
        try {
            response = ApiFetcher.getSignedResponse(ApiFetcher.API_URL + "me", api_token,false, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            String name = jsonObject.getString("name");
            String pk = jsonObject.getString("pkey");
            String image_url = jsonObject.getString("card_image");
            String mobile = jsonObject.getString("mobile");
            String email = jsonObject.getString("display_email");
            String website = jsonObject.getString("website");

            Contact me = new Contact(name, pk, image_url, mobile, email, website);

            sharedPreferences.edit().putString("name", name).
                    putString("pk", pk).putString("image_url", image_url).
                    putString("mobile", mobile).putString("email", email).
                    putString("website", website).commit();

            return me;
        } catch (JSONException e) {
            Log.d("FetchSelfTask", "JSON EXCEPTION" + e.toString());
            return null;
        }
    }
}


class FetchUserTask extends AsyncTask<String, Void, Contact>
{
    Context context;
    public FetchUserTask setContext(Context context){this.context = context; return this;}
    @Override
    protected Contact doInBackground(String... strings) {
        if (context == null) {
            Log.d("FetchUserTask", "Context not given");
            return null;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String api_token = sharedPreferences.getString("api_key", null);
        if (api_token == null) {
            Log.d("FetchUserTask", "Api key not given");
            return null;
        }
        String response;
        try {
            String which = strings[0];
            response = ApiFetcher.getSignedResponse(ApiFetcher.API_URL + "user/" + which, api_token,false, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            String name = jsonObject.getString("name");
            String pk = jsonObject.getString("pkey");
            String image_url = jsonObject.getString("card_image");
            String mobile = jsonObject.getString("mobile");
            String email = jsonObject.getString("display_email");
            String website = jsonObject.getString("website");

            Contact which = new Contact(name, pk, image_url, mobile, email, website);

            return which;
        } catch (JSONException e) {
            Log.d("FetchUserTask", "JSON EXCEPTION" + e.toString());
            return null;
        }
    }
}
