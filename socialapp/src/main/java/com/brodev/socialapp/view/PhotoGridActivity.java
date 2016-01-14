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
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.view.MenuItem;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.actionbarsherlock.app.SherlockActivity;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;

public final class PhotoGridActivity extends SherlockActivity {

	static final int MENU_SET_MODE = 0;

	private PullToRefreshGridView mPullRefreshGridView;
	private GridView mGridView;

	private String[] imageUrls;

	private String[] imagePager;

	private String[] imagePhotoIds;

	private String[] imageHasLike;

	private String[] imageFeedisLike;

	private String[] imageTotal_like;

	private String[] imageTotal_comment;

	private String[] imageItemid;

	private String[] imageType;

	private NetworkUntil networkUntil = new NetworkUntil();

	private int page;

	private ArrayList<String> stringArrayList;

	private ArrayList<String> PagerList;

	private ArrayList<String> ImagesId;

	private ArrayList<String> HasLike;

	private ArrayList<String> FeedisLike;

	private ArrayList<String> Total_like;

	private ArrayList<String> Total_comment;

	private ArrayList<String> Itemid;

	private ArrayList<String> Type;

	private int viewmore = 0;

	private ImageAdapter adapter;

	private String album_user_id;

	private int itemView = 20;

	private float countImage;

	private String user_id;

	private String page_id;

	private String module_id;

	private String group_id;

	private String album_id;
	
	private PhraseManager phraseManager; 	
	private TextView tv;
	private ProgressBar loading;
	
	int totalImages = 1;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ptr_grid);
		
		album_id = null;
		album_user_id = null;
		countImage = 1;
		page_id = null;
		module_id = null;
		group_id = null;
		page = 1;
		
		
		mPullRefreshGridView = (PullToRefreshGridView) findViewById(R.id.pull_refresh_grid);
		mGridView = mPullRefreshGridView.getRefreshableView();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		phraseManager = new PhraseManager(getApplicationContext());
		loading = (ProgressBar) findViewById(R.id.content_loading);
				
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (this.getIntent().hasExtra("album_name")) {
				if (this.getIntent().hasExtra("total_photo")) {
					getSupportActionBar().setTitle(bundle.getString("album_name") + "(" + bundle.getString("total_photo") + ")");
				} else {
					getSupportActionBar().setTitle(bundle.getString("album_name"));
				}
			}

			if (this.getIntent().hasExtra("user_id")) {
				user_id = bundle.getString("user_id");
				new ShowGridPhoto().execute(user_id, null);
				
			} else if (this.getIntent().hasExtra("album_user_id")) {
				album_user_id = bundle.getString("album_user_id");
				album_id = bundle.getString("album_id");
				new ShowGridPhoto().execute(album_user_id, album_id);
				
			} else if (this.getIntent().hasExtra("page_id")) {
				page_id = bundle.getString("page_id");
				new ShowGridPhoto().execute(page_id, null);
				
			} else if (this.getIntent().hasExtra("module_id")) {
				album_id = bundle.getString("album_id");
				module_id = bundle.getString("module_id");
				group_id = bundle.getString("group_id");
				new ShowGridPhoto().execute(group_id, album_id);
				
			} else {
				new ShowGridPhoto().execute(null, null);
			}

		}

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				stringArrayList = null;
				imageUrls = null;

				imagePager = null;

				imagePhotoIds = null;

				imageHasLike = null;

				imageFeedisLike = null;

				imageTotal_like = null;

				imageTotal_comment = null;

				imageItemid = null;

				imageType = null;
				page = 1;
				adapter = new ImageAdapter();
				totalImages = 1;
				
				if (album_user_id != null) {
					new ShowGridPhoto().execute(album_user_id, album_id);
				} else if (module_id != null) {
					new ShowGridPhoto().execute(group_id, album_id);
				} else if (page_id != null) {
					new ShowGridPhoto().execute(page_id, null);
				} else if (user_id != null) {
					new ShowGridPhoto().execute(user_id, null);
				} else {
					new ShowGridPhoto().execute(null, null);
				}
				mPullRefreshGridView.onRefreshComplete();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				
				if (album_user_id != null) {
					if (countImage < 2) {
						itemView = (int) (20 * (countImage));
					} else {
						countImage = countImage - 1;
						itemView = itemView + 20;
					}
					adapter.notifyDataSetChanged();
					mPullRefreshGridView.onRefreshComplete();
				} else {
					if (viewmore != 1) {
						page++;
						if (module_id != null) {
							new ShowGridPhoto().execute(group_id, album_id);
						} else if (page_id != null) {
							new ShowGridPhoto().execute(page_id, null);
						} else if (user_id != null) {
							new ShowGridPhoto().execute(user_id, null);
						} else {
							new ShowGridPhoto().execute(null, null);
						}
					} else {
						mPullRefreshGridView.onRefreshComplete();
					}
				}

			}

		});

		tv = new TextView(this);
		tv.setGravity(Gravity.CENTER);
		tv.setVisibility(View.GONE);				
		mPullRefreshGridView.setEmptyView(tv);

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

	public class ShowGridPhoto extends AsyncTask<String, Void, String> {
		User user = (User) getApplicationContext().getApplicationContext();
		String resultstring = null;
		JSONObject mainJSON = null;
		JSONArray outJson = null;
		JSONObject total = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			
			if (isCancelled()) {
				return null;
			}
			
			try {
				// Use BasicNameValuePair to create GET data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method","accountapi.getPhotos"));
				if (page > 1) {
					pairs.add(new BasicNameValuePair("page", "" + page));
				} else {
					pairs.add(new BasicNameValuePair("page", "undefined"));
				}

				if (params[0] != null) {
					if (user_id != null) {
						pairs.add(new BasicNameValuePair("user_id", "" + params[0]));
					} else if (page_id != null) {
						pairs.add(new BasicNameValuePair("module", "pages"));
						pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
					} else if (module_id != null) {
						pairs.add(new BasicNameValuePair("module", "pages"));
						pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
					}
					if (params[1] != null) {
						pairs.add(new BasicNameValuePair("album_id", "" + params[1]));
					}
				}
				
				// url request
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}
				
				resultstring = null;
				// request GET method to server
				
//				if (adapter.getCount() != totalImages) {
					resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
//				}
				
				if (resultstring != null) {
					JSONObject mainJSON = new JSONObject(resultstring);
					JSONArray outJson = mainJSON.getJSONArray("output");

					if (stringArrayList == null) {
						stringArrayList = new ArrayList<String>();
						PagerList = new ArrayList<String>();
						ImagesId = new ArrayList<String>();
						HasLike = new ArrayList<String>();
						FeedisLike = new ArrayList<String>();
						Total_like = new ArrayList<String>();
						Total_comment = new ArrayList<String>();
						Itemid = new ArrayList<String>();
						Type = new ArrayList<String>();
					}
					JSONObject total = mainJSON.getJSONObject("api");
					totalImages = Integer.parseInt(total.getString("total"));
					if (outJson.length() < 20) {
						viewmore = 1;
					} else {
						float leng = outJson.length();
						float itemv = itemView;
						countImage = leng / itemv;

					}

					for (int i = 0; i < outJson.length(); i++) {

						JSONObject JsonPic = outJson.getJSONObject(i);

						stringArrayList.add(JsonPic.getJSONObject("photo_sizes").getString("100"));

						ImagesId.add(JsonPic.getString("photo_id"));

						if (JsonPic.has("feed_total_like")) {
							HasLike.add(JsonPic.getString("feed_total_like"));
							Total_like.add(JsonPic.getString("feed_total_like"));
						}

						if (JsonPic.has("item_id")) {
							Itemid.add(JsonPic.getString("item_id"));
						}
						if (!JsonPic.isNull("feed_is_liked") && JsonPic.getString("feed_is_liked") != "false") {
							if (!"".equals(JsonPic.getString("feed_is_liked"))) {
								FeedisLike.add("feed_is_liked");
							} else {
								FeedisLike.add("null");
							}
						}
						if (JsonPic.has("total_comment")) {
							Total_comment.add(JsonPic.getString("total_comment"));
						}

						Type.add(JsonPic.getJSONObject("social_app").getString("type_id"));

						if (Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD) {
							PagerList.add(JsonPic.getJSONObject("photo_sizes").getString("240"));
						} else {
							PagerList.add(JsonPic.getJSONObject("photo_sizes").getString("500"));
						}

					}

					imagePhotoIds = ImagesId.toArray(new String[ImagesId.size()]);
					imageUrls = stringArrayList.toArray(new String[stringArrayList.size()]);
				
					imagePager = PagerList.toArray(new String[stringArrayList.size()]);

					imageHasLike = HasLike.toArray(new String[HasLike.size()]);

					imageFeedisLike = FeedisLike.toArray(new String[FeedisLike.size()]);

					imageTotal_like = Total_like.toArray(new String[Total_like.size()]);

					imageTotal_comment = Total_comment.toArray(new String[Total_comment.size()]);

					imageItemid = Itemid.toArray(new String[Itemid.size()]);

					imageType = Type.toArray(new String[Type.size()]);

					imageType = Type.toArray(new String[Type.size()]);
				}

				

			} catch (Exception ex) {
				// Log.i("comment item", ex.getMessage());
			}
			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {

				if (adapter == null) {
					adapter = new ImageAdapter();
				}

				if (adapter != null) {
					mGridView.setAdapter(adapter);
				}

				mGridView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
						startImagePagerActivity(position);
					}
				});

				adapter.notifyDataSetChanged();

				// Call onRefreshComplete when the list has been refreshed.
				mPullRefreshGridView.onRefreshComplete();

			}
			loading.setVisibility(View.GONE);
			

		}

	}

	public class ImageAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			if (imageUrls == null) {		
				tv.setText(phraseManager.getPhrase(getApplicationContext(), "photo.no_photos_found"));
				tv.setVisibility(View.VISIBLE);
				return 0;
			}
			
			if (album_user_id != null && viewmore == 0)
				return itemView;
			return imageUrls.length;

		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ImageView imageView;
			if (convertView == null) {
				imageView = (ImageView) getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
			} else {
				imageView = (ImageView) convertView;
			}
			networkUntil.drawImageUrl(imageView, imageUrls[position], R.drawable.loading);

			return imageView;
		}
	}

	private void startImagePagerActivity(int position) {
		Intent intent = new Intent(this, ImagePagerActivity.class);
		intent.putExtra("image", imagePager);
		intent.putExtra("photo_id", imagePhotoIds);
		intent.putExtra("HasLike", imageHasLike);
		intent.putExtra("FeedisLike", imageFeedisLike);
		intent.putExtra("Total_like", imageTotal_like);
		intent.putExtra("Total_comment", imageTotal_comment);
		intent.putExtra("Itemid", imageItemid);
		intent.putExtra("Type", imageType);

		intent.putExtra("position", position);
		startActivity(intent);
	}

}
