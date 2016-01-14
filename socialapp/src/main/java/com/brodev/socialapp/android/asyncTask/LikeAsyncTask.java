package com.brodev.socialapp.android.asyncTask;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class LikeAsyncTask extends AsyncTask<String, Void, String> {

	private NetworkUntil networkUntil = new NetworkUntil();
	private User user;
	
	public LikeAsyncTask(Context context) {
		user = (User) context.getApplicationContext();
	}
	
	@Override
	protected String doInBackground(String... params) {
		try {
			String likerequest;
			// Use BasicNameValuePair to create GET data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("token", params[0]));
			
			if (("like").equals(params[4])) {
				pairs.add(new BasicNameValuePair("method", "accountapi.like"));
			} else {
				pairs.add(new BasicNameValuePair("method", "accountapi.unlike"));
			}
			
			if(params[2] != null){
				pairs.add(new BasicNameValuePair("type", "" + params[2]));
				pairs.add(new BasicNameValuePair("item_id", "" + params[1]));
			} else {
				pairs.add(new BasicNameValuePair("feed_id", "" + params[3]));
			}
			
			// url request
			String URL = null;
			if (Config.CORE_URL == null) {
				URL = Config.makeUrl(user.getCoreUrl(), null, false);	
			} else {
				URL = Config.makeUrl(Config.CORE_URL, null, false);
			}

			// request GET method to server			
			likerequest = networkUntil.makeHttpRequest(URL, "GET", pairs);
			Log.i("like request", likerequest);
		} catch(Exception ex) {
			//Log.i(DEBUG_TAG, ex.getMessage());
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

}
