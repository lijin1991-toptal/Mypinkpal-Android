package com.brodev.socialapp.facebook;

import android.content.Context;

import com.facebook.Response;

public class FacebookResponse {
	
	ResponsePreference responsePreference;

    public FacebookResponse(Context context) {
        responsePreference = new ResponsePreference(context);
    }

    public void storeResponse(Response response) {    	
        responsePreference.storeResponse((String) response.getGraphObject().getProperty("email"), (String) response.getGraphObject().getProperty("gender"), response.getRequest().getSession().getApplicationId(), response.getRequest().getSession().getAccessToken());
    }

    public String getEmail() {
        return responsePreference.getEmail();
    }

    public String getGender() {
        return responsePreference.getGender();
    }

    public String getAppId() {
        return responsePreference.getAppId();
    }

    public String getAccessToken() {
        return responsePreference.getAccessToken();
    }
}
