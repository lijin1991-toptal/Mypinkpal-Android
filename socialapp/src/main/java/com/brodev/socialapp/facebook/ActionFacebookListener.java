package com.brodev.socialapp.facebook;

import com.facebook.FacebookRequestError;
import com.facebook.Response;

public interface ActionFacebookListener {

	/**
     * Callbacks
     */
    public void onStart();

    public void onSuccess(Response response);

    public void onError(FacebookRequestError error, Exception exception);
    
    public void onFinish();
	
}
