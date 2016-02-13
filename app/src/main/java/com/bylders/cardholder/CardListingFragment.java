package com.bylders.cardholder;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class CardListingFragment extends Fragment {

	private Bitmap logo;
	private EditText name, phone, email, website, title,
			company, text_long;
	private ImageView card_image;
	private ProgressBar progress;

	public CardListingFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_card_listing, container, false);
		name = (EditText) view.findViewById(R.id.text_name);
		phone = (EditText) view.findViewById(R.id.text_phone);
		email = (EditText) view.findViewById(R.id.text_email);
		website = (EditText) view.findViewById(R.id.text_website);
		title = (EditText) view.findViewById(R.id.text_title);
		company = (EditText) view.findViewById(R.id.text_company);
		text_long = (EditText) view.findViewById(R.id.text_long);
		card_image = (ImageView) view.findViewById(R.id.image_card);
		progress = (ProgressBar) view.findViewById(R.id.card_loading);
		((FloatingActionButton)view.findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				renderButtonClicked(v);
			}
		});
		((Button) view.findViewById(R.id.button_logo)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				uploadLogoOnClick(v);
			}
		});

		return view;
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK){
			Uri selectedImage = data.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};

			Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);

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
		progress.setVisibility(View.VISIBLE);
		card_image.setAlpha(0.3f);
		SendDataTask sendDataTask = new SendDataTask(){
			@Override
			protected void onPostExecute(String s) {
				Log.d("RenderFinished", "Received" + s);

				try {
					JSONObject json = new JSONObject(s);
					String image_url = json.getString("image_url");
					Picasso.with(getActivity())
							.load(image_url)
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
									Toast.makeText(getActivity(), "Unable to render card.", Toast.LENGTH_SHORT).show();
								}
							});
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.setContext(getActivity()).setBitmap(logo);
		sendDataTask.execute(name.getText().toString(), phone.getText().toString(),
				email.getText().toString(), website.getText().toString(),
				title.getText().toString(), company.getText().toString(),
				text_long.getText().toString());
	}

}
