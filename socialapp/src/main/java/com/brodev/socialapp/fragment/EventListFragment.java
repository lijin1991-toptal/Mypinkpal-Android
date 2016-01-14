package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Event;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.DashboardActivity;
import com.brodev.socialapp.view.EventDetailActivity;
import com.brodev.socialapp.view.FriendActivity;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.asyncTask.LikeAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

@SuppressLint("ValidFragment")
public class EventListFragment extends SherlockListFragment {

	private User user;
	private int page, clickedPos;
	private NetworkUntil networkUntil = new NetworkUntil();
	private EventAdapter adapter = null;
	private int totalPage, currentPos;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private ProgressBar loading;
	private PhraseManager phraseManager;
	private ColorView colorView;
	private String method = null;
	private String categoryId = null;
	private String categoryName = null;
	private TextView categoryHeader;
	Event event = null;
	
	public EventListFragment() {
	}
	
	public EventListFragment(String method, String categoryId, String categoryName) {
		this.method = method;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = (User) getActivity().getApplicationContext();
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
		page = 1;
		clickedPos = -1;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// create view from friend_fragment xml
		View view = inflater.inflate(R.layout.event_fragment, container, false);
		
		categoryHeader = (TextView) view.findViewById(R.id.event_name);
		
		//set header name
		if ("my".equals(method) && categoryName == null) {
			categoryHeader.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.my_events"));
		} else if ("friend".equals(method) && categoryName == null) {
			categoryHeader.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.friends_events"));
		} else if (categoryName != null) {
			categoryHeader.setText(categoryName);
		} else {
			categoryHeader.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.all_events"));
		}
		
		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.event_fragment_list);
		
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				page = 0;
				adapter = new EventAdapter(getActivity().getApplicationContext());
				new EventTask().execute(page);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				++page;						
				new EventTask().execute(page);
			}

		});
		
		loading = (ProgressBar) view.findViewById(R.id.content_loading);

		return view;
	}

	@Override
	public void onResume() {
		loading.setVisibility(View.GONE);
		super.onResume();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (getArguments() != null) {
			categoryId = getArguments().getString("categoryId");
			categoryName = getArguments().getString("name");
		}
		getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_UPDATE_RSVP));
		actualListView = mPullRefreshListView.getRefreshableView();

		try {
			EventTask mt = new EventTask();
			mt.execute(page);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Receiving message
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
				
			String eventId = intent.getExtras().getString("eventId");
			String rsvpId = intent.getExtras().getString("rsvpId");
			
			if (eventId != null && rsvpId != null && clickedPos != -1) {
				event = adapter.getItem(clickedPos);
				event.setRsvpId(Integer.parseInt(rsvpId));
				adapter.notifyDataSetChanged();
				//reset clicked position
				clickedPos = -1;
			}
			
		}
	};
	
	@Override
	public void onDestroy() {
		try{
			adapter = null;
			getActivity().unregisterReceiver(mHandleMessageReceiver);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		super.onDestroy();
	}
	
	
	/**
	 * function get result from get method
	 * 
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET(int page) {
		String resultstring = null;		
		if (adapter != null && adapter.getCount() == totalPage) {
			return null;
		}
		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getEventsV2"));
		pairs.add(new BasicNameValuePair("page", "" + page));
		
		if (method != null) {
			pairs.add(new BasicNameValuePair("view", method));
		}
		
		if (categoryId != null) {
			pairs.add(new BasicNameValuePair("category", categoryId));
		}

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
	public EventAdapter getEventAdapter(EventAdapter madapter, String resString) {
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);

				Object intervention = mainJSON.get("output");

				if (intervention instanceof JSONArray) {

					JSONArray outJson = (JSONArray) intervention;

					// get api
					JSONObject total = mainJSON.getJSONObject("api");
					totalPage = Integer.parseInt(total.getString("total"));

					JSONObject outputJson = null;
					Event item = null;

					for (int i = 0; i < outJson.length(); i++) {
						outputJson = outJson.getJSONObject(i);
						item = new Event();
						try {
							//set event id
							item.setEventId(outputJson.getString("event_id"));
							//set event Rsvp
							if (outputJson.has("rsvp_id") && !outputJson.isNull("rsvp_id") && !"".equals(outputJson.getString("rsvp_id"))) {
								item.setRsvpId(Integer.parseInt(outputJson.getString("rsvp_id")));	
							}
							//set event image
							item.setEventImage(outputJson.getString("image_path"));
							//set event title
							item.setTitle(Html.fromHtml(outputJson.getString("title")).toString());
							//set event start time
							item.setStartTime(Html.fromHtml(outputJson.getString("start_time_phrase")).toString());
							//set event start time
							item.setTimePhrase(Html.fromHtml(outputJson.getString("time_phrase")).toString());
							//set event user name
							item.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
							//set event user image
							item.setUserImage(outputJson.getString("user_image"));
							//set header
							if (outputJson.has("header_phrase") && !outputJson.isNull("header_phrase")) {
								item.setHeaderPhrase(Html.fromHtml(outputJson.getString("header_phrase")).toString());
							}
							//set is liked
							if (outputJson.has("is_liked") && !outputJson.isNull("is_liked")) {
								item.setLiked(true);
							} else {
								item.setLiked(false);
							}
							//set total like
							item.setTotalLike(Integer.parseInt(outputJson.getString("total_like")));
							//set can post comment
							item.setCanPostComment(outputJson.getBoolean("can_post_comment"));
							
							madapter.add(item);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} else if (intervention instanceof JSONObject) {
					JSONObject outputJSON = mainJSON.getJSONObject("output");
					// if has notice
					if (!outputJSON.isNull("notice")) {
						Event mess = new Event();
						mess.setNotice(Html.fromHtml(outputJSON.getString("notice")).toString());
						madapter.add(mess);
						return madapter;
					}

				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Event mess = new Event();
				mess.setNotice(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.no_events_found"));
				madapter.add(mess);
				return madapter;
			}
		}
		return madapter;
	}

	/**
	 * Get event list
	 */
	public class EventTask extends AsyncTask<Integer, Void, String> {

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
			try {
				// get result from get method
				resultstring = getResultFromGET(params[0]);
			} catch (Exception ex) {
				return resultstring;
			}
			
			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					// init friend adapter
					if (page == 1 || adapter == null) {
						adapter = new EventAdapter(getActivity().getApplicationContext());
					}
					
					adapter = getEventAdapter(adapter, result);

					if (adapter != null) {
						currentPos = getListView().getFirstVisiblePosition();
						actualListView.setAdapter(adapter);
						getListView().setSelectionFromTop(currentPos + 1, 0);

						adapter.notifyDataSetChanged();
						mPullRefreshListView.onRefreshComplete();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			mPullRefreshListView.onRefreshComplete();
		}

	}	

	/**
	 * Create event adapter
	 */
	public class EventAdapter extends ArrayAdapter<Event> {
		public EventAdapter(Context context) {
			super(context, 0);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final Event item = getItem(position);
			EventViewHolder holder = null;

			if (view == null) {
				int layout = R.layout.event_list_row;

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				
				// call element from xml
				LinearLayout rootLayout = (LinearLayout) view.findViewById(R.id.event_root_layout);
				ImageView eventImage = (ImageView) view.findViewById(R.id.event_image);
				TextView eventTitle = (TextView) view.findViewById(R.id.event_title);
				TextView eventTime = (TextView) view.findViewById(R.id.event_time);
				TextView eventName = (TextView) view.findViewById(R.id.event_fullname);
				ImageView eventUserImage = (ImageView) view.findViewById(R.id.event_user_image);
				TextView eventHeader = (TextView) view.findViewById(R.id.event_header);
				RelativeLayout eventHeaderLayout = (RelativeLayout) view.findViewById(R.id.event_header_layout);
				TextView eventRsvp = (TextView) view.findViewById(R.id.event_rsvp);
				TextView eventLike = (TextView) view.findViewById(R.id.event_like);
				TextView eventTotalLike = (TextView) view.findViewById(R.id.event_total_like);
				ImageView eventImageLike = (ImageView) view.findViewById(R.id.event_like_icon);
				TextView eventComment = (TextView) view.findViewById(R.id.event_comment);
				LinearLayout eventLikeTotalLayout = (LinearLayout) view.findViewById(R.id.event_total_like_layout);
				TextView eventTimePhrase = (TextView) view.findViewById(R.id.event_time_stamp);
				
				TextView notice = (TextView) view.findViewById(R.id.notice);

				view.setTag(new EventViewHolder(rootLayout, eventImage, eventTitle,
						eventTime, eventName, eventUserImage, eventHeader,
						eventHeaderLayout, eventRsvp, eventLike,
						eventTotalLike, eventImageLike, eventComment,
						eventTimePhrase, eventLikeTotalLayout, notice));
			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof EventViewHolder) {
					holder = (EventViewHolder) tag;
				}
			}

			if (item != null && holder != null) 
			{
				if (item.getNotice() != null) {
					view.findViewById(R.id.event_content).setVisibility(View.GONE);
					view.findViewById(R.id.event_notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
					colorView.changeColorText(holder.notice, user.getColor());
				} else {
					view.findViewById(R.id.event_content).setVisibility(View.VISIBLE);
					view.findViewById(R.id.event_notice_layout).setVisibility(View.GONE);
					//action click row
					holder.rootLayout.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(), EventDetailActivity.class);
							intent.putExtra("event_id", item.getEventId());
							getActivity().startActivity(intent);
						}
					});
				}
				
				//set event image
				if (holder.eventImage != null) {
					networkUntil.drawImageUrl(holder.eventImage, item.getEventImage(), R.drawable.loading);
				}
				
				if (holder.eventTitle != null) {
					holder.eventTitle.setText(item.getTitle());
					colorView.changeColorText(holder.eventTitle, user.getColor());
				}
				
				if (holder.eventTime != null) {
					holder.eventTime.setText(item.getStartTime());
				}
				
				if (holder.eventTimePhrase != null) {
					holder.eventTimePhrase.setText(item.getTimePhrase());
				}
				
				if (holder.eventName != null) {
					holder.eventName.setText(item.getFullname());
				}
				
				if (holder.eventUserImage != null) {
					networkUntil.drawImageUrl(holder.eventUserImage, item.getUserImage(), R.drawable.loading);
				}
				
				if (holder.eventHeader != null) {
					if (item.getHeaderPhrase() != null) {
						holder.eventHeaderLayout.setVisibility(View.VISIBLE);
						holder.eventHeader.setText(item.getHeaderPhrase());
					} else {
						holder.eventHeaderLayout.setVisibility(View.GONE);
					}
				}
				
				if (holder.eventRsvp != null) {
					String rsvp = null;
					holder.eventRsvp.setVisibility(View.VISIBLE);
					if (item.getRsvpId() == 1) {
						rsvp = phraseManager.getPhrase(getActivity().getApplicationContext(), "event.attending");
					} else if (item.getRsvpId() == 2) {
						rsvp = phraseManager.getPhrase(getActivity().getApplicationContext(), "event.maybe_attending");
					} else if (item.getRsvpId() == 3) {
						rsvp = phraseManager.getPhrase(getActivity().getApplicationContext(), "event.not_attending");
					} else {
						holder.eventRsvp.setVisibility(View.GONE);
					}
					
					if (rsvp != null) {
						holder.eventRsvp.setText(rsvp);	
					} 
				}
				
				if (holder.eventLike != null) {
					if (item.isLiked()) {
						holder.eventLike.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
					} else {
						holder.eventLike.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
					}
					colorView.changeColorText(holder.eventLike, user.getColor());
					colorView.changeColorLikeIcon(holder.eventImageLike, user.getColor());
					
					holder.eventLike.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							onClickLikeItem(position, item.isLiked());
						}
					});
				}
				
				if (holder.eventTotalLike != null) {
					holder.eventTotalLike.setText("" + item.getTotalLike());
					colorView.changeColorText(holder.eventTotalLike, user.getColor());
				}
				
				if (holder.eventComment != null) {
					if (item.isCanPostComment()) {
						holder.eventComment.setVisibility(View.VISIBLE);
						holder.eventComment.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.comment"));
						colorView.changeColorText(holder.eventComment, user.getColor());
					} else {
						holder.eventComment.setVisibility(View.GONE);
					}
				}
				
				//action click
				if (item.getNotice() == null) {
					holder.eventRsvp.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							onClickRsvpItem(item.getEventId(), item.getRsvpId());
							clickedPos = position;
						}
					});
				}
				
				if (item.getTotalLike() > 0) {
					holder.eventLikeTotalLayout.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(), FriendActivity.class);
							intent.putExtra("type", "event");
							intent.putExtra("item_id", item.getEventId());
							intent.putExtra("total_like", item.getTotalLike());
							getActivity().startActivity(intent);
						}
					});
				}
			}

			return view;
		}
		
		/**
		 * Action click update event rsvp 
		 * @param module
		 * @param item
		 */
		protected void onClickRsvpItem(String module, int item) {
			if (getActivity() == null)
				return;
			DashboardActivity activity = (DashboardActivity) getActivity();
			activity.showRSVPDialog(module, item);
		}
		
		protected void onClickLikeItem(int pos, boolean isLiked) {
			Event item = getItem(pos);
			if (!isLiked) {
				item.setLiked(true);
				item.setTotalLike(item.getTotalLike() + 1);
				new LikeAsyncTask(getActivity().getApplicationContext()).execute(user.getTokenkey(), item.getEventId(), "event", null, "like");
			} else {
				item.setLiked(false);
				if (item.getTotalLike() > 0) {
					item.setTotalLike(item.getTotalLike() - 1);	
				}
				new LikeAsyncTask(getActivity().getApplicationContext()).execute(user.getTokenkey(), item.getEventId(), "event", null, "unlike");
			}
			notifyDataSetChanged();
		}
	}

	public class EventViewHolder {
		public final LinearLayout rootLayout;
		public final ImageView eventImage;
		public final TextView eventTitle;
		public final TextView eventTime;
		public final TextView eventName;
		public final ImageView eventUserImage;
		public final TextView eventHeader;
		public final RelativeLayout eventHeaderLayout;
		public final TextView eventRsvp;
		public final TextView eventLike;
		public final TextView eventTotalLike;
		public final ImageView eventImageLike;
		public final TextView eventComment;
		public final TextView eventTimePhrase;
		public final LinearLayout eventLikeTotalLayout;
		public final TextView notice;

		public EventViewHolder(LinearLayout rootLayout, ImageView eventImage, TextView eventTitle,
				TextView eventTime, TextView eventName,
				ImageView eventUserImage, TextView eventHeader,
				RelativeLayout eventHeaderLayout, TextView eventRsvp,
				TextView eventLike, TextView eventTotalLike,
				ImageView eventImageLike, TextView eventComment,
				TextView eventTimePhrase, LinearLayout eventLikeTotalLayout, TextView notice) {
			this.rootLayout = rootLayout;
			this.eventImage = eventImage;
			this.eventTitle = eventTitle;
			this.eventTime = eventTime;
			this.eventName = eventName;
			this.eventUserImage = eventUserImage;
			this.eventHeader = eventHeader;
			this.eventHeaderLayout = eventHeaderLayout;
			this.eventRsvp = eventRsvp;
			this.eventLike = eventLike;
			this.eventTotalLike = eventTotalLike;
			this.eventImageLike = eventImageLike;
			this.eventComment = eventComment;
			this.eventTimePhrase = eventTimePhrase;
			this.eventLikeTotalLayout = eventLikeTotalLayout;
			this.notice = notice;
		}
	}
	
}
