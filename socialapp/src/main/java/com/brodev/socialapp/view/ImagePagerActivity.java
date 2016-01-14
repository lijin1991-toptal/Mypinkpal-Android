/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.CommentDetailFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
@SuppressLint("InlinedApi")
public class ImagePagerActivity extends SherlockFragmentActivity {

	private static final String STATE_POSITION = "STATE_POSITION";
	private ViewPager pager;
	private RelativeLayout likeArena;
	private NetworkUntil networkUntil = new NetworkUntil();
	private PhraseManager phraseManager;
	private TextView total_like, total_comment, like, comment;
	private ImageView like_icon, comment_icon;
	private LinearLayout likeCommentView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		
		ColorDrawable color = new ColorDrawable(Color.TRANSPARENT);
		color.setAlpha(128);
		getSupportActionBar().setBackgroundDrawable(color);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.ac_image_pager);

		File cacheDir = new File(this.getCacheDir(), "imgcachedir");
		if (!cacheDir.exists())
			cacheDir.mkdir();
		
		// phrase manager
		phraseManager = new PhraseManager(getApplicationContext());

		Bundle bundle = getIntent().getExtras();

		String[] imageUrls = bundle.getStringArray("image");
		String[] imagesId = bundle.getStringArray("photo_id");
		String[] HasLike = bundle.getStringArray("HasLike");
		String[] FeedisLike = bundle.getStringArray("FeedisLike");
		String[] Total_like = bundle.getStringArray("Total_like");
		String[] Total_comment = bundle.getStringArray("Total_comment");
		String[] Itemid = bundle.getStringArray("Itemid");
		String[] Type = bundle.getStringArray("Type");	

		int pagerPosition = bundle.getInt("position", 0);

		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new ImagePagerAdapter(this, imageUrls, imagesId,
				HasLike, FeedisLike, Total_like, Total_comment, Itemid, Type));

		pager.setOffscreenPageLimit(2);
		pager.setCurrentItem(pagerPosition);

		this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	public void onResume() {
		super.onResume();
		getSupportActionBar().show();
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
	 * Change color
	 * @param colorCode
	 */
	private void changeColor(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			like.setTextColor(Color.parseColor("#da6e00"));
			comment.setTextColor(Color.parseColor("#da6e00"));
			total_like.setTextColor(Color.parseColor("#da6e00"));
			total_comment.setTextColor(Color.parseColor("#da6e00"));
			like_icon.setImageResource(R.drawable.brown_like_icon);
			comment_icon.setImageResource(R.drawable.brown_commet_icon);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			like.setTextColor(Color.parseColor("#ef4964"));
			comment.setTextColor(Color.parseColor("#ef4964"));
			total_like.setTextColor(Color.parseColor("#ef4964"));
			total_comment.setTextColor(Color.parseColor("#ef4964"));
			like_icon.setImageResource(R.drawable.pink_like_icon);
			comment_icon.setImageResource(R.drawable.pink_commet_icon);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			like.setTextColor(Color.parseColor("#3a8d01"));
			comment.setTextColor(Color.parseColor("#3a8d01"));
			total_like.setTextColor(Color.parseColor("#3a8d01"));
			total_comment.setTextColor(Color.parseColor("#3a8d01"));
			like_icon.setImageResource(R.drawable.green_like_icon);
			comment_icon.setImageResource(R.drawable.green_commet_icon);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			like.setTextColor(Color.parseColor("#8190db"));
			comment.setTextColor(Color.parseColor("#8190db"));
			total_like.setTextColor(Color.parseColor("#8190db"));
			total_comment.setTextColor(Color.parseColor("#8190db"));
			like_icon.setImageResource(R.drawable.violet_like_icon);
			comment_icon.setImageResource(R.drawable.violet_commet_icon);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			like.setTextColor(Color.parseColor("#ff0606"));
			comment.setTextColor(Color.parseColor("#ff0606"));
			total_like.setTextColor(Color.parseColor("#ff0606"));
			total_comment.setTextColor(Color.parseColor("#ff0606"));
			like_icon.setImageResource(R.drawable.red_like_icon);
			comment_icon.setImageResource(R.drawable.red_commet_icon);
		}else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			like.setTextColor(Color.parseColor("#4e529b"));
			comment.setTextColor(Color.parseColor("#4e529b"));
			total_like.setTextColor(Color.parseColor("#4e529b"));
			total_comment.setTextColor(Color.parseColor("#4e529b"));
			like_icon.setImageResource(R.drawable.dark_violet_like_icon);
			comment_icon.setImageResource(R.drawable.dark_violet_commet_icon);
		} else {
			like.setTextColor(Color.parseColor("#0084c9"));
			comment.setTextColor(Color.parseColor("#0084c9"));
			total_like.setTextColor(Color.parseColor("#0084c9"));
			total_comment.setTextColor(Color.parseColor("#0084c9"));
			like_icon.setImageResource(R.drawable.like_icon);
			comment_icon.setImageResource(R.drawable.commet_icon);
		}
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private String[] images;
		private String[] imageid;
		private String[] HasLike;
		private String[] FeedisLike;
		private String[] Total_like;
		private String[] Total_comment;
		private String[] Itemid;
		private String[] Type;
		User user = (User) getApplicationContext();
		private LayoutInflater inflater;
		private ImagePagerActivity imagePagerActivity;


		public ImagePagerAdapter(ImagePagerActivity imagePagerActivity,
				String[] images, String[] imageid, String[] hasLike,
				String[] feedisLike, String[] total_like,
				String[] total_comment, String[] itemid, String[] type) {
			super();
			this.imagePagerActivity = imagePagerActivity;
			this.images = images;
			this.imageid = imageid;
			this.HasLike = hasLike;
			this.FeedisLike = feedisLike;
			this.Total_like = total_like;
			this.Total_comment = total_comment;
			this.Itemid = itemid;
			this.Type = type;

			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public int getItemPosition(Object item) {
			return POSITION_NONE;
		}

		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
			final PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.image);

			likeArena = (RelativeLayout) imageLayout.findViewById(R.id.like_arena_photo);

			// set view for like arena
			total_like = (TextView) imageLayout.findViewById(R.id.total_like);
			total_comment = (TextView) imageLayout.findViewById(R.id.total_comment);
			like_icon = (ImageView) imageLayout.findViewById(R.id.grid_item_like_icon);
			comment_icon = (ImageView) imageLayout.findViewById(R.id.grid_item_comment_icon);
			like = (TextView) imageLayout.findViewById(R.id.like);
			comment = (TextView) imageLayout.findViewById(R.id.comment);
			comment.setText(phraseManager.getPhrase(getApplicationContext(), "feed.comment"));
			likeCommentView = (LinearLayout) imageLayout.findViewById(R.id.like_comment_photo_view);

			//change color
			changeColor(user.getColor());
			
			imageView.setOnPhotoTapListener(new OnPhotoTapListener() {

				@Override
				public void onPhotoTap(View view, float x, float y) {
					// TODO Auto-generated method stub
					likeArena.setVisibility(View.VISIBLE);
					getSupportActionBar().show();
				}
			});

			networkUntil.drawImageUrl(imageView, images[position], R.drawable.loading);

			if (Itemid != null) {
				if (total_like != null && HasLike[position] != null && FeedisLike.length > 0) {
					like.setVisibility(View.VISIBLE);
					if (FeedisLike[position] != null) {
						if ("null".equals(FeedisLike[position])) {
							like.setText(phraseManager.getPhrase(getApplicationContext(), "feed.like"));
						} else {
							like.setText(phraseManager.getPhrase(getApplicationContext(), "feed.unlike"));
						}

					}

					like.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							onListItemClick(FeedisLike[position], position);
						}
					});

					total_like.setVisibility(View.VISIBLE);
					like_icon.setVisibility(View.VISIBLE);
					total_like.setText(Total_like[position]);
					
					likeCommentView.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(ImagePagerActivity.this, FriendActivity.class);
							intent.putExtra("type", Type[position]);
							intent.putExtra("item_id", Itemid[position]);
							startActivity(intent);
						}
					});
				} else {
					total_like.setVisibility(View.GONE);
					like_icon.setVisibility(View.GONE);
					like.setVisibility(View.GONE);
				}

				if (total_comment != null && Total_comment.length > 0) {
					if (Total_comment[position] != null) {
						comment.setVisibility(View.VISIBLE);
						total_comment.setVisibility(View.VISIBLE);
						comment_icon.setVisibility(View.VISIBLE);
						total_comment.setText(Total_comment[position]);
						comment.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								onCommentClick(position);
							}
						});
					} else {
						comment.setVisibility(View.GONE);
						total_comment.setVisibility(View.GONE);
						comment_icon.setVisibility(View.GONE);
					}

				} else {
					comment.setVisibility(View.GONE);
					total_comment.setVisibility(View.GONE);
					comment_icon.setVisibility(View.GONE);
				}
			} else {
				FetchFactTask fft = new FetchFactTask(imagePagerActivity,
						total_like, total_comment, like_icon, comment_icon,
						like, comment);
				fft.execute(user.getTokenkey(), imageid[position]);
			}

			((ViewPager) view).addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}

		protected void onListItemClick(String liked, int pos) {

			if (liked == null || "null".equals(liked)) {
				like.setText(phraseManager.getPhrase(getApplicationContext(), "feed.unlike"));
                Log.d("psyh10", "TEXT: " + phraseManager.getPhrase(getApplicationContext(), "feed.unlike"));
				FeedisLike[pos] = "liked";
				total_like.setText(Integer.toString(Integer.parseInt(Total_like[pos]) + 1));
				Total_like[pos] = Integer.toString(Integer.parseInt(Total_like[pos]) + 1);
				new test().execute(Itemid[pos], Type[pos], null, "like");
                Log.d("psyh10", "like: " + like.getText());
			} else {
				like.setText(phraseManager.getPhrase(getApplicationContext(),"feed.like"));
				total_like.setText(Integer.toString(Integer.parseInt(Total_like[pos]) - 1));
				Total_like[pos] = Integer.toString(Integer.parseInt(Total_like[pos]) - 1);
				FeedisLike[pos] = "null";
                Log.d("psyh10", "TEXT: " + phraseManager.getPhrase(getApplicationContext(),"feed.like"));
				new test().execute(Itemid[pos], Type[pos], null, "unlike");
                Log.d("psyh10", "like: " + like.getText());
                like.setVisibility(View.GONE);
			}
		}

		public class test extends AsyncTask<String, Void, String> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected String doInBackground(String... params) {
				String likerequest = null;
				try {
					// Use BasicNameValuePair to create GET data
					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("token", user.getTokenkey()));

					if (("like").equals(params[3])) {
						pairs.add(new BasicNameValuePair("method", "accountapi.like"));
					} else {
						pairs.add(new BasicNameValuePair("method", "accountapi.unlike"));
					}

					if (params[1] != null) {
						pairs.add(new BasicNameValuePair("type", "" + params[1]));
						pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
					} else {
						pairs.add(new BasicNameValuePair("feed_id", "" + params[2]));
					}

					// url request
					String URL = Config.CORE_URL + Config.URL_API;
					// request GET method to server

					likerequest = networkUntil.makeHttpRequest(URL, "GET", pairs);

				} catch (Exception ex) {

				}
				return likerequest;
			}
		}

		protected void onCommentClick(int position) {
			imagePagerActivity.doShowCommentDetail(position, Type[position], Itemid[position], null);
		}

	}

	public void doShowCommentDetail(int position, String type_id,
			String item_id, String module_id) {

		CommentDetailFragment.newInstance(position, type_id, item_id, module_id, "").show(this);
	}

}