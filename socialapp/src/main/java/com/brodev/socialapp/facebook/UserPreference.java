package com.brodev.socialapp.facebook;

import android.content.Context;

public class UserPreference extends FacebookPreference {
	
    private static final String USER_FULLNAME = "facebook_user_fullname";
    private static final String USER_BIRTHDAY = "facebook_user_birthday";
    private static final String USER_ID = "facebook_user_id";
    private static final String USER_USERNAME = "facebook_user_username";
	
	public UserPreference(Context context) {
		super(context);
	}
	
	/**
	 * Function store basic info
	 * @param fullname
	 * @param birth
	 * @param userId
	 * @param username
	 */
	public void storeUser(String fullname, String birth, String userId, String username) {
        editor.putString(USER_FULLNAME, fullname);
        editor.putString(USER_BIRTHDAY, birth);
        editor.putString(USER_ID, userId);
        editor.putString(USER_USERNAME, username);
        editor.commit();
    }

    public String getFullName() {
        return pref.getString(USER_FULLNAME, null);
    }

    public String getBirthday() {
        return pref.getString(USER_BIRTHDAY, null);
    }

    public String getId() {
        return pref.getString(USER_ID, null);
    }

    public String getUserName() {
        return pref.getString(USER_USERNAME, null);
    }

}
