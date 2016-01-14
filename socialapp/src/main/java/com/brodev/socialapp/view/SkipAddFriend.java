package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SkipAddFriend extends SherlockActivity {
	
	private String emailLogin;
	private PhraseManager phraseManager;
	private User user;
	private JSONArray jsonEmail;
	private HashSet<String> lstEmail;
	private NetworkUntil networkUntil = new NetworkUntil();
	private ProgressBar processLayout;
	private Button findFriend;
	private ImageView findFriendImage;
	
	private void goToSite() {
		Intent myIntent = new Intent(SkipAddFriend.this, DashboardActivity.class);
		myIntent.putExtra("email", emailLogin);		
		startActivity(myIntent);
		finish();
	}

	/**
	 * Change color app
	 * @param colorCode
	 */
	private void changeColorApp(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			findFriend.setBackgroundResource(R.drawable.brown_skip_button_background);
			findFriendImage.setImageResource(R.drawable.brown_add_friend_image);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			findFriend.setBackgroundResource(R.drawable.pink_skip_button_background);
			findFriendImage.setImageResource(R.drawable.pink_add_friend_image);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			findFriend.setBackgroundResource(R.drawable.green_skip_button_background);
			findFriendImage.setImageResource(R.drawable.green_add_friend_image);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			findFriend.setBackgroundResource(R.drawable.violet_skip_button_background);
			findFriendImage.setImageResource(R.drawable.violet_add_friend_image);
		}  else if ("Red".equalsIgnoreCase(colorCode)) {
			findFriend.setBackgroundResource(R.drawable.red_skip_button_background);
			findFriendImage.setImageResource(R.drawable.red_add_friend_image);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			findFriend.setBackgroundResource(R.drawable.dark_violet_skip_button_background);
			findFriendImage.setImageResource(R.drawable.dark_violet_add_friend_image);
		} else {
			findFriend.setBackgroundResource(R.drawable.skip_button_background);
			findFriendImage.setImageResource(R.drawable.add_friend_image);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.add_friend);	

		user = (User) getApplication().getApplicationContext();
		lstEmail = new HashSet<String>();
		processLayout = (ProgressBar) findViewById(R.id.add_friend_loading);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (this.getIntent().hasExtra("email")) {
				emailLogin = bundle.getString("email");
			}
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);	
		setSupportProgressBarIndeterminateVisibility(false);
		
		phraseManager = new PhraseManager(getApplicationContext());
		initView();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.skip_add_friend_image, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goToSite();
			return true;
		case R.id.action_skip:
			goToSite();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 
	 */
	private void initView() {
		TextView title = (TextView) this.findViewById(R.id.title);
		title.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.choose_who_you_d_like_to_add_as_friends"));
		findFriend = (Button) this.findViewById(R.id.inviteFriend);
		findFriendImage = (ImageView) this.findViewById(R.id.audiochatImg);
		findFriend.setText(phraseManager.getPhrase(getApplicationContext(), "invite.find_friends"));
		//set color
		changeColorApp(user.getColor());
		
		findFriend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				findFriend.setEnabled(false);
				processLayout.setVisibility(View.VISIBLE);
				lstEmail = getEmailFromContact();
				if (lstEmail.size() > 0) {
					new FilterContact().execute();
				} else {
					goToSite();
				}
			}
		});
		
		TextView content = (TextView) this.findViewById(R.id.content);
		content.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.contact_find_friends_info"));
	}
	
	/**
	 * get Email from contact
	 */
	public HashSet<String> getEmailFromContact() {
		HashSet<String> lstEmail = new HashSet<String>();
		
		try {
			ContentResolver cr = getContentResolver();
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			
			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {
					String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
					Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							null, ContactsContract.CommonDataKinds.Email.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					
					while (emailCur.moveToNext()) {
						
						String emailContact = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						
						if (emailContact != null && isEmailValid(emailContact)) {
							lstEmail.add(emailContact);
						}
					}
					emailCur.close();
				}
			}
			cur.close();
		} catch (Exception ex) {
			findFriend.setEnabled(true);
			processLayout.setVisibility(View.GONE);
			ex.printStackTrace();
		}
		
		return lstEmail;
	}
	
	/**
	 * method is used for checking valid email id format.
	 * 
	 * @param email
	 * @return boolean true for valid false for invalid
	 */
	public static boolean isEmailValid(String email) {
	    boolean isValid = false;
	
	    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	    CharSequence inputStr = email;
	
	    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(inputStr);
	    if (matcher.matches()) {
	        isValid = true;
	    }
	    return isValid;
	}
	
	/**
	 * Class filter contact request
	 */
	public class FilterContact extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String resultstring = null;
			
			if (isCancelled()) {
				return null;
			}
			
			try {
				if (lstEmail.size() == 0) {
					return null;
				}
				jsonEmail = new JSONArray(lstEmail);
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
				pairs.add(new BasicNameValuePair("callback", jsonEmail.toString()));
				
				// url request
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), "getFilterContact", true) + "&token=" + user.getTokenkey();
				} else {
					URL = Config.makeUrl(Config.CORE_URL, "getFilterContact", true) + "&token=" + user.getTokenkey();
				}
				
				// request GET method to server
				resultstring = networkUntil.makeHttpRequest(URL, "POST", pairs);
				
				Log.i("FILTER CONTACT", resultstring);
			} catch (Exception ex) {
				findFriend.setEnabled(true);
				processLayout.setVisibility(View.GONE);
			}
			
			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			
			try {
				JSONObject outputJSon = new JSONObject(result);
				JSONObject output = outputJSon.getJSONObject("output");
				
				JSONObject notUser = output.getJSONObject("notUser");
				JSONObject notFriend = output.getJSONObject("isUser");
				
				int iNotFriend = notFriend.getInt("length");
				int iNotuser = notUser.getInt("length");
				Intent intent = null;
				
				//if not friend
				if (iNotFriend > 0) {
					//redirect to add friend
					intent = new Intent(SkipAddFriend.this, InviteActivity.class);
					intent.putExtra("email", emailLogin);
					//put extra user info
					intent.putExtra("not_friend", notFriend.getJSONArray("contact").toString());
					intent.putExtra("not_friend_length", iNotFriend);
					//put extra not user on site if have
					if (iNotuser > 0) {
						intent.putExtra("not_user", notUser.getJSONArray("contact").toString());
						intent.putExtra("not_user_length", iNotuser);
					}
					intent.putExtra("find_friends", true);
				}
				//if email list is not user on site
				else if (iNotuser > 0) {
					//redirect to invite
					intent = new Intent(SkipAddFriend.this, InviteActivity.class);
					intent.putExtra("email", emailLogin);
					//put extra info
					intent.putExtra("not_user", notUser.getJSONArray("contact").toString());
					intent.putExtra("not_user_length", iNotuser);
					intent.putExtra("find_friends", false);
				}
				// no data
				else {
					//redirect to import contact
					intent = new Intent(SkipAddFriend.this, SyncContactActivity.class);
					intent.putExtra("email", emailLogin);		
				}
				
				if (intent != null) {
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					finish();
				}
				
			} catch (Exception ex) {
				findFriend.setEnabled(true);
				processLayout.setVisibility(View.GONE);
			}
			
			super.onPostExecute(result);
		}
	}

}
