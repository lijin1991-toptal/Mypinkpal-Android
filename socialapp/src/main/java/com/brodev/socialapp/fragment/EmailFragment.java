package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragment;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class EmailFragment extends SherlockFragment {

	private User user;
	private NetworkUntil network = new NetworkUntil();
	private EditText searchTxt, writeMessageTxt, subjectTxt;
	private String URL_SEND_MAIL = null;
	private int sUserId;
	private String feed_link;
	private String content = null;
	private String subject = null;
	private String email = null;
	private PhraseManager phraseManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		user = (User) getActivity().getApplicationContext();
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// create view from friend_fragment xml
		final View view = inflater.inflate(R.layout.share_email, container, false);
		
		searchTxt = (EditText) view.findViewById(R.id.searchEdit);
		String mailTo = phraseManager.getPhrase(getActivity().getApplicationContext(), "mail.to");
		searchTxt.setHint(mailTo);
		
		subjectTxt = (EditText) view.findViewById(R.id.share_subject);
		String mailSubject = phraseManager.getPhrase(getActivity().getApplicationContext(), "mail.subject");
		subjectTxt.setHint(mailSubject);
		
		writeMessageTxt = (EditText) view.findViewById(R.id.share_post_text);
		writeMessageTxt.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "share.message"));
		setHasOptionsMenu(true);
		getActivity().setProgressBarIndeterminateVisibility(false);

		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			feed_link = Html.fromHtml(extras.getString("feed_link_url")).toString();
		}
		
		subjectTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "share.check_out"));
		
		writeMessageTxt.setText(feed_link);
		
		return view;
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.poststatus, menu);
	 
		super.onCreateOptionsMenu(menu, inflater);
    }

	
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		 switch (item.getItemId()) {
		 	case R.id.action_post:
		 		
		 		content = writeMessageTxt.getText().toString();
				subject = subjectTxt.getText().toString();
				email = searchTxt.getText().toString();
				if (content.trim().length() > 0) {
					new composeTask().execute(String.valueOf(sUserId), content, subject, email);
				}
				writeMessageTxt.setText("");
				getActivity().finish();
			break; 
		 }
		return false;
	}

	/**
	 * Send email task
	 * 
	 * @author Nguyen Dat
	 */
	public class composeTask extends AsyncTask<String, Void, String> {

		private ProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setMessage("Send...");
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			if (isCancelled()) {
				return null;
			}
			// String url
			String result = null;
			try {
				if (Config.CORE_URL == null) {
					URL_SEND_MAIL = Config.makeUrl(user.getCoreUrl(), "emailShare", true) + "&token=" + user.getTokenkey();
				} else {
					URL_SEND_MAIL = Config.makeUrl(Config.CORE_URL, "emailShare", true) + "&token=" + user.getTokenkey();
				}
				// Use BasicNameValuePair to store POST data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();

				pairs.add(new BasicNameValuePair("email", params[3]));
				pairs.add(new BasicNameValuePair("message", params[1]));
				pairs.add(new BasicNameValuePair("subject",	params[2]));

				result = network.makeHttpRequest(URL_SEND_MAIL, "POST", pairs);

				System.out.println(pairs);
				Log.i("post share", result);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					JSONObject mainJSON = new JSONObject(result);
					mProgressDialog.setMessage(mainJSON.getString("output"));

					getActivity().finish();

				} catch (Exception ex) {

					ex.printStackTrace();
				}

			}
		}
	}

}
