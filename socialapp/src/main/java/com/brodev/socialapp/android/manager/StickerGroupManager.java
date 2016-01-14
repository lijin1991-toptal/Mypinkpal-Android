package com.brodev.socialapp.android.manager;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StickerGroupManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// SharedPreferences mode
	int PRIVATE_MODE = 0;

	// SharedPreferences file name
	private static final String PREF_NAME = "socialapp_sticker_group";

	// key for url
	private static final String STICKER_GROUP = "sticker_group";

	// Constructor
	public StickerGroupManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Save sticker group
	 * @param context
	 * @param jsonArray
	 */
	public void saveStickerGroup(Context context, JSONArray jsonArray) {
		try {
			editor.putString(STICKER_GROUP, jsonArray.toString());
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
	public JSONArray loadStickerGroup(Context context) throws JSONException {
		return new JSONArray(pref.getString(STICKER_GROUP, "[]"));
	}
	
	/**
	 * Clear sticker group
	 */
	public void clearStickerGroup(Context context) {
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();
	}

}
