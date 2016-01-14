package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.Video;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.VideoPlay;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class VideoListFragment extends SherlockListFragment {

	private User user;
	private int page;
	private NetworkUntil networkUntil = new NetworkUntil();
	private VideoAdapter adapter = null;
	private int totalPage = 1;
	private int currentPos;
	
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;

	// Phrase manager
	private PhraseManager phraseManager;
	private ColorView colorView;
	private String name, itemId, list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
		user = (User) getActivity().getApplicationContext();
		page = 1;
		itemId = "0";
		list = "all";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// create view from friend_fragment xml
		View view = inflater.inflate(R.layout.music_fragment, container, false);
		// tab
		if (getArguments() != null) {
			itemId = getArguments().getString("itemId");
			name = getArguments().getString("name");
			list = getArguments().getString("list");
		}

		TextView moduleName = (TextView) view.findViewById(R.id.moduleName);
		if (name != null) {
			moduleName.setText(name);
		} else {
			moduleName.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "video.video"));
		}

		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.music_fragment_list);

		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				page = 1;
				adapter = new VideoAdapter(getActivity().getApplicationContext());
				new VideoTask().execute(page);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				++page;
				new VideoTask().execute(page);
			}

		});

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		currentPos = 0;
		actualListView = mPullRefreshListView.getRefreshableView();

		try {
			VideoTask mt = new VideoTask();
			mt.execute(page);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	Video video = null;

	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) {
		video = (Video) actualListView.getAdapter().getItem(position);
		if (video.getNotice() == null) {
			// init intent
			Intent intent = new Intent(this.getActivity(), VideoPlay.class);
			intent.putExtra("video", video);

			startActivity(intent);
		}
	}

	/**
	 * function get result from get method
	 * 
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET(int page) {
		String resultstring;

		if (adapter != null && adapter.getCount() == totalPage && page != 1) {
			return null;
		}

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getVideos"));
		pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
		pairs.add(new BasicNameValuePair("page", "" + page));
		pairs.add(new BasicNameValuePair("list", "" + list));
		pairs.add(new BasicNameValuePair("category", "" + itemId));

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
	public VideoAdapter getVideoAdapter(VideoAdapter madapter, String resString) {
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);

				Object intervention = mainJSON.get("output");
				// get api
				JSONObject total = mainJSON.getJSONObject("api");
				totalPage = Integer.parseInt(total.getString("total"));

				if (intervention instanceof JSONArray) {

					JSONArray outJson = (JSONArray) intervention;

					// get api

					JSONObject outputJson = null;
					Video item = null;

					for (int i = 0; i < outJson.length(); i++) {
						outputJson = outJson.getJSONObject(i);
						item = new Video();
						// set video id
						item = item.convertVideo(outputJson);

						madapter.add(item);
					}
				} else if (intervention instanceof JSONObject) {
					JSONObject outputJSON = mainJSON.getJSONObject("output");
					// if has notice
					if (!outputJSON.isNull("notice")) {
						Video mess = new Video();
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
	public class VideoTask extends AsyncTask<Integer, Void, String> {

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

			// get result from get method
			resultstring = getResultFromGET(params[0]);

			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					if (page == 1 || adapter == null) {
						adapter = new VideoAdapter(getActivity().getApplicationContext());
					}

					if (adapter != null) {
						adapter = getVideoAdapter(adapter, result);

						currentPos = getListView().getFirstVisiblePosition();
						actualListView.setAdapter(adapter);
						getListView().setSelectionFromTop(currentPos + 1, 0);

						adapter.notifyDataSetChanged();

					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			mPullRefreshListView.onRefreshComplete();
		}

	}

	/**
	 * Create browse adapter
	 * 
	 * @author Huy Nguyen
	 */
	public class VideoAdapter extends ArrayAdapter<Video> {
		public VideoAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Video item = getItem(position);
			VideoViewHolder holder = null;

			if (view == null) {
				int layout = R.layout.video_list_row;

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				// call element from xml

				TextView title = (TextView) view.findViewById(R.id.title);
				TextView full_name = (TextView) view.findViewById(R.id.full_name);
				TextView time_stamp = (TextView) view.findViewById(R.id.time_stamp);
				ImageView image_user = (ImageView) view.findViewById(R.id.image_user);
				TextView total_like = (TextView) view.findViewById(R.id.total_like);
				TextView total_comment = (TextView) view.findViewById(R.id.total_comment);
				TextView notice = (TextView) view.findViewById(R.id.notice);
				ImageView likeImg = (ImageView) view.findViewById(R.id.likes_feed_txt);
				ImageView commentImg = (ImageView) view.findViewById(R.id.comments_feed_txt);
				
				view.setTag(new VideoViewHolder(image_user, title, time_stamp,
						full_name, total_like, total_comment, notice, likeImg, commentImg));
			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof VideoViewHolder) {
					holder = (VideoViewHolder) tag;
				}
			}

			if (item != null && holder != null) {
				//change color
				colorView.changeColorLikeCommnent(holder.likeImg, holder.commentImg, user.getColor());
				
				// if has notice
				if (item.getNotice() != null) {
					view.findViewById(R.id.thumbnail).setVisibility(View.GONE);
					view.findViewById(R.id.content_view).setVisibility(View.GONE);
					// enable friend requests view
					view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
					colorView.changeColorText(holder.notice, user.getColor());
				}
				// set user image;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getImage_path())) {
						networkUntil.drawImageUrl(holder.imageHolder,item.getImage_path(), R.drawable.loading);
					}
				}
				// set song name;
				if (holder.title != null) {
					holder.title.setText(item.getTitle());
					colorView.changeColorText(holder.title, user.getColor());
				}
				// set short text
				if (holder.full_name != null) {
					holder.full_name.setText(item.getFull_name());
				}

				// set time stamp
				if (holder.time_stamp != null) {
					holder.time_stamp.setText(item.getTime_stamp());
				}
				// set total like
				if (holder.total_like != null) {
					holder.total_like.setText(String.valueOf(item.getTotal_like()));
					colorView.changeColorText(holder.total_like, user.getColor());
				}
				// set total comment
				if (holder.total_comment != null) {
					holder.total_comment.setText(String.valueOf(item.getTotal_comment()));
					colorView.changeColorText(holder.total_comment, user.getColor());
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
	public class VideoViewHolder {
		public final ImageView imageHolder;
		public final TextView title;
		public final TextView time_stamp;
		public final TextView full_name;
		public final TextView total_like;
		public final TextView total_comment;
		public final TextView notice;
		public final ImageView likeImg;
		public final ImageView commentImg;

		public VideoViewHolder(ImageView icon, TextView title,
				TextView full_name, TextView time_stamp, TextView total_like,
				TextView total_comment, TextView notice, ImageView likeImg, ImageView commentImg) {
			this.imageHolder = icon;
			this.title = title;
			this.time_stamp = time_stamp;
			this.total_like = total_like;
			this.total_comment = total_comment;
			this.notice = notice;
			this.full_name = full_name;
			this.likeImg = likeImg;
			this.commentImg = commentImg;
		}
	}

}
