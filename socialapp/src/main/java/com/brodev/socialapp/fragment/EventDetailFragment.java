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
package com.brodev.socialapp.fragment;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.manager.NextActivity;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Event;
import com.brodev.socialapp.entity.Feed;
import com.brodev.socialapp.entity.FeedMini;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.AlbumSelectedActivity;
import com.brodev.socialapp.view.EventDetailActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.brodev.socialapp.view.ImagePagerActivity;
import com.brodev.socialapp.view.ImageUpload;
import com.brodev.socialapp.view.ShareActivity;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public final class EventDetailFragment extends SherlockListFragment {
	
	private String eventId;
	private Event event;
	private User user;
	private PhraseManager phraseManager;
	private FeedAdapter fa = null;
	private PullToRefreshListView mPullRefreshListView;
	private int page, currentpos;
	private ListView actualListView;
	private static final String TIME = "time_phrase";
	private static final String FULLNAME = "full_name";
	private static final String ICON = "feed_icon";
	private static final String TITLE = "title_phrase_html";
	private static final String IMAGE = "feed_image";
	private static final String USERIMAGE = "user_image";	
	private RelativeLayout share_button, photo_button;
	private TextView statusTxt, photoTxt;
	private ImageView shareImage, photoImage;
	private ColorView colorView;
	private NetworkUntil networkUntil = new NetworkUntil();
	private boolean bNotice;
	private Button eventRSVP;
    private ImageGetter imageGetter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getActivity().getIntent().getExtras();
		page = 1;
		currentpos = 0;
		eventId = null;
		bNotice = false;
		event = new Event();
		user = (User) getActivity().getApplicationContext();
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
        this.imageGetter = new ImageGetter(getActivity().getApplicationContext());

		if (bundle != null) {
			if (getActivity().getIntent().hasExtra("event_id")) {
				eventId = bundle.getString("event_id");
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_UPDATE_RSVP));
		actualListView = mPullRefreshListView.getRefreshableView();

		try {
			new EventTask().execute(eventId);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.event_detail_view, container, false);
		
		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.event_fragment_list_detail);

		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				page = 1;
				new FeedLoadTask().execute(String.valueOf(page), event.getFeedCallBack());
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				++page;
				new FeedLoadTask().execute(String.valueOf(page), event.getFeedCallBack());
			}

		});
		
		return view;
	}

	/**
	 * Class request get event
	 */
	public class EventTask extends AsyncTask<String, Void, String> {
		String result = null;
		
		@Override
		protected String doInBackground(String... params) {
			if (isCancelled()) {
				return null;
			}
			try {
				// get result from get method
				result = getResultFromGET(params[0]);
			} catch (Exception ex) {
				return null;
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				if (result != null) {
					getEventAdapter(result);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			super.onPostExecute(result);
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
			
			if (eventId != null && rsvpId != null) {
				event.setRsvpId(Integer.parseInt(rsvpId));
				changeStatusButton();
			}
			
		}
	};
	
	@Override
	public void onDestroy() {
		try{
			fa = null;
			getActivity().unregisterReceiver(mHandleMessageReceiver);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		super.onDestroy();
	}
	
	/**
	 * Change button status
	 */
	private void changeStatusButton() {
		String rsvp = null;
		if (event.getRsvpId() == 1) {
			rsvp = phraseManager.getPhrase(getActivity().getApplicationContext(), "event.attending");
		} else if (event.getRsvpId() == 2) {
			rsvp = phraseManager.getPhrase(getActivity().getApplicationContext(), "event.maybe_attending");
		} else if (event.getRsvpId() == 3) {
			rsvp = phraseManager.getPhrase(getActivity().getApplicationContext(), "event.not_attending");
		} else {
			rsvp = phraseManager.getPhrase(getActivity().getApplicationContext(), "event.submit_your_rsvp");
		}
		if (rsvp != null) {
			eventRSVP.setText(rsvp);	
		} 
	}
	
	/**
	 * Load feed list of logged user
	 */
	public class FeedLoadTask extends AsyncTask<String, Void, String> {
		String result = null;
		
		@Override
		protected String doInBackground(String... params) {

			if (isCancelled()) {
				return null;
			}
			try {
				// get result from get method
				result = getResultFeedGet(params[0], params[1]);
			} catch (Exception ex) {
				return null;
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
                if (result != null) {
                    if (fa == null) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.event_header_detail, getListView(), false);

                        ImageView eventImage = (ImageView) header.findViewById(R.id.event_image);
                        networkUntil.drawImageUrl(eventImage, event.getEventImage(), R.drawable.loading);

                        TextView eventTitle = (TextView) header.findViewById(R.id.event_title);
                        eventTitle.setText(event.getTitle());
                        colorView.changeColorText(eventTitle, user.getColor());

                        TextView eventTimeTitle = (TextView) header.findViewById(R.id.event_time_title);
                        eventTimeTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.time"));

                        eventRSVP = (Button) header.findViewById(R.id.event_rsvp_detail);
                        changeStatusButton();

                        eventRSVP.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                if (getActivity() == null)
                                    return;
                                EventDetailActivity activity = (EventDetailActivity) getActivity();
                                activity.showRSVPDialog(event.getEventId(), event.getRsvpId());
                            }
                        });

                        TextView eventTime = (TextView) header.findViewById(R.id.event_time);
                        eventTime.setText(event.getStartTime());

                        if (event.getCategory() != null) {
                            header.findViewById(R.id.event_category_layout).setVisibility(View.VISIBLE);
                            TextView eventCategoryTitle = (TextView) header.findViewById(R.id.event_category_title);
                            eventCategoryTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.category"));

                            TextView eventCategory = (TextView) header.findViewById(R.id.event_category_detail);
                            eventCategory.setText(event.getCategory());
                        }

                        TextView eventLocationTitle = (TextView) header.findViewById(R.id.event_location_title);
                        eventLocationTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.location"));

                        TextView eventLocation = (TextView) header.findViewById(R.id.event_location);
                        eventLocation.setText(event.getLocation());

                        TextView eventCreateByTitle = (TextView) header.findViewById(R.id.event_create_by);
                        eventCreateByTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.created_by"));

                        TextView eventCreateName = (TextView) header.findViewById(R.id.event_create_name);
                        eventCreateName.setText(event.getFullname());

                        if (event.getMap() != null) {
                            ImageView mapView = (ImageView) header.findViewById(R.id.event_map);
                            mapView.setVisibility(View.VISIBLE);
                            networkUntil.drawImageUrl(mapView, event.getMap(), R.drawable.loading);
                        }

                        if (event.getDescription() != null) {
                            TextView eventDescription = (TextView) header.findViewById(R.id.event_description_detail);
                            eventDescription.setVisibility(View.VISIBLE);

                            // interesting part starts from here here:
                            Html.ImageGetter ig = imageGetter.create(0, event.getDescription(), eventDescription);
                            eventDescription.setTag(0);
                            eventDescription.setText(Html.fromHtml(event.getDescription(), ig, null));
                        }

                        getListView().addHeaderView(header, null, false);

                        statusTxt = (TextView) header.findViewById(R.id.statusTxt);
                        statusTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.status"));

                        photoTxt = (TextView) header.findViewById(R.id.photoTxt);
                        photoTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.photo"));

                        if (event.isCanPostComment()) {
                            shareImage = (ImageView) header.findViewById(R.id.image_icon_status);
                            photoImage = (ImageView) header.findViewById(R.id.image_icon_photo);

                            share_button = (RelativeLayout) header.findViewById(R.id.share_button);
                            photo_button = (RelativeLayout) header.findViewById(R.id.photo_button);

                            share_button.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    statusTxt.setTextColor(Color.parseColor("#ffffff"));
                                    colorView.changeColorAction(share_button, user.getColor());
                                    shareImage.setImageResource(R.drawable.status_white_icon);
                                    Intent intent = new Intent(getActivity(), ImageUpload.class);
                                    intent.putExtra("event_id", String.valueOf(event.getEventId()));
                                    getActivity().startActivity(intent);
                                }
                            });

                            photo_button.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    photoTxt.setTextColor(Color.parseColor("#ffffff"));
                                    colorView.changeColorAction(photo_button, user.getColor());
                                    photoImage.setImageResource(R.drawable.photo_white_icon);
                                    Intent intent = new Intent(getActivity(), AlbumSelectedActivity.class);
                                    intent.putExtra("event_id", String.valueOf(event.getEventId()));
                                    getActivity().startActivity(intent);
                                }
                            });
                        } else {
                            header.findViewById(R.id.user_profile_share_layout).setVisibility(View.GONE);
                        }
                    }

                    if (page == 1 || fa == null) {
                        fa = new FeedAdapter(getActivity());
                    }

                    fa = getFeedAdapter(fa, result);

                    if (fa != null) {
                        if (page == 1) {
                            actualListView.setAdapter(fa);
                        } else {
                            currentpos = getListView().getFirstVisiblePosition();
                            actualListView.setAdapter(fa);
                            getListView().setSelectionFromTop(currentpos + 1, 0);
                        }
                    }

                    fa.notifyDataSetChanged();
                }

				mPullRefreshListView.onRefreshComplete();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			super.onPostExecute(result);
		}
	}

	/**
	 * function get result from get method
	 * 
	 * @param eventId
	 * @return string result
	 */
	public String getResultFromGET(String eventId) {
		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getEventV2"));
		pairs.add(new BasicNameValuePair("event_id", eventId));

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
	 * Function create event adapter
	 */
	public void getEventAdapter(String resString) {

		try {
			
			JSONObject mainJSON = new JSONObject(resString);

			Object intervention = mainJSON.get("output");
			if (intervention instanceof JSONObject) 
			{
				JSONObject outputJson = (JSONObject) intervention;

				//set event id
				event.setEventId(outputJson.getString("event_id"));
				//set event Rsvp
				if (outputJson.has("rsvp_id") && !outputJson.isNull("rsvp_id")) {
					event.setRsvpId(Integer.parseInt(outputJson.getString("rsvp_id")));	
				}
				//set event image
				event.setEventImage(outputJson.getString("image_path"));
				//set event title
				event.setTitle(Html.fromHtml(outputJson.getString("title")).toString());
				//set event start time
				event.setStartTime(Html.fromHtml(outputJson.getString("event_date")).toString());
				//set category
				if (outputJson.has("categories") && !outputJson.isNull("categories")) {
					event.setCategory(Html.fromHtml(outputJson.getString("categories")).toString());
				}
				//set location
				if (outputJson.has("event_location") && !outputJson.isNull("event_location")) {
					event.setLocation(Html.fromHtml(outputJson.getString("event_location")).toString());
				}
				//set location
				if (outputJson.has("map_img") && !outputJson.isNull("map_img")) {
					event.setMap(Html.fromHtml(outputJson.getString("map_img")).toString());
				}
				//set description
				if (outputJson.has("description") && !outputJson.isNull("description")) {
					event.setDescription(outputJson.getString("description"));
				}
				//set full name
				event.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
				//set can post comment
				event.setCanPostComment(outputJson.getBoolean("can_post_comment"));
				//set feedback
				Object obj = outputJson.get("feed_callback");
				if (obj instanceof JSONObject) {
					event.setFeedCallBack(outputJson.getString("feed_callback"));
				}
				new FeedLoadTask().execute(String.valueOf(page), event.getFeedCallBack());
			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}
	
	/**
	 * get feed list of logged user
	 */
	public String getResultFeedGet(String page, String aFeedCallback) {

		String resultstring;
		String URL = null;
		if (Config.CORE_URL == null) {
			URL = Config.makeUrl(user.getCoreUrl(), "getFeed", true) + "&token=" + user.getTokenkey() + "&page=" + page;
		} else {
			URL = Config.makeUrl(Config.CORE_URL, "getFeed", true) + "&token=" + user.getTokenkey() + "&page=" + page;
		}
		
		JSONObject objCallback = null;
		try {
			objCallback = new JSONObject(aFeedCallback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("callback", "" + objCallback));
		
		resultstring = networkUntil.makeHttpRequest(URL, "POST", pairs);	

		return resultstring;
	}

	/**
	 * Function create feed adapter
	 * 
	 * @return feed Adapter
	 */
	public FeedAdapter getFeedAdapter(FeedAdapter fadapter, String resString) {		
		if (resString != null) {
			try {
				// init feed adapter

				JSONObject mainJSON = new JSONObject(resString);
				JSONArray outJson = mainJSON.getJSONArray("output");

				// if output json is null
				if (outJson.length() == 0 && page == 1) {
					Feed _feed = new Feed();
					_feed.setNo_share(true);
					_feed.setNotice(phraseManager.getPhrase(getActivity().getApplicationContext(),
							"feed.there_are_no_new_feeds_to_view_at_this_time"));
					fadapter.add(_feed);
				}

				JSONObject pagesObj = null;

				for (int i = 0; i < outJson.length(); i++) {
					pagesObj = outJson.getJSONObject(i);

					Feed objFeed = new Feed();
					objFeed.setFeedId(pagesObj.getString("feed_id"));
					if (pagesObj.has("item_id")) {
						objFeed.setItemId(pagesObj.getString("item_id"));
					}

					objFeed.setFullName(pagesObj.getString(FULLNAME));
					objFeed.setUserId(pagesObj.getString("user_id"));
					objFeed.setTime(pagesObj.getString(TIME));
					objFeed.setIcon(pagesObj.getString(ICON));
					if (pagesObj.has(TITLE)) {
						objFeed.setTitle(pagesObj.getString(TITLE));
					}
					
					objFeed.setUserImage(pagesObj.getString(USERIMAGE));

					if (pagesObj.has("no_share")) {
						objFeed.setNo_share(pagesObj.getBoolean("no_share"));
					} else {
						objFeed.setNo_share(false);
					}

					if (pagesObj.has("feed_title")) {
						objFeed.setTitleFeed(Html.fromHtml(pagesObj.getString("feed_title")).toString());
					}

					objFeed.setFeedLink(pagesObj.getString("feed_link"));
					if (pagesObj.has("parent_module_id") && !pagesObj.isNull("parent_module_id")) {
						objFeed.setModule(pagesObj.getString("parent_module_id"));
					}

					if (pagesObj.has("enable_like")) {
						if (!pagesObj.isNull("feed_is_liked") && pagesObj.getString("feed_is_liked") != "false") {
							objFeed.setFeedIsLiked("feed_is_liked");
						}
						objFeed.setEnableLike(pagesObj.getBoolean("enable_like"));
					} else {
						objFeed.setEnableLike(false);
					}

					if (pagesObj.has("can_post_comment")) {
						objFeed.setCanPostComment(pagesObj.getBoolean("can_post_comment"));
					} else {
						objFeed.setCanPostComment(false);
					}

					if (pagesObj.has("comment_type_id")) {
						objFeed.setComment_type_id(pagesObj.getString("comment_type_id"));
					}

					if (pagesObj.has("total_comment")) {
						objFeed.setTotalComment(pagesObj.getString("total_comment"));
					}

					if (pagesObj.has("profile_page_id")) {
						objFeed.setProfile_page_id(pagesObj.getString("profile_page_id"));
					}

					if (pagesObj.has("feed_total_like")) {
						objFeed.setHasLike(pagesObj.getString("feed_total_like"));
						objFeed.setTotalLike(Integer.parseInt(pagesObj.getString("feed_total_like")));
					}

					if (pagesObj.has("feed_status")) {
						objFeed.setStatus(pagesObj.getString("feed_status"));
					}

                    if (pagesObj.has("feed_status_html")) {
                        objFeed.setStatus(pagesObj.getString("feed_status_html"));
                    }

					// get more info for link...

					if (pagesObj.has("feed_title_extra")) {
						objFeed.setFeedTitleExtra(Html.fromHtml(pagesObj.getString("feed_title_extra")).toString());
					}

					if (pagesObj.has("feed_content")) {
						objFeed.setFeedContent(Html.fromHtml(pagesObj.getString("feed_content")).toString());
					}

					objFeed.setType(pagesObj.getJSONObject("social_app").getString("type_id"));

					if (pagesObj.has("can_share_item_on_feed")) {
						objFeed.setCan_share_item_on_feed(pagesObj.getBoolean("can_share_item_on_feed"));
					}

					if (pagesObj.has("like_type_id")) {
						objFeed.setLikeTypeId(pagesObj.getString("like_type_id"));
					}

					if (pagesObj.has("like_item_id")) {
						objFeed.setLikeItemId(pagesObj.getString("like_item_id"));
					}

					if (pagesObj.has("feed_link_share") && !pagesObj.isNull("feed_link_share"))
						objFeed.setShareFeedLink(Html.fromHtml(pagesObj.getString("feed_link_share")).toString());

					if (pagesObj.has("feed_link_share_url") && !pagesObj.isNull("feed_link_share_url"))
						objFeed.setShareFeedLinkUrl(Html.fromHtml(pagesObj.getString("feed_link_share_url")).toString());

					if (pagesObj.has("custom_data_cache")) {
						if (pagesObj.getJSONObject("custom_data_cache").has("thread_id") && !pagesObj.getJSONObject("custom_data_cache").isNull("thread_id"))
							objFeed.setDataCacheId(pagesObj.getJSONObject("custom_data_cache").getString("thread_id"));
					}

					if (pagesObj.has("social_app")) {
						JSONObject socialObj = pagesObj.getJSONObject("social_app");
						Object intervention = socialObj.get("link");

						if (intervention instanceof JSONObject) {
							JSONObject requestObj = socialObj.getJSONObject("link").getJSONObject("request");

							if (requestObj.has("page_id")) {
								objFeed.setPage_id_request(requestObj.getString("page_id"));
							} else if (requestObj.has("user_id")) {
								objFeed.setUser_id_request(requestObj.getString("user_id"));
							} else if (requestObj.has("photo_id")) {
								objFeed.setPhoto_id_request(requestObj.getString("photo_id"));
							}
						}
					}

					if (!pagesObj.isNull(IMAGE)) {

						ArrayList<String> Images_feed = new ArrayList<String>();

						for (int m = 0; m < pagesObj.getJSONArray(IMAGE).length(); m++) {
							Images_feed.add(pagesObj.getJSONArray(IMAGE).getString(m));
							if (m == 0) {
								objFeed.setImage1(pagesObj.getJSONArray(IMAGE).getString(m));
							} else if (m == 1) {
								objFeed.setImage2(pagesObj.getJSONArray(IMAGE).getString(m));
							} else if (m == 2) {
								objFeed.setImage3(pagesObj.getJSONArray(IMAGE).getString(m));
							} else if (m == 3) {
								objFeed.setImage4(pagesObj.getJSONArray(IMAGE).getString(m));
							}
							objFeed.setFeed_Image(Images_feed);
						}
					}

					if (pagesObj.has("photos_id") && !pagesObj.isNull("photos_id")) {

						ArrayList<String> Images_id = new ArrayList<String>();
						for (int s = 0; s < pagesObj.getJSONArray("photos_id").length(); s++) {
							Images_id.add(pagesObj.getJSONArray("photos_id").getString(s));
							if (s == 0) {
								objFeed.setImage_id_1(pagesObj.getJSONArray("photos_id").getString(s));
							} else if (s == 1) {
								objFeed.setImage_id_2(pagesObj.getJSONArray("photos_id").getString(s));
							} else if (s == 2) {
								objFeed.setImage_id_3(pagesObj.getJSONArray("photos_id").getString(s));
							} else if (s == 3) {
								objFeed.setImage_id_4(pagesObj.getJSONArray("photos_id").getString(s));
							}
						}
						objFeed.setImagesId(Images_id);
					}

					// if have share feed
					if (pagesObj.has("share_feed")) {
						Object intervention = pagesObj.get("share_feed");

						if (intervention instanceof JSONObject) {
							JSONObject shareObj = (JSONObject) intervention;
							FeedMini feedMini = new FeedMini();

							if (shareObj.has("full_name")) {
								feedMini.setFullname(shareObj.getString("full_name"));
							}

							if (shareObj.has("feed_info")) {
								feedMini.setFeedInfo(shareObj.getString("feed_info"));
							}

							if (shareObj.has("feed_status") && !shareObj.isNull("feed_status")
									&& !"".equals(shareObj.getString("feed_status"))) {
								feedMini.setFeedStatus(shareObj.getString("feed_status"));
							}

							if (shareObj.has("feed_title") && !shareObj.isNull("feed_title")
									&& !"".equals(shareObj.getString("feed_title"))) {
								feedMini.setFeedTitle(shareObj.getString("feed_title"));
							}

							if (shareObj.has("feed_image") && !shareObj.isNull("feed_image")
									&& !"".equals(shareObj.getString("feed_image"))) {
								feedMini.setFeedImage(shareObj.getString("feed_image"));
							}
							feedMini.setModule(pagesObj.getString("parent_module_id"));
							objFeed.setFeedMini(feedMini);

						}
					}

					fadapter.add(objFeed);

				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return fadapter;

	}

	/**
	 * Create feed browse adapter
	 */
	public class FeedAdapter extends ArrayAdapter<Feed> {
		
		FeedViewHolder holder;

		public FeedAdapter(Context context) {
			super(context, 0);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final Feed item = getItem(position);
			holder = null;

			if (view == null) {
				int layout = R.layout.feed_item;

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				// call element from xml

				ImageView iv = (ImageView) view.findViewById(R.id.grid_item_img);
				TextView Title = (TextView) view.findViewById(R.id.grid_item_fullname);
				TextView title_feed = (TextView) view.findViewById(R.id.grid_item_title);
                TextView feedStatus = (TextView) view.findViewById(R.id.grid_item_status);
				TextView Time = (TextView) view.findViewById(R.id.grid_item_time);
				ImageView icon = (ImageView) view.findViewById(R.id.grid_item_icon);
				ImageView feedimg_small = (ImageView) view.findViewById(R.id.grid_feedimg_small);
				ImageView feedimg = (ImageView) view.findViewById(R.id.grid_feedimg);
				ImageView feedimg1 = (ImageView) view.findViewById(R.id.grid_feedimg1);
				ImageView feedimg2 = (ImageView) view.findViewById(R.id.grid_feedimg2);
				ImageView feedimg3 = (ImageView) view.findViewById(R.id.grid_feedimg3);
				ImageView feedimg4 = (ImageView) view.findViewById(R.id.grid_feedimg4);
				TextView total_like = (TextView) view.findViewById(R.id.total_like);
				TextView total_comment = (TextView) view.findViewById(R.id.total_comment);
				ImageView like_icon = (ImageView) view.findViewById(R.id.grid_item_like_icon);
				ImageView comment_icon = (ImageView) view.findViewById(R.id.grid_item_comment_icon);
				LinearLayout like_view = (LinearLayout) view.findViewById(R.id.like_view_view);
				TextView like = (TextView) view.findViewById(R.id.like);
				TextView comment = (TextView) view.findViewById(R.id.comment);
				RelativeLayout like_view_like = (RelativeLayout) view.findViewById(R.id.like_view_like);
				RelativeLayout like_view_comment = (RelativeLayout) view.findViewById(R.id.like_view_comment);
				RelativeLayout like_view_share = (RelativeLayout) view.findViewById(R.id.like_view_share);
				TextView shareBtn = (TextView) view.findViewById(R.id.share);

				TextView notice = (TextView) view.findViewById(R.id.notice);

				// share feed item view
				RelativeLayout shareFeedView = (RelativeLayout) view.findViewById(R.id.share_feed_item_view);
				TextView titleFeedShare = (TextView) view.findViewById(R.id.share_feed_title_txt);
				TextView titleOfFeedShare = (TextView) view.findViewById(R.id.share_feed_title_of_feed_txt);
				TextView feedShareStatus = (TextView) view.findViewById(R.id.share_feed_status_txt);
				ImageView feedShareImage = (ImageView) view.findViewById(R.id.share_feed_image_view);

				// more info link feed
				TextView feedTitleExtra = (TextView) view.findViewById(R.id.grid_item_link_title_extra);
				TextView feedContent = (TextView) view.findViewById(R.id.grid_item_link_feed_content);

				// set phrase				
				comment.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.comment"));
				shareBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.share"));
				RelativeLayout feedSmallLayout = (RelativeLayout) view.findViewById(R.id.grid_feed_item_small_layout);

				View feedLikeView = (View) view.findViewById(R.id.feed_like_view);
				LinearLayout feedLikeCommentLayout = (LinearLayout) view.findViewById(R.id.feed_like_comment_layout);
				
				ImageView likeImg = (ImageView) view.findViewById(R.id.like_icon_img);
				ImageView commentImg = (ImageView) view.findViewById(R.id.total_comment_image);
				
				view.setTag(new FeedViewHolder(iv, Title, title_feed,
						feedStatus, Time, total_like, total_comment, like_icon,
						comment_icon, like_view, like, comment, icon,
						feedimg_small, feedimg, feedimg1, feedimg2, feedimg3,
						feedimg4, like_view_like, like_view_comment,
						like_view_share, notice, shareBtn, shareFeedView,
						titleFeedShare, titleOfFeedShare, feedShareStatus,
						feedShareImage, feedTitleExtra, feedContent,
						feedSmallLayout, feedLikeView, feedLikeCommentLayout,
						likeImg, commentImg));
			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof FeedViewHolder) {
					holder = (FeedViewHolder) tag;
				}
			}

			if (item != null && holder != null) {

				//change color
				colorView.changeColorLikeCommnent(holder.likeImg, holder.commentImg, user.getColor());
				
				if (item.getNotice() != null) {
					// invisible
					view.findViewById(R.id.feed_item_layout_all).setVisibility(View.GONE);
					// show notice
					view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
					bNotice = true;
					colorView.changeColorText(holder.notice, user.getColor());
				} else {
					view.findViewById(R.id.feed_item_layout_all).setVisibility(View.VISIBLE);
					view.findViewById(R.id.notice_layout).setVisibility(View.GONE);
				}

				// set share feed item if have
				if (item.getFeedMini() != null) {
					// visible share feed view
					holder.shareFeedItemView.setVisibility(View.VISIBLE);
					// set value to view
					holder.titleFeedShare.setText(item.getFeedMini()
							.getFullname()
							+ " "
							+ item.getFeedMini().getFeedInfo());
					// set title share feed if have
					if (item.getFeedMini().getFeedTitle() != null) {
						holder.titleOfFeedShare.setVisibility(View.VISIBLE);
						holder.titleOfFeedShare.setText(item.getFeedMini().getFeedTitle());
					} else {
						holder.titleOfFeedShare.setVisibility(View.GONE);
					}
					// set status of share feed if have
					if (item.getFeedMini().getFeedStatus() != null) {
						holder.feedShareStatus.setVisibility(View.VISIBLE);
						holder.feedShareStatus.setText(item.getFeedMini().getFeedStatus());
					} else {
						holder.feedShareStatus.setVisibility(View.GONE);
					}
					// set share feed image if have
					if (item.getFeedMini().getFeedImage() != null && item.getFeedMini().getModule().equals("photo")) {
						holder.shareFeedImage.setVisibility(View.VISIBLE);
						networkUntil.drawImageUrl(holder.shareFeedImage, item.getFeedMini().getFeedImage(), R.drawable.loading);
					} else {
						holder.shareFeedImage.setVisibility(View.GONE);
					}
				} else {
					holder.shareFeedItemView.setVisibility(View.GONE);
				}

				// set image user;
				if (holder.iv != null) {
					if (!"".equals(item.getUserImage())) {
						networkUntil.drawImageUrl(holder.iv,item.getUserImage(), R.drawable.loading);
						holder.iv.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (item.getProfile_page_id() != null
										&& !("0").equals(item.getProfile_page_id())) {
									Intent intent = new Intent(getActivity(), FriendTabsPager.class);
									intent.putExtra("page_id", item.getProfile_page_id());
									startActivity(intent);
								} else {
									Intent intent = new Intent(getActivity(), FriendTabsPager.class);
									intent.putExtra("user_id", item.getUserId());
									startActivity(intent);
								}
							}
						});
					}
				}

				// set share
				if (holder.like_view_share != null && item.getNo_share() != true) {
					holder.like_view_share.setVisibility(View.VISIBLE);
					holder.shareBtn.setVisibility(View.VISIBLE);
					holder.like_view_share.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(), ShareActivity.class);
							intent.putExtra("parent_feed_id", item.getItemId());
							intent.putExtra("parent_module_id", item.getType());
							intent.putExtra("can_share_item_on_feed", item.getCan_share_item_on_feed());
							intent.putExtra("user_id", item.getUserId());
							intent.putExtra("feed_link", item.getShareFeedLink());
							intent.putExtra("feed_link_url", item.getShareFeedLinkUrl());
							startActivity(intent);
						}
					});
				} else {
					holder.shareBtn.setVisibility(View.GONE);
					holder.like_view_share.setVisibility(View.GONE);
				}

				// set title;
				//set title;
				if (holder.Title != null && item.getTitle() != null) {
					holder.Title.setText(Html.fromHtml(item.getTitle()));
					if (item.getFeedMini() != null) {
						holder.Title.setText(Html.fromHtml(item.getTitle() + " " + phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.shared")));
					}
				}
				
				// set feed time
				if (holder.Time != null) {
					holder.Time.setText(item.getTime());
				}
				// set feed icon
				if (holder.icon != null) {
					if (!"".equals(item.getIcon())) {
						networkUntil.drawImageUrl(holder.icon, item.getIcon(), R.drawable.loading);
					}
				}
				// set title_feed;

				if (holder.feedStatus != null && item.getStatus() != null
						&& !("null").equals(item.getStatus()) && !item.getType().equals("blog")) {
					holder.feedStatus.setVisibility(View.VISIBLE);

                    // interesting part starts from here here:
                    Html.ImageGetter ig = imageGetter.create(position, item.getStatus(), holder.feedStatus);
                    holder.feedStatus.setTag(position);
                    holder.feedStatus.setText(Html.fromHtml(item.getStatus(), ig, null));
				} else {
					holder.feedStatus.setVisibility(View.GONE);
				}

				// set_total_like

				if (holder.total_like != null && item.getHasLike() != null && item.getEnableLike() != null && item.getEnableLike() != false) {
					holder.like.setVisibility(View.VISIBLE);					
					if (item.getFeedIsLiked() == null) {
						holder.like.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
					} else {
						holder.like.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
					}

					holder.like_view_like.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								onListItemClick(position, item.getFeedIsLiked());
							}
						});
					holder.like_view_like.setVisibility(View.VISIBLE);
					holder.total_like.setVisibility(View.VISIBLE);
					holder.like_icon.setVisibility(View.VISIBLE);
					holder.total_like.setText(Integer.toString(item.getTotalLike()));
					colorView.changeColorText(holder.total_like, user.getColor());
				} else {					
					holder.like_view_like.setVisibility(View.GONE);
					holder.total_like.setVisibility(View.GONE);
					holder.like_icon.setVisibility(View.GONE);
					holder.like.setVisibility(View.GONE);
				}

				// set_total_comment

				if (holder.total_comment != null && item.getTotalComment() != null && item.getCanPostComment() == true) {
					holder.comment.setVisibility(View.VISIBLE);
					holder.total_comment.setVisibility(View.VISIBLE);
					holder.comment_icon.setVisibility(View.VISIBLE);
					holder.like_view_comment.setVisibility(View.VISIBLE);					
					holder.total_comment.setText(item.getTotalComment());
					colorView.changeColorText(holder.total_comment, user.getColor());
					holder.like_view_comment.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if ("event".equals(item.getType())) {
								Intent intent = new Intent(getActivity(), ImageUpload.class);
								intent.putExtra("event_id", String.valueOf(item.getItemId()));
								startActivity(intent);
							} else {
								onCommentClick(position);
							}
						}
					});
				} else {
					
					holder.like_view_comment.setVisibility(View.GONE);
					holder.comment.setVisibility(View.GONE);
					holder.total_comment.setVisibility(View.GONE);
					holder.comment_icon.setVisibility(View.GONE);
				}
				
				if (("photo").equals(item.getType()) || ("photo_comment").equals(item.getType())) {
					holder.title_feed.setVisibility(View.GONE);
					holder.feedimg_small.setVisibility(View.GONE);
					holder.feedContent.setVisibility(View.GONE);
					holder.feedTitleExtra.setVisibility(View.GONE);
					holder.feedimg_small.setImageBitmap(null);
					if (item.getImage1() != null) {

						if (item.getImage2() != null) {

							if (item.getImage3() != null && item.getImage4() == null) {
								holder.feedimg1.setVisibility(View.VISIBLE);
								holder.feedimg2.setVisibility(View.VISIBLE);
								holder.feedimg3.setVisibility(View.VISIBLE);
								holder.feedimg4.setImageBitmap(null);
								holder.feedimg4.setVisibility(View.GONE);
								holder.feedimg.setImageBitmap(null);
								holder.feedimg.setVisibility(View.GONE);
								networkUntil.drawImageUrl(holder.feedimg1, item.getImage1(), R.drawable.loading);
								networkUntil.drawImageUrl(holder.feedimg2, item.getImage2(), R.drawable.loading);
								networkUntil.drawImageUrl(holder.feedimg3, item.getImage3(), R.drawable.loading);

								holder.feedimg1.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 0);
									}
								});

								holder.feedimg2.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 1);
									}
								});

								holder.feedimg3.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 2);
									}
								});

							} else if (item.getImage4() != null) {
								holder.feedimg1.setVisibility(View.VISIBLE);
								holder.feedimg2.setVisibility(View.VISIBLE);
								holder.feedimg3.setVisibility(View.VISIBLE);
								holder.feedimg4.setVisibility(View.VISIBLE);
								holder.feedimg.setImageBitmap(null);
								holder.feedimg.setVisibility(View.GONE);

								networkUntil.drawImageUrl(holder.feedimg1, item.getImage1(), R.drawable.loading);
								networkUntil.drawImageUrl(holder.feedimg2, item.getImage2(), R.drawable.loading);
								networkUntil.drawImageUrl(holder.feedimg3, item.getImage3(), R.drawable.loading);
								networkUntil.drawImageUrl(holder.feedimg4, item.getImage4(), R.drawable.loading);

								holder.feedimg1.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 0);
									}
								});

								holder.feedimg2.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 1);
									}
								});

								holder.feedimg3.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 2);
									}
								});

								holder.feedimg4.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 3);
									}
								});

							} else {
								holder.feedimg1.setVisibility(View.VISIBLE);
								holder.feedimg2.setVisibility(View.VISIBLE);
								holder.feedimg3.setImageBitmap(null);
								holder.feedimg4.setImageBitmap(null);
								holder.feedimg3.setVisibility(View.GONE);
								holder.feedimg4.setVisibility(View.GONE);
								holder.feedimg.setImageBitmap(null);
								holder.feedimg.setVisibility(View.GONE);
								networkUntil.drawImageUrl(holder.feedimg1, item.getImage1(), R.drawable.loading);
								networkUntil.drawImageUrl(holder.feedimg2, item.getImage2(), R.drawable.loading);

								holder.feedimg1.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 0);
									}
								});

								holder.feedimg2.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										onPhotoClick(position, 1);
									}
								});

							}

						} else {
							holder.feedimg.setVisibility(View.VISIBLE);
							holder.feedimg1.setVisibility(View.GONE);
							holder.feedimg2.setVisibility(View.GONE);
							holder.feedimg3.setVisibility(View.GONE);
							holder.feedimg4.setVisibility(View.GONE);
							holder.feedimg1.setImageBitmap(null);
							holder.feedimg2.setImageBitmap(null);
							holder.feedimg3.setImageBitmap(null);
							holder.feedimg4.setImageBitmap(null);

							networkUntil.drawImageUrl(holder.feedimg, item.getImage1(), R.drawable.loading);

							holder.feedimg.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									onPhotoClick(position, 4);
								}
							});

						}

					} else {
						holder.feedimg.setImageBitmap(null);
						holder.feedimg.setVisibility(View.GONE);
					}
				} else {
					holder.feedimg.setVisibility(View.GONE);
					holder.feedimg1.setVisibility(View.GONE);
					holder.feedimg2.setVisibility(View.GONE);
					holder.feedimg3.setVisibility(View.GONE);
					holder.feedimg4.setVisibility(View.GONE);
					holder.feedimg1.setImageBitmap(null);
					holder.feedimg2.setImageBitmap(null);
					holder.feedimg3.setImageBitmap(null);
					holder.feedimg4.setImageBitmap(null);
					holder.feedimg.setImageBitmap(null);

					if (holder.title_feed != null && item.getTitleFeed() != null && !("").equals(item.getTitleFeed())) {
						holder.title_feed.setVisibility(View.VISIBLE);
						holder.title_feed.setText(item.getTitleFeed());
						holder.feedSmallLayout.setVisibility(View.VISIBLE);
					} else {
						holder.title_feed.setVisibility(View.GONE);
					}

					// add text info
					if (holder.feedTitleExtra != null && item.getFeedTitleExtra() != null && !("").equals(item.getFeedTitleExtra())) {
						holder.feedTitleExtra.setVisibility(View.VISIBLE);
						holder.feedTitleExtra.setText(item.getFeedTitleExtra());
						holder.feedSmallLayout.setVisibility(View.VISIBLE);
					} else {
						holder.feedTitleExtra.setVisibility(View.GONE);
					}

					// add text info
					if (holder.feedContent != null && item.getFeedContent() != null && !("").equals(item.getFeedContent())) {
						holder.feedContent.setVisibility(View.VISIBLE);
						holder.feedContent.setText(item.getFeedContent());
						holder.feedSmallLayout.setVisibility(View.VISIBLE);
					} else {
						holder.feedContent.setVisibility(View.GONE);
					}

					if (holder.feedimg_small != null) {
						if (item.getImage1() != null && !"".equals(item.getUserImage())) {
							holder.feedimg_small.setVisibility(View.VISIBLE);
							networkUntil.drawImageUrl(holder.feedimg_small, item.getImage1(), R.drawable.loading);
							holder.feedSmallLayout.setVisibility(View.VISIBLE);
						} else {
							holder.feedimg_small.setVisibility(View.GONE);
							holder.feedimg_small.setImageBitmap(null);
						}
					}

					if ((item.getTitleFeed() == null || "".equals(item.getTitleFeed()))
							&& (item.getFeedContent() == null || ("").equals(item.getFeedContent()))
							&& (item.getImage1() == null || ("").equals(item.getUserImage()))) {
						holder.feedSmallLayout.setVisibility(View.GONE);
					}

					holder.feedSmallLayout.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (item.getPage_id_request() != null) {
								Intent intent = new Intent(getActivity(), FriendTabsPager.class);
								intent.putExtra("page_id", item.getPage_id_request());
								startActivity(intent);
							} else if (item.getUser_id_request() != null) {
								Intent intent = new Intent(getActivity(), FriendTabsPager.class);
								intent.putExtra("user_id", item.getUser_id_request());
								startActivity(intent);
							} else {
								if ("link".equals(item.getType())) {
			            			String url = item.getFeedLink();
			            			if (!url.startsWith("http://") && !url.startsWith("https://"))
			            				url = "http://" + url;
			            			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			            			startActivity(browserIntent);
			            		} else 
			            			new NextActivity(getActivity()).linkActivity(item.getType(), item.getItemId(), item.getDataCacheId(), item.getTitleFeed(), item.getFeedLink());
							}

						}
					});
				}

			}

			return view;
		}

		protected void onListItemClick(int position, String like) {
			Feed item1 = getItem(position);

			if (like == null) {

				item1.setFeedIsLiked("1");
				item1.setTotalLike(item1.getTotalLike() + 1);

				new likeRequest().execute(item1.getItemId(), item1.getType(), item1.getFeedId(), "like");
			} else {
				item1.setFeedIsLiked(null);
				item1.setTotalLike(item1.getTotalLike() - 1);
				new likeRequest().execute(item1.getItemId(), item1.getType(), item1.getFeedId(), "unlike");

			}

			notifyDataSetChanged();
		}

		protected void onCommentClick(int position) {
			Feed item2 = getItem(position);
			if (getActivity() == null)
				return;
			CommentDetailFragment.newInstance(position, item2.getFeedId(), null, "event", item2.getItemId()).show(getActivity());	
		}

		protected void onPhotoClick(int position, int photo_position) {
			String[] imagesId;
			String[] itemid = null;
			String[] imageHasLike = null;
			String[] imageFeedisLike = null;
			String[] imageTotal_like = null;
			String[] imageTotal_comment = null;
			String[] imageType = null;
			Feed item3 = getItem(position);

			Intent intent = new Intent(getActivity(), ImagePagerActivity.class);
			String[] imageUrls = item3.getFeed_Image().toArray(
					new String[item3.getFeed_Image().size()]);
			if (photo_position < 4) {
				imagesId = item3.getImagesId().toArray(new String[item3.getImagesId().size()]);
				intent.putExtra("position", photo_position);

			} else {
				ArrayList<String> Images_id = new ArrayList<String>();
				ArrayList<String> item_id = new ArrayList<String>();
				ArrayList<String> Has_Like = new ArrayList<String>();
				ArrayList<String> FeedisLiked = new ArrayList<String>();
				ArrayList<String> totallike = new ArrayList<String>();
				ArrayList<String> totalcomment = new ArrayList<String>();
				ArrayList<String> typeid = new ArrayList<String>();

				Images_id.add(item3.getPhoto_id_request());
				item_id.add(item3.getItemId());
				Has_Like.add(item3.getHasLike());
				if (item3.getHasLike() != null && item3.getEnableLike() != null && item3.getEnableLike() != false) {
					if (item3.getFeedIsLiked() == null) {
						FeedisLiked.add("null");
					} else {
						FeedisLiked.add(item3.getFeedIsLiked());
					}
				}
				totallike.add(Integer.toString(item3.getTotalLike()));

				if (item3.getTotalComment() != null) {
					totalcomment.add(item3.getTotalComment());
				}

				typeid.add(item3.getType());

				imagesId = Images_id.toArray(new String[Images_id.size()]);
				itemid = item_id.toArray(new String[item_id.size()]);
				imageHasLike = Has_Like.toArray(new String[Has_Like.size()]);
				imageFeedisLike = FeedisLiked.toArray(new String[FeedisLiked.size()]);
				imageTotal_like = totallike.toArray(new String[totallike.size()]);
				imageTotal_comment = totalcomment.toArray(new String[totalcomment.size()]);
				imageType = typeid.toArray(new String[typeid.size()]);
			}

			intent.putExtra("photo_id", imagesId);
			intent.putExtra("image", imageUrls);
			intent.putExtra("Itemid", itemid);
			intent.putExtra("HasLike", imageHasLike);
			intent.putExtra("FeedisLike", imageFeedisLike);
			intent.putExtra("Total_like", imageTotal_like);
			intent.putExtra("Total_comment", imageTotal_comment);
			intent.putExtra("Type", imageType);
			startActivity(intent);
		}

		public class likeRequest extends AsyncTask<String, Void, String> {
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
					String likerequest;
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
					String URL = null;
					if (Config.CORE_URL == null) {
						URL = Config.makeUrl(user.getCoreUrl(), null, false);	
					} else {
						URL = Config.makeUrl(Config.CORE_URL, null, false);
					}
					// request GET method to server

					likerequest = networkUntil.makeHttpRequest(URL, "GET", pairs);
					Log.i("like request", likerequest);
				} catch (Exception ex) {
					// Log.i(DEBUG_TAG, ex.getMessage());
				}
				return null;
			}

		}

	}

	/**
	 * Class feed view holder
	 */
	public class FeedViewHolder {
		public final ImageView iv;
		public final TextView Title;
		public final TextView title_feed;
		public final TextView feedStatus;
		public final TextView Time;

		// like view
		public final TextView total_like;
		public final TextView total_comment;
		public final ImageView like_icon;
		public final ImageView comment_icon;
		public final ImageView likeImg;
		public final ImageView commentImg;
		public final LinearLayout like_view;
		public final TextView like;
		public final TextView comment;

		// icon feed
		public final ImageView icon;
		// feed_small
		public final ImageView feedimg_small;
		// feed photo
		public final ImageView feedimg;
		public final ImageView feedimg1;
		public final ImageView feedimg2;
		public final ImageView feedimg3;
		public final ImageView feedimg4;

		// share
		public final RelativeLayout like_view_like;
		public final RelativeLayout like_view_comment;
		public final RelativeLayout like_view_share;
		public final RelativeLayout feedSmallLayout;
		public final TextView shareBtn;

		// notice
		public final TextView notice;
		// add info for link
		public final TextView feedTitleExtra;
		public final TextView feedContent;

		// share feed item view
		public final RelativeLayout shareFeedItemView;
		public final TextView titleFeedShare;
		public final TextView titleOfFeedShare;
		public final TextView feedShareStatus;
		public final ImageView shareFeedImage;
		public final View feedLikeView;
		public final LinearLayout feedLikeCommentView;

		public FeedViewHolder(ImageView iv, TextView title,
				TextView title_feed, TextView feedStatus, TextView time,
				TextView total_like, TextView total_comment,
				ImageView like_icon, ImageView comment_icon,
				LinearLayout like_view, TextView like, TextView comment,
				ImageView icon, ImageView feedimg_small, ImageView feedimg,
				ImageView feedimg1, ImageView feedimg2, ImageView feedimg3,
				ImageView feedimg4, RelativeLayout like_view_like,
				RelativeLayout like_view_comment,
				RelativeLayout like_view_share, TextView notice,
				TextView shareBtn, RelativeLayout shareFeedItemView,
				TextView titleFeedShare, TextView titleOfFeedShare,
				TextView feedShareStatus, ImageView shareFeedImage,
				TextView feedTitleExtra, TextView feedContent,
				RelativeLayout feedSmallLayout, View feedLikeView, 
				LinearLayout feedLikeCommentView, ImageView likeImg, ImageView commentImg) {
			super();
			this.iv = iv;
			this.Title = title;
			this.title_feed = title_feed;
			this.feedStatus = feedStatus;
			this.Time = time;
			this.total_like = total_like;
			this.total_comment = total_comment;
			this.like_icon = like_icon;
			this.comment_icon = comment_icon;
			this.like_view = like_view;
			this.like = like;
			this.comment = comment;
			this.icon = icon;
			this.feedimg_small = feedimg_small;
			this.feedimg = feedimg;
			this.feedimg1 = feedimg1;
			this.feedimg2 = feedimg2;
			this.feedimg3 = feedimg3;
			this.feedimg4 = feedimg4;
			this.like_view_like = like_view_like;
			this.like_view_comment = like_view_comment;
			this.like_view_share = like_view_share;
			this.notice = notice;
			this.shareBtn = shareBtn;
			this.shareFeedItemView = shareFeedItemView;
			this.titleFeedShare = titleFeedShare;
			this.titleOfFeedShare = titleOfFeedShare;
			this.feedShareStatus = feedShareStatus;
			this.shareFeedImage = shareFeedImage;
			this.feedTitleExtra = feedTitleExtra;
			this.feedContent = feedContent;
			this.feedSmallLayout = feedSmallLayout;
			this.feedLikeView = feedLikeView;
			this.feedLikeCommentView = feedLikeCommentView;
			this.likeImg = likeImg;
			this.commentImg = commentImg;
		}
	}
	
	@Override
	public void onResume() {

		if (fa != null && Config.feed.isContinueFeed()) {
			if (bNotice) {
				fa.clear();	
			}
			
			fa.insert(Config.feed, 0);
			fa.notifyDataSetChanged();
			bNotice = false;
			Config.feed.setContinueFeed(false);
		}
		if (photoTxt != null) {
			photoTxt.setTextColor(Color.parseColor("#797979"));				
			photo_button.setBackgroundColor(Color.parseColor("#f3f3f3"));				
			photoImage.setImageResource(R.drawable.photo_black_icon);
			
			statusTxt.setTextColor(Color.parseColor("#797979"));				
			share_button.setBackgroundColor(Color.parseColor("#f3f3f3"));				
			shareImage.setImageResource(R.drawable.status_black_icon);
		}
		super.onResume();
	}
}
