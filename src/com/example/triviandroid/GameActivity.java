package com.example.triviandroid;

// Imports
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
// - END OF Google API



// Misc.
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
// - END OF Imports -

public class GameActivity extends Activity 
	implements GoogleApiClient.ConnectionCallbacks,
				GoogleApiClient.OnConnectionFailedListener, 
				OnClickListener {

	// Google API Client Constants & Variables
	private static final int RC_SIGN_IN = 0; // Request code used to invoke sign in user interactions
	
	private GoogleApiClient mGoogleApiClient; // Client used to invoke sign-in user interactions
	private boolean mIntentInProgress; // A flag indicating that a PendingIntent is in progress
	private boolean mSignInClicked; // A flag tracking whether sign-in button has been clicked
	private ConnectionResult mConnectionResult; // Stores the connection result from onConnectionFailed callbacks	
	// - END OF Google API Client Constants & Variables =
	
	// Misc. Constants
	
	//  - END OF Misc. Constants -
	
	// Misc. Variables
	private View signInView = findViewById(R.id.buttonSignIn);
	private View signOutView = findViewById(R.id.buttonSignOut);
	
	private SignInButton signInButton = (SignInButton) signInView;
	private Button signOutButton = (Button) signOutView;
	// - END OF Misc. Variables -
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_main);
		
		// register Google Plus Sign-In button listener
		signInButton.setOnClickListener(this);
		
		// register Sign-Out button listener
		signOutButton.setOnClickListener(this);
		
		// Google API Client Builder
		mGoogleApiClient = new GoogleApiClient.Builder(this)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.addApi(Plus.API)
			.addScope(Plus.SCOPE_PLUS_LOGIN)
			.build();
		// - END OF Google API Client Builder -
		
	} // - END OF OnCreate() Method -
		
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}
		
	protected void onStop() {
		super.onStop();
			
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}
	
	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
			    startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
			    RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				// The intent was canceled before it was sent.  Return to the default
			    // state and attempt to connect to get an updated ConnectionResult.
			    mIntentInProgress = false;
			    mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
		    // Store the ConnectionResult so that we can use it later when the user clicks
		    // 'sign-in'.
		    mConnectionResult = result;

		    if (mSignInClicked) {
		    	// The user has already clicked 'sign-in' so we attempt to resolve all
		    	// errors until the user is signed in, or they cancel.
		    	resolveSignInError();
		    }
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mSignInClicked = false;
		Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();	
	}
	
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}
			
			mIntentInProgress = false;

		    if (!mGoogleApiClient.isConnecting()) {
		      mGoogleApiClient.connect();
		    }
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.buttonSignIn
		  && !mGoogleApiClient.isConnecting()) {
			mSignInClicked = true;
			resolveSignInError();
			signOutButton.setVisibility(View.VISIBLE);
			signInButton.setVisibility(View.GONE);
		} else if (v.getId() == R.id.buttonSignOut) {
			if (mGoogleApiClient.isConnected()) {
				Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
				mGoogleApiClient.disconnect();
				mGoogleApiClient.connect();
				signOutButton.setVisibility(View.INVISIBLE);
				signInButton.setVisibility(View.VISIBLE);
			}
		}
	}
		
} // - END OF Class GameActivity -
