package com.brodev.socialapp.android;

import java.util.HashMap;

import com.brodev.socialapp.view.LoginActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "Social_App";

	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";

	// password (make variable public to access from outside)
	public static final String KEY_PASS = "password";

	// Email address (make variable public to access from outside)
	public static final String KEY_EMAIL = "email";

    // Quickblox username and password
    // bronislaw
    public static final String KEY_USERNAME = "username";
    public static final String KEY_QBPASS = "qbpassword";
    public static final String KEY_QBID = "qbid";

	// Constructor
	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	/**
	 * Create login session
	 * */
	public void createLoginSession(String pass, String email, String username, String qbPass, String qbId) {
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN, true);

		// Storing password in pref
		editor.putString(KEY_PASS, pass);

		// Storing email in pref
		editor.putString(KEY_EMAIL, email);

        // Storing QBID in pref
        editor.putString(KEY_USERNAME, username);

        // Storing QBPASSWORD in pref
        editor.putString(KEY_QBPASS, qbPass);

        // Storing QBID in pref
        editor.putString(KEY_QBID, qbId);

		// commit changes
		editor.commit();
	}

	/**
	 * Check login method wil check user login status If false it will redirect
	 * user to login page Else won't do anything
	 * */
	public boolean checkLogin() {
		// Check login status
		if (!this.isLoggedIn()) {
			// user is not logged
			return false;
		}
		return true;
	}

	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		// user name
		user.put(KEY_PASS, pref.getString(KEY_PASS, ""));

		// user email id
		user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));

        // user username
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, ""));

        // user Quickblox Password
        user.put(KEY_QBPASS, pref.getString(KEY_QBPASS, ""));

        // user Quickblox ID
        user.put(KEY_QBID, pref.getString(KEY_QBID, ""));

		// return user
		return user;
	}

	/**
	 * Clear session details
	 * */
	public void logoutUser(Activity activity) {
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();
		Log.i("clear Session", "done");
		
		// After logout redirect user to Loing Activity
		Intent i = new Intent(activity, LoginActivity.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// Staring Login Activity
		activity.startActivity(i);
		activity.finish();
	}

	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn() {
		return pref.getBoolean(IS_LOGIN, false);
	}
}
