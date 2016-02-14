package com.bylders.cardholder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class CardListing extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

	private Bitmap logo;
	private EditText name, phone, email, website, title,
						company, text_long;
	private ImageView card_image;
	private ProgressBar progress;
	private Spinner spinner;
	Contact me;
	private int template_id = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_listing);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		test();

		name = (EditText) findViewById(R.id.text_name);
		phone = (EditText) findViewById(R.id.text_phone);
		email = (EditText) findViewById(R.id.text_email);
		website = (EditText) findViewById(R.id.text_website);
		title = (EditText) findViewById(R.id.text_title);
		company = (EditText) findViewById(R.id.text_company);
		text_long = (EditText) findViewById(R.id.text_long);
		card_image = (ImageView) findViewById(R.id.image_card);
		progress = (ProgressBar) findViewById(R.id.card_loading);
		spinner = (Spinner) findViewById(R.id.spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.layouts_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		me = Contact.getContactFromDb(PreferenceManager.getDefaultSharedPreferences(this).getString("pk", null), this);

		if(me != null){
			Picasso.with(this).load(ApiFetcher.BASE_URL + me.contact_image_url).into(card_image);
			name.setText(me.name);
			phone.setText(me.mobile);
			email.setText(me.email);
			website.setText(me.website);
			text_long.setText(me.address);
		}

		setTitle();
	}

	private void setTitle()
	{
		String name = PreferenceManager.getDefaultSharedPreferences(this).getString("name", null);
		if (name != null) setTitle(name);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_card_listing, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void test()
	{
		FetchSelfTask fetchSelfTask = new FetchSelfTask(){
			@Override
			protected void onPostExecute(Contact contact) {
				super.onPostExecute(contact);
				if (contact == null)
				{
					Log.v("TEST", "Fetched NULL self");
					return;
				}
				Log.v("TEST", "Fetched self" + contact.toString());
				contact.save(context);
			}
		}.setContext(this);
		fetchSelfTask.execute();


		FetchUserTask fetchUserTask = new FetchUserTask(){
			@Override
			protected void onPostExecute(Contact contact) {
				super.onPostExecute(contact);
				if(contact == null)
				{
					Log.v("TEST", "Fetched NULL user");
					return;
				}
				Log.v("TEST", "Fetched user" + contact.toString());
			}
		}.setContext(this);
		fetchUserTask.execute(PreferenceManager.getDefaultSharedPreferences(this).getString("pk", null));
	}

	private static final int PICK_IMAGE = 157;
	public void uploadLogoOnClick(View view) {
		Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
		getIntent.setType("image/*");

		Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		pickIntent.setType("image/*");

		Intent chooserIntent = Intent.createChooser(getIntent, "Select Image for logo");
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

		startActivityForResult(chooserIntent, PICK_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
			Uri selectedImage = data.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				logo = BitmapFactory.decodeFile(filePath);
				if(logo!=null){
					((Button) findViewById(R.id.button_logo)).setText(filePath);
				}
			}
		}
	}

	public void renderButtonClicked(View view) {
		// handle type of layout over here. Or VIEW_HIDE them in the onCreate method

		Log.d("RenderCalled", "Sending request to server");
		progress.setVisibility(View.VISIBLE);
		card_image.setAlpha(0.3f);
		SendDataTask sendDataTask = new SendDataTask(){
			@Override
			protected void onPostExecute(String s) {
				Log.d("RenderFinished", "Received" + s);

				try {
					JSONObject json = new JSONObject(s);
					String image_url = json.getJSONObject("card_image").getString("url");


					if(me!=null && image_url != null){
						me.contact_image_url = image_url;
//						me.contact_image_url = ApiFetcher.API_URL.substring(0, ApiFetcher.API_URL.length() -7) + image_url;
						me.save(getApplicationContext());
					}

					Picasso.with(getApplicationContext())
							.load(ApiFetcher.BASE_URL + me.contact_image_url)
							.into(card_image, new Callback() {
								@Override
								public void onSuccess() {
									card_image.setAlpha(1.0f);
									progress.setVisibility(View.GONE);
								}

								@Override
								public void onError() {
									card_image.setAlpha(1.0f);
									progress.setVisibility(View.GONE);
									Toast.makeText(CardListing.this, "Unable to render card.", Toast.LENGTH_SHORT).show();
								}
							});
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.setContext(this).setBitmap(logo);
		sendDataTask.execute(name.getText().toString(), phone.getText().toString(),
				email.getText().toString(), website.getText().toString(),
				title.getText().toString(), company.getText().toString(),
				text_long.getText().toString(), Integer.toString(template_id));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		template_id = position + 1;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}
}
