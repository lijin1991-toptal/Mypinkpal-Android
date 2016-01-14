package com.brodev.socialapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.ChatActivity;
import com.brodev.socialapp.view.ComposeActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * class add friend fragment 
 * @author ducpham
 */
public class ConfirmFriendFragment extends SherlockListFragment {
	
	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private ConfirmFriendAdapter cfa;
	private String URL_APPROVE_FRIEND;
	private Button confirmBtn, denyBtn, sendMailBtn, viewProfileBtn;
	private PhraseManager phraseManager;
	private ColorView colorView;

    private RelativeLayout noInternetLayout, listLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
	
	/**
     * Create a new instance of CountingFragment, providing "num"
     * as an argument.
     */
    public static ConfirmFriendFragment newInstance(int num) {
    	ConfirmFriendFragment f = new ConfirmFriendFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", 1);
        f.setArguments(args);
        return f;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		user = (User) getActivity().getApplicationContext();
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		
		//request get friend requests via GET
        loadConfirmFriend();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//create view from friend_fragment xml
		View view = inflater.inflate(R.layout.friend_request_fragment, container, false);

        listLayout = (RelativeLayout) view.findViewById(R.id.friend_request_layout);

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
                        loadConfirmFriend();
                    }
                }, 1000);
            }
        });

		return view;
	}

    private void loadConfirmFriend() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                listLayout.setVisibility(View.VISIBLE);
                //fetch data
                new FriendRequestsTask().execute();
            } else {
                // display error
                listLayout.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            listLayout.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }
	
	/**
	 * get friend list of logged user 
	 * @author ducpham
	 */
	public class FriendRequestsTask extends AsyncTask<String, Void, String> {
		String resultstring = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			//get result from get method
			if (isCancelled()) {
				return null;
			}
			
			resultstring = getResultFromGET();
			
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					//init friend adapter
					cfa = new ConfirmFriendAdapter(getActivity());
					
					cfa = getFriendAdapter(cfa, result);
					
					if (cfa != null) {
						setListAdapter(cfa);	
					}
					
				} catch(Exception ex) {
				}
			}
		}
	}

    /**
     * function get result from get method
     * @return
     */
	public String getResultFromGET() 
	{
		String resultstring;
		
		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getPendingFriendRequest"));
		
		// url request
		String URL = Config.makeUrl(user.getCoreUrl(), "", false);
		// request GET method to server
		resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
					
		return resultstring;
	}
	
	/**
	 * Function create Friend adapter
	 * @return Friend Adapter
	 */
	public ConfirmFriendAdapter getFriendAdapter(ConfirmFriendAdapter madapter, String resString) 
	{
		if (resString != null) 
		{	
			try {
				JSONObject mainJSON = new JSONObject(resString);
				
				Object intervention = mainJSON.get("output");
			
				if (intervention instanceof JSONArray) {
				// get output json array
					JSONArray outJson = (JSONArray) intervention;
					
					JSONObject outputJson = null;
					Friend friend = null;
					
					for (int i = 0; i < outJson.length(); i++) {
						outputJson = outJson.getJSONObject(i);
						friend = new Friend();
						//set user id
						friend.setUser_id(outputJson.getString("user_id"));
                        //set user email
                        friend.setEmail(outputJson.getString("email"));
						//set full name
						friend.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
						//set image
						friend.setIcon(outputJson.getString("user_image_path"));
						//set request id
						friend.setRequestId(Integer.parseInt(outputJson.getString("request_id")));
						
						madapter.add(friend);
					}
				} else if (intervention instanceof JSONObject) {
					JSONObject noticeJSON = (JSONObject) intervention;
					Friend f = new Friend();
					f.setNotice(Html.fromHtml(noticeJSON.getString("notice")).toString());
					madapter.add(f);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
                listLayout.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
				return null;
			}
		}
		return madapter;
	}
	
	/**
	 * Class request confirm friend request
	 * @author ducpham
	 */
	public class ConfirmRequest extends AsyncTask<Integer, Void, String> 
	{
		String resultstring;
		
		@Override
		protected String doInBackground(Integer... params) {
			if (isCancelled()) {
				return null;
			}
			//get result from get method
			// String url
			URL_APPROVE_FRIEND = Config.makeUrl(user.getCoreUrl(), "approveFriendRequest", true) + "&token=" + user.getTokenkey();
			
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("request_id", ""+params[0]));
			
			resultstring = networkUntil.makeHttpRequest(URL_APPROVE_FRIEND, "POST", pairs);
			
			Log.i("DEBUG", resultstring);
			
			return resultstring;
		}
	}
	
	/**
	 * Change color
	 * @param colorCode
	 */
	private void changeColor(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			confirmBtn.setBackgroundResource(R.drawable.brown_add_friend_confirm_request);
			sendMailBtn.setBackgroundResource(R.drawable.brown_add_friend_confirm_request);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			confirmBtn.setBackgroundResource(R.drawable.pink_add_friend_confirm_request);
			sendMailBtn.setBackgroundResource(R.drawable.pink_add_friend_confirm_request);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			confirmBtn.setBackgroundResource(R.drawable.green_add_friend_confirm_request);
			sendMailBtn.setBackgroundResource(R.drawable.green_add_friend_confirm_request);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			confirmBtn.setBackgroundResource(R.drawable.violet_add_friend_confirm_request);
			sendMailBtn.setBackgroundResource(R.drawable.violet_add_friend_confirm_request);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			confirmBtn.setBackgroundResource(R.drawable.red_add_friend_confirm_request);
			sendMailBtn.setBackgroundResource(R.drawable.red_add_friend_confirm_request);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			confirmBtn.setBackgroundResource(R.drawable.dark_violet_add_friend_confirm_request);
			sendMailBtn.setBackgroundResource(R.drawable.dark_violet_add_friend_confirm_request);
		} else {
			confirmBtn.setBackgroundResource(R.drawable.add_friend_confirm_request);
			sendMailBtn.setBackgroundResource(R.drawable.add_friend_confirm_request);
		}
	}
	
	/**
	 * Class deny friend request
	 * @author ducpham
	 */
	public class DenyRequest extends AsyncTask<Integer, Void, String> 
	{
		String resultstring;
		
		@Override
		protected String doInBackground(Integer... params) {
			if (isCancelled()) {
				return null;
			}
			URL_APPROVE_FRIEND = Config.makeUrl(user.getCoreUrl(), "denyFriendRequest", true) + "&token=" + user.getTokenkey();
			
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("request_id", ""+params[0]));
			
			resultstring = networkUntil.makeHttpRequest(URL_APPROVE_FRIEND, "POST", pairs);
			
			Log.i("DEBUG", resultstring);
			
			return resultstring;
		}
	}
	
	/**
	 * Create confirm friend list browse adapter
	 * @author ducpham
	 */
	public class ConfirmFriendAdapter extends ArrayAdapter<Friend> 
	{		
		public ConfirmFriendAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent) 
		{
			View view = convertView;
			final Friend item = getItem(position);
			ConfirmFriendHolder holder = null;

			if (view == null) {
				int layout = R.layout.friend_list_row;
				
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				//call element from xml
				ImageView icon = (ImageView) view.findViewById(R.id.friend_image_friend);
				TextView title = (TextView) view.findViewById(R.id.title);
				//enable friend requests view
				view.findViewById(R.id.friend_requests_action).setVisibility(View.VISIBLE);
				
				//widget
				confirmBtn = (Button) view.findViewById(R.id.confirm_request);
				confirmBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "friend.confirm"));
				denyBtn = (Button) view.findViewById(R.id.not_now);
				denyBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "friend.deny"));
				sendMailBtn = (Button) view.findViewById(R.id.send_a_message);
				sendMailBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.send_a_message"));
				viewProfileBtn = (Button) view.findViewById(R.id.view_profile);
				viewProfileBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.view_profile"));
				
				TextView notice = (TextView) view.findViewById(R.id.notice);
				
				view.setTag(new ConfirmFriendHolder(icon, title, confirmBtn, denyBtn, sendMailBtn, viewProfileBtn, notice));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof ConfirmFriendHolder) {
					holder = (ConfirmFriendHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				changeColor(user.getColor());
				
				//if no notice
				if (item.getNotice() != null) {
					view.findViewById(R.id.friend_image_friend).setVisibility(View.GONE);
					view.findViewById(R.id.friend_content_view).setVisibility(View.GONE);
					view.findViewById(R.id.user_online_layout).setVisibility(View.GONE);
					//enable friend requests view
					view.findViewById(R.id.friend_notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
					colorView.changeColorText(holder.notice, user.getColor());
				}
				
				if (item.isConfirmRequest() == true) {
					holder.confirmBtn.setVisibility(View.GONE);
					holder.denyBtn.setVisibility(View.GONE);
					holder.sendMailBtn.setVisibility(View.VISIBLE);
					holder.viewProfileBtn.setVisibility(View.VISIBLE);
				}
				
				if (item.getUser_id() == null) {
					holder.confirmBtn.setVisibility(View.GONE);
					holder.denyBtn.setVisibility(View.GONE);
				}
				
				//action click confirm friend request
				holder.confirmBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickConfirmRequest(position, item.isConfirmRequest());
					}
				});
				
				//action click deny friend request
				holder.denyBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickDenyRequest(position);
					}
				});
				
				//action click send a message 
				holder.sendMailBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = null;
						if (user.getChatKey() != null) {
							intent = new Intent(getActivity(), ChatActivity.class);
							intent.putExtra("image", item.getIcon());
						} else {
							intent = new Intent(getActivity(), ComposeActivity.class);
						}
						
						intent.putExtra("fullname", item.getFullname());
						intent.putExtra("user_id", item.getUser_id());

						if (intent != null) {
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					}
				});
				
				//action click view profile user
				holder.viewProfileBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						
						Intent intent = new Intent(getActivity(), FriendTabsPager.class);
						intent.putExtra("user_id", item.getUser_id());
						startActivity(intent);
					}
				});
				
				//action click user name
				holder.title.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getActivity(), FriendTabsPager.class);
						intent.putExtra("user_id", item.getUser_id());
						startActivity(intent);
					}
				});
				
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
		
		/**
		 * function for action click confirm button
		 * @param pos
		 * @param confirm
		 */
		protected void onClickConfirmRequest(int pos, boolean confirm) {
			Friend friend = getItem(pos); 
			if (confirm == false) {
				friend.setConfirmRequest(true);
				new ConfirmRequest().execute(friend.getRequestId());
			}
			notifyDataSetChanged();
		}
		/**
		 * function for action click deny friend request
		 * @param pos
		 */
		protected void onClickDenyRequest(int pos) {
			Friend friend = getItem(pos);
			friend.setUser_id(null);
			new DenyRequest().execute(friend.getRequestId());
			notifyDataSetChanged();
		}

	}
	
	/**
	 * Confirm request add friend
	 * @author ducpham
	 */
	public class ConfirmFriendHolder {
		public final ImageView imageHolder;
		public final TextView title;
		public final Button confirmBtn;
		public final Button denyBtn;
		public final Button sendMailBtn;
		public final Button viewProfileBtn;
		public final TextView notice;

		public ConfirmFriendHolder(ImageView image, TextView title,
				Button confirmBtn, Button denyBtn, Button sendMailBtn,
				Button viewProfileBtn, TextView notice) {
			this.imageHolder = image;
			this.title = title;
			this.confirmBtn = confirmBtn;
			this.denyBtn = denyBtn;
			this.sendMailBtn = sendMailBtn;
			this.viewProfileBtn = viewProfileBtn;
			this.notice = notice;
		}

	}
}
