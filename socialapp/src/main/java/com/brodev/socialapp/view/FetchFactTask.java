package com.brodev.socialapp.view;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Feed;
import com.brodev.socialapp.entity.User;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FetchFactTask extends AsyncTask<String, Void, String>{
	
	ImagePagerActivity imagePagerActivity;
	TextView total_like;
	TextView total_comment;
	ImageView like_icon;
	ImageView comment_icon;
	TextView like;
	TextView comment;
	String userToken;
	User user;
	NetworkUntil networkUntil = new NetworkUntil();
	String resultstring = null;
	Feed objFeed;
	
	public FetchFactTask(ImagePagerActivity imagePagerActivity, TextView total_like, TextView total_comment,
			ImageView like_icon, ImageView comment_icon, TextView like,
			TextView comment) {
		super();
		this.imagePagerActivity = imagePagerActivity;
		this.total_like = total_like;
		this.total_comment = total_comment;
		this.like_icon = like_icon;
		this.comment_icon = comment_icon;
		this.like = like;
		this.comment = comment;
		user = (User) imagePagerActivity.getApplication();
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	@Override
	protected String doInBackground(String... params) {
		try {
			userToken = params[0];
			// Use BasicNameValuePair to create GET data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("token", params[0]));
			pairs.add(new BasicNameValuePair("method", "accountapi.getPhoto"));
			pairs.add(new BasicNameValuePair("photo_id", "" + params[1]));
			
			// url request
			String URL = null;
			if (Config.CORE_URL == null) {
				URL = Config.makeUrl(user.getCoreUrl(), null, false);	
			} else {
				URL = Config.makeUrl(Config.CORE_URL, null, false);
			}
			
			// request GET method to server
			
			resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
			Log.i("photo detail", resultstring);
			JSONObject mainJSON = new JSONObject(resultstring);
			JSONObject  pagesObj = mainJSON.getJSONObject("output");
			
				objFeed = new Feed();
				//objFeed.setFeedId(pagesObj.getString("feed_id"));
				if(pagesObj.has("item_id")){
					objFeed.setItemId(pagesObj.getString("item_id"));
				}
				
				if(pagesObj.has("title")){
				objFeed.setTitle(Html.fromHtml(pagesObj.getString("title")).toString());
				}
				
				if(pagesObj.has("feed_title")){
					objFeed.setTitleFeed(pagesObj.getString("feed_title"));
				}
				
				if(pagesObj.has("feed_title")){
					objFeed.setComment_type_id(pagesObj.getString("comment_type_id"));
				}
				
				if(pagesObj.has("feed_link")){
				objFeed.setFeedLink(pagesObj.getString("feed_link"));
				}
				
				if(pagesObj.has("parent_module_id") && !pagesObj.isNull("parent_module_id")){
					objFeed.setModule(pagesObj.getString("parent_module_id"));
				}
				
				if(!pagesObj.isNull("feed_is_liked") && pagesObj.getString("feed_is_liked") != "false"){
					objFeed.setFeedIsLiked("feed_is_liked");
				}

				if(pagesObj.has("can_post_comment")){
				objFeed.setCanPostComment(pagesObj.getBoolean("can_post_comment"));
				}else{
					objFeed.setCanPostComment(false);
				}
				
				if(pagesObj.has("total_comment")){
					objFeed.setTotalComment(pagesObj.getString("total_comment"));
				}
				
				if(pagesObj.has("profile_page_id")){
					objFeed.setProfile_page_id(pagesObj.getString("profile_page_id"));
				}
				objFeed.setType(pagesObj.getJSONObject("social_app").getString("type_id"));
				if(pagesObj.has("feed_total_like")){
					
					objFeed.setHasLike(pagesObj.getString("feed_total_like"));
					objFeed.setTotalLike(Integer.parseInt(pagesObj.getString("feed_total_like")));
				}
				
				
		} catch(Exception ex) {
			//Log.i(DEBUG_TAG, ex.getMessage());
		}
		return resultstring;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			
			//Toast.makeText(getApplicationContext(), objFeed.getHasLike(), Toast.LENGTH_SHORT).show();
			if (total_like != null && objFeed.getHasLike() != null ) {
				like.setVisibility(View.VISIBLE);
				if(objFeed.getFeedIsLiked() == null){
					like.setText("Like");
				}else{
					like.setText("Unlike");
				}
				
		        like.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View v) {
		                onListItemClick(objFeed.getFeedIsLiked());
		            }
		        });

				total_like.setVisibility(View.VISIBLE);
				like_icon.setVisibility(View.VISIBLE);
				total_like.setText(Integer.toString(objFeed.getTotalLike()));
			}else{
				total_like.setVisibility(View.GONE);
				like_icon.setVisibility(View.GONE);
				like.setVisibility(View.GONE);
			}
			
			//set_total_comment
			
			if (total_comment != null && objFeed.getTotalComment() != null) {
				comment.setVisibility(View.VISIBLE);
				total_comment.setVisibility(View.VISIBLE);
				comment_icon.setVisibility(View.VISIBLE);
				total_comment.setText(objFeed.getTotalComment());
				comment.setOnClickListener(new View.OnClickListener() {

					@Override
		            public void onClick(View v) {
		                onCommentClick(0);
		            }
		        });
			}else{
				comment.setVisibility(View.GONE);
				total_comment.setVisibility(View.GONE);
				comment_icon.setVisibility(View.GONE);
			}
			
		}
	}
	
	protected void onListItemClick(String liked) {

        if (liked == null) {
        	
        	like.setText("Unlike");
        	total_like.setText(Integer.toString(objFeed.getTotalLike() + 1));
        	//Toast.makeText(getContext(), Integer.toString(item1.getTotalLike()), Toast.LENGTH_SHORT).show();
        	
        	new test().execute(objFeed.getItemId(), objFeed.getType(), objFeed.getFeedId(), "like");
        }else{
        	like.setText("Like");
        	total_like.setText(Integer.toString(objFeed.getTotalLike() - 1));
        	new test().execute(objFeed.getItemId(), objFeed.getType(), objFeed.getFeedId(), "unlike");
        
        }

    }
	
	protected void onCommentClick(int position) {
		
		imagePagerActivity.doShowCommentDetail(position, objFeed.getType(), objFeed.getItemId(), objFeed.getModule());
        
    }
	
	public class test extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				String likerequest;
				// Use BasicNameValuePair to create GET data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", userToken));
				
				if(("like").equals(params[3])){
					pairs.add(new BasicNameValuePair("method", "accountapi.like"));
				}else{
					pairs.add(new BasicNameValuePair("method", "accountapi.unlike"));
				}
				
				if(params[1] != null){
					pairs.add(new BasicNameValuePair("type", "" + params[1]));
					pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
				}else{
					pairs.add(new BasicNameValuePair("feed_id", "" + params[2]));
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
		
		
	}

}
