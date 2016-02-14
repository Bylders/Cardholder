package com.bylders.cardholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private ListView mListView;
	private ArrayAdapter<String> mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	@Override
	protected void onResume() {
		super.onResume();
//		recreate();
	}

	public void refresh()
	{
		Contact.readHashSet(getApplicationContext());
		final ArrayList<String> actual = new ArrayList<>(Contact.hashSet);
		mAdapter = new ContactListAdapter(this, R.layout.contact_card ,actual);
		mAdapter.notifyDataSetChanged();
		mListView.setAdapter(mAdapter);


		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), ContactDetails.class);
				Contact which = Contact.getContactFromDb(actual.get(position), getApplicationContext());
				intent.putExtra("pk", which.pk);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle("CardHolder");

		boolean logged_in = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("loggedin", false);
		if(!logged_in){
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		}

		mListView = (ListView) findViewById(R.id.list_view);
		Contact.readHashSet(this);

		Intent intent = getIntent();
		String action = intent.getAction();
		Uri data = intent.getData();
		if(data!=null) {
			String[] parsed = data.toString().split("/");
			if (parsed.length != 0){
				String hash = parsed[parsed.length - 1];
				ConnectTask connectTask = new ConnectTask(){
					@Override
					protected void onPostExecute(Contact contact) {
						if(contact != null)
						{
							Toast.makeText(MainActivity.this, "Added " + contact.name, Toast.LENGTH_SHORT).show();
							refresh();
						}
					}
				}.setContext(this);
				connectTask.execute(hash);
			}
		}

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
		Contact me = Contact.getContactFromDb(PreferenceManager.getDefaultSharedPreferences(this).getString("pk", null), this);

		refresh();


		((com.github.clans.fab.FloatingActionButton)findViewById(R.id.edit_fab)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplication(), CardListing.class));
			}
		});

		((com.github.clans.fab.FloatingActionButton)findViewById(R.id.share_fab)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplication(), ShareCard.class));
			}
		});

		((com.github.clans.fab.FloatingActionButton)findViewById(R.id.refresh_fab)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ResyncTask resyncTask = new ResyncTask(){
					@Override
					protected void onPostExecute(Void aVoid) {
						super.onPostExecute(aVoid);
						refresh();
						Toast.makeText(MainActivity.this, "Refreshed data.", Toast.LENGTH_SHORT).show();
					}
				}.setContext(getApplicationContext());
				resyncTask.execute();
			}
		});

	}

}


class ContactListAdapter extends ArrayAdapter<String> {

	private Context context;

	public ContactListAdapter(Context context, int resource) {
		super(context, resource);
		this.context = context;
	}

	public ContactListAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;

		String pk = getItem(position);

		Contact who = Contact.getContactFromDb(pk, context);
		if (view == null){
			LayoutInflater layoutInflater = LayoutInflater.from(context);
			view = layoutInflater.inflate(R.layout.contact_card, null);
			ImageView image = (ImageView) view.findViewById(R.id.image_card);
			if(image != null && who != null)
			{
				Picasso.with(context).load(ApiFetcher.BASE_URL + who.contact_image_url).into(image);
			}
		}

		return view;
	}
}