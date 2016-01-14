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
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Post;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class PostThreadActivity extends SherlockActivity {

	private NetworkUntil networkUntil = new NetworkUntil();
	private User user;
	private PhraseManager phraseManager;
	private ProgressDialog dialog;
	private String threadId, message, quoteTxt;
	private EditText messageEditText;
	private int iSubcribe = 1;
	private RadioButton rbtSubcribe, rbtNoSubcribe;
	private TextView subcribeTxt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_thread);
		
		user = (User) getApplication().getApplicationContext();
		phraseManager = new PhraseManager(getApplicationContext());
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "forum.post_a_reply"));
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) 
		{
			if (getIntent().hasExtra("thread_id")) 
				threadId = bundle.getString("thread_id");
			
			if (getIntent().hasExtra("quote_text")) 
				quoteTxt = bundle.getString("quote_text");
		}
		
		//set phrase
		subcribeTxt = (TextView) findViewById(R.id.subcribeTxt);
		subcribeTxt.setText(phraseManager.getPhrase(getApplicationContext(), "forum.subscribe"));
		messageEditText = (EditText) findViewById(R.id.forum_post_thread);
		messageEditText.setHint(phraseManager.getPhrase(getApplicationContext(), "friend.message"));
		rbtSubcribe = (RadioButton) findViewById(R.id.forum_subcribe);
		rbtSubcribe.setText(phraseManager.getPhrase(getApplicationContext(), "user.yes"));
		rbtNoSubcribe = (RadioButton) findViewById(R.id.forum_no_subcribe);
		rbtNoSubcribe.setText(phraseManager.getPhrase(getApplicationContext(), "user.no"));
		
		if (quoteTxt != null) {
			messageEditText.setText(quoteTxt);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.post_thread, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.submit_post_thread:
				message = messageEditText.getText().toString().trim();
				if (message.length() == 0) {
					Toast.makeText(getApplicationContext(), "Provide some text.", Toast.LENGTH_LONG).show();
				} else {
					dialog = ProgressDialog.show(this, "Posting","Please wait...", true);
					new PostThreadTask().execute(threadId, message, String.valueOf(iSubcribe));
				}
				return true;
				
		}
		return super.onOptionsItemSelected(item);
	}

	public class PostThreadTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String result = null;
			try {
				String url = null;
				if (Config.CORE_URL == null) {
					url = Config.makeUrl(user.getCoreUrl(), "addPost", true) + "&token=" + user.getTokenkey();
				} else {
					url = Config.makeUrl(Config.CORE_URL, "addPost", true) + "&token=" + user.getTokenkey();
				}

				// Use BasicNameValuePair to store POST data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("thread_id", params[0]));
				pairs.add(new BasicNameValuePair("text", params[1]));
				pairs.add(new BasicNameValuePair("subcribe", params[2]));

				result = networkUntil.makeHttpRequest(url, "POST", pairs);
			} catch (Exception ex) {
				ex.printStackTrace();
				if (dialog.isShowing())
					dialog.dismiss();
				Toast.makeText(getApplicationContext(), "error... Please try again", Toast.LENGTH_LONG).show();
				finish();
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (dialog.isShowing())
				dialog.dismiss();
			
			if (result != null) {
				getSinglePost(result);
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
	
	private void getSinglePost(String result) {
		Config.post = new Post();
		
		try {
			JSONObject mainJSON = new JSONObject(result);
			JSONObject outputJSON = mainJSON.getJSONObject("output");
			if (outputJSON.has("post_id") && !outputJSON.isNull("post_id"))
				Config.post.setPostId(outputJSON.getString("post_id"));
			
			//set full name
			if (outputJSON.has("full_name") && !outputJSON.isNull("full_name"))
				Config.post.setFullname(Html.fromHtml(outputJSON.getString("full_name")).toString());
			
			//set total post
			if (outputJSON.has("total_post") && !outputJSON.isNull("total_post"))
				Config.post.setTotalPost(outputJSON.getString("total_post"));
			
			// set text
			if (outputJSON.has("text") && !outputJSON.isNull("text"))
				Config.post.setText(Html.fromHtml(outputJSON.getString("text")).toString());
			
			// set time phrase
			if (outputJSON.has("time_phrase") && !outputJSON.isNull("time_phrase"))
				Config.post.setTimePhrase(Html.fromHtml(outputJSON.getString("time_phrase")).toString());
			
			//set user image
			Config.post.setUserImagePath(user.getUserImage());
			
			//set liked
			if (outputJSON.has("is_liked") && !outputJSON.isNull("is_liked"))
				Config.post.setIsLiked(outputJSON.getString("is_liked"));
			
			//set total like
			if (outputJSON.has("total_like") && !outputJSON.isNull("total_like"))
				Config.post.setTotalLike(outputJSON.getString("total_like"));
			
			//set quote
			if (outputJSON.has("quote") && !outputJSON.isNull("quote"))
				Config.post.setQuote(Html.fromHtml(outputJSON.getString("quote")).toString());
			
			//set link post
			if (outputJSON.has("share_feed_link") && !outputJSON.isNull("share_feed_link"))
				Config.post.setLinkSharePost(outputJSON.getString("share_feed_link"));
			
			if (outputJSON.has("share_feed_link_url") && !outputJSON.isNull("share_feed_link_url"))
				Config.post.setLinkShareUrlPost(outputJSON.getString("share_feed_link_url"));
			
			Config.post.setContinued(true);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
