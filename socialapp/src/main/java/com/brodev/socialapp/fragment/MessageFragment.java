package com.brodev.socialapp.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Message;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.ChatActivity;
import com.brodev.socialapp.view.ComposeActivity;
import com.brodev.socialapp.view.ConversationActivity;
import com.brodev.socialapp.view.DashboardActivity;
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

/**
 * Class message fragment
 * @author ducpham
 */
public class MessageFragment extends SherlockListFragment {
	
	private NetworkUntil network = new NetworkUntil();
	private MessageAdapter ma =  null;
	private int page, total, currentPos, clickedPos;
	private User user;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private PhraseManager phraseManager;
	private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private ImageGetter imageGetter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		colorView = new ColorView(getActivity().getApplicationContext());
		user = (User) getActivity().getApplicationContext();
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
		super.onCreate(savedInstanceState);
	}
		
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		//disable search menu item
		MenuItem search = menu.findItem(R.id.actionBar_chat);
		search.setVisible(false);
		
		inflater.inflate(R.menu.message, menu);
		 
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	 	case R.id.action_compose:
	 		if (user.getChatKey() == null) {
		 		//Go to compose activity
		 		Intent intent = new Intent(getActivity(), ComposeActivity.class);
		 		
				getActivity().startActivity(intent);
	 		} else {
	 			DashboardActivity activity = (DashboardActivity) getActivity();
	 			activity.showRightBar();
	 		}
		break; 
		}
		return false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		page = total = 1; 
		currentPos = 0;
		clickedPos = -1;
		actualListView = mPullRefreshListView.getRefreshableView();
		
		TextView view = new TextView(getActivity().getApplicationContext());
		view.setLines(1);
		actualListView.addFooterView(view, null, true);
		
		getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_CHAT_ACTION));
		
		try {
            loadMessage();
		} catch(Exception ex) {
		}
		super.onActivityCreated(savedInstanceState);
	}

    private void loadMessage() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                mPullRefreshListView.setVisibility(View.VISIBLE);

                //fetch data
                new MessageLoadMoreTask().execute(page);
            } else {
                // display error
                mPullRefreshListView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mPullRefreshListView.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param message
	 */
	public static void displayMessage(Context context, String type, String userId, String message) {
        Intent intent = new Intent(Config.DISPLAY_CHAT_ACTION);
        intent.putExtra("type", type);
        
        intent.putExtra("userId", userId);
        intent.putExtra("message", message);
        
        context.sendBroadcast(intent);
    }
	
	/**
	 * 
	 * @param context
	 * @param isRead
	 */
	public static void displayMessageRead(Context context, boolean isRead) {
        Intent intent = new Intent(Config.DISPLAY_CHAT_ACTION_READ);
        intent.putExtra("read", true);
        
        context.sendBroadcast(intent);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//create view from friend_fragment xml
		View view = inflater.inflate(R.layout.message_fragment, container, false);
	
		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.message_fragment_list);

		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				page = 1;
				//call message refresh task to execute
				new MessageLoadMoreTask().execute(page);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				++page;
				//set null list
				new MessageLoadMoreTask().execute(page);
			}
		});

        //no internet connection
        noInternetLayout = (RelativeLayout) view.findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) view.findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) view.findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) view.findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) view.findViewById(R.id.no_internet_image);

        //change color for no internet
        colorView.changeImageForNoInternet(noInternetImg, noInternetBtn, user.getColor());

        //set text for no internet element
        noInternetBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.try_again"));
        noInternetTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_title"));
        noInternetContent.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_content"));

        //action click load try again
        noInternetBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //fetch data
                        loadMessage();
                    }
                }, 1000);
            }
        });

		return view;
	}

	/**
	 * function get result from get method
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET(int page) 
	{
		String resultstring;
		
		Log.e("ADAPTER COUNT", String.valueOf(ma.getCount()));
		
		if(ma.getCount() >= total && page != 1) {
			return null;
		}
		
		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getEmail"));
		pairs.add(new BasicNameValuePair("page", "" + page));
		
		// url request
		String URL = null;
		if (Config.CORE_URL == null) {
			URL = Config.makeUrl(user.getCoreUrl(), null, false);	
		} else {
			URL = Config.makeUrl(Config.CORE_URL, null, false);
		}
		
		// request GET method to server
		resultstring = network.makeHttpRequest(URL, "GET", pairs);
					
		return resultstring;
	}
	
	/**
	 * Function create message adapter
	 * @return Message Adapter
	 */
	public MessageAdapter getMessAdapter(MessageAdapter madapter, String resString) 
	{
		if (resString != null) 
		{	
			try {
				JSONObject mainJSON = new JSONObject(resString);
				
				//get api JSON
				JSONObject apiJSON = mainJSON.getJSONObject("api");
				total = Integer.parseInt(apiJSON.getString("total"));
				
				//get output JSON
				JSONObject outputJSON = mainJSON.getJSONObject("output");
				
				//if has notice
				if (!outputJSON.isNull("notice")) {
					Message mess = new Message();
					mess.setNotice(outputJSON.getString("notice"));
					madapter.add(mess);
					return madapter;
				}
				
				//get messages JSON ARRAY
				JSONArray messagesJSON = outputJSON.getJSONArray("emails");
				
				JSONObject json = null; 
				Message message = null;
				
				for (int i = 0; i < messagesJSON.length(); i++) {
					message = new Message();
					json = messagesJSON.getJSONObject(i);
					//set thread id;
					if (json.has("thread_id") && !json.isNull("thread_id"))
						message.setThreadId(Integer.parseInt(json.getString("thread_id")));
					//set content message;
                    if (user.getChatKey() == null) {
                        if (json.has("preview_html") && !json.isNull("preview_html"))
                            message.setPreview(json.getString("preview_html"));
                    } else {
                        if (json.has("preview") && !json.isNull("preview"))
                            message.setPreview(json.getString("preview"));
                    }

					//set time phrase;
					if (json.has("time_phrase") && !json.isNull("time_phrase"))
						message.setTimephrase(json.getString("time_phrase"));
					//set to user Id;
					if (json.has("user_id") && !json.isNull("user_id"))
						message.setToUserId(Integer.parseInt(json.getString("user_id")));
					//set full name;
					if (json.has("full_name") && !json.isNull("full_name"))
						message.setFullname(json.getString("full_name"));
					//set user image 
					if (json.has("user_image_path") && !json.isNull("user_image_path"))
						message.setUserImage(json.getString("user_image_path"));
					//read unread
					if (json.has("isRead") && !json.isNull("isRead")) 
						message.setRead(json.getBoolean("isRead"));
					//block conversation
					if (json.has("isBlock") && !json.isNull("isBlock"))
						message.setBlock(json.getBoolean("isBlock"));
					
					//add message to message adapter
					madapter.add(message);
				}
			} catch(Exception ex) {
                mPullRefreshListView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
				return null;
			}
		}
		return madapter;
		
	}
	
	/**
	 * Load more message list of logged user 
	 */
	public class MessageLoadMoreTask extends AsyncTask<Integer, Void, String> 
	{
		String result = null;
		
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
				
				if (ma == null || page == 1) {
					ma = new MessageAdapter(getActivity());
				}

				//get result from get method
				result = getResultFromGET(params[0]);
			} catch (Exception e) {
			}

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			try {
                if (result != null) {
                    ma = getMessAdapter(ma, result);

                    if (ma != null) {
                        currentPos = getListView().getFirstVisiblePosition();
                        actualListView.setAdapter(ma);

                        if (page != 1)
                            getListView().setSelectionFromTop(currentPos + 1, 0);

                        ma.notifyDataSetChanged();

                    }
                }
                mPullRefreshListView.onRefreshComplete();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
			super.onPostExecute(result);
		}
	}
	
	/**
	 * Class message view holder
	 */
	public class MessageViewHolder {
		public final RelativeLayout messageView; 
		public final ImageView imageHolder;
		public final TextView title;
		public final TextView preview;
		public final TextView time;
		public final TextView notice;

		public MessageViewHolder(RelativeLayout messageView, ImageView icon,
				TextView title, TextView preview, TextView time, TextView notice) {
			this.messageView = messageView;
			this.imageHolder = icon;
			this.title = title;
			this.preview = preview;
			this.time = time;
			this.notice = notice;
		}
	}
	
	/**
	 * Class Email Adapter
	 */
	public class MessageAdapter extends ArrayAdapter<Message> 
	{
		public MessageAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent) 
		{
			View view = convertView;
			final Message item = getItem(position);
			MessageViewHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.message_list_row;
				
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				//call element from xml
				RelativeLayout messageView = (RelativeLayout) view.findViewById(R.id.message_view);
				ImageView icon = (ImageView) view.findViewById(R.id.user_image);
				TextView title = (TextView) view.findViewById(R.id.title);
                TextView preview = (TextView) view.findViewById(R.id.message_preview);
				TextView time = (TextView) view.findViewById(R.id.time);
				
				TextView notice = (TextView) view.findViewById(R.id.notice);
				
				view.setTag(new MessageViewHolder(messageView, icon, title, preview, time, notice));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof MessageViewHolder) {
					holder = (MessageViewHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				if (!item.isRead())
					holder.messageView.setBackgroundColor(getActivity().getResources().getColor(R.color.is_read));
				
				if (item.isBlock()) {
					view.findViewById(R.id.block_image).setVisibility(View.VISIBLE);
					view.findViewById(R.id.time).setVisibility(View.GONE);
					holder.messageView.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
				} else {
					view.findViewById(R.id.block_image).setVisibility(View.GONE);
					view.findViewById(R.id.time).setVisibility(View.VISIBLE);
				}
				
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
					if (!"".equals(item.getUserImage())) {
						network.drawImageUrl(holder.imageHolder, item.getUserImage(), R.drawable.loading);
					}
				}
				//set full name;
				if (holder.title != null) {
					colorView.changeColorText(holder.title, user.getColor());
					holder.title.setText(item.getFullname());
				}
				//set preview
				if (holder.preview != null && item.getPreview() != null) {

                    if (user.getChatKey() == null) {
                        // interesting part starts from here here:
                        Html.ImageGetter ig = imageGetter.create(position, item.getPreview(), holder.preview);

                        holder.preview.setTag(position);
                        holder.preview.setText(Html.fromHtml(item.getPreview(), ig, null));
                    } else {
                        holder.preview.setText(Html.fromHtml(item.getPreview()).toString());
                    }
                }
				//set time
				if (holder.time != null) {
					holder.time.setText(item.getTimephrase());
				}
				
				//click long item
				if (item.getNotice() == null) {
					if (user.getChatKey() != null && user.getChatSecretKey() != null) {
						//action long click
                        holder.messageView.setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View arg0) {
                                onClickLongItem(position, String.valueOf(item.getToUserId()), item.isBlock());
                                clickedPos = position;
                                return false;
                            }
                        });
					}
					
					//action click
					holder.messageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            onClickItem(item, position);
                        }
                    });

                    holder.preview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            onClickItem(item, position);
                        }
                    });
				}
			}
			
			return view;
		}
		/**
		 * @param pos
		 */
		protected void onClickLongItem(int pos, String userId, boolean isBlock) {
			if (getActivity() == null)
				return;
			DashboardActivity activity = (DashboardActivity) getActivity();
			activity.showConversationDialog(userId, isBlock);
		}
		
		/**
		 * click item
		 * @param item
		 * @param pos
		 */
		protected void onClickItem(Message item, int pos) {
			Intent intent = null;
			if (user.getChatKey() == null) {
				//init intent
				intent = new Intent(getActivity(), ConversationActivity.class);
				intent.putExtra("thread_id", item.getThreadId());
				intent.putExtra("fullname", item.getFullname());
				intent.putExtra("page", 1);
			} else {
				intent = new Intent(getActivity(), ChatActivity.class);
				intent.putExtra("fullname", item.getFullname());
				intent.putExtra("user_id", String.valueOf(item.getToUserId()));
				intent.putExtra("image", item.getUserImage());
				
				if (item.isRead() == false) {
					displayMessage(getActivity().getApplicationContext(), "read", String.valueOf(item.getToUserId()), null);
					displayMessageRead(getActivity(), true);
				}
					
			}
			if (intent != null) {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getActivity().startActivity(intent);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		try{
			ma = null;
			getActivity().unregisterReceiver(mHandleMessageReceiver);
		}catch(Exception ex){
			
		}
		super.onDestroy();
	}
	
	/**
	 * Receiving message
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			String type = intent.getExtras().getString("type");		
			String message = intent.getExtras().getString("params");
			
			if (message != null && clickedPos != -1) {
				try {
					JSONObject jo = new JSONObject(message);
					//if action successful
					Message ms = ma.getItem(clickedPos);
					//delete conversation
					if (type.equals("commit:delete") && jo.has("status") && !jo.isNull("status") ) {
						if (jo.getString("status").equals("successful")) {
							ma.remove(ms);
							//if message adapter is null
							if (ma != null && ma.getCount() == 0) {
								Message m = new Message();
								m.setNotice(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_new_messages"));
								ma.add(m);
								actualListView.setAdapter(ma);
								ma.notifyDataSetChanged();
							}
						} else if (jo.getString("status").equals("fail")) {
							updateUI(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.failed_to_delete_conversation"));
						}
					}
					//unblock contact
					else if (type.equals("commit:unblock") && jo.has("status") && !jo.isNull("status")) {
						if (jo.getString("status").equals("successful")) {
							ms.setBlock(false);
							ma.notifyDataSetChanged();
						} else if (jo.getString("status").equals("fail")) {
							updateUI(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.failed_to_unblock_contact"));
						}
					}
					//block contact
					else if (type.equals("commit:block") && jo.has("status") && !jo.isNull("status")) {
						if (jo.getString("status").equals("successful")) {
							ms.setBlock(true);
							ma.notifyDataSetChanged();
						} else if (jo.getString("status").equals("fail")) {
							updateUI(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.failed_to_block_contact"));
						}
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				//reset clicked position
				clickedPos = -1;
			}
		}
	};
	
	private void updateUI(final String content) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getActivity().getApplicationContext(), content, Toast.LENGTH_LONG).show();
			}
		});
	}
}
