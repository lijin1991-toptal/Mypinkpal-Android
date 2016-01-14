package com.brodev.socialapp.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.quickblox.chat.QBChatService;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Logout extends AsyncTask<String, Void, String> 
{
	private NetworkUntil network = new NetworkUntil();
	private User user;
	
	public Logout(Context context) {
		user = (User) context.getApplicationContext();
	}

	@Override
	protected String doInBackground(String... params) {
		
		if (isCancelled()) {
			return null;
		}
		
		// url request
		String URL = null;
		if (Config.CORE_URL == null) {
			URL = Config.makeUrl(user.getCoreUrl(), null, false);	
		} else {
			URL = Config.makeUrl(Config.CORE_URL, null, false);
		}
		
		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", params[0]));
		pairs.add(new BasicNameValuePair("method", "accountapi.logOut"));
		pairs.add(new BasicNameValuePair("devive", "0"));
		
		// request GET method to server
		String resultstring = network.makeHttpRequest(URL, "GET", pairs);
		
		Log.i("LOG OUT", resultstring);

        if (QBChatService.isInitialized())
            QBChatService.getInstance().destroy();
		
		return null;
	}
}
