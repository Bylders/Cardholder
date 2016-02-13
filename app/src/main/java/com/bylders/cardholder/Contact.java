package com.bylders.cardholder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import java.io.Serializable;

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

    public Contact(String name, String pk, String contact_image_url, String mobile, String email, String website) {
        this.name = name;
        this.pk = pk;
        this.contact_image_url = contact_image_url;
        this.mobile = mobile;
        this.email = email;
        this.website = website;
    }
}