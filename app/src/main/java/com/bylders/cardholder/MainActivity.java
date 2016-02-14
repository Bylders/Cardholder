package com.bylders.cardholder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private ListView mListView;
	private ArrayAdapter<Contact> mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mListView = (ListView) findViewById(R.id.list_view);


		String w = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png";

//		FetchSelfTask fetchSelfTask = new FetchSelfTask(){
//			@Override
//			protected void onPostExecute(Contact contact) {
//				super.onPostExecute(contact);
//				if (contact == null)
//				{
//					Log.v("TEST", "Fetched NULL self");
//					return;
//				}
//				Log.v("TEST", "Fetched self" + contact.toString());
//				contact.save(context);
//			}
//		}.setContext(this);
//		fetchSelfTask.execute();
		Contact me = Contact.getContactFromDb(PreferenceManager.getDefaultSharedPreferences(this).getString("pk", null), this);

		Contact[] myDataset = {new Contact(w, w, w, w, w,w,w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w),
				new Contact(w, w, w, w, w,w, w, w)}; // TODO
		final ArrayList<Contact> actual = new ArrayList<>();
//		for(Contact c: myDataset) actual.add(c);
		for(int i = 0; i < 10; i++) actual.add(me);
		//actual.get(0).save(this);
		mAdapter = new ContactListAdapter(this, R.layout.contact_card ,actual);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), ContactDetails.class);
				Contact which = actual.get(position);
				intent.putExtra("pk", which.pk);
				startActivity(intent);
			}
		});
		

	}

}


class ContactListAdapter extends ArrayAdapter<Contact> {

	private Context context;

	public ContactListAdapter(Context context, int resource) {
		super(context, resource);
		this.context = context;
	}

	public ContactListAdapter(Context context, int resource, List<Contact> objects) {
		super(context, resource, objects);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;

		Contact who = getItem(position);
		if (view == null){
			LayoutInflater layoutInflater = LayoutInflater.from(context);
			view = layoutInflater.inflate(R.layout.contact_card, null);
			ImageView image = (ImageView) view.findViewById(R.id.image_card);
			if(image != null)
			{
				Picasso.with(context).load(who.contact_image_url).into(image);
			}
		}

		return view;
	}
}