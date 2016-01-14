package com.brodev.socialapp.android.asyncTask;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.http.NetworkUntil;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings.Secure;

public class RegisterChatServer extends AsyncTask<String, Void, String> {

	private NetworkUntil networkUtil = new NetworkUntil();
	private Context context;
	
	public RegisterChatServer(Context context) {
		this.context = context;
	}
	
	@Override
	protected String doInBackground(String... params) {
		
		String result = null;
		try { 
			
			String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			md.update(androidId.getBytes());
//			byte[] digest = md.digest();
//			StringBuffer sb = new StringBuffer();
//			for (byte b : digest) {
//				sb.append(Integer.toHexString((int) (b & 0xff)));
//			}
//			Config.hashDevice = sb.toString();
            Config.hashDevice = androidId;

			String registerUrl = params[0] + "/" + params[3] + Config.CHAT_REGISTER;
			
			if (!params[0].startsWith("http://"));
				registerUrl = "http://" + registerUrl;
			
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			pairs.add(new BasicNameValuePair("user_id", params[1]));
			pairs.add(new BasicNameValuePair("secret", params[2]));
			pairs.add(new BasicNameValuePair("hash", Config.hashDevice));
			
			result = networkUtil.makeHttpRequest(registerUrl, "GET", pairs);
			
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
