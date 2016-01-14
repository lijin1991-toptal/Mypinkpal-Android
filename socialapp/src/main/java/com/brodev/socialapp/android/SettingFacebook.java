package com.brodev.socialapp.android;

import org.json.JSONException;
import org.json.JSONObject;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.facebook.SettingFacebookApp;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import android.content.Context;
import android.os.AsyncTask;

public class SettingFacebook extends AsyncTask<Void, Void, String> {

	NetworkUntil networkUntil = new NetworkUntil();
	private Context mContext;

	public SettingFacebook(Context context) {
		mContext = context;
	}

	@Override
	protected String doInBackground(Void... params) {
		String resultstring = null;
		try {
			// get check key url
			String url_getSetting = Config.CORE_URL + Config.URL_GET_SETTING;

			resultstring = networkUntil.makeHttpRequest(url_getSetting, "GET", null);

		} catch (Exception ex) {
			return null;
		}

		return resultstring;
	}

	@Override
	protected void onPostExecute(String result) {

		if (result != null) {

			try {
				JSONObject settingJSON = new JSONObject(result);
				SettingFacebookApp settingFb = new SettingFacebookApp(mContext);
				//store facebook app id 
			//	settingFb.storeAppId(settingJSON.getString("app_id"), settingJSON.getBoolean("display_fb"));
				settingFb.storeAppId(mContext.getResources().getString(R.string.app_id), settingJSON.getBoolean("display_fb"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
