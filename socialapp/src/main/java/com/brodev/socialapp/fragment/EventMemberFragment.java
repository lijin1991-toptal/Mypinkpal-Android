package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.FriendActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

public final class EventMemberFragment extends SherlockListFragment {

	private String URL, eventId;
	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private ColorView colorView;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private Friend friend;
	private MemberAdapter memberAdapter;
	private PhraseManager phraseManager;
	private int attendingLength, maybeAttendingLength, notAttendingLength;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		URL = null; eventId = null;
		attendingLength = 0;
		maybeAttendingLength = 0;
		notAttendingLength = 0;
		
		colorView = new ColorView(getActivity().getApplicationContext());
		user = (User) getActivity().getApplicationContext();
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		
		Bundle bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			if (getActivity().getIntent().hasExtra("event_id")) {
				eventId = bundle.getString("event_id");
			}
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.event_member_fragment	, container, false);
		
		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.event_member_fragment);
		
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				
				new GetMemberEvent().execute(eventId);
				
			}
		});
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		actualListView = mPullRefreshListView.getRefreshableView();
		try {
			new GetMemberEvent().execute(eventId);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * Add member to layout
	 * @param layout
	 * @param item
	 * @return
	 */
	public LinearLayout addMemberToLayout(LinearLayout layout, Friend item) {
		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	    View view;	    
		// Add the text layout to the parent layout
    	view = layoutInflater.inflate(R.layout.friend_list_row, layout, false);
    	LinearLayout viewLayout = (LinearLayout) view.findViewById(R.id.listFriend);
    	ImageView icon = (ImageView) view.findViewById(R.id.friend_image_friend);    	
    	if (item.getIcon() != null) {
    		networkUntil.drawImageUrl(icon, item.getIcon(), R.drawable.loading);
    	}
    	
    	TextView full_name = (TextView) view.findViewById(R.id.title);
    	full_name.setText(item.getFullname());    	
    	colorView.changeColorText(full_name, user.getColor());
		return viewLayout;
	}

	
	/**
	 * Request get member of event
	 */
	public class GetMemberEvent extends AsyncTask<String, Void, String> {

		String result = null;
		
		@Override
		protected String doInBackground(String... params) {
			if (isCancelled()) {
				return null;
			}
			
			try {
				URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method", "accountapi.getEventMemberV2"));
				pairs.add(new BasicNameValuePair("event_id", params[0]));
				
				// request GET method to server
				result = networkUntil.makeHttpRequest(URL, "GET", pairs);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					
					JSONObject mainJson = new JSONObject(result);
					JSONObject outputJson = mainJson.getJSONObject("output");
					
					JSONObject attendingJson = outputJson.getJSONObject("attending");
					attendingLength = attendingJson.getInt("length_attending");
					
					JSONObject maybeAttendingJson = outputJson.getJSONObject("maybe_attending");
					maybeAttendingLength = maybeAttendingJson.getInt("length_maybe_attending");
					
					JSONObject notAttendingJson = outputJson.getJSONObject("not_attending");
					notAttendingLength = notAttendingJson.getInt("length_not_attending");
					
					memberAdapter = new MemberAdapter(getActivity(), 
							Html.fromHtml(maybeAttendingJson.getString("phrase")).toString(),
							Html.fromHtml(notAttendingJson.getString("phrase")).toString(),
							Html.fromHtml(attendingJson.getString("phrase")).toString(),
							attendingJson, 
							attendingLength,
							maybeAttendingJson, 
							maybeAttendingLength,
							notAttendingJson, 
							notAttendingLength, 
							phraseManager.getPhrase(getActivity().getApplicationContext(), "event.view_guest_list")
					);
					
					actualListView.setAdapter(memberAdapter);
					
					// Call onRefreshComplete when the list has been refreshed.
					mPullRefreshListView.onRefreshComplete();
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
	}
	
	/**
	 * member adapter
	 */
	public class MemberAdapter extends BaseAdapter {
		LayoutInflater mInflater;
		String phrase, phraseMaybe, phraseNot, guestList;
		JSONObject json;
		JSONObject jsonMaybe;
		JSONObject jsonNot;
		Context mContext;
		int _attendingLength, _maybeAttendingLength, _notAttendingLength;
		
		public MemberAdapter(Context context, String phraseMaybe, String phraseNot,
				String phrase, JSONObject json, int attendingLength,
				JSONObject jsonMaybe, int maybeAttendingLength,
				JSONObject jsonNot, int notAttendingLength, String guestList) {
			this.mContext = context;
			this.phrase = phrase;
			this.phraseMaybe = phraseMaybe;
			this.json = json;
			this._attendingLength = attendingLength;
			this.jsonMaybe = jsonMaybe;
			this._maybeAttendingLength = maybeAttendingLength;
			this.phraseNot = phraseNot;
			this.jsonNot = jsonNot;
			this._notAttendingLength = notAttendingLength;
			this.guestList = guestList;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ViewHolder holder = null;
			  
	        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.event_member_list, null);
	            holder = new ViewHolder();
	            
	            holder.header = (TextView) convertView.findViewById(R.id.event_attending);
				holder.memberLayout = (LinearLayout) convertView.findViewById(R.id.event_attending_layout);
				holder.lstGuest = (TextView) convertView.findViewById(R.id.event_attending_guest_list);
				
				holder.headerMaybe = (TextView) convertView.findViewById(R.id.event_maybe_attending);
				holder.maybeMemberLayout = (LinearLayout) convertView.findViewById(R.id.event_maybe_attending_layout);
				holder.lstGuestMaybe = (TextView) convertView.findViewById(R.id.event_maybe_attending_guest_list);
				
				holder.headerNot = (TextView) convertView.findViewById(R.id.event_not_attending);
				holder.notMemberLayout = (LinearLayout) convertView.findViewById(R.id.event_not_attending_layout);
				holder.lstGuestNot = (TextView) convertView.findViewById(R.id.event_not_attending_guest_list);
				
	            convertView.setTag(holder);
	        } else {
	            holder = (ViewHolder) convertView.getTag();
	        }
	        
	        //set attending
	        holder.header.setText("" + _attendingLength + " " + phrase);
	        buildLayout(holder.memberLayout, json, _attendingLength);
	        if (_attendingLength > 0) {
	        	holder.lstGuest.setVisibility(View.VISIBLE);
	        	holder.lstGuest.setText(guestList);	
	        } else {
	        	holder.lstGuest.setVisibility(View.GONE);
	        }
	        
	        
	        holder.headerMaybe.setText("" + _maybeAttendingLength + " " + phraseMaybe);
	        buildLayout(holder.maybeMemberLayout, jsonMaybe, _maybeAttendingLength);
	        if (_maybeAttendingLength > 0) {
	        	holder.lstGuestMaybe.setVisibility(View.VISIBLE);
	        	holder.lstGuestMaybe.setText(guestList);	
	        } else {
	        	holder.lstGuestMaybe.setVisibility(View.GONE);
	        }
	        
	        holder.headerNot.setText("" + _notAttendingLength + " " + phraseNot);
	        buildLayout(holder.notMemberLayout, jsonNot, _notAttendingLength);
	        if (_notAttendingLength > 0) {
	        	holder.lstGuestNot.setVisibility(View.VISIBLE);
	        	holder.lstGuestNot.setText(guestList);	
	        } else {
	        	holder.lstGuestNot.setVisibility(View.GONE);
	        }
	        
	        //action click view more guest list
	        final Intent intent = new Intent(getActivity(), FriendActivity.class);
			intent.putExtra("event_id", eventId);
			
	        holder.lstGuest.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					intent.putExtra("rsvp_id", 1);
					getActivity().startActivity(intent);
				}
			});
	        
			holder.lstGuestMaybe.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					intent.putExtra("rsvp_id", 2);
					getActivity().startActivity(intent);
				}
			});
			
			holder.lstGuestNot.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					intent.putExtra("rsvp_id", 3);
					getActivity().startActivity(intent);
				}
			});
	        
	        return convertView;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}
	
	/**
	 * View holder
	 */
	public class ViewHolder {
		public TextView header;
		public TextView headerMaybe;
		public TextView headerNot;
		public LinearLayout memberLayout;
		public LinearLayout maybeMemberLayout;
		public LinearLayout notMemberLayout;
		public TextView lstGuest;
		public TextView lstGuestMaybe;
		public TextView lstGuestNot;
	}
	
	/**
	 * Build layout from json
	 * @param rootEventLayout
	 * @param eventLayout
	 * @param lstTv
	 * @param lstGuest
	 * @param json
	 * @param length
	 */
	private void buildLayout(LinearLayout eventLayout, JSONObject json, int length) 
	{
		try {
			eventLayout.removeAllViews();
			if (length > 0) {
				JSONArray aAttending = json.getJSONArray("value");
				JSONObject value = null;
				
				for (int i = 0; i < aAttending.length(); i++) {
					friend = new Friend();					
					value = aAttending.getJSONObject(i);
					friend.setFullname(Html.fromHtml(value.getString("full_name")).toString());
					friend.setIcon(value.getString("user_image"));
					friend.setUser_id(value.getString("user_id"));
					LinearLayout itemLayout = addMemberToLayout(eventLayout, friend);
					eventLayout.addView(itemLayout);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
}
