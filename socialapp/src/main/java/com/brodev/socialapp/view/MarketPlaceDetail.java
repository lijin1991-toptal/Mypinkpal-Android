/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.brodev.socialapp.view;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.MarketPlace;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.CommentFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public final class MarketPlaceDetail extends SherlockFragmentActivity {

	static final int MENU_SET_MODE = 0;
	User user;
	MarketPlace marketPlace;
	private PhraseManager phraseManager;
	
	NetworkUntil networkUntil = new NetworkUntil();
	private ColorView colorView;
    private ImageGetter imageGetter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.market_place_content_view);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle bundle = getIntent().getExtras();
		marketPlace = (MarketPlace) bundle.get("marketplace");
	
		phraseManager = new PhraseManager(getApplicationContext());
		user = (User) getApplicationContext();
		colorView = new ColorView(getApplicationContext());
        this.imageGetter = new ImageGetter(getApplicationContext());
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		
		if (marketPlace.getTime_stamp().equals("0")) {
			this.getMarketPlaceAdapter();
		}
		
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "marketplace.marketplace"));
		initView();
		
		// get comment fragment
		Bundle comment = new Bundle();
		comment.putString("type", "marketplace");
		comment.putInt("itemId", marketPlace.getListing_id());
		comment.putInt("totalComment", marketPlace.getTotal_comment());
		comment.putInt("total_like", marketPlace.getTotal_like());
		comment.putBoolean("no_share", false);
		comment.putBoolean("is_liked", marketPlace.getIs_liked());
		comment.putBoolean("can_post_comment", marketPlace.getCan_post_comment());
		
		CommentFragment commentFragment = new CommentFragment();
		commentFragment.setArguments(comment);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.commentfragment_wrap, commentFragment).commit();
	}

	private void initView() {
		
		ImageView userImage = (ImageView) this.findViewById(R.id.image_user);
		
		if (!"".equals(marketPlace.getUser_image_path())) {
			networkUntil.drawImageUrl(userImage, marketPlace.getUser_image_path(), R.drawable.loading);
		}
		
		userImage.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent intent = new Intent(MarketPlaceDetail.this,
						FriendTabsPager.class);
				intent.putExtra("user_id", marketPlace.getUser_id());
				startActivity(intent);
				return false;
			}
		});
		
		
		// set title
		TextView title = (TextView) this.findViewById(R.id.title);
		title.setText(marketPlace.getTitle());
		colorView.changeColorText(title, user.getColor());
		
		// set content
        TextView content = (TextView) this.findViewById(R.id.content);

        // interesting part starts from here here:
        Html.ImageGetter ig = imageGetter.create(0, marketPlace.getText(), content);

        content.setTag(0);
        content.setText(Html.fromHtml(marketPlace.getText(), ig, null));
		
		TextView timestampTxt = (TextView) findViewById(R.id.txtTimestamp);
		timestampTxt.setText(phraseManager.getPhrase(getApplicationContext(), "marketplace.posted_on"));
		TextView timestamp = (TextView) findViewById(R.id.time_stamp);
		timestamp.setText(marketPlace.getTime_stamp());
		TextView price = (TextView) this.findViewById(R.id.price);
		
		if (marketPlace.getPrice() == 0) {
			price.setText(phraseManager.getPhrase(getApplicationContext(), "marketplace.free"));
		} else {
			price.setText(marketPlace.getCurrency() + " " + marketPlace.getPrice());
		}
		
		TextView locationTxt = (TextView) findViewById(R.id.txtLocation);
		locationTxt.setText(phraseManager.getPhrase(getApplicationContext(), "marketplace.location"));
		TextView txtLocation = (TextView) this.findViewById(R.id.location);
		String location = marketPlace.getCountry_name();
		
		if (!marketPlace.getCountry_child_name().equals("")) {
			location += " > " + marketPlace.getCountry_child_name();
		}
		if (!marketPlace.getCity_name().equals("")) {
			location += " > " + marketPlace.getCity_name();
		}
		
		txtLocation.setText(location);
		// set short text
		TextView fullnameTxt = (TextView) findViewById(R.id.txtFullname);
		fullnameTxt.setText(phraseManager.getPhrase(getApplicationContext(), "marketplace.posted_by"));
		
		TextView shortText = (TextView) findViewById(R.id.fullName);
		shortText.setText(marketPlace.getFull_name());
		colorView.changeColorText(shortText, user.getColor());
		
		shortText.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent intent = new Intent(MarketPlaceDetail.this, FriendTabsPager.class);
				intent.putExtra("user_id", marketPlace.getUser_id());
				startActivity(intent);
				return false;
			}
		});
		
		TextView total_like = (TextView) findViewById(R.id.total_like);
		total_like.setText(String.valueOf(marketPlace.getTotal_like()));
		colorView.changeColorText(total_like, user.getColor());
		
		TextView total_comment = (TextView) findViewById(R.id.total_comment);
		total_comment.setText(String.valueOf(marketPlace.getTotal_comment()));
		colorView.changeColorText(total_comment, user.getColor());
		
		ImageView likeImg = (ImageView) this.findViewById(R.id.likes_feed_txt);
		ImageView commentImg = (ImageView) this.findViewById(R.id.comments_feed_txt);
		colorView.changeColorLikeCommnent(likeImg, commentImg, user.getColor());
		
		//get list images
		if (!marketPlace.getImages().equals("")) {
			LinearLayout listImages = (LinearLayout) findViewById(R.id.listImages);	
			JSONObject objOutputImage = null;
			try {
				JSONArray objImages = new JSONArray(marketPlace.getImages());
				for (int i = 0; i < objImages.length(); i++) {
					objOutputImage = objImages.getJSONObject(i);
					ImageView imageView = new ImageView(getApplicationContext());
					
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.marketplace_image), (int) getResources().getDimension(R.dimen.marketplace_image));
					
					lp.setMargins(5, 5, 5, 0);
					
					imageView.setLayoutParams(lp);
				
					imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					final String imagePath = objOutputImage.getString("image_path");
					networkUntil.drawImageUrl(imageView, imagePath, R.drawable.loading);
					
					imageView.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
							intent.putExtra("image", imagePath);
							intent.putExtra("title", marketPlace.getTitle());
							startActivity(intent);
						}
					});
					
					listImages.addView(imageView);
					
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			this.findViewById(R.id.horizontalScrollView1).setVisibility(View.GONE);
			this.findViewById(R.id.marketplace_list_image_view).setVisibility(View.GONE);
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
     *  function get result from get method
     * @return
     */
	public String getResultFromGET() {
		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getlisting"));
		pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
		pairs.add(new BasicNameValuePair("listingId", "" + marketPlace.getListing_id()));

		// url request
		String URL = null;
		if (Config.CORE_URL == null) {
			URL = Config.makeUrl(user.getCoreUrl(), null, false);	
		} else {
			URL = Config.makeUrl(Config.CORE_URL, null, false);
		}

		// request GET method to server
		resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);

		return resultstring;
	}

	/**
	 * Function create Music adapter
	 * 
	 * @return Music Adapter
	 */
	public void getMarketPlaceAdapter() {

		String resString = this.getResultFromGET();
		try {
			JSONObject mainJSON = new JSONObject(resString);

			Object intervention = mainJSON.get("output");
			if (intervention instanceof JSONObject) {
				JSONObject outputJSON = mainJSON.getJSONObject("output");
				// set title
				marketPlace = marketPlace.convertMarketPlace(outputJSON); 

			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}
}
