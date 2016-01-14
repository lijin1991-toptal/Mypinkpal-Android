package com.brodev.socialapp.android.manager;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StickerManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// SharedPreferences mode
	int PRIVATE_MODE = 0;

	// SharedPreferences file name
	private static final String PREF_NAME = "socialapp_sticker";
	
	private static final String PREF_TIME = "socialapp_sticker_time";

	// Constructor
	public StickerManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Save sticker group
	 * @param context
	 * @param jsonArray
	 */
	public void saveSticker(Context context, String sticker, JSONArray jsonArray, long time) {
		try {
			editor.putString(sticker, jsonArray.toString());
			editor.putLong(sticker+"_"+PREF_TIME, time);
			editor.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Load sticker group
	 * @param context
	 * @return json array
	 * @throws JSONException
	 */
	public JSONArray loadSticker(Context context, String sticker) throws JSONException {
		return new JSONArray(pref.getString(sticker, "[]"));
	}
	
	public long loadTimeSticker(Context context, String sticker) throws Exception {
		long time = pref.getLong(sticker+"_"+PREF_TIME, 0); 
		return time;
	}
	
	/**
	 * Clear sticker group
	 */
	public void clearSticker(Context context, String sticker) {
		// Clearing all data from Shared Preferences
		editor.remove(sticker);
		editor.remove(sticker+"_"+PREF_TIME);
		editor.commit();
	}
}
