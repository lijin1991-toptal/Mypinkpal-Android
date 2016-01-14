package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Music;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.MusicPlaySong;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MusicListFragment extends SherlockListFragment  {
	
	private User user;
	private ProgressBar loading;
	private NetworkUntil networkUntil = new NetworkUntil();
	private MusicAdapter ma = null;
	private int page, currentPos, totalPage = 1;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private ColorView colorView;
	
	//Phrase manager
	private PhraseManager phraseManager;
	private String type, name, itemId, list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = (User) getActivity().getApplicationContext();
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
		page = 1;
		itemId = "0";
		list = "all";
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//create view from friend_fragment xml
		View view = inflater.inflate(R.layout.music_fragment, container, false);
		//tab
		if (getArguments() != null) {
			type = getArguments().getString("type");
			itemId = getArguments().getString("itemId");
			name = getArguments().getString("name");
			list = getArguments().getString("list");
		}
		
		TextView moduleName = (TextView) view.findViewById(R.id.moduleName);
		if (name != null) {
			moduleName.setText(name);
		} else {
			moduleName.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "music.music"));	
		}	
		
		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.music_fragment_list);
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				ma = new MusicAdapter(getActivity().getApplicationContext());
				page = 1;
				new MusicTask().execute(page);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				++page;
				new MusicTask().execute(page);
			}

		});
		loading = (ProgressBar) view.findViewById(R.id.content_loading); 
		
		return view;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		loading.setVisibility(View.GONE);
		super.onResume();
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		super.onActivityCreated(savedInstanceState);
		
		currentPos = 0;
		actualListView = mPullRefreshListView.getRefreshableView();		
		
		try {
			MusicTask mt = new MusicTask();
			mt.execute(page);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	Music music = null;
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) 
	{
		music = (Music) actualListView.getAdapter().getItem(position);
		
		//init intent
		if (music.getNotice() == null) {
			loading.setVisibility(View.VISIBLE);
			Intent intent = new Intent(this.getActivity(), MusicPlaySong.class);					
			intent.putExtra("song", music);	
			
			startActivity(intent);
		}
	}
	
	/**
	 * function get result from get method
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET(int page) 
	{
		String resultstring;
		
		if (ma != null && ma.getCount() == totalPage) {
			return null;
		}
		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getAllSongs"));
		pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
		pairs.add(new BasicNameValuePair("page", "" + page));
		pairs.add(new BasicNameValuePair("list", "" + list));
		pairs.add(new BasicNameValuePair("type", "" + type));
		pairs.add(new BasicNameValuePair("genre", "" + itemId));
		
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
	 * @return Music Adapter
	 */
	public MusicAdapter getMusicAdapter(MusicAdapter madapter, String resString) 
	{
		if (resString != null) 
		{	
			try {
				JSONObject mainJSON = new JSONObject(resString);
				
				Object intervention = mainJSON.get("output");
				
				if (intervention instanceof JSONArray) {
				
					JSONArray outJson = (JSONArray) intervention;
					
					// get api
					JSONObject total = mainJSON.getJSONObject("api");
					totalPage = Integer.parseInt(total.getString("total"));
					
					JSONObject outputJson = null;
					Music music = null;
					
					for (int i = 0; i < outJson.length(); i++) {
						outputJson = outJson.getJSONObject(i);
						music = new Music();
						//set song id
						music.setSong_id(outputJson.getString("song_id"));
						//set user id
						music.setUser_id(outputJson.getString("user_id"));
						//set title
						music.setTitle(Html.fromHtml(outputJson.getString("title")).toString());
						//set song path
						music.setSong_path(outputJson.getString("song_path"));
						//set user image
						music.setUser_image_path(outputJson.getString("user_image_path"));
						//set short text
						music.setShort_text(Html.fromHtml(outputJson.getString("short_text")).toString());
						//set total like
						music.setTotal_like(outputJson.getString("total_like"));
						//set total comment
						music.setTotal_comment(outputJson.getString("total_comment"));
						//set time stamp
						music.setTime_stamp(Html.fromHtml(outputJson.getString("time_stamp")).toString());
						
						//set is liked
						if (outputJson.has("is_liked") && !outputJson.isNull("is_liked")) {
							Object inte = outputJson.get("is_liked");
							if (inte instanceof String) 
								music.setLiked(true);
						}
							
						//set share
						if (outputJson.has("no_share")) 
							music.setShare(true);
						
						//set can post comment
						if (outputJson.has("can_post_comment"))
							music.setCanPostComment(outputJson.getBoolean("can_post_comment"));
						else 
							music.setCanPostComment(true);
						
						madapter.add(music);
					}
				} else if (intervention instanceof JSONObject) {
					JSONObject outputJSON = mainJSON.getJSONObject("output");
					//if has notice
					if (!outputJSON.isNull("notice")) {
						Music mess = new Music();
						mess.setNotice(outputJSON.getString("notice"));
						madapter.add(mess);
						return madapter;
					}
					
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return madapter;
	}
		
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
			
			//get result from get method
			resultstring = getResultFromGET(params[0]);
			
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					//init friend adapter
					if (page == 1 || ma == null) {
						ma = new MusicAdapter(getActivity());
					}
					ma = getMusicAdapter(ma, result);
					
					if (ma != null) {
						currentPos = getListView().getFirstVisiblePosition();
						actualListView.setAdapter(ma);
						getListView().setSelectionFromTop(currentPos + 1, 0);
						
						ma.notifyDataSetChanged();
						mPullRefreshListView.onRefreshComplete();
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		


	}
	
	/**
	 * Create music browse adapter
	 * @author Huy Nguyen
	 */
	public class MusicAdapter extends ArrayAdapter<Music> 
	{
		public MusicAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View view = convertView;
			Music item = getItem(position);
			MusicViewHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.music_list_row;
				
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				//call element from xml
				
				TextView title = (TextView) view.findViewById(R.id.title);				
				TextView short_text = (TextView) view.findViewById(R.id.short_text);
				TextView time_stamp = (TextView) view.findViewById(R.id.time_stamp);
				ImageView image_user = (ImageView) view.findViewById(R.id.image_user);
				TextView total_like = (TextView) view.findViewById(R.id.total_like);
				TextView total_comment = (TextView) view.findViewById(R.id.total_comment);
				TextView notice = (TextView) view.findViewById(R.id.notice);
				
				ImageView likeImg = (ImageView) view.findViewById(R.id.likes_feed_txt);
				ImageView commentImg = (ImageView) view.findViewById(R.id.comments_feed_txt);
				
				view.setTag(new MusicViewHolder(image_user, title, time_stamp,
						short_text, total_like, total_comment, notice, likeImg,
						commentImg));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof MusicViewHolder) {
					holder = (MusicViewHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				//change color
				colorView.changeColorLikeCommnent(holder.likeImg, holder.commentImg, user.getColor());
				
				//if has notice
				if (item.getNotice() != null) {
					view.findViewById(R.id.thumbnail).setVisibility(View.GONE);
					view.findViewById(R.id.content_view).setVisibility(View.GONE);
					//enable friend requests view
					view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
					colorView.changeColorText(holder.notice, user.getColor());
				}
				//set image friend;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getUser_image_path())) {
						
						networkUntil.drawImageUrl(holder.imageHolder, item.getUser_image_path(), R.drawable.loading);
					}
				}
				//set song name;
				if (holder.title != null) {
					holder.title.setText(item.getTitle());
					colorView.changeColorText(holder.title, user.getColor());
				}
				//set short text
				if (holder.short_text != null) {
					holder.short_text.setText(item.getShort_text());
				}
				//set time stamp
				if (holder.time_stamp != null) {
					holder.time_stamp.setText(item.getTime_stamp());
				}
				//set total like
				if (holder.total_like != null) {
					holder.total_like.setText(item.getTotal_like());
					colorView.changeColorText(holder.total_like, user.getColor());
				}
				//set total comment
				if (holder.total_comment != null) {
					holder.total_comment.setText(item.getTotal_comment());
					colorView.changeColorText(holder.total_comment, user.getColor());
				}				
				
			}
			
			return view;
		}
	}
	
	
	/**
	 * Class music view holder
	 * @author Huy Nguyen
	 */
	public class MusicViewHolder {
		public final ImageView imageHolder;
		public final TextView title;
		public final TextView time_stamp;
		public final TextView short_text;
		public final TextView total_like;
		public final TextView total_comment;
		public final TextView notice;
		public final ImageView likeImg;
		public final ImageView commentImg;

		public MusicViewHolder(ImageView icon, TextView title,
				TextView time_stamp, TextView short_text, TextView total_like,
				TextView total_comment, TextView notice, ImageView likeImg,
				ImageView commentmg) {
			this.imageHolder = icon;
			this.title = title;
			this.time_stamp = time_stamp;
			this.short_text = short_text;
			this.total_like = total_like;
			this.total_comment = total_comment;
			this.notice = notice;
			this.likeImg = likeImg;
			this.commentImg = commentmg;
		}
	}
	
}
