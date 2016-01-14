package com.brodev.socialapp.facebook;

import com.facebook.FacebookRequestError;
import com.facebook.Response;

public class ActionListener implements ActionFacebookListener {

	@Override
	public void onStart() { }

	@Override
	public void onSuccess(Response response) { }

	@Override
	public void onFinish() { }

	@Override
	public void onError(FacebookRequestError error, Exception exception) { }

}
