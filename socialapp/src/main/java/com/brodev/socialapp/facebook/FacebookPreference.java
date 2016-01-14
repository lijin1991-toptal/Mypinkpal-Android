package com.brodev.socialapp.facebook;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class FacebookPreference extends FacebookConnectActivity {
	
	protected final SharedPreferences pref;
    protected final SharedPreferences.Editor editor;

    private static final String FACEBOOK_SHARED = "facebook_preference";

    public FacebookPreference(Context context) {
        pref = context.getSharedPreferences(FACEBOOK_SHARED, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    public void logoutFacebookUser(Activity activity) {
    	editor.clear();
		editor.commit();
		Log.i("clear Facebook Session", "done");
		disconnect();
    }
    
}
