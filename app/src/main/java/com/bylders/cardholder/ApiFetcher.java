package com.bylders.cardholder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
			String address = jsonObject.getString("address");

            Contact me = new Contact(name, pk, image_url, mobile, email, website, address, response);

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
			String address = jsonObject.getString("address");

			Contact which = new Contact(name, pk, image_url, mobile, email, website, address, response);
			which.save(context);
			return  which;
        } catch (JSONException e) {
            Log.d("FetchUserTask", "JSON EXCEPTION" + e.toString());
            return null;
        }
    }
}

class SendDataTask extends AsyncTask<String, Void, String>
{
	private Context context;
	private Bitmap bitmap;
	public SendDataTask setContext(Context context){this.context = context; return this;}
	public SendDataTask setBitmap(Bitmap bitmap){this.bitmap= bitmap; return this;}
	private final String TAG = "SendDataTask";

	@Override
	protected String doInBackground(String... params) {
		if(context == null){
			Log.d(TAG, "context not set");
			return null;
		}
		String api_token = PreferenceManager.getDefaultSharedPreferences(context).getString("api_key", null);
		if (api_token == null){
			Log.d(TAG, "Api key not found");
			return null;
		}

		OkHttpClient client = new OkHttpClient();
		String url = ApiFetcher.API_URL + "set?api_key=" + api_token;
		MultipartBody.Builder builder = new MultipartBody.Builder()
				.setType(MultipartBody.FORM);

		if(bitmap == null){
			Log.d(TAG, "logo bitmap isn't set");
		} else {
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, bao);
			String encodedImage = Base64.encodeToString(bao.toByteArray(), Base64.DEFAULT);
			builder.addFormDataPart("image", encodedImage);//"logo.png", RequestBody.create(MediaType.parse("image/png"), data));
		}

		// assume if layout doesn't need a field, it'll send null for it. And "" for empty string.
		String[] FIELD_NAMES = {"name", "mobile", "email", "website", "title", "company", "address"};
		for(int i = 0; i < FIELD_NAMES.length; i++)
		{
			if (params[i] != null){
				builder.addFormDataPart(FIELD_NAMES[i], params[i]);
			}
		}

		Request request = new Request.Builder().url(url).post(builder.build()).build();

		try {
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
			return  response.body().string();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}
}