package com.brodev.socialapp.android.asyncTask;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Comment;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PostCommentAsyncTask extends AsyncTask<String, Void, Comment> {

	private NetworkUntil networkUtil = new NetworkUntil();
	private User user;
	
	public PostCommentAsyncTask(Context context) {
		user = (User) context.getApplicationContext();
	}
	
	@Override
	protected Comment doInBackground(String... params) {
		
		Comment comment = new Comment();
		String URL = null;
		try {
			if (Config.CORE_URL == null) {
				URL = Config.makeUrl(user.getCoreUrl(), "comment", true) + "&token=" + params[0];
			} else {
				URL = Config.makeUrl(Config.CORE_URL, "comment", true) + "&token=" + params[0];
			}
			
			if (params[2] != null) {
				 URL += "&type=" + params[1] + "&item_id=" + params[2];
			} else {
				 URL += "&feed_id=" + params[3];
			}
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("comment", params[4]));
			
			// request POST method to server
			String resultstring = networkUtil.makeHttpRequest(URL, "POST", pairs);
			Log.i("Comment Post Comment ACITIVITY", resultstring);
			
			JSONObject mainJSON = new JSONObject(resultstring);
			JSONObject pagesObj = mainJSON.getJSONObject("output");
			
			comment = comment.convertComment(comment, pagesObj);
			
		} catch (Exception ex) {
			return null;
		}
		return comment;
	}

	@Override
	protected void onPostExecute(Comment result) {	
		
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

}
