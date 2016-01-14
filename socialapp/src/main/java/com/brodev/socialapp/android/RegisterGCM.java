package com.brodev.socialapp.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.content.Context;
import android.os.AsyncTask;

public class RegisterGCM extends AsyncTask<String, Void, String> {

	private NetworkUntil network = new NetworkUntil();
	private User user;
	
	public RegisterGCM(Context context) {
		user = (User) context.getApplicationContext();
	}

	@Override
	protected String doInBackground(String... params) {

		String URL = null;
		if (Config.CORE_URL == null) {
			URL = Config.makeUrl(user.getCoreUrl(), "registergcm", true) + "&token=" + params[0];
		} else {
			URL = Config.makeUrl(Config.CORE_URL, "registergcm", true) + "&token=" + params[0];
		}

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();

		pairs.add(new BasicNameValuePair("email", params[1]));
		pairs.add(new BasicNameValuePair("userId", params[2]));
		pairs.add(new BasicNameValuePair("regId", params[3]));

		String result = network.makeHttpRequest(URL, "POST", pairs);

		return result;
	}

}
