package com.brodev.socialapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.manager.NotificationNextActivity;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Notification;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.EventDetailActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.brodev.socialapp.view.chats.GroupDialogActivity;
import com.brodev.socialapp.view.chats.PrivateDialogActivity;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.users.model.QBUser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends SherlockListFragment {
	
	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private NotificationAdapter na;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;

    private RelativeLayout noInternetLayout, notificationLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private ColorView colorView;
    private PhraseManager phraseManager;

    private ImageGetter imageGetter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        user = (User) getActivity().getApplicationContext();
        colorView = new ColorView(getActivity().getApplicationContext());
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        this.imageGetter = new ImageGetter(getActivity().getApplicationContext());

		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//create view from friend_fragment xml
		View view = inflater.inflate(R.layout.notification_fragment, container, false);
		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.notify_fragment);

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new NotificationTask().execute();
				
			}
		});

        notificationLayout = (RelativeLayout) view.findViewById(R.id.nofication_fragment_layout);
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
                        loadNotification();
                    }
                }, 1000);
            }
        });

		return view;
	}


    private void loadNotification() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                notificationLayout.setVisibility(View.VISIBLE);
                //fetch data
                new NotificationTask().execute();
            } else {
                // display error
                notificationLayout.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            notificationLayout.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);

		actualListView = mPullRefreshListView.getRefreshableView();

        loadNotification();
		
	}
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) 
	{
		Notification notify = (Notification) actualListView.getAdapter().getItem(position);
		
		if (notify.getNotice() == null) {
			Intent intent = null;
            if ((notify.getQbDialogId() != null || notify.getQbDialogId().length() != 0) &&
                    (notify.getTypeId().equals("chatroom_invited") || notify.getTypeId().equals("received_message"))) {
                QBDialog notifyDialog = DatabaseManager.getDialogByDialogId(getActivity(), notify.getQbDialogId());
                if (notifyDialog == null) {
                    Log.e("Quickblox:", "Can't get QBDialog from QBDialog ID");
                    Toast.makeText(getActivity(), "Can't get QBDialog from QBDialog ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                QBDialogType type = notifyDialog.getType();
                if (type == QBDialogType.PRIVATE) {
                    QBUser selfUser = AppSession.getSession().getUser();
                    int friendId = 0;
                    int arrGet = 0;
                    int selfId = selfUser.getId();
                    ArrayList<Integer> arrayList = new ArrayList<Integer>();
                    arrayList = notifyDialog.getOccupants();
                    for (int i =0; i < arrayList.size(); i++) {
                        arrGet = arrayList.get(i);
                        if (arrGet != selfId)
                            friendId = arrayList.get(i);
                    }
                    com.quickblox.q_municate_core.models.User occupantUser =
                            DatabaseManager.getUserById(getActivity().getApplicationContext(), friendId);
                    if (occupantUser == null) {
                        QBAddFriendCommand.start(getActivity(), friendId);
                        Toast.makeText(getActivity().getApplicationContext(), "error... Please try again", Toast.LENGTH_LONG).show();
                        return;
                    }
                    intent = new Intent(getActivity(), PrivateDialogActivity.class);
                    intent.putExtra(QBServiceConsts.EXTRA_OPPONENT, occupantUser);
                    intent.putExtra(QBServiceConsts.EXTRA_DIALOG, notifyDialog);
                    getActivity().startActivity(intent);
                } else {
                    intent = new Intent(getActivity(), GroupDialogActivity.class);
                    intent.putExtra(QBServiceConsts.EXTRA_DIALOG, notifyDialog);
                    intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, notify.getQbDialogId());
                    getActivity().startActivity(intent);
                }
            } else if (notify.getUserId() != null){
				intent = new Intent(getActivity(), FriendTabsPager.class);
				intent.putExtra("user_id", notify.getUserId());
				getActivity().startActivity(intent);
			} else if (notify.getTypeId().contains("event") && !notify.getTypeId().equals("event_comment")) {
				intent = new Intent(getActivity(), EventDetailActivity.class);
				intent.putExtra("event_id", notify.getItemId());
				getActivity().startActivity(intent);
			} else {
				intent = new NotificationNextActivity(getActivity().getApplicationContext()).notificationLinkActivity(intent, notify.getTypeId(), notify.getItemId(), notify.getLink(), true);
			}
		}
		
	}
	
	/**
	 * Function get result from GET
	 * @return string
	 */
	public String getResultFromGET() 
	{
		String resultstring = null;
		
		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getNotifications"));
		
		// url request
		String URL = null;
		if (Config.CORE_URL == null) {
			URL = Config.makeUrl(user.getCoreUrl(), null, false);	
		} else {
			URL = Config.makeUrl(Config.CORE_URL, null, false);
		}
		
		// request GET method to server
		resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
		
		Log.i("DEBUG", resultstring);
		
		return resultstring;
	}
	
	/**
	 * function get notification adapter
	 * 
	 * @param madapter
	 * @param resString
	 * @return notification adapter
	 */
	public NotificationAdapter getNoficationAdapter(NotificationAdapter madapter, String resString) 
	{
		if (resString != null) 
		{
			try {
				JSONObject mainJSON = new JSONObject(resString);
				
				Object intervention = mainJSON.get("output");
				
				if (intervention instanceof JSONArray) {
					
					JSONArray outJson = (JSONArray) intervention;
					
					JSONObject notiJSONObj = null;
					Notification notify = null;
					for (int i = 0; i < outJson.length(); i++) {
						notiJSONObj = outJson.getJSONObject(i);
						
						notify = new Notification();
						//set notification id
						notify.setNotificationId(Integer.parseInt(notiJSONObj.getString("notification_id")));
						//set link on web
						notify.setLink(notiJSONObj.getString("link"));
						//set time phrase;
						notify.setTimePhrase(notiJSONObj.getString("time_phrase"));
						//set user image;
						notify.setUserImage(notiJSONObj.getString("user_image"));
						//set icon
						if (!notiJSONObj.isNull("icon")) {
							notify.setIcon(notiJSONObj.getString("icon"));
						}

                        notify.setQbDialogId(notiJSONObj.getString("dialog_id"));
						
						JSONObject request = notiJSONObj.getJSONObject("social_app").getJSONObject("link").getJSONObject("request");
						
						//if has item id
						if (request.has("item_id")) {
							notify.setItemId(request.getString("item_id"));	
						}
						
						if (request.has("type_id")) {
							notify.setTypeId(request.getString("type_id"));	
						}
						
						if (request.has("user_id")) {
							notify.setUserId(request.getString("user_id"));
						}
						
						//set message
						notify.setMesage(notiJSONObj.getString("message"));

                        if (request.has("message_html")) {
                            notify.setMesage(request.getString("message_html"));
                        }
						
						madapter.add(notify);
					}
				} else if (intervention instanceof JSONObject) {
					JSONObject output = (JSONObject) intervention;
					
					Notification notify = new Notification();
					notify.setNotice(output.getString("notice"));
					
					madapter.add(notify);
				}
				
			} catch(Exception ex) {
                notificationLayout.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
				ex.printStackTrace();
			}
		}
		
		return madapter;
	}
	
	/**
	 * Class notification task
	 * @author ducpham
	 */
	public class NotificationTask extends AsyncTask<Integer, Void, String>
	{
		String resultstring = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Integer... params) {
			//get result from get method
			
			if (isCancelled()) {
				return null;
			}
			
			// Simulates a background task
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			
			resultstring = getResultFromGET();
			
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					//init friend adapter
					na = new NotificationAdapter(getActivity());
					
					na = getNoficationAdapter(na, result);
					
					if (na != null) {
						actualListView.setAdapter(na);	
					}

					// Call onRefreshComplete when the list has been refreshed.
					mPullRefreshListView.onRefreshComplete();
				} catch(Exception ex) {
				}
			}
			
		}
	}
	
	/**
	 * Notification adapter
	 * @author ducpham
	 */
	public class NotificationAdapter extends ArrayAdapter<Notification> 
	{
		public NotificationAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View view = convertView;
			Notification item = getItem(position);
			NotificationHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.notification_list_row;
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				
				ImageView image = (ImageView) view.findViewById(R.id.notification_image_friend);
                TextView message = (TextView) view.findViewById(R.id.notification_message);
				ImageView icon = (ImageView) view.findViewById(R.id.notification_icon);
				TextView time = (TextView) view.findViewById(R.id.notification_time_phrase);
				
				TextView notice = (TextView) view.findViewById(R.id.notice);
				
				view.setTag(new NotificationHolder(image, message, icon, time, notice));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof NotificationHolder) {
					holder = (NotificationHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				
				//if has notice
				if (item.getNotice() != null) {
					view.findViewById(R.id.thumbnail).setVisibility(View.GONE);
					view.findViewById(R.id.notification_content_view).setVisibility(View.GONE);
					//enable friend requests view
					view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
				}
				
				//set image
				if (holder.imageHolder != null) {
					if (!"".equals(item.getUserImage())) {
						networkUntil.drawImageUrl(holder.imageHolder, item.getUserImage(), R.drawable.loading);
					}
				}
				//set message
				if (holder.message != null && item.getMesage() != null) {

                    // interesting part starts from here here:
                    Html.ImageGetter ig = imageGetter.create(position, item.getMesage(), holder.message);

                    holder.message.setTag(position);
                    holder.message.setText(Html.fromHtml(item.getMesage(), ig, null));

                }
				//set icon
				if (holder.icon != null) {
					if (!"".equals(item.getIcon()) && item.getIcon() != null) {
						networkUntil.drawImageUrl(holder.icon, item.getIcon(), R.drawable.loading);
					} else {
						holder.icon.setVisibility(View.GONE);
					}
				}
				//set time phrase
				if (holder.timePhrase != null) {
					holder.timePhrase.setText(item.getTimePhrase());
				}
				
			}
		
			return view;
		}
	}
	
	/**
	 * class notification holder
	 * @author ducpham
	 */
	public class NotificationHolder {
		public final ImageView imageHolder;
		public final TextView message;
		public final ImageView icon;
		public final TextView timePhrase;
		public final TextView notice;
		
		public NotificationHolder(ImageView imageHolder, TextView message, ImageView icon, TextView timePhare, TextView notice) {
			this.imageHolder = imageHolder;
			this.message = message;
			this.icon = icon;
			this.timePhrase = timePhare;
			this.notice = notice;
		}
	}

	@Override
	public void onDestroy() {
		try{
			na = null;
		}catch(Exception ex){

		}
		super.onDestroy();
	}
	
	
	
}
