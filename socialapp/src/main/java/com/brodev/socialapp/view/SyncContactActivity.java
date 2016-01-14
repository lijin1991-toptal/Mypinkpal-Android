package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;

import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SyncContactActivity extends SherlockActivity {

	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private String emailLogin;
	private PhraseManager phraseManager;
	
	private ProgressBar syncLoading;
	private Button syncAccept;

	private void goToSite() {
		Intent myIntent = new Intent(SyncContactActivity.this, DashboardActivity.class);
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
			syncAccept.setBackgroundResource(R.drawable.brown_skip_button_background);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			syncAccept.setBackgroundResource(R.drawable.pink_skip_button_background);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			syncAccept.setBackgroundResource(R.drawable.green_skip_button_background);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			syncAccept.setBackgroundResource(R.drawable.violet_skip_button_background);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			syncAccept.setBackgroundResource(R.drawable.red_skip_button_background);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			syncAccept.setBackgroundResource(R.drawable.dark_violet_skip_button_background);
		} else {
			syncAccept.setBackgroundResource(R.drawable.skip_button_background);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_active);
		
		user = (User) getApplicationContext();
		
		syncLoading = (ProgressBar) findViewById(R.id.sync_loading);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (this.getIntent().hasExtra("email")) {
				emailLogin = bundle.getString("email");
			}
		}
		
		phraseManager = new PhraseManager(getApplicationContext());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "admincp.import"));
		
		TextView content = (TextView) findViewById(R.id.content);
		content.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.add_profile_picture_and_other_info_to_contacts_contacts_will_be_synced_from_now_on"));
		
		syncAccept = (Button) findViewById(R.id.syncAccept);
		syncAccept.setText(phraseManager.getPhrase(getApplicationContext(), "admincp.import"));
		
		//change color
		changeColorApp(user.getColor());
		
		syncAccept.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				syncAccept.setEnabled(false);
				syncLoading.setVisibility(View.VISIBLE);
				new SyncContactTask().execute();
			}
		});
		
	}

	public class SyncContactTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String resultstring = null;
			
			if (isCancelled()) {
				return null;
			}
			
			try {
				// Use BasicNameValuePair to create GET data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method", "accountapi.importContact"));
				pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
				
				// url request
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}
			
				// request GET method to server
				resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
				
			} catch (Exception ex) {
				syncAccept.setEnabled(true);
				syncLoading.setVisibility(View.GONE);
				ex.printStackTrace();
			}

			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {	
					JSONObject json = new JSONObject(result);
					JSONObject output = json.getJSONObject("output");
					 JSONArray friendArray = output.getJSONArray("friends");
					 JSONObject friend = null;
					 if (friendArray.length() == 0) {
						 goToSite();
						 return;
					 } else {
						 for (int i = 0; i < friendArray.length(); i++) {
							 friend = friendArray.getJSONObject(i);
							 ArrayList <ContentProviderOperation> ops = new ArrayList <ContentProviderOperation>();
							 int rawContactInsertIndex = ops.size();
							 ops.add(ContentProviderOperation.newInsert(
									 ContactsContract.RawContacts.CONTENT_URI)
									     .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
									     .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
									     .build());
							 
							 if (friend.has("full_name") && !friend.isNull("full_name")) {
								 ops.add(ContentProviderOperation.newInsert(
									     ContactsContract.Data.CONTENT_URI)
									         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
									         .withValue(ContactsContract.Data.MIMETYPE,
									     ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
									         .withValue(
									     ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
									     Html.fromHtml(friend.getString("full_name")).toString()).build());
							 }
							 
							 if (friend.has("email") && !friend.isNull("email")) {
								 ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
								         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
								         .withValue(ContactsContract.Data.MIMETYPE,
								     ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
								         .withValue(ContactsContract.CommonDataKinds.Email.DATA, friend.getString("email"))
								         .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
								         .build());
							 }
							 // Asking the Contact provider to create a new contact  
						     getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
						
						}
						 goToSite();
					 }
					 
				} catch (Exception ex) {
					syncAccept.setEnabled(true);
					syncLoading.setVisibility(View.GONE);
					ex.printStackTrace();
				}
			}

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goToSite();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
}
