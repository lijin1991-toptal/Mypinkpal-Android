package com.brodev.socialapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.asyncTask.FriendsAsyncTask;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.UserComposeSearch;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ComposeActivity extends SherlockListActivity {

	private User user;
	private NetworkUntil network = new NetworkUntil();
	private EditText searchTxt, writeMessageTxt;
	private Button sendBtn;
	private String URL_SEND_MAIL = null;
	private int sUserId;
	private String content, fullname, imageUser;
	private UserSearchAdapter ca;
	private Timer tTimer = new Timer(); 
	private PhraseManager phraseManager;
	private ImageView searchImgView;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
	
	/**
	 * Change color
	 * @param colorCode
	 */
	private void changeColor(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.brown_comment_post_icon);
			searchImgView.setBackgroundResource(R.drawable.brown_share_post_search);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.pink_comment_post_icon);
			searchImgView.setBackgroundResource(R.drawable.pink_share_post_search);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.green_comment_post_icon);
			searchImgView.setBackgroundResource(R.drawable.green_share_post_search);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.violet_comment_post_icon);
			searchImgView.setBackgroundResource(R.drawable.violet_share_post_search);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.red_comment_post_icon);
			searchImgView.setBackgroundResource(R.drawable.red_share_post_search);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.dark_violet_comment_post_icon);
			searchImgView.setBackgroundResource(R.drawable.dark_violet_share_post_search);
		} else {
			sendBtn.setBackgroundResource(R.drawable.comment_post_icon);
			searchImgView.setBackgroundResource(R.drawable.share_post_search);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//This has to be called before setContentView and you must use the
        //class in com.actionbarsherlock.view and NOT android.view
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.activity_compose);
		setSupportProgressBarIndeterminateVisibility(false);
		//get user info
		user = (User) getApplication().getApplicationContext();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		content = null;
		fullname = null;
		imageUser = null;
		phraseManager = new PhraseManager(getApplicationContext());
		sUserId = 0;
		
		searchTxt = (EditText) findViewById(R.id.searchEdit);
		sendBtn = (Button) findViewById(R.id.send_email);
		sendBtn.setText(phraseManager.getPhrase(getApplicationContext(), "mail.send"));
		writeMessageTxt = (EditText) findViewById(R.id.write_message);
		writeMessageTxt.setHint(phraseManager.getPhrase(getApplicationContext(), "feed.write_a_comment"));
		searchTxt.setHint(phraseManager.getPhrase(getApplicationContext(), "mail.to") + ":");
		searchImgView = (ImageView) findViewById(R.id.post_comment);
		
		//change color
		changeColor(user.getColor());
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String fullname = extras.getString("fullname");
			sUserId = Integer.parseInt(extras.getString("user_id"));
			searchTxt.setText(fullname);
		}
		
		// change title
		getSupportActionBar().setTitle(getString(R.string.new_message));
		
		//action change text in edit text
		searchTxt.addTextChangedListener(new TextWatcher() {
			 
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//do nothing
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				//do nothing
			}
			
			@Override
			public void afterTextChanged(final Editable s) {
				if (s.toString().trim().length() > 0) {
					tTimer.cancel();
					tTimer = new Timer();
					tTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							//request search
							if (s.toString().trim().length() > 0) {
								if (user.getChatKey() == null) {
									//search core user list 
									new UserListSearchTask().execute(s.toString().trim());	
								} else {
									searchListChat(s.toString().trim(), "friend");
								}	
							}
								
						}
					}, 300);
					
				}
			}
		});
		
		// action click button send email
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected())
                {
                    content = writeMessageTxt.getText().toString();
                    if (content.trim().length() > 0 && sUserId > 0) {
                        setSupportProgressBarIndeterminateVisibility(true);
                        if (user.getTokenChatServer() == null) {
                            new composeTask(ComposeActivity.this).execute(String.valueOf(sUserId), content);
                        } else {
                            displayMessage(getApplicationContext(), "send", String.valueOf(sUserId), content.trim());

                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            intent.putExtra("fullname", fullname);
                            intent.putExtra("user_id", String.valueOf(sUserId));
                            intent.putExtra("image", imageUser);
                            startActivity(intent);
                            setSupportProgressBarIndeterminateVisibility(false);
                        }

                    }
                    writeMessageTxt.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                    finish();
                }

			}
		});
	}
	
	/**
	 * search list chat
	 * @param query
	 * @param typeSearch
	 */
	public void searchListChat(String query, String typeSearch) {
		try {
			FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask(getApplicationContext());
			friendsAsyncTask.execute(user.getTokenkey(), query, typeSearch);
			String result = friendsAsyncTask.get();
			if (result != null) {
				ca = new UserSearchAdapter(getApplicationContext());
				JSONObject main = new JSONObject(result);
				JSONObject outputJson = main.getJSONObject("output");
				Object invt = outputJson.get("aSearchResults");
				
				JSONObject json = null;
				UserComposeSearch friend = null;
				if (invt instanceof JSONArray) {
					JSONArray output = (JSONArray) invt;
					
					for (int i = 0; i < output.length(); i++) {
						json = output.getJSONObject(i);
						if (!json.getString("item_id").equals(user.getUserId())) {
							friend = new UserComposeSearch();
							friend.setUserId(Integer.parseInt(json.getString("item_id")));
							friend.setFullname(Html.fromHtml(json.getString("item_title")).toString());
							friend.setImage(json.getString("user_image"));
							
							ca.add(friend);
							if (ca != null) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										setListAdapter(ca);
									}
								});
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param message
	 */
	public static void displayMessage(Context context, String type, String userId, String message) {
        Intent intent = new Intent(Config.DISPLAY_MESSAGE_ACTION);
        intent.putExtra("type", type);
        
        intent.putExtra("userId", userId);
        intent.putExtra("message", message);
        
        context.sendBroadcast(intent);
        
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) {
		UserComposeSearch user = (UserComposeSearch) getListAdapter().getItem(position);
		
		//set full name into text edit
		searchTxt.setText(user.getFullname().toString());
		
		if (searchTxt.getText().toString().trim().length() > 0 && searchTxt.getText().toString().trim().length() > 0) {
			sUserId = user.getUserId();
			fullname = user.getFullname();
			imageUser = user.getImage();
		}
		
		//focus to edit text write message
		writeMessageTxt.requestFocus();
	}
	
	/**
	 * Send email task
	 * 
	 * @author ducpham
	 */
	public class composeTask extends AsyncTask<String, Void, String> {

		private Activity activity;
		public composeTask(Activity activity) {
			this.activity = activity;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// String url
			if (Config.CORE_URL == null) {
				URL_SEND_MAIL = Config.makeUrl(user.getCoreUrl(), "sendEmail", true) + "&token=" + user.getTokenkey();
			} else {
				URL_SEND_MAIL = Config.makeUrl(Config.CORE_URL, "sendEmail", true) + "&token=" + user.getTokenkey();
			}
			
			// Use BasicNameValuePair to store POST data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("to", params[0]));
			pairs.add(new BasicNameValuePair("subject", "brodev"));
			pairs.add(new BasicNameValuePair("message", params[1]));

			String result = network.makeHttpRequest(URL_SEND_MAIL, "POST", pairs);

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) 
			{	
				try {
					JSONObject mainJSON = new JSONObject(result);
					JSONObject outputJSON = mainJSON.getJSONObject("output");
					
					int threadId = Integer.parseInt(outputJSON.getString("thread_id"));
					
					Intent intent = new Intent(activity, ConversationActivity.class);
					intent.putExtra("thread_id",threadId);
					intent.putExtra("fullname", searchTxt.getText().toString());
					intent.putExtra("page", 1);
					
					setSupportProgressBarIndeterminateVisibility(false);
					activity.startActivity(intent);
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Class execute get search list user
	 * 
	 * @author ducpham
	 */
	public class UserListSearchTask extends AsyncTask<String, Void, String> {
		String result = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			if (isCancelled()) {
				return null;
			}
			// Simulates a background task
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			// get result from get method
			result = getResultFromGET(params[0]);

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				ca = new UserSearchAdapter(getApplicationContext());
				
				ca = getUserAdapter(ca, result);

				if (ca != null) {
					setListAdapter(ca);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * function get result from get method
	 * @param sSearch
	 * @return result
	 */
	public String getResultFromGET(String sSearch) {

		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.searchFriend"));
		pairs.add(new BasicNameValuePair("search_for", sSearch));
		
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
	 * Get Adapter from json
	 * @param ucs
	 * @param resString
	 * @return adapter
	 */
	public UserSearchAdapter getUserAdapter(UserSearchAdapter ucs, String resString) 
	{
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);
				// get output json array
				Object intervention = mainJSON.get("output");
				
				JSONObject objJSON = null;
				
				if (intervention instanceof JSONObject) 
				{
					JSONObject outputJSON = (JSONObject) intervention;
					
					for (int i = 0; i < outputJSON.length(); i++) {
						objJSON = outputJSON.getJSONObject(outputJSON.names().getString(i));
						UserComposeSearch uc = new UserComposeSearch();
						uc = getUser(uc, objJSON);
						// add user list to adapter
						ucs.add(uc);
					}
				} else if (intervention instanceof JSONArray) {
					JSONArray outputJSON = (JSONArray) intervention;
					
					for (int i = 0; i < outputJSON.length(); i++) {
						objJSON = outputJSON.getJSONObject(i);
						UserComposeSearch uc = new UserComposeSearch();
						uc = getUser(uc, objJSON);
						//add user list to adapter
						ucs.add(uc);
					}
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return ucs;
	}
	
	/**
	 * 
	 * @param output
	 * @return uc
	 */
	public UserComposeSearch getUser(UserComposeSearch uc, JSONObject output) 
	{
		try {
			// set user image
			uc.setImage(output.getString("user_image"));
			// set full name
			uc.setFullname(output.getString("full_name"));
			//set user id
			uc.setUserId(Integer.parseInt(output.getString("user_id")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return uc;
	}
	
	/**
	 * Create List user search adapter
	 * 
	 * @author ducpham
	 */
	public class UserSearchAdapter extends ArrayAdapter<UserComposeSearch> {
		
		public UserSearchAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			UserComposeSearch item = getItem(position);
			UserSearchViewHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.share_list_user_search;

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				// call element from xml
				ImageView icon = (ImageView) view.findViewById(R.id.image_friend);
				TextView content = (TextView) view.findViewById(R.id.title);
				view.setTag(new UserSearchViewHolder(icon, content));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();	
				if (tag instanceof UserSearchViewHolder) {
					holder = (UserSearchViewHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				// set image friend;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getImage())) {
						network.drawImageUrl(holder.imageHolder, item.getImage(), R.drawable.loading);
					}
				}
				// set full name
				if (holder.text != null) {
					holder.text.setText(item.getFullname());
				}
			}
			
			return view;
		}
	}
	
	/**
	 * Class list user - search view holder
	 * 
	 * @author ducpham
	 */
	public class UserSearchViewHolder {
		public final ImageView imageHolder;
		public final TextView text;

		public UserSearchViewHolder(ImageView icon, TextView text) {
			this.imageHolder = icon;
			this.text = text;
		}
	}
	
}
