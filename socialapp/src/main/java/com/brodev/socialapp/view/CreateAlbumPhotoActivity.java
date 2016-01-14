package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.PrivacyManager;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CreateAlbumPhotoActivity extends SherlockActivity {

	private User user;
	private NetworkUntil networkUtil = new NetworkUntil();
	private EditText albumName, albumDescription;
	private TextView albumPrivacy, CommentPrivacy;
	//phrase manager
	private PhraseManager phraseManager;
	private String sAlbumPrivacy, sCommentPrivacy, sName, sDescription;
	private ProgressDialog dialog;
	private PrivacyManager privacyManage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_album_photo);
		privacyManage = new PrivacyManager(getApplicationContext());
		user = (User) getApplication().getApplicationContext();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		phraseManager = new PhraseManager(getApplicationContext());
		
		// change title
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "accountapi.create_a_new_photo_album"));
		
		//init
		sAlbumPrivacy = null;
		sCommentPrivacy = null;
		sName = null;
		sDescription = null;
		
		//widget
		albumPrivacy = (TextView) findViewById(R.id.album_privacy);
		CommentPrivacy= (TextView) findViewById(R.id.comment_privacy);
		albumName = (EditText) findViewById(R.id.edit_album_name);
		albumName.setHint(phraseManager.getPhrase(getApplicationContext(), "photo.name"));
		albumDescription = (EditText) findViewById(R.id.edit_description_name);
		albumDescription.setHint(phraseManager.getPhrase(getApplicationContext(), "photo.description"));
		if (sAlbumPrivacy == null) {
			sAlbumPrivacy = String.valueOf("0");
			albumPrivacy.setText(phraseManager.getPhrase(getApplicationContext(), "privacy.everyone"));
		}
		
		if (sCommentPrivacy == null) {
			sCommentPrivacy = String.valueOf("0");
			CommentPrivacy.setText(phraseManager.getPhrase(getApplicationContext(), "privacy.everyone"));
		}
		
		//action click choose album privacy
		albumPrivacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showMessageBox(albumPrivacy);
				sAlbumPrivacy = getPrivacy(albumPrivacy, sAlbumPrivacy);
			}
		});
		
		//action click choose comment privacy
		CommentPrivacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMessageBox(CommentPrivacy);
				sCommentPrivacy = getPrivacy(CommentPrivacy, sCommentPrivacy);
			}
		});
		TextView albumPrivacy = (TextView) findViewById(R.id.textView3);
		albumPrivacy.setText(phraseManager.getPhrase(getApplicationContext(), "photo.album_s_privacy"));
		

		TextView albumCommentPrivacy = (TextView) findViewById(R.id.textView4);
		albumCommentPrivacy.setText(phraseManager.getPhrase(getApplicationContext(), "photo.comment_privacy"));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.create_album_photo, menu);
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create_album:
			//action request create photo
			sName = albumName.getText().toString().trim();
			sDescription = albumDescription.getText().toString().trim();
			try {
				//request to server
				dialog = ProgressDialog.show(CreateAlbumPhotoActivity.this, phraseManager.getPhrase(getApplicationContext(), "accountapi.posting"), 
						phraseManager.getPhrase(getApplicationContext(), "accountapi.please_wait"), true);
				if (sName.length() == 0) 
					Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "photo.provide_a_name_for_your_album"), Toast.LENGTH_LONG).show();
				else 
					new CreatePhotoAlbum().execute(sName, sDescription, sAlbumPrivacy, sCommentPrivacy);
			} catch (Exception e) {
				e.printStackTrace();
				dialog.dismiss();
			}
			return true;
			
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Show dialog choose privacy
	 */
	private void showMessageBox(final TextView privacy) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setItems(privacyManage.getValue(),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position
						// of the selected item
						if (which == 0) {
							privacy.setText(phraseManager.getPhrase(getApplicationContext(), "privacy.everyone"));
						} else if (which == 1) {
							privacy.setText(phraseManager.getPhrase(getApplicationContext(), "privacy.friends"));
						} else if (which == 2) {
							privacy.setText(phraseManager.getPhrase(getApplicationContext(), "privacy.friends_of_friends"));
						} else if (which == 3) {
							privacy.setText(phraseManager.getPhrase(getApplicationContext(), "privacy.only_me"));
						}
					}
				});
		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * get privacy
	 * @param privacy
	 * @param sPrivacy
	 * @return
	 */
	private String getPrivacy(TextView privacy, String sPrivacy) {
		
		String txtPrivacy = privacy.getText().toString().trim();
		
		if (txtPrivacy.equals(phraseManager.getPhrase(getApplicationContext(), "privacy.friends"))) {
			sPrivacy = String.valueOf("1");
		} else if (txtPrivacy.equals(phraseManager.getPhrase(getApplicationContext(), "privacy.friends_of_friends"))){
			sPrivacy = String.valueOf("2");
		} else if (txtPrivacy.equals(phraseManager.getPhrase(getApplicationContext(), "privacy.only_me"))) {
			sPrivacy = String.valueOf("3");
		} else {
			sPrivacy = String.valueOf("0");
		}
			
		return sPrivacy;
	}
	
	/**
	 * Class create photo album
	 */
	public class CreatePhotoAlbum extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String result = null;
			try {
				if (isCancelled()) {
					return null;
				}
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method", "accountapi.addAlbum"));
				pairs.add(new BasicNameValuePair("title", params[0]));
				pairs.add(new BasicNameValuePair("description", params[1]));
				pairs.add(new BasicNameValuePair("privacy", params[2]));
				pairs.add(new BasicNameValuePair("privacy_comment", params[3]));
				
				// url request
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}
				
				// request GET method to server
				result = networkUtil.makeHttpRequest(URL, "GET", pairs);
				
			} catch (Exception ex) {
				return null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if (result != null) {
				try {
					JSONObject mainJSON = new JSONObject(result);
					//get output JSON
					JSONObject outputJSON = mainJSON.getJSONObject("output");
					String albumId = outputJSON.getString("album_id");
					String albumName = outputJSON.getString("album_title");
					
					Intent i = new Intent("com.brodev.socialapp.android.album.new");
					i.putExtra("album_name", albumName);
					i.putExtra("album_id", albumId);
					sendBroadcast(i);
					finish();
				} catch (Exception ex) {
					dialog.dismiss();
					ex.printStackTrace();
				}
			}
			
			super.onPostExecute(result);
		}
		
	}
}
