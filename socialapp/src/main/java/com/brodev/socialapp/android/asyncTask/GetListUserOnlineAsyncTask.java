package com.brodev.socialapp.android.asyncTask;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.http.NetworkUntil;

import android.os.AsyncTask;

public class GetListUserOnlineAsyncTask extends AsyncTask<String, Void, String> {

	private NetworkUntil networkUntil = new NetworkUntil();
	
	@Override
	protected String doInBackground(String... params) {
		String result = null;
		try {
			String getOnlineListUrl = params[0] + "/" + params[1] + Config.CHAT_ONLINE_LIST;
			
			if (!params[0].startsWith("http://"));
				getOnlineListUrl = "http://" + getOnlineListUrl;
				
				result = networkUntil.makeHttpRequest(getOnlineListUrl, "GET", null);
			
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
