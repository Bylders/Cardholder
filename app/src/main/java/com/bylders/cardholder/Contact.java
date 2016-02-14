package com.bylders.cardholder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by darkryder on 13/2/16.
 */
public class Contact implements Serializable {
    public String name;
    public String pk;
    public String contact_image_url;
    public String mobile;
    public String email;
    public String website;
    public String address;
    public String json_string;
    public String qr_code;

    public static HashSet<String> hashSet = new HashSet<>();

    public Contact(String name, String pk, String contact_image_url, String mobile, String email, String website, String address, String qr_code, String json_string) {
        this.name = name;
        this.pk = pk;
        this.contact_image_url = contact_image_url;
        this.mobile = mobile;
        this.email = email;
        this.website = website;
        this.json_string = json_string;
        this.address = address;
        this.qr_code = qr_code;
    }

    public void save(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(this.pk, this.json_string).commit();
    }

    public static Contact getContactFromDb(String pk, Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString(pk, null);
        if(json == null) return null;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            String name = jsonObject.getString("name");
            String pk_ = jsonObject.getString("pkey");
            String image_url = jsonObject.getJSONObject("card_image").getString("url");
            String mobile = jsonObject.getString("mobile");
            String email = jsonObject.getString("display_email");
            String website = jsonObject.getString("website");
            String address = jsonObject.getString("address");
            String qr_code = jsonObject.getJSONObject("qr_code").getString("url");

            return new Contact(name, pk_, image_url, mobile, email, website, address, qr_code, json);
        } catch (JSONException e) {
            Log.d("SerialiseTask", "JSON EXCEPTION" + e.toString());
            return null;
        }
    }

    public static void commitHashSet(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove("_contacts").putStringSet("_contacts", hashSet).commit();
    }

    public static void readHashSet(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Contact.hashSet = (HashSet<String>)sharedPreferences.getStringSet("_contacts", new HashSet<String>());
    }
}