package com.brodev.socialapp.android.asyncTask;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Comment;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.content.Context;
import android.os.AsyncTask;

public class LoadCommentAsyncTask extends AsyncTask<String, Void, List<Comment>> {
	
	private NetworkUntil networkUtil = new NetworkUntil();
	private User user;
	
	public LoadCommentAsyncTask(Context context) {
		user = (User) context.getApplicationContext();
	}
	
	@Override
	protected void onPostExecute(List<Comment> result) {
		super.onPostExecute(result);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected List<Comment> doInBackground(String... params) {
		List<Comment> lstComment = new ArrayList<Comment>();
		
		try {
			// Use BasicNameValuePair to create GET data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("token", params[0]));
			pairs.add(new BasicNameValuePair("method", "accountapi.getFeedComments"));
			if(params[1] != null){
				pairs.add(new BasicNameValuePair("type", "" + params[1]));
				pairs.add(new BasicNameValuePair("item_id", "" + params[2]));
			} else {
				pairs.add(new BasicNameValuePair("feed_id", "" + params[3]));
				
			}
			if(params[3] != null){
				pairs.add(new BasicNameValuePair("total", "" + params[4]));
			}
			pairs.add(new BasicNameValuePair("page", "" + params[5]));
			// url request
			String URL = null;
			if (Config.CORE_URL == null) {
				URL = Config.makeUrl(user.getCoreUrl(), null, false);	
			} else {
				URL = Config.makeUrl(Config.CORE_URL, null, false);
			}
			
			// request GET method to server
			String resultstring = networkUtil.makeHttpRequest(URL, "GET", pairs);
			
			if (resultstring != null) {
				try {
					JSONObject mainJSON = new JSONObject(resultstring);
					JSONArray outJson = mainJSON.getJSONArray("output");
					
					//get list comment
					for (int i = 0; i < outJson.length(); i++) {
						JSONObject pagesObj = outJson.getJSONObject(i);
						Comment comment = new Comment();
						comment = comment.convertComment(comment, pagesObj);
						lstComment.add(comment);
					}
					
				} catch (Exception ex) {
				}
			}
		} catch (Exception ex) {
			return null;
		}
		
		return lstComment;
	}
	
	
	
}
