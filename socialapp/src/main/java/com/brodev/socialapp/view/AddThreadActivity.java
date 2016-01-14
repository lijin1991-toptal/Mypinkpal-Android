package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.List;

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
import com.brodev.socialapp.entity.ForumThread;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class AddThreadActivity extends SherlockActivity {

	private NetworkUntil networkUntil = new NetworkUntil();
	private PhraseManager phraseManager;
	private User user;
	private EditText forumTitle, forumContent;
	private TextView subcribeTxt;
	private RadioButton rbtSubcribe, rbtNoSubcribe;
	private int iSubcribe, forumId;
	private ProgressDialog dialog;
	private String title, message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_thread);
		
		phraseManager = new PhraseManager(getApplicationContext());
		user = (User) getApplication().getApplicationContext();
		iSubcribe = 1;
		title = null;
		message = null;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		//set phrase
		forumTitle = (EditText) findViewById(R.id.forum_title);
		forumTitle.setHint(phraseManager.getPhrase(getApplicationContext(), "photo.title"));
		forumContent = (EditText) findViewById(R.id.forum_content);
		forumContent.setHint(phraseManager.getPhrase(getApplicationContext(), "friend.message"));
		subcribeTxt = (TextView) findViewById(R.id.subcribeTxt);
		subcribeTxt.setText(phraseManager.getPhrase(getApplicationContext(), "forum.subscribe"));
		
		rbtSubcribe = (RadioButton) findViewById(R.id.forum_subcribe);
		rbtSubcribe.setText(phraseManager.getPhrase(getApplicationContext(), "user.yes"));
		rbtNoSubcribe = (RadioButton) findViewById(R.id.forum_no_subcribe);
		rbtNoSubcribe.setText(phraseManager.getPhrase(getApplicationContext(), "user.no"));
		
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "forum.post_new_thread"));
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) 
		{
			if (getIntent().hasExtra("forum_id")) 
				forumId = bundle.getInt("forum_id");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.add_thread, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.submit_thread:
				title = forumTitle.getText().toString().trim();
				message = forumContent.getText().toString().trim();
				if (title.length() == 0) {
					Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.provide_a_title_for_your_thread"), Toast.LENGTH_LONG).show();
				} else if (message.length() == 0) {
					Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.provide_some_text"), Toast.LENGTH_LONG).show();
				} else {
					dialog = ProgressDialog.show(this, phraseManager.getPhrase(getApplicationContext(), "accountapi.posting"), phraseManager.getPhrase(getApplicationContext(), "accountapi.please_wait"), true);
					new AddThreadTask().execute(String.valueOf(forumId), title, message, String.valueOf(iSubcribe));
				}
				return true;
			default:
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public class AddThreadTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String result = null;
			try {
				String url = null;
				if (Config.CORE_URL == null) {
					url = Config.makeUrl(user.getCoreUrl(), "addThread", true) + "&token=" + user.getTokenkey();
				} else {
					url = Config.makeUrl(Config.CORE_URL, "addThread", true) + "&token=" + user.getTokenkey();
				}

				// Use BasicNameValuePair to store POST data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("forum_id", params[0]));
				pairs.add(new BasicNameValuePair("title", params[1]));
				pairs.add(new BasicNameValuePair("text", params[2]));
				pairs.add(new BasicNameValuePair("subcribe", params[3]));

				result = networkUntil.makeHttpRequest(url, "POST", pairs);
			} catch (Exception ex) {
				ex.printStackTrace();
				if (dialog.isShowing())
					dialog.dismiss();
				finish();
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (dialog.isShowing())
				dialog.dismiss();
			
			if (result != null) {
				getSingleThread(result);
			}
			
			finish();
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * Action click subcribe
	 * @param view
	 */
	public void onSubcribeClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.forum_subcribe:
	            if (checked)
	            	iSubcribe = 1;
	            break;
	        case R.id.forum_no_subcribe:
	            if (checked)
	            	iSubcribe = 0;
	            break;
	    }
	}
	
	/**
	 * Get forum thread after 
	 * @param sResponse
	 */
	private void getSingleThread(String sResponse) {
		Config.forumThread = new ForumThread();
		
		try {
			JSONObject mainJSON = new JSONObject(sResponse);
			JSONObject outputJSON = mainJSON.getJSONObject("output");
			Object intervention = outputJSON.get("thread");
			
			if (intervention instanceof JSONArray) {
				JSONArray outJson = (JSONArray) intervention;
				JSONObject threadObj = null;
				
				threadObj = outJson.getJSONObject(0);
				if (threadObj.has("thread_id") && !threadObj.isNull("thread_id"))
					Config.forumThread.setThreadId(threadObj.getString("thread_id"));

				// set thread title
				if (threadObj.has("title") && !threadObj.isNull("title"))
					Config.forumThread.setThreadTitle(Html.fromHtml(threadObj.getString("title")).toString());

				// set thread phrase
				if (threadObj.has("phrase") && !threadObj.isNull("phrase"))
					Config.forumThread.setThreadPhrase(Html.fromHtml(threadObj.getString("phrase")).toString());

				// set thread reply
				Config.forumThread.setTotalReply("0");

				// set thread view	
				Config.forumThread.setTotalView("0");

				// set user image
				if (threadObj.has("user_image_path") && !threadObj.isNull("user_image_path"))
					Config.forumThread.setUserImage(threadObj.getString("user_image_path"));
				
				//set continued
				Config.forumThread.setContinued(true);
				
				Log.i("test post", "done");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
