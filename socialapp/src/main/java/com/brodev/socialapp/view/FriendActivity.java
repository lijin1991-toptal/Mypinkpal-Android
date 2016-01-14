package com.brodev.socialapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends SherlockListActivity {

	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private FriendAdapter fa;
	private int page, totalPage, currentPos, rsvpId;
	private String user_id, type, item_id, feed_id, eventId;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private ColorView colorView;
	private boolean isProfile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.pages_fragment);
		currentPos = 0;
		page = 1;
		isProfile = false;
		totalPage = 1;
		colorView = new ColorView(getApplicationContext());
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (this.getIntent().hasExtra("user_id")) {
				user_id = bundle.getString("user_id");
				if (this.getIntent().hasExtra("is_profile")) {
					isProfile = bundle.getBoolean("is_profile");
				}
			} else if (this.getIntent().hasExtra("type")) {
				type = bundle.getString("type");
				if ("forum".equals(type)) {
					type = "forum_post";
				}
				item_id = bundle.getString("item_id");
				totalPage = bundle.getInt("total_like");
			} else if (this.getIntent().hasExtra("feed_id")) {
				feed_id = bundle.getString("feed_id");
			} else if (this.getIntent().hasExtra("event_id")) {
				eventId = bundle.getString("event_id");
				rsvpId = bundle.getInt("rsvp_id");
			}
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		user = (User) getApplicationContext();

		try {
			FriendTask ft = new FriendTask();
			ft.execute(page);
			
			mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.friend_fragment_list);

			mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

				@Override
				public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
					page = 1;
					new FriendTask().execute(page);
				}

				@Override
				public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
					++page;
					new FriendTask().execute(page);
				}

			});
			
			actualListView = mPullRefreshListView.getRefreshableView();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		super.onCreate(savedInstanceState);
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
	 * function get result from get method
	 * 
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET(int page) {
		String resultstring;

		if (fa != null && fa.getCount() >= totalPage) {
			return null;
		}

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		if (user_id != null) {
			pairs.add(new BasicNameValuePair("method", "accountapi.getFriends"));
			pairs.add(new BasicNameValuePair("user_id", user_id));
			if (isProfile) {
				pairs.add(new BasicNameValuePair("is_profile", String.valueOf(isProfile)));
			}
		} else if (type != null) {
			pairs.add(new BasicNameValuePair("method", "accountapi.getLikes"));
			pairs.add(new BasicNameValuePair("type", "" + type));
			pairs.add(new BasicNameValuePair("item_id", "" + item_id));
		} else if (feed_id != null) {
			pairs.add(new BasicNameValuePair("method", "accountapi.getLikes"));
			pairs.add(new BasicNameValuePair("feed_id", "" + feed_id));
		} else if (eventId != null) {
			pairs.add(new BasicNameValuePair("method", "accountapi.getEventMemberByRsvp"));
			pairs.add(new BasicNameValuePair("event_id", "" + eventId));
			pairs.add(new BasicNameValuePair("rsvp", "" + rsvpId));
		}

		pairs.add(new BasicNameValuePair("page", "" + page));

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
	 * Function create Friend adapter
	 * 
	 * @return Friend Adapter
	 */
	public FriendAdapter getFriendAdapter(FriendAdapter madapter, String resString) {
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);
				
				if (user_id != null || eventId != null) {
					totalPage = mainJSON.getJSONObject("api").getInt("total");	
				}
				
				Object intervention = mainJSON.get("output");
				
				if (intervention instanceof JSONArray) {

					JSONArray outJson = (JSONArray) intervention;
					JSONObject outputJson = null;
					Friend friend = null;

					for (int i = 0; i < outJson.length(); i++) {
						outputJson = outJson.getJSONObject(i);
						friend = new Friend();
						// set user id
						if (outputJson.has("user_id")) {
							friend.setUser_id(outputJson.getString("user_id"));
						}
                        //set user email
                        if (outputJson.has("email"))
                            friend.setEmail(outputJson.getString("email"));
						// set full name
						if (outputJson.has("full_name")) {
							friend.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
						}

						// set image
						if (outputJson.has("user_image_path")) {
							friend.setIcon(outputJson.getString("user_image_path"));
						}
						
						// set birthday phrase
						if (!outputJson.isNull("dob_setting")) {
							int dobSetting = Integer.parseInt(outputJson.getString("dob_setting"));
							if (dobSetting != 3 && dobSetting != 1) {
								if (dobSetting == 2) {
									friend.setBirthday(outputJson.getString("month") + " " + outputJson.getString("day"));
								} else {
									friend.setBirthday(outputJson.getString("month") + " " + outputJson.getString("day") + ", " + outputJson.getString("year"));
								}
							}
						}

						// set gender
						if (outputJson.has("gender_phrase")) {
							friend.setGender(outputJson.getString("gender_phrase"));
						}

						madapter.add(friend);

					}
				} else if (intervention instanceof JSONObject) {
					JSONObject output = (JSONObject) intervention;
					Friend f = new Friend();
					f.setNotice(Html.fromHtml(output.getString("notice")).toString());
					madapter.add(f);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return madapter;
	}

	public class FriendTask extends AsyncTask<Integer, Void, String> {

		String resultstring = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {

			if (isCancelled()) {
				return null;
			}
			// Simulates a background task
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			// get result from get method
			resultstring = getResultFromGET(params[0]);
			
			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					// init friend adapter
					if (page == 1 || fa == null) {
						fa = new FriendAdapter(getApplicationContext());
					}

					fa = getFriendAdapter(fa, result);

					if (fa != null) {
						currentPos = getListView().getFirstVisiblePosition();
						actualListView.setAdapter(fa);
						getListView().setSelectionFromTop(currentPos + 1, 0);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			fa.notifyDataSetChanged();
			mPullRefreshListView.onRefreshComplete();
		}

	}

	
	/**
	 * Create friend browse adapter
	 */
	public class FriendAdapter extends ArrayAdapter<Friend> 
	{
		public FriendAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View view = convertView;
			Friend item = getItem(position);
			FriendViewHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.friend_list_row;
				
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				//call element from xml
				ImageView icon = (ImageView) view.findViewById(R.id.friend_image_friend);
				TextView title = (TextView) view.findViewById(R.id.title);
				
				//notice 
				TextView notice = (TextView) view.findViewById(R.id.notice);
				
				view.setTag(new FriendViewHolder(icon, title, notice));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof FriendViewHolder) {
					holder = (FriendViewHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				//if has notice
				if (item.getNotice() != null) {
					view.findViewById(R.id.friend_image_friend).setVisibility(View.GONE);
					view.findViewById(R.id.friend_content_view).setVisibility(View.GONE);
					view.findViewById(R.id.user_online_layout).setVisibility(View.GONE);
					
					//enable friend requests view
					view.findViewById(R.id.friend_notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
					colorView.changeColorText(holder.notice, user.getColor());
				}
				
				//set image friend;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getIcon())) {
						networkUntil.drawImageUrl(holder.imageHolder, item.getIcon(), R.drawable.loading);
					}
				}
				//set full name;
				if (holder.title != null) {
					holder.title.setText(item.getFullname());
					colorView.changeColorText(holder.title, user.getColor());
				}
				
			}
			
			return view;
		}
	}
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) 
	{
		Friend friend = (Friend) actualListView.getAdapter().getItem(position);
        if (friend.getNotice() == null)
        {
            //init intent
            Intent intent = new Intent(this, FriendTabsPager.class);
            intent.putExtra("user_id", friend.getUser_id());
            startActivity(intent);
        }
	}
	
	/**
	 * Class friend view holder
	 */
	public class FriendViewHolder {
		public final ImageView imageHolder;
		public final TextView title;		
		public final TextView notice;

		public FriendViewHolder(ImageView icon, TextView title, TextView notice) {
			this.imageHolder = icon;
			this.title = title;			
			this.notice = notice;
		}
	}
}
