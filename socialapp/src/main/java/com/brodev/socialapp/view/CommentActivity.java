package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Feed;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.CommentFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Activity after click push notification alert on device
 */
public class CommentActivity extends SherlockFragmentActivity {

	private User user;
	private NetworkUntil networkUtil = new NetworkUntil();
	private List<String> listUrlImage = new ArrayList<String>();

	private ImageView userImage, icon, linkImage, singleImage, likeImg, commentImg;
	private TextView titlePhrase, timePhrase, statusPhrase, linkTitle,
			feedTitleExtra, feedContent, totalLike, totalComment;
	
	private LinearLayout horizontalView, linkLayout;
	
	private Feed feed;
	private ColorView colorView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//This has to be called before setContentView and you must use the
        //class in com.actionbarsherlock.view and NOT android.view
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
		setContentView(R.layout.activity_comment);

		setSupportProgressBarIndeterminateVisibility(false);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		user = (User) getApplication().getApplicationContext();
		colorView = new ColorView(getApplicationContext());
		
		// Retrive the data from GCMIntentService.java
		Intent i = getIntent();

		String itemId = i.getStringExtra("item");
		String typeId = i.getStringExtra("type");
		String moduleId = typeId;

		if (typeId.equals("pages")) {
			typeId = "pages_comment";
			moduleId = null;
		} else if ("feed_comment_link".equals(typeId)) {
			typeId = "link";
		}
		
		//link layout
		linkLayout = (LinearLayout) findViewById(R.id.link_view);

		userImage = (ImageView) findViewById(R.id.user_image);
		icon = (ImageView) findViewById(R.id.item_icon_action);
		titlePhrase = (TextView) findViewById(R.id.action_view);
		timePhrase = (TextView) findViewById(R.id.item_time_action);
		statusPhrase = (TextView) findViewById(R.id.status);
		horizontalView = (LinearLayout) findViewById(R.id.horizonalImage);
		linkImage = (ImageView) findViewById(R.id.item_link);
		linkTitle = (TextView) findViewById(R.id.item_link_title);
		feedTitleExtra = (TextView) findViewById(R.id.item_link_title_extra);
		feedContent = (TextView) findViewById(R.id.item_link_feed_content);
		singleImage = (ImageView) findViewById(R.id.single_image_view);
		totalLike = (TextView) findViewById(R.id.total_like);
		totalComment = (TextView) findViewById(R.id.total_comment);
		
		likeImg = (ImageView) findViewById(R.id.likes_feed_txt);
		commentImg = (ImageView) findViewById(R.id.comments_feed_txt);
		
		// get item feed
		GetItemFeed getItemFeed = new GetItemFeed();
		getItemFeed.execute(typeId, itemId, moduleId);

		//set view
		SetView(getItemFeed);
		
		//action click link
		linkLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(CommentActivity.this, WebviewActivity.class);
				intent.putExtra("html", feed.getFeedLink());
				startActivity(intent);
			}
		});
		
	}
	
	/**
	 * Set view
	 * @param getItemFeed
	 */
	public void SetView(GetItemFeed getItemFeed) {
		try {
			if (getItemFeed.get() != null) {
				// set user image
				if (userImage != null) {
					if (!"".equals(feed.getUserImage())) {
						userImage.setVisibility(View.VISIBLE);
						networkUtil.drawImageUrl(userImage, feed.getUserImage(), R.drawable.loading);
						
						//action click user image
						userImage.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent(CommentActivity.this, FriendTabsPager.class);
								if(feed.getProfile_page_id() != null && !("0").equals(feed.getProfile_page_id())){
					            	intent.putExtra("page_id", feed.getProfile_page_id());
				            	} else {
					            	intent.putExtra("user_id", feed.getUserId());
				            	}
								startActivity(intent);
							}
						});
					}
				}
				// set title phrase
				titlePhrase.setText(Html.fromHtml(feed.getTitle()).toString());

				// set feed icon
				if (icon != null) {
					if (!"".equals(feed.getIcon())) {
						icon.setVisibility(View.VISIBLE);
						networkUtil.drawImageUrl(icon, feed.getIcon(), R.drawable.loading);
					}
				}
				// set time phrase
				timePhrase.setText(Html.fromHtml(feed.getTime()).toString());

				// set status
				if (feed.getStatus() != null && !("null").equals(feed.getStatus())) {
					statusPhrase.setVisibility(View.VISIBLE);
					statusPhrase.setText(Html.fromHtml(feed.getStatus()).toString());
				}

				// set feed image
				if (listUrlImage.size() > 0) {
					// if is photo module
					if (feed.getType().equals("photo")) {
						if (listUrlImage.size() == 1) {
							singleImage.setVisibility(View.VISIBLE);
							horizontalView.setVisibility(View.GONE);
							networkUtil.drawImageUrl(singleImage, listUrlImage.get(0),R.drawable.loading);
						} else {
							horizontalView.setVisibility(View.VISIBLE);
							singleImage.setVisibility(View.GONE);
							for (int j = 0; j < listUrlImage.size(); j++) {
								ImageView image = new ImageView(getApplicationContext());
								
								LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.comment_image_view), 
										(int) getResources().getDimension(R.dimen.comment_image_view));
								lp.setMargins(5, 5, 5, 0);
								
								image.setLayoutParams(lp);
							
								image.setScaleType(ImageView.ScaleType.CENTER_CROP);
								
								networkUtil.drawImageUrl(image, listUrlImage.get(j),R.drawable.loading);
									
								horizontalView.addView(image);
							}
						}
					} else {
						linkImage.setVisibility(View.VISIBLE);
						linkTitle.setVisibility(View.VISIBLE);
						feedTitleExtra.setVisibility(View.VISIBLE);
						feedContent.setVisibility(View.VISIBLE);
						networkUtil.drawImageUrl(linkImage, listUrlImage.get(0), R.drawable.loading);
						linkTitle.setText(feed.getTitleFeed());
						feedTitleExtra.setText(feed.getFeedTitleExtra());
						feedContent.setText(feed.getFeedContent());
					}
				}
				
				colorView.changeColorLikeCommnent(likeImg, commentImg, user.getColor());
				
				totalLike.setText(String.valueOf(feed.getTotalLike()));
				colorView.changeColorText(totalLike, user.getColor());
				
				if (feed.getTotalComment() != null) {
					totalComment.setText(feed.getTotalComment());
					colorView.changeColorText(totalComment, user.getColor());
				}
					
				Bundle comment = new Bundle();
				comment.putString("type", feed.getType());
				comment.putInt("itemId", Integer.parseInt(feed.getItemId()));
				comment.putInt("totalComment", Integer.parseInt(feed.getTotalComment()));
				comment.putInt("total_like", feed.getTotalLike());
				comment.putBoolean("is_liked", (feed.getFeedIsLiked() != null) ? true : false);
				comment.putBoolean("can_post_comment", feed.getCanPostComment());
				
				CommentFragment commentFragment = new CommentFragment();
				commentFragment.setArguments(comment);
				getSupportFragmentManager().beginTransaction()
						.add(R.id.commentfragment_wrap, commentFragment).commit();
			}
		} catch (Exception ex) {
			
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 * Class get item feed
	 * @author ducpham
	 */
	public class GetItemFeed extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			String resultstring = null;

			try {
				// Use BasicNameValuePair to create GET data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method", "accountapi.getItem"));
				if (params[1] != null) {
					pairs.add(new BasicNameValuePair("type_id", params[0]));
					pairs.add(new BasicNameValuePair("item_id", params[1]));
				} else {
					pairs.add(new BasicNameValuePair("module", params[2]));
					pairs.add(new BasicNameValuePair("item_id", params[1]));
				}

				// url request
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}				
				// request GET method to server

				resultstring = networkUtil.makeHttpRequest(URL, "GET", pairs);

				feed = new Feed();

				JSONObject mainJSON = new JSONObject(resultstring);
				JSONObject pagesObj = mainJSON.getJSONObject("output");

				if (pagesObj.has("feed_id")) {
					feed.setFeedId(pagesObj.getString("feed_id"));
				}

				if (pagesObj.has("item_id")) {
					feed.setItemId(pagesObj.getString("item_id"));
				}

				if (pagesObj.has("full_name")) {
					feed.setFullName(pagesObj.getString("full_name"));
				}
				if (pagesObj.has("time_phrase")) {
					feed.setTime(pagesObj.getString("time_phrase"));
				}
				if (pagesObj.has("feed_icon")) {
					feed.setIcon(pagesObj.getString("feed_icon"));
				}

				if (pagesObj.has("user_image")) {
					feed.setUserImage(pagesObj.getString("user_image"));
				}

				if (pagesObj.has("title_phrase")) {
					feed.setTitle(Html.fromHtml(
							pagesObj.getString("title_phrase")).toString());
				}

				if (pagesObj.has("feed_status")) {
					feed.setStatus(Html.fromHtml(pagesObj.getString("feed_status")).toString());
				}

				// set image list
				if (!pagesObj.isNull("feed_image")) {
					JSONArray fImage = pagesObj.getJSONArray("feed_image");
					for (int i = 0; i < fImage.length(); i++) {
						listUrlImage.add(fImage.getString(i));
					}
				}
				if(pagesObj.has("photo_sizes")){
					listUrlImage.add(pagesObj.getJSONObject("photo_sizes").getString("500"));
				}
				
				// set type id
				feed.setType(pagesObj.getJSONObject("social_app").getString("type_id"));

				if (pagesObj.has("feed_link")) {
					feed.setFeedLink(pagesObj.getString("feed_link"));
				}

				if (pagesObj.has("feed_title")) {
					feed.setTitleFeed(Html.fromHtml(pagesObj.getString("feed_title")).toString());
				}

				if (pagesObj.has("feed_title_extra")) {
					feed.setFeedTitleExtra(Html.fromHtml(pagesObj.getString("feed_title_extra")).toString());
				}

				if (pagesObj.has("feed_content")) {
					feed.setFeedContent(Html.fromHtml(pagesObj.getString("feed_content")).toString());
				}

				if (pagesObj.has("feed_total_like")) {
					feed.setHasLike(pagesObj.getString("feed_total_like"));
					feed.setTotalLike(Integer.parseInt(pagesObj.getString("feed_total_like")));
				}

				if (pagesObj.has("enable_like")) {
					if (!pagesObj.isNull("feed_is_liked") && pagesObj.getString("feed_is_liked") != "false") {
						feed.setFeedIsLiked("feed_is_liked");
					}
					feed.setEnableLike(pagesObj.getBoolean("enable_like"));
				} else {
					feed.setEnableLike(false);
				}

				if (pagesObj.has("can_post_comment")) {
					feed.setCanPostComment(pagesObj.getBoolean("can_post_comment"));
				} else {
					feed.setCanPostComment(false);
				}

				if (pagesObj.has("total_comment")) {
					feed.setTotalComment(pagesObj.getString("total_comment"));
				}

				if(pagesObj.has("comment_type_id")){
					feed.setComment_type_id(pagesObj.getString("comment_type_id"));
				}
				
				if(pagesObj.has("profile_page_id")){
					feed.setProfile_page_id(pagesObj.getString("profile_page_id"));
				}
			} catch (Exception ex) {

			}
			return resultstring;
		}


		@Override
		protected void onPostExecute(String result) {
			
		}
	}

}
