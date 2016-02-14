package com.bylders.cardholder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ContactDetails extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_details);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Intent intent = getIntent();
		if(intent.getStringExtra("pk") != null){
			final Contact contact = Contact.getContactFromDb(intent.getStringExtra("pk"), this);
			if(contact != null){
				setTitle(contact.name);
				((TextView) findViewById(R.id.text)).setText(contact.address);
				ImageView imageView = ((ImageView) findViewById(R.id.image_card));
				Picasso.with(this).load(ApiFetcher.BASE_URL + contact.contact_image_url).into(imageView);

				FloatingActionButton call_fab = (FloatingActionButton) findViewById(R.id.call);
				call_fab.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.mobile));
						startActivity(intent);
					}
				});

				FloatingActionButton mail_fab = (FloatingActionButton) findViewById(R.id.email);
				mail_fab.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						Log.v("aT", "Sending email to " + contact.email);
						intent.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.email});
						intent.putExtra(Intent.EXTRA_SUBJECT, "");
						intent.putExtra(Intent.EXTRA_TEXT, "");
						startActivity(Intent.createChooser(intent, "Send Email"));
					}
				});

				FloatingActionButton website_fab = (FloatingActionButton) findViewById(R.id.website);
				website_fab.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String url = contact.website;
						if (!url.startsWith("http://") && !url.startsWith("https://"))
							url = "http://" + url;
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				});
			}
		}

	}

}
