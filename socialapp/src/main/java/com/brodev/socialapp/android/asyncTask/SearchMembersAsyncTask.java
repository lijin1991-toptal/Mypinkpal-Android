package com.brodev.socialapp.android.asyncTask;

import android.content.Context;
import android.os.AsyncTask;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class SearchMembersAsyncTask extends AsyncTask<String, Void, String> {

	private NetworkUntil networkUntil = new NetworkUntil();
	private User user;

	public SearchMembersAsyncTask(Context context) {
		user = (User) context.getApplicationContext();
	}
	
	@Override
	protected String doInBackground(String... params) {
		String resultstring;

		// url link
		String url = null;
		if (Config.CORE_URL == null) {
			url = Config.makeUrl(user.getCoreUrl(), null, false);	
		} else {
			url = Config.makeUrl(Config.CORE_URL, null, false);
		}

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", params[0]));
		//pairs.add(new BasicNameValuePair("method", "accountapi.searchFriend"));
        pairs.add(new BasicNameValuePair("method", "accountapi.getSearchFriend"));
	pairs.add(new BasicNameValuePair("user_id", params[1]));
        pairs.add(new BasicNameValuePair("find", params[2]));
		//pairs.add(new BasicNameValuePair("type", "user"));
		//pairs.add(new BasicNameValuePair("page", "1"));

		resultstring = networkUntil.makeHttpRequest(url, "GET", pairs);

		return resultstring;
	}
	
}
