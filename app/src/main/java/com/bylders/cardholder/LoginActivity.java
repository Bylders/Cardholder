package com.bylders.cardholder;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";
    private static GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN =0;
    Bundle b = new Bundle();
    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        SignInButton btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);

        btnSignIn.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build()
        ;

    }

    protected void onStart() {
        Log.v(TAG, "Start");
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    protected void onDestroy(){
        Log.e(TAG, "Application Destroyed");
        super.onDestroy();
    }
    @Override
    public void onConnected(Bundle bundle) {
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
//        Intent intent = new Intent(LoginActivity.this, CardListing.class);

        // Get user's information
        getProfileInformation();
//        intent.putExtras(b);
//        startActivity(intent);
        mSignInClicked = false;
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        //updateUI(false);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed:" + result);

        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
        }
        else {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        //System.out.println("Hello");
        Log.v(TAG, "ActivityResult: " + requestCode);
        if ((requestCode == RC_SIGN_IN) && (responseCode == RESULT_OK)) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                // Signin button clicked
                signInWithGplus();
                break;
            /*case R.id.btn_sign_out:
                // Signout button clicked
                signOutFromGplus();
                break;*/
        }
    }

    private void signOutFromGplus() {
        //if(Holder.FLAG==1) {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }

    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {

            try {

                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {

                mGoogleApiClient.connect();
            }
        }
    }

    public void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                Log.e(TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);
//                Holder.USER_NAME = personName;
//                Holder.EMAIL_ID = email;
                b.putString("txtName", personName);
                b.putString("txtEmail", email);
                b.putString("txtURL", personPhotoUrl );

                SharedPreferences pref = getSharedPreferences("DATA", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("loggedin", true);
                editor.putString("userName", personName);
                editor.putString("userEmail", email);
                editor.commit();

                loginAtServer task = new loginAtServer(getApplicationContext());
                task.execute(email, personName);

            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class loginAtServer extends AsyncTask<String, Void, Boolean>
    {
        Context context;
        public loginAtServer(Context context) {this.context = context;}

        @Override
        protected Boolean doInBackground(String... strings) {
            String resp = null;
            try {
                Log.i("Extra", "Start trying to communicate with the server" + strings.toString());
                String API_URL = "http://steady-dagger-158651.nitrousapp.com:3000/api/v1/";
                URL url = new URL(API_URL + "emaillogin?email=" + strings[0] + "&name=" + strings[1].split(" ")[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                Log.i("Extra", "1" + url.toString());

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) throw new IOException("InputStream was null");

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) return null;

                resp = buffer.toString();
                Log.i("Extra", "2");
                JSONObject jsonResponse = new JSONObject(resp);

                String token = jsonResponse.getString("api_key");
                String pk = jsonResponse.getString("pk");

                Log.i("Extra", token + jsonResponse);
                Log.i("Extra", "3");

                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putString("api_key", token)
                        .putString("pk", pk)
                        .putBoolean("loggedin", true)
                        .commit();
                Log.d(TAG, "apikey" + token + " pk:" + pk);
                Intent mainActivity = new Intent(context,MainActivity.class);
                mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivity);
                finish();
                return true;
            } catch (IOException e)
            {
                if (resp != null) Log.d("PingTest", resp.toString());
                Log.d("PingTest", "NetworkError");
            } catch (JSONException e)
            {
                Log.d("PingTest JSON", e.toString());
            }
            return false;
        }
    }

}