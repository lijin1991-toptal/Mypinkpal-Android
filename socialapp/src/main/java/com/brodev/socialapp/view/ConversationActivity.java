package com.brodev.socialapp.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Conversation;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class ConversationActivity extends SherlockListActivity {

	private NetworkUntil network = new NetworkUntil();
	private User user;
	private ConversationAdapter ca;
	private Button sendBtn;
	private EditText sendEdit;
	String URL_SEND_MAIL, imageUser, content = null;
	private int page, threadId;
	private PhraseManager phraseManager;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
    private ImageGetter imageGetter;
	
	/**
	 * Change background
	 * @param colorCode
	 */
	private void changeColor(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.brown_comment_post_icon);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.pink_comment_post_icon);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.green_comment_post_icon);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.violet_comment_post_icon);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.red_comment_post_icon);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			sendBtn.setBackgroundResource(R.drawable.dark_violet_comment_post_icon);
		} else {
			sendBtn.setBackgroundResource(R.drawable.comment_post_icon);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		// get user
		user = (User) getApplication().getApplicationContext();
		phraseManager = new PhraseManager(getApplicationContext());
        this.imageGetter = new ImageGetter(getApplicationContext());
		
		// get button
		sendBtn = (Button) findViewById(R.id.send_email);
		sendBtn.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.send"));
		sendEdit = (EditText) findViewById(R.id.write_message);
		sendEdit.setHint(phraseManager.getPhrase(getApplicationContext(), "accountapi.write_a_message"));
		
		//change color
		changeColor(user.getColor());

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// get data from before activity
		threadId = extras.getInt("thread_id");
		String fullname = extras.getString("fullname");
		page = extras.getInt("page");

		// change title
		getSupportActionBar().setTitle(fullname);

		new ConversationTask().execute(page, threadId);

		
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.conversation_activity_list);

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				++page;
				new ConversationTask().execute(page, threadId);
			}
		});
		
		actualListView = mPullRefreshListView.getRefreshableView();
		
		// action click button send email
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				content = sendEdit.getText().toString();
				if (content.trim().length() > 0) {
					new sendEmailTask().execute(String.valueOf(threadId), content);	
				}

				// add to conversation adapter
				if (content.toString().trim().length() != 0) {
					Conversation cons = new Conversation();
					
					//get timestamp of current date
					Date date = new Date();
					long time = date.getTime() / 1000;
					
					cons.setTimePhrase(convertTime(time));
					cons.setText(Html.fromHtml(content).toString());
					cons.setLogged(true);
					ca.add(cons);

					setListAdapter(ca);
					((ConversationAdapter) getListAdapter()).notifyDataSetChanged();
					getListView().setSelection(ca.getCount() - 1);
				}

				sendEdit.setText("");
			}
		});
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_MESSAGE_ACTION));
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
	
	/**
	 * Receiving mail from push notification
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String thread_id = intent.getExtras().getString("thread_id");
			String preview = intent.getExtras().getString("mail");
			
			if (thread_id != null && threadId == Integer.parseInt(thread_id)) 
			{
				Conversation cons = new Conversation();
				//get timestamp of current date
				Date date = new Date();
				long time = date.getTime() /1000;
				
				cons.setTimePhrase(convertTime(time));
				cons.setText(preview);
				if (imageUser != null) {
					cons.setImage(imageUser);	
				}
				ca.add(cons);

				setListAdapter(ca);
				((ConversationAdapter) getListAdapter()).notifyDataSetChanged();
				getListView().setSelection(ca.getCount() - 1);
			}
		}
	};
	
	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mHandleMessageReceiver);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

	/**
	 * Send email task
	 * 
	 * @author ducpham
	 */
	public class sendEmailTask extends AsyncTask<String, Void, String> {

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
			pairs.add(new BasicNameValuePair("thread_id", params[0]));
			pairs.add(new BasicNameValuePair("subject", "1"));
			pairs.add(new BasicNameValuePair("message", params[1]));

			String result = network.makeHttpRequest(URL_SEND_MAIL, "POST", pairs);

			Log.i("DEBUG", result);

			return result;
		}
	}

	/**
	 * Class execute thread conversation
	 * 
	 * @author ducpham
	 */
	public class ConversationTask extends AsyncTask<Integer, Void, String> {
		String result = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {

			if (isCancelled()) {
				return null;
			}
			// Simulates a background task
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			// get result from get method
			result = getResultFromGET(params[0], params[1]);

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				if (ca == null) {
					ca = new ConversationAdapter(getApplicationContext());
				}
				ca = getConsAdapter(ca, result);

				if (ca != null) {
					setListAdapter(ca);
				}
				
				// We need notify the adapter that the data have been changed
				ca.notifyDataSetChanged();
				actualListView.setSelection(getListAdapter().getCount());
				mPullRefreshListView.onRefreshComplete();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			
			
		}
	}

	/**
	 * function get result from get method
	 * 
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET(int page, int threadId) {

		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getThread"));
		pairs.add(new BasicNameValuePair("thread_id", "" + threadId));
		pairs.add(new BasicNameValuePair("page", "" + page));

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
	 * Get Conversation Adapter from json
	 * 
	 * @param ca
	 * @param resString
	 * @return Conversation Adapter
	 */
	public ConversationAdapter getConsAdapter(ConversationAdapter ca, String resString) {
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);
				// get output json array
				JSONArray outputJSON = mainJSON.getJSONArray("output");

				JSONObject objJSON = null;
                Conversation cons = null;

				for (int i = 0; i < outputJSON.length(); i++) {
					objJSON = outputJSON.getJSONObject(i);
                    cons = new Conversation();

					if (user.getUserId().equals(objJSON.get("user_id"))) {
						cons.setLogged(true);
					} else {
						//set image of user
						if (page == 1) {
							imageUser = objJSON.getString("user_image_path");	
						}
					}
					// set user image
					cons.setImage(objJSON.getString("user_image_path"));
					
					// set content
                    if (objJSON.has("text_html")) {
                        cons.setText(objJSON.getString("text_html"));
                    } else {
                        cons.setText(objJSON.getString("text"));
                    }
					
					// set time
					cons.setTimePhrase(convertTime(Long.parseLong(objJSON.getString("time_stamp"))));

					// add conversation to adapter
					ca.insert(cons, i);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return ca;
	}

	/**
	 * Change background text
	 * @param textView
	 * @param colorCode
	 */
	private void changeBackgroundText(TextView textView, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.brown_bubble);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.pink_bubble);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.green_bubble);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.violet_bubble);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.red_bubble);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.dark_violet_bubble);
		} else {
			textView.setBackgroundResource(R.drawable.bubble);
		}
	}
	
	/**
	 * Create Conversation adapter
	 * 
	 * @author ducpham
	 */
	public class ConversationAdapter extends ArrayAdapter<Conversation> {

		public ConversationAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).isLogged() ? 0 : 1;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public boolean isEnabled(int position) {
			return !getItem(position).isLogged();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Conversation item = getItem(position);
			ConversationViewHolder holder = null;

			if (view == null) {
				int layout = R.layout.conversation_list_row;

				if (item.isLogged()) {
					layout = R.layout.conversation_list_row_right;
				}

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				// call element from xml
				ImageView icon = (ImageView) view.findViewById(R.id.image_conversation);
                TextView content = (TextView) view.findViewById(R.id.content_conversation);
				TextView time = (TextView) view.findViewById(R.id.time_conversation);
				
				view.setTag(new ConversationViewHolder(icon, content, time));
			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof ConversationViewHolder) {
					holder = (ConversationViewHolder) tag;
				}
			}

			if (item != null && holder != null) {
				// set image friend;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getImage())) {
						network.drawImageUrl(holder.imageHolder, item.getImage(), R.drawable.loading);
					}
				}
				// set content of conversation
				if (holder.text != null) {
					if (item.isLogged()) {
						changeBackgroundText(holder.text, user.getColor());
					}
					holder.text.setVisibility(View.VISIBLE);

                    // interesting part starts from here here:
                    Html.ImageGetter ig = imageGetter.create(position, item.getText(), holder.text);

                    holder.text.setTag(position);
                    holder.text.setText(Html.fromHtml(item.getText(), ig, null));
				}
				// set time
				if (holder.time != null) {
					holder.time.setText(item.getTimePhrase());
				}
			}

			return view;
		}
	}

	/**
	 * Class conversation view holder
	 * 
	 * @author ducpham
	 */
	public class ConversationViewHolder {
		public final ImageView imageHolder;
		public final TextView text;
		public final TextView time;

		public ConversationViewHolder(ImageView icon, TextView text,
				TextView time) {
			this.imageHolder = icon;
			this.text = text;
			this.time = time;
		}
	}
	
	/**
	 * Convert time stamp
	 * @param timestamp
	 * @return
	 */
	private String convertTime(long timestamp) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	    return sdf.format(new Date(timestamp * 1000));
	}

}
