package com.brodev.socialapp.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Html;
import android.util.Log;

public class PhraseManager {

	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "Phrase_Social_App";
	
	//
	private static final String KEY_JSON = "json";
	
	// Constructor
	public PhraseManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	/**
	 * Save JSONObject
	 * @param context
	 * @param object
	 */
	public void saveJSONObject(Context context, JSONObject object) {
		editor.remove(KEY_JSON);
		Log.i("PHRASE", object.toString());
		editor.putString(KEY_JSON, object.toString());
		
		editor.commit();
	}
	
	/**
	 * Load JSON Object
	 * @param context
	 * @param key
	 * @return JSONObject
	 * @throws JSONException
	 */
	public JSONObject loadJSONObject(Context context) throws JSONException {
		return new JSONObject(pref.getString(KEY_JSON, "{}"));
	}
	
	/**
	 * Get String from Resource
	 * @param context
	 * @param name
	 * @return key
	 */
	public int getStringResource(Context context, String name) {
		int resId = context.getResources().getIdentifier(name, "string", context.getPackageName());
		return resId;
	}
	
	/**
	 * Get phrase 
	 * @param context
	 * @param key
	 * @return phrase
	 */
	public String getPhrase(Context context, String key) {
		
		String phrase = null;
		
		try {
			//load JSONObject from SharedPreferences
			JSONObject jsonObj = loadJSONObject(context);
			
			if (jsonObj.has(key)) {
				phrase = Html.fromHtml(jsonObj.getString(key)).toString();
			}
			
		} catch (Exception ex) {
			return key;
		}
		
		return phrase;
	}
	
}
