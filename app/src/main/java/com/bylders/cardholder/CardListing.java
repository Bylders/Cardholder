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
import android.widget.EditText;

public class CardListing extends AppCompatActivity {

	private Bitmap logo;
	private EditText name, phone, email, website, title,
						company, text_long;

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
			}
		}
	}

	public void renderButtonClicked(View view) {
		// handle type of layout over here. Or VIEW_HIDE them in the onCreate method

		Log.d("RenderCalled", "Sending request to server");
		SendDataTask sendDataTask = new SendDataTask(){
			@Override
			protected void onPostExecute(String s) {
				Log.d("RenderFinished", "Received" + s);
			}
		};
		sendDataTask.execute(name.toString(), phone.toString(), email.toString(), website.toString(),
				title.toString(), company.toString(), text_long.toString());

	}
}
