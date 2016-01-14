package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockListFragment;

import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.MusicAlbum;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.DashboardActivity;

import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.mypinkpal.app.Rightsidebar.MusicRightFragment;
//Sliding menu

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @company Brodev.com
 * @author Huy Nguyen
 */
public class MusicAlbumsFragment extends SherlockListFragment {

	User user;
	int page = 1;
	NetworkUntil networkUntil = new NetworkUntil();
	MusicAlbumAdapter ma;
	// pager
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private static final String DEBUG_TAG = "SOCIAL_APP";

	// Phrase manager
	private PhraseManager phraseManager;
	String type, name, itemId = "0";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// create view from friend_fragment xml
		View view = inflater.inflate(R.layout.music_album_fragment, container,
				false);
		// tab

		type = getArguments().getString("type");
		itemId = getArguments().getString("itemId");
		name = getArguments().getString("name");

		TextView moduleName = (TextView) view.findViewById(R.id.moduleName);
		if (name != null) {
			moduleName.setText(name);
		} else {
			moduleName.setText(phraseManager.getPhrase(getActivity()
					.getApplicationContext(), "music.music"));
		}

		mPullRefreshListView = (PullToRefreshListView) view
				.findViewById(R.id.album_fragment_list);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		// disable search menu item
		MenuItem search = menu.findItem(R.id.actionBar_chat);
		search.setVisible(false);

		inflater.inflate(R.menu.music, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		user = (User) getView().getContext().getApplicationContext();

		actualListView = mPullRefreshListView.getRefreshableView();

		try {
			MusicTask mt = new MusicTask();
			mt.execute(page);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	MusicAlbum music = null;
	/**
	 * Change fragment on dash board activity
	 * 
	 * @param fragment
	 */
	public void switchFragment(Fragment fragment, Fragment fragRight, int mode) {
		if (getActivity() == null)
			return;
		if (getActivity() instanceof DashboardActivity) {
			DashboardActivity fca = (DashboardActivity) getActivity();
			// set mode for sliding menu
			fca.setModeSliding(mode);
			fca.switchContent(fragment);
			if (fragRight != null) {
				fca.switchContentForRight(fragRight);
			}
		}
	}

	@Override
	public void onListItemClick(ListView listview, View view, int position,
			long id) {
		music = (MusicAlbum) actualListView.getAdapter().getItem(position);
		// init intent
		Fragment newContent = new MusicFragment();
		
		Fragment newContentRight = new com.mypinkpal.app.Rightsidebar.MusicRightFragment();
		
		int mode = Config.LEFT_RIGHT_SLIDING;
		Bundle bundle = new Bundle();
		bundle.putString("type", "album");
		bundle.putString("itemId", String.valueOf(music.getAlbum_id()));
		bundle.putString("name", music.getName());
		newContent.setArguments(bundle);
		switchFragment(newContent, newContentRight, mode);
	}

	/**
	 * function get result from get method
	 * 
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET(int page) {
		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getAlbums"));
		pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
		pairs.add(new BasicNameValuePair("page", "" + page));
		pairs.add(new BasicNameValuePair("type", "" + itemId));

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
	public MusicAlbumAdapter getMusicAlbumAdapter(MusicAlbumAdapter madapter, String resString) {
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);

				Object intervention = mainJSON.get("output");

				if (intervention instanceof JSONArray) {

					JSONArray outJson = (JSONArray) intervention;

					// get api

					JSONObject outputJson = null;
					MusicAlbum music = null;
					for (int i = 0; i < outJson.length(); i++) {
						outputJson = outJson.getJSONObject(i);	
						music = new MusicAlbum();	
						// set album id
						music.setAlbum_id(outputJson.getInt("album_id"));
						//set name
						music.setName(Html.fromHtml(
								outputJson.getString("name")).toString());
						//set text
						music.setText(Html.fromHtml(
								outputJson.getString("text")).toString());
						//set time phrase
						music.setTime_phrase(outputJson.getString("time_stamp"));
						//get album image
						music.setAlbum_image_path(outputJson.getString("album_image_path"));
						//get is like
						
						if (outputJson.isNull("is_liked")) {
						music.setIs_like(false);
						} else {
						music.setIs_like(outputJson.getBoolean("is_liked"));
						}
						//get total track
						music.setAlbum_total_track(outputJson.getInt("total_track"));
						//get full name
						music.setUser_full_name(outputJson.getString("full_name"));
						//set total comment
						music.setTotal_comment(outputJson.getInt("total_comment"));
						//set total like
						music.setTotal_like(outputJson.getInt("total_like"));
						
						madapter.add(music);
					}
				} else if (intervention instanceof JSONObject) {

					JSONObject outputJSON = mainJSON.getJSONObject("output");
					//if has notice
					if (!outputJSON.isNull("notice")) {
						MusicAlbum mess = new MusicAlbum();
						mess.setNotice(outputJSON.getString("notice"));
						madapter.add(mess);
						return madapter;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return madapter;
	}

	/**
	 * get friend list of logged user
	 * 
	 * @author Huy Nguyen
	 */
	public class MusicTask extends AsyncTask<Integer, Void, String> {

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
				if (ma != null) {
					ma = new MusicAlbumAdapter(getActivity());
				}
			} catch (InterruptedException e) {
			}

			// init friend adapter
			if (ma == null) {
				ma = new MusicAlbumAdapter(getActivity());
			}

			// get result from get method
			resultstring = getResultFromGET(params[0]);

			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					Log.i("MUSICALBUM", result);	
					ma = getMusicAlbumAdapter(ma, result);

					if (ma != null) {

						actualListView.setAdapter(ma);

						ma.notifyDataSetChanged();
						mPullRefreshListView.onRefreshComplete();
					}
				} catch (Exception ex) {
					Log.i(DEBUG_TAG, ex.getMessage());
				}
			}
		}

	}

	/**
	 * Create music browse adapter
	 * 
	 * @author Huy Nguyen
	 */
	public class MusicAlbumAdapter extends ArrayAdapter<MusicAlbum> {
		public MusicAlbumAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			MusicAlbum item = getItem(position);
			MusicViewHolder holder = null;

			if (view == null) {
				int layout = R.layout.music_list_row;

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				// call element from xml

				TextView title = (TextView) view.findViewById(R.id.title);
				TextView short_text = (TextView) view
						.findViewById(R.id.short_text);
				TextView time_stamp = (TextView) view
						.findViewById(R.id.time_stamp);
				ImageView image_user = (ImageView) view
						.findViewById(R.id.image_user);
				TextView total_like = (TextView) view
						.findViewById(R.id.total_like);
				TextView total_comment = (TextView) view
						.findViewById(R.id.total_comment);
				
				view.setTag(new MusicViewHolder(image_user, title, time_stamp,
						short_text, total_like, total_comment));
			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof MusicViewHolder) {
					holder = (MusicViewHolder) tag;
				}
			}

			if (item != null && holder != null) {

				// set image friend;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getAlbum_image_path())) {

						networkUntil.drawImageUrl(holder.imageHolder,
								item.getAlbum_image_path(), R.drawable.loading);
					}
				}
				// set song name;
				if (holder.title != null) {
					holder.title.setText(item.getName());
				}
				// set short text
				if (holder.short_text != null) {
					holder.short_text.setText(item.getUser_full_name());
				}
				// set time stamp
				if (holder.time_stamp != null) {
					holder.time_stamp.setText(item.getTime_phrase());
				}
				// set total like
				if (holder.total_like != null) {
					holder.total_like.setText(String.valueOf(item.getTotal_like()));
				}
				// set total comment
				if (holder.total_comment != null) {
					holder.total_comment.setText(String.valueOf(item.getTotal_comment()));
				}

			}

			return view;
		}
	}

	/**
	 * Class music view holder
	 * 
	 * @author Huy Nguyen
	 */
	public class MusicViewHolder {
		public final ImageView imageHolder;
		public final TextView title;
		public final TextView time_stamp;
		public final TextView short_text;
		public final TextView total_like;
		public final TextView total_comment;

		public MusicViewHolder(ImageView icon, TextView title,
				TextView time_stamp, TextView short_text, TextView total_like,
				TextView total_comment) {
			this.imageHolder = icon;
			this.title = title;
			this.time_stamp = time_stamp;
			this.short_text = short_text;
			this.total_like = total_like;
			this.total_comment = total_comment;
		}
	}

}
