package com.bylders.cardholder;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ShareCard extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_card);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Contact me = Contact.getContactFromDb(PreferenceManager.getDefaultSharedPreferences(this).getString("pk", null), this);
		if(me != null){
			ImageView imageView = (ImageView)findViewById(R.id.share_image);
			Picasso.with(this).load(ApiFetcher.BASE_URL + me.qr_code).into(imageView);
		}
	}

	public void shareButtonClicked(View view) {

		String name = PreferenceManager.getDefaultSharedPreferences(this).getString("name", null);
		String pk = PreferenceManager.getDefaultSharedPreferences(this).getString("pk", null);

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "Hello, find my contact at " + "cardholder://qr/" + pk;
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, name);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}
}
