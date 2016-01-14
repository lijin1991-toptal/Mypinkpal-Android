package com.brodev.socialapp.android.asyncTask;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.http.NetworkUntil;

import android.os.AsyncTask;

public class GetListUnreadMessageTask extends AsyncTask<String, Void, String> {

	private NetworkUntil networkUntil = new NetworkUntil();
	
	@Override
	protected String doInBackground(String... params) {
		String result = null;
		if (isCancelled()) {
			return null;
		}
		
		try {
			String getUnReadListUrl = params[0] + "/" + params[1] + Config.CHAT_UNREAD;
			
			if (!params[0].startsWith("http://"));
				getUnReadListUrl = "http://" + getUnReadListUrl;
				
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			pairs.add(new BasicNameValuePair("user_id", params[2]));
				
			result = networkUntil.makeHttpRequest(getUnReadListUrl, "GET", pairs);	
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

}
