package com.brodev.socialapp.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationSettingFragment extends SherlockFragment {

	private User user;
	private PhraseManager phraseManager;
	private NetworkUntil networkUntil = new NetworkUntil();
	private CheckBox chkComment, chkLike, chkNewFriend, chkFriendRequest, chkNewMessage;
	private TextView notiTitle;
	private Button notiBtn;
	private LinearLayout notificationSettingLayout;
	private ProgressBar progressBar;
	private ArrayList<String> data;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private ScrollView notificationLayout;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		user = (User) getActivity().getApplicationContext();
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());
		data = new ArrayList<String>();
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//create view from notification_fragment xml
		View view = inflater.inflate(R.layout.notification_setting_fragment, container, false);

        notificationLayout = (ScrollView) view.findViewById(R.id.notification_setting_layout);
		
		chkComment = (CheckBox) view.findViewById(R.id.chkComment);
		chkComment.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.new_comments"));
		
		chkLike = (CheckBox) view.findViewById(R.id.chkLike);
		chkLike.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.notification_for_likes"));
		
		chkNewFriend = (CheckBox) view.findViewById(R.id.chkNewFriend);
		chkNewFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.new_friend"));
		
		chkFriendRequest = (CheckBox) view.findViewById(R.id.chkFriendRequest);
		chkFriendRequest.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.friend_request"));
		
		chkNewMessage = (CheckBox) view.findViewById(R.id.chkNewMessage);
		chkNewMessage.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.new_messages"));
		
		notiTitle = (TextView) view.findViewById(R.id.notification_title);
		notiTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.mobile_notification_settings"));
		
		notiBtn = (Button) view.findViewById(R.id.update_notification_setting);
		notiBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.save"));
		changeColorApp(user.getColor());
		
		notificationSettingLayout = (LinearLayout) view.findViewById(R.id.notification_setting_content);
		progressBar = (ProgressBar) view.findViewById(R.id.notification_setting_loading);

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
                        loadNotificationSetting();
                    }
                }, 1000);
            }
        });

		return view;
	}

	/**
	 * Change color app
	 * @param colorCode
	 */
	private void changeColorApp(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			notiBtn.setBackgroundResource(R.drawable.brown_skip_button_background);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			notiBtn.setBackgroundResource(R.drawable.pink_skip_button_background);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			notiBtn.setBackgroundResource(R.drawable.green_skip_button_background);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			notiBtn.setBackgroundResource(R.drawable.violet_skip_button_background);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			notiBtn.setBackgroundResource(R.drawable.red_skip_button_background);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			notiBtn.setBackgroundResource(R.drawable.dark_violet_skip_button_background);
		} else {
			notiBtn.setBackgroundResource(R.drawable.skip_button_background);
		}
	}

    private void loadNotificationSetting() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                notificationLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                //fetch data
                new GetNotificationSetting().execute(user.getTokenkey());
            } else {
                // display error
                progressBar.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                notificationLayout.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            progressBar.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            notificationLayout.setVisibility(View.GONE);
        }
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		try {
            loadNotificationSetting();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		//action click
		chkComment.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					data.add("\"comment.add_new_comment\"");	
				} else {
					data.remove("\"comment.add_new_comment\"");
				}
			}
		});
		
		chkLike.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					data.add("\"like.new_like\"");	
				} else {
					data.remove("\"like.new_like\"");
				}
			}
		});
		
		chkNewFriend.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					data.add("\"friend.new_friend_accepted\"");	
				} else {
					data.remove("\"friend.new_friend_accepted\"");
				}
			}
		});
		
		chkFriendRequest.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					data.add("\"friend.new_friend_request\"");	
				} else {
					data.remove("\"friend.new_friend_request\"");
				}
			}
		});
		
		chkNewMessage.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					data.add("\"mail.new_message\"");	
				} else {
					data.remove("\"mail.new_message\"");
				}
			}
		});
		
		notiBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//request update to server
				try {
                    connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    networkInfo = connMgr.getActiveNetworkInfo();

                    if (networkInfo != null && networkInfo.isConnected())
                    {
                        new UpdateNotificationSetting().execute(data.toString());
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                    }
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		super.onActivityCreated(savedInstanceState);
	}
	
	/**
	 * Class request get notification setting
	 */
	public class GetNotificationSetting extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String resultstring = null;
			
			try {
				// url request
				String URL = null;
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", params[0]));
				pairs.add(new BasicNameValuePair("method", "accountapi.getNotificationSettings"));
				
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}
				
				// request GET method to server
				resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
				
			} catch(Exception ex) {
                noInternetLayout.setVisibility(View.VISIBLE);
                notificationLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
				ex.printStackTrace();
				return null;
			}
			return resultstring;
		}
		

		@Override
		protected void onPostExecute(String result) {
			try {
				if (result != null) {
					JSONObject mainJSON = new JSONObject(result);
					
					JSONArray outputJSOn = mainJSON.getJSONArray("output");
					
					for (int i = 0; i < outputJSOn.length(); i++) {
						if ("comment.add_new_comment".equals(outputJSOn.getString(i))) {
							chkComment.setChecked(false);
						} else if ("like.new_like".equals(outputJSOn.getString(i))) {
							chkLike.setChecked(false);
						} else if ("friend.new_friend_accepted".equals(outputJSOn.getString(i))) {
							chkNewFriend.setChecked(false);
						} else if ("friend.new_friend_request".equals(outputJSOn.getString(i))) {
							chkFriendRequest.setChecked(false);
						} else if ("mail.new_message".equals(outputJSOn.getString(i))) {
							chkNewMessage.setChecked(false);
						}
					}
					
					notificationSettingLayout.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.GONE);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * Class request update notification setting
	 */
	public class UpdateNotificationSetting extends AsyncTask<String, Void, String> {
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String resultstring = null;
				
			try {
				String URL = Config.makeUrl(user.getCoreUrl(), "updateNotificationSettings", true)
						+ "&token=" + user.getTokenkey();
				
				// Use BasicNameValuePair to store POST data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				
				if (params[0].length() > 0) {
					pairs.add(new BasicNameValuePair("notification", params[0]));
				}
					
				resultstring = networkUntil.makeHttpRequest(URL, "POST", pairs);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					JSONObject mainJSON = new JSONObject(result);
					String outputJSON = Html.fromHtml(mainJSON.getString("output")).toString();
					progressBar.setVisibility(View.GONE);
					Toast.makeText(getActivity().getApplicationContext(), outputJSON, Toast.LENGTH_LONG).show();	
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
		
	}
	
}
