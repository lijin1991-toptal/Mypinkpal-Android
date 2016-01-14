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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.Video;
import com.brodev.socialapp.fragment.CommentFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public final class VideoPlay extends SherlockFragmentActivity {

	static final int MENU_SET_MODE = 0;
	private Video video;
	private NetworkUntil networkUntil = new NetworkUntil();
	private User user;
	private PhraseManager phraseManager;
	private ColorView colorView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_player);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		user = (User) getApplication().getApplicationContext();
		phraseManager = new PhraseManager(getApplicationContext());
		colorView = new ColorView(getApplicationContext());

		Bundle bundle = getIntent().getExtras();

		video = (Video) bundle.get("video");
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "video.video"));
		if (video.getTime_stamp().equals("0")) {
			this.getVideoAdapter();
		}
		
		initView();
		// get comment fragment
		Bundle comment = new Bundle();
		comment.putString("type", "video");
		comment.putInt("itemId", video.getVideo_id());
		comment.putInt("totalComment", video.getTotal_comment());
		comment.putInt("total_like", video.getTotal_like());
		comment.putBoolean("is_liked", video.getIs_like());
		comment.putBoolean("can_post_comment", video.getCan_post_comment());
		CommentFragment commentFragment = new CommentFragment();
		commentFragment.setArguments(comment);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.commentfragment_wrap, commentFragment).commit();

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

	/** This method initialise all the views in project */
	private void initView() {
		// set song name
		TextView songName = (TextView) findViewById(R.id.songName);
		songName.setText(video.getTitle());
		colorView.changeColorText(songName, user.getColor());

		// set short text
		TextView shortText = (TextView) findViewById(R.id.shortText);
		shortText.setText(video.getFull_name());

		TextView time_stamp = (TextView) findViewById(R.id.time_stamp);
		time_stamp.setText(video.getTime_stamp());
		TextView total_like = (TextView) findViewById(R.id.total_like);
		total_like.setText(String.valueOf(video.getTotal_like()));
		colorView.changeColorText(total_like, user.getColor());
		
		TextView total_comment = (TextView) findViewById(R.id.total_comment);
		total_comment.setText(String.valueOf(video.getTotal_comment()));
		colorView.changeColorText(total_comment, user.getColor());

		TextView content = (TextView) findViewById(R.id.videoContent);
		content.setText(video.getText());
	
		ImageView likeImg = (ImageView) this.findViewById(R.id.likes_feed_txt);
		ImageView commentImg = (ImageView) this.findViewById(R.id.comments_feed_txt);
		colorView.changeColorLikeCommnent(likeImg, commentImg, user.getColor());
		
		ImageView videoImage = (ImageView) findViewById(R.id.videoImage);
		if (video.isYoutube()) {
			networkUntil.drawImageUrl(videoImage, video.GetBigImage(), R.drawable.loading);
		} else {
			networkUntil.drawImageUrl(videoImage, video.getImage_path(), R.drawable.loading);
		}
		
		if (!video.getDuration().equals("")) {
			TextView duration = (TextView) findViewById(R.id.duration);
			duration.setText(video.getDuration());
		}
		
		// set User image
		ImageView userImage = (ImageView) findViewById(R.id.image_user);
		networkUntil.drawImageUrl(userImage, video.getUser_image_path(), R.drawable.loading);
		userImage.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent intent = new Intent(VideoPlay.this, FriendTabsPager.class);
				intent.putExtra("user_id", video.getUser_id());
				startActivity(intent);
				return false;
			}
		});

		videoImage.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (video.isYoutube()) {
					String videoId = video.getTubeId();
					Intent lVideoIntent = new Intent(null, Uri.parse("ytv://"
							+ videoId), getApplicationContext(),
							OpenYouTubePlayerActivity.class);
					startActivity(lVideoIntent);
				} else {
					Intent intent = new Intent(getApplicationContext(), WebviewActivity.class);
					Log.i("CHECKLINK", video.getWeb_link());		
					intent.putExtra("html", video.getWeb_link());

					startActivity(intent);
				}

				return false;
			}
		});

	}

    /**
     * function get result from get method
     * @return
     */
	public String getResultFromGET() {
		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getVideo"));
		pairs.add(new BasicNameValuePair("videoId", "" + video.getVideo_id()));

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

	public void getVideoAdapter() {
		String resString = getResultFromGET();

		try {
			JSONObject mainJSON = new JSONObject(resString);

			Object intervention = mainJSON.get("output");

			if (intervention instanceof JSONObject) {
				JSONObject outputJSON = mainJSON.getJSONObject("output");

				video = video.convertVideo(outputJSON);
			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}
}