package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;

import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class InviteActivity extends SherlockListActivity {

	private int contactFriendLength, contactUserLength;
	private NetworkUntil networkUntil = new NetworkUntil();
	private FriendAdapter fa;
	private User user;
	private String choseEmail;
	private ArrayList<String> lstChoose;
	private String emailLogin, sNotUser, sNotFriends;
	private boolean bFindFriends;	
	private PhraseManager phraseManager;
	private ProgressBar loading;
	private Button findAll, nextBtn;

	private void goToSite() {
		Intent myIntent = new Intent(InviteActivity.this, DashboardActivity.class);
		myIntent.putExtra("email", emailLogin);
		startActivity(myIntent);
	}

	/**
	 * Change color app
	 * @param colorCode
	 */
	private void changeColorApp(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			nextBtn.setBackgroundResource(R.drawable.brown_skip_button_background);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			nextBtn.setBackgroundResource(R.drawable.pink_skip_button_background);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			nextBtn.setBackgroundResource(R.drawable.green_skip_button_background);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			nextBtn.setBackgroundResource(R.drawable.violet_skip_button_background);
		}  else if ("Red".equalsIgnoreCase(colorCode)) {
			nextBtn.setBackgroundResource(R.drawable.red_skip_button_background);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			nextBtn.setBackgroundResource(R.drawable.dark_violet_skip_button_background);
		} else {
			nextBtn.setBackgroundResource(R.drawable.skip_button_background);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.invite_fragment);
		sNotUser = sNotFriends = null;
		choseEmail = null;
		lstChoose = new ArrayList<String>();
		
		user = (User) getApplication().getApplicationContext();
		
		Bundle bundle = getIntent().getExtras();
		
		if (bundle != null) {
			if (this.getIntent().hasExtra("email")) {
				emailLogin = bundle.getString("email");
			}
			
			if (getIntent().hasExtra("find_friends")) {
				bFindFriends = bundle.getBoolean("find_friends");	
			}
			
			if (getIntent().hasExtra("not_friend")) {
				sNotFriends = bundle.getString("not_friend");
			}
			
			if (getIntent().hasExtra("not_user")) {
				sNotUser = bundle.getString("not_user");
			}
			
			if (getIntent().hasExtra("not_friend_length")) {
				contactFriendLength = bundle.getInt("not_friend_length");
			}
			
			if (getIntent().hasExtra("not_user_length")) {
				contactUserLength = bundle.getInt("not_user_length");
			}
		}
		
		phraseManager = new PhraseManager(getApplicationContext());
		loading = (ProgressBar) findViewById(R.id.content_loading);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		findAll = (Button) findViewById(R.id.addAll);
		nextBtn = (Button) findViewById(R.id.next);
		nextBtn.setText(phraseManager.getPhrase(getApplicationContext(), "link.next"));
		
		//set color app
		changeColorApp(user.getColor());
		
		findViewById(R.id.topInvite).setVisibility(View.VISIBLE);
		TextView totalFound = (TextView) findViewById(R.id.totalFound);
		
		findAll.setEnabled(true);
		loading.setVisibility(View.GONE);
		
		if (bFindFriends) {
			getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "invite.find_friends"));
			findAll.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.add_all"));
			
			//set found contact
			totalFound.setText(contactFriendLength + " " + phraseManager.getPhrase(getApplicationContext(), "accountapi.contacts_found"));
			
		} else {
			getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "accountapi.invite"));
			findAll.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.invite_all"));
			
			//set found contact
			totalFound.setText(contactUserLength + " " + phraseManager.getPhrase(getApplicationContext(), "accountapi.contacts_found"));
		}
		
		//action click invite/add all
		findAll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try {
					findAll.setEnabled(false);
					loading.setVisibility(View.VISIBLE);
					if (bFindFriends) {
						new RequestAddFriend().execute(choseEmail);
					} else {
						new InviteUser().execute(choseEmail);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			
			}
		});
		
		fa = new FriendAdapter(getApplicationContext());
		
		if (sNotFriends != null && bFindFriends) {
			try {
				JSONArray notFriend = new JSONArray(sNotFriends);
				Friend friend = null;
				JSONObject friendJSON;
				for (int i = 0; i < notFriend.length(); i++) {
					friend = new Friend();
					friendJSON = notFriend.getJSONObject(i);
					friend.setUser_id(friendJSON.getString("user_id"));
					friend.setFullname(Html.fromHtml(friendJSON.getString("full_name")).toString());
					friend.setIcon(friendJSON.getString("user_image_path"));
					fa.add(friend);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} 
		
		if (sNotUser != null && !bFindFriends) {
			try {
				JSONArray notUser = new JSONArray(sNotUser);
				Friend friend = null;
				for (int i = 0; i < notUser.length(); i++) {
					friend = new Friend();
					friend.setFullname(Html.fromHtml(notUser.getString(i)).toString());
					fa.add(friend);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (fa != null) {
			setListAdapter(fa);
		}
		
		nextBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				goNextActivity();
			}
		});
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goToSite();
			return true;
		case R.id.action_skip:
			goNextActivity();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * go to next activity
	 */
	private void goNextActivity() {
		
		if (lstChoose.size() == 0) {
			goNext();
			return;
		}
		loading.setVisibility(View.VISIBLE);
		if (bFindFriends) {
			new RequestAddFriend().execute(lstChoose.toString());
		} else {
			new InviteUser().execute(lstChoose.toString());
		}
	}
	
	private void goNext() {
		loading.setVisibility(View.GONE);
		Intent myIntent = null;
		if (!bFindFriends) {
			myIntent = new Intent(InviteActivity.this, SyncContactActivity.class);
		} else {
			myIntent = new Intent(InviteActivity.this, InviteActivity.class);
			//put extra info
			myIntent.putExtra("not_user", sNotUser);
			myIntent.putExtra("not_user_length", contactUserLength);
			myIntent.putExtra("find_friends", false);
		}
	
		myIntent.putExtra("email", emailLogin);
		
		if (myIntent != null) {
			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(myIntent);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.skip_add_friend, menu);
		return true;
	}
	
	/**
	 * Class request add friends
	 */
	public class RequestAddFriend extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String resultstring = null;
			
			if (isCancelled()) {
				return null;
			}
			
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				
				if (params[0] == null) {
					pairs.add(new BasicNameValuePair("callback", sNotFriends));	
				} else {
					pairs.add(new BasicNameValuePair("callback", params[0]));	
				}
				
				String URL = null;
				if (Config.CORE_URL == null) {
					URL =  Config.makeUrl(user.getCoreUrl(), "getRequestFriend", true) + "&token=" + user.getTokenkey();
				} else {
					URL =  Config.makeUrl(Config.CORE_URL, "getRequestFriend", true) + "&token=" + user.getTokenkey();
				}
				
				// request GET method to server
				resultstring = networkUntil.makeHttpRequest(URL, "POST", pairs);
				
				Log.i("Request add friend >> ", resultstring);
			} catch (Exception ex) {
				findAll.setEnabled(true);
				loading.setVisibility(View.GONE);
				ex.printStackTrace();
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			goNext();
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * Class invite user
	 */
	public class InviteUser extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String resultstring = null;
			
			if (isCancelled()) {
				return null;
			}
			
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
				
				if (params[0] == null) {
					pairs.add(new BasicNameValuePair("callback", sNotUser));	
				} else {
					pairs.add(new BasicNameValuePair("callback", params[0]));	
				}
				
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), "inviteEmail", true) + "&token=" + user.getTokenkey();
				} else {
					URL = Config.makeUrl(Config.CORE_URL, "inviteEmail", true) + "&token=" + user.getTokenkey();
				}
				
				// request GET method to server
				resultstring = networkUntil.makeHttpRequest(URL, "POST", pairs);
				
				Log.i("Invite CONTACT", resultstring);
				
			} catch (Exception ex) {
				findAll.setEnabled(true);
				loading.setVisibility(View.GONE);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			goNext();
			super.onPostExecute(result);
		}
	}

	/**
	 * Create friend browse adapter
	 */
	public class FriendAdapter extends ArrayAdapter<Friend> {
		
		public FriendAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final Friend item = getItem(position);
			FriendViewHolder holder = null;

			if (view == null) {
				int layout = R.layout.invite_list_row;

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				// call element from xml
				ImageView icon = (ImageView) view.findViewById(R.id.image_friend);
				TextView full_name = (TextView) view.findViewById(R.id.fullName);
				CheckBox checkboxHolder = (CheckBox) view.findViewById(R.id.selected_email);

				view.setTag(new FriendViewHolder(icon, full_name, checkboxHolder));
			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof FriendViewHolder) {
					holder = (FriendViewHolder) tag;
				}
			}

			if (item != null && holder != null) {
				// set image friend;
				if (holder.imageHolder != null) {

					if (!"".equals(item.getIcon()) && item.getIcon() != null) {
						holder.imageHolder.setVisibility(View.VISIBLE);
						networkUntil.drawImageUrl(holder.imageHolder, item.getIcon(), R.drawable.loading);
					} else {
						holder.imageHolder.setVisibility(View.GONE);
					}
				}
				// set full name;
				if (holder.title != null) {
					holder.title.setText(item.getFullname());
				}

			}
			
			changeColor(holder.checkboxHolder, user.getColor());
			holder.checkboxHolder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
					if (isChecked) {
						if (bFindFriends) {
							lstChoose.add("\"" + item.getUser_id() + "\"");	
						} else 
							lstChoose.add("\"" + item.getFullname() + "\"");
	                } else {
	                	if (bFindFriends) {
	                		lstChoose.remove("\"" + item.getUser_id() + "\"");	
	                	} else 
	                		lstChoose.remove("\"" + item.getFullname() + "\"");
	                	
	                }
				}
			});
			
			return view;
		}
		
		/**
		 * Change color when click checkbox
		 * @param checkbox
		 * @param colorCode
		 */
		private void changeColor(CheckBox checkbox, String colorCode) {
			if ("Brown".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.brown_checkbox_selector);
			} else if ("Pink".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.pink_checkbox_selector);
			} else if ("Green".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.green_checkbox_selector);
			} else if ("Violet".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.violet_checkbox_selector);
			} else if ("Red".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.red_checkbox_selector);
			}else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.dark_violet_checkbox_selector);
			} else {
				checkbox.setButtonDrawable(R.drawable.checkbox_selector);
			}
		}
	}
	
	

	/**
	 * Class friend view holder
	 */
	public class FriendViewHolder {
		public final ImageView imageHolder;
		public final TextView title;
		public final CheckBox checkboxHolder;

		public FriendViewHolder(ImageView icon, TextView title, CheckBox checkboxHolder) {
			this.imageHolder = icon;
			this.title = title;
			this.checkboxHolder = checkboxHolder;
		}
	}

}
