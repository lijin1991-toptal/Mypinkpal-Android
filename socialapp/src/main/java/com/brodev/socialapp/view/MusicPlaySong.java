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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import android.os.Handler;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Music;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.CommentFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public final class MusicPlaySong extends SherlockFragmentActivity implements
		OnClickListener, OnTouchListener, OnCompletionListener,
		OnBufferingUpdateListener {

	static final int MENU_SET_MODE = 0;
	private MediaPlayer mediaPlayer;
	private Music music;
	private NetworkUntil networkUntil = new NetworkUntil();
	private ImageButton buttonPlayPause;
	private SeekBar seekBarProgress;
	private User user;
	private int mediaFileLengthInMilliseconds; // this value contains the song
												// duration in milliseconds.
												// Look at getDuration() method
												// in MediaPlayer class

	private final Handler handler = new Handler();
	private ColorView colorView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_player);

		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		user = (User) getApplication().getApplicationContext();
		colorView = new ColorView(getApplicationContext());
		
		Bundle bundle = getIntent().getExtras();

		music = (Music) bundle.get("song");
		
		if (music.getTitle().equals("")) {
			this.getMusicAdapter();
		}
		initView();
		// get comment fragment
		Bundle comment = new Bundle();
		comment.putString("type", "music_song");
		comment.putInt("itemId", Integer.parseInt(music.getSong_id()));
		comment.putInt("totalComment", Integer.parseInt(music.getTotal_comment()));
		comment.putInt("total_like", Integer.parseInt(music.getTotal_like()));
		comment.putBoolean("no_share", music.isShare());
		comment.putBoolean("is_liked", music.isLiked());
		comment.putBoolean("can_post_comment", music.isCanPostComment());
		
		CommentFragment commentFragment = new CommentFragment();
		commentFragment.setArguments(comment);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.commentfragment_wrap, commentFragment).commit();

	}

	/** This method initialise all the views in project */
	private void initView() {
		buttonPlayPause = (ImageButton) findViewById(R.id.buttonPlayPause);
		buttonPlayPause.setOnClickListener(this);

		seekBarProgress = (SeekBar) findViewById(R.id.SeekBarTestPlay);
		seekBarProgress.setMax(99); // It means 100% .0-99
		seekBarProgress.setOnTouchListener(this);

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnCompletionListener(this);

		try {
			mediaPlayer.setDataSource(music.getSong_path()); // setup song from
																// http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3
																// URL to
																// mediaplayer
																// data source
			mediaPlayer.prepareAsync(); // you must call this method after setup
										// the datasource in setDataSource
										// method. After calling prepare() the
										// instance of MediaPlayer starts load
										// data from URL to internal buffer.
		} catch (Exception e) {
			e.printStackTrace();
		}

		mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the
																	// song
																	// length in
																	// milliseconds
																	// from URL

		// set song name
		TextView songName = (TextView) findViewById(R.id.songName);
		songName.setText(music.getTitle());
		colorView.changeColorText(songName, user.getColor());
		
		// set short text
		TextView shortText = (TextView) findViewById(R.id.shortText);
		shortText.setText(music.getShort_text());
		
		TextView time_stamp = (TextView) findViewById(R.id.time_stamp);
		time_stamp.setText(music.getTime_stamp());
		TextView total_like = (TextView) findViewById(R.id.total_like);
		total_like.setText(music.getTotal_like());
		colorView.changeColorText(total_like, user.getColor());
		
		TextView total_comment = (TextView) findViewById(R.id.total_comment);
		total_comment.setText(music.getTotal_comment());
		colorView.changeColorText(total_comment, user.getColor());
		
		ImageView likeImg = (ImageView) this.findViewById(R.id.likes_feed_txt);
		ImageView commentImg = (ImageView) this.findViewById(R.id.comments_feed_txt);
		colorView.changeColorLikeCommnent(likeImg, commentImg, user.getColor());
		
		// set User image
		ImageView userImage = (ImageView) findViewById(R.id.image_user);
		networkUntil.drawImageUrl(userImage, music.getUser_image_path(),
				R.drawable.loading);
		userImage.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent intent = new Intent(MusicPlaySong.this,
						FriendTabsPager.class);
				intent.putExtra("user_id", music.getUser_id());
				startActivity(intent);
				return false;
			}
		});

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.buttonPlayPause) {
			/**
			 * ImageButton onClick event handler. Method which start/pause
			 * mediaplayer playing
			 */

			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.start();
				buttonPlayPause.setImageResource(R.drawable.mp_btn_pause);
			} else {
				mediaPlayer.pause();
				buttonPlayPause.setImageResource(R.drawable.mp_btn_play);
			}

			primarySeekBarProgressUpdater();
		}

	}

	/**
	 * Method which updates the SeekBar primary progress by current song playing
	 * position
	 */
	private void primarySeekBarProgressUpdater() {
		seekBarProgress.setMax(mediaPlayer.getDuration());
		seekBarProgress.setProgress(mediaPlayer.getCurrentPosition());
		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
				public void run() {
					primarySeekBarProgressUpdater();
				}
			};
			handler.postDelayed(notification, 1000);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.SeekBarTestPlay) {
			/**
			 * Seekbar onTouch event handler. Method which seeks MediaPlayer to
			 * seekBar primary progress position
			 */
			if (mediaPlayer.isPlaying()) {
				SeekBar sb = (SeekBar) v;
				int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100)
						* sb.getProgress();
				mediaPlayer.seekTo(playPositionInMillisecconds);
			}
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		/**
		 * MediaPlayer onCompletion event handler. Method which calls then song
		 * playing is complete
		 */
		buttonPlayPause.setImageResource(R.drawable.mp_btn_play);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		/**
		 * Method which updates the SeekBar secondary progress by current song
		 * loading from URL position
		 */
		seekBarProgress.setSecondaryProgress(percent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			mediaPlayer.stop();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * function get result from get method
	 * 
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET() {
		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getSong"));
		pairs.add(new BasicNameValuePair("songId", "" + music.getSong_id()));

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
	public void getMusicAdapter() 
	{
		String resString = getResultFromGET();
		
			try {
				JSONObject mainJSON = new JSONObject(resString);
				
				Object intervention = mainJSON.get("output");
				
				if (intervention instanceof JSONObject) {
					JSONObject outputJSON = mainJSON.getJSONObject("output");
					
					//set user id
					music.setUser_id(outputJSON.getString("user_id"));
					//set title
					music.setTitle(Html.fromHtml(outputJSON.getString("title")).toString());
					//set song path
					music.setSong_path(outputJSON.getString("song_path"));
					//set user image
					music.setUser_image_path(outputJSON.getString("user_image_path"));
					//set short text
					music.setShort_text(outputJSON.getString("short_text"));
					//set total like
					music.setTotal_like(outputJSON.getString("total_like"));
					//set total comment
					music.setTotal_comment(outputJSON.getString("total_comment"));
					//set time stamp
					music.setTime_stamp(Html.fromHtml(outputJSON.getString("time_stamp")).toString());
					
					//set is liked
					if (outputJSON.has("is_liked") && !outputJSON.isNull("is_liked")) {
						Object inte = outputJSON.get("is_liked");
						if (inte instanceof String) 
							music.setLiked(true);
					}
					
					//set can post comment
					if (outputJSON.has("can_post_comment")) {
						music.setCanPostComment(outputJSON.getBoolean("can_post_comment"));
					}
						
					//set share
					if (outputJSON.has("no_share")) 
						music.setShare(true);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				
			}
		
		
	}
}