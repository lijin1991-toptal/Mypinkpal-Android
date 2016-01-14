package com.brodev.socialapp.facebook;

import android.content.Context;

import com.facebook.model.GraphUser;

public class FacebookUser {
	UserPreference userPreference;
	
	

    public FacebookUser(Context context) {
        userPreference = new UserPreference(context);
    }

    public void storeUser(GraphUser user) {
        userPreference.storeUser(user.getFirstName() + " " + user.getLastName(), user.getBirthday(), user.getId(), user.getUsername());
    }

    
    public String getFullName() {
        return userPreference.getFullName();
    }

    public String getBirthday() {
        return userPreference.getBirthday();
    }

    public String getId() {
        return userPreference.getId();
    }

    public String getUserName() {
        return userPreference.getUserName();
    }
}
