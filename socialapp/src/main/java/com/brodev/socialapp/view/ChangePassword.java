/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.brodev.socialapp.http.NetworkUntil;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.actionbarsherlock.app.SherlockActivity;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.Privacy;

public final class ChangePassword extends SherlockActivity {

	static final int MENU_SET_MODE = 0;
	User user;
	PhraseManager phraseManage;
	CheckBox chkCategory;
	NetworkUntil networkUntil = new NetworkUntil();
	private String URL_POST_CHECK_PASS = null;
	private NetworkUntil network = new NetworkUntil();
	Privacy privacy;
	String password, password_salt;
	private EditText oldPassword, newPassword, retypePassword;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_password);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		phraseManage = new PhraseManager(getApplicationContext());
		Bundle bundle = getIntent().getExtras();
		user = (User) getApplicationContext();
		password = bundle.getString("password");
		password_salt = bundle.getString("password_salt");
		initView();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.poststatus, menu);

		return true;
	}

	private void initView() {
		getSupportActionBar().setTitle(
				phraseManage.getPhrase(getApplicationContext(),
						"user.change_password"));

		oldPassword = (EditText) this.findViewById(R.id.textOldPassword);
		oldPassword.setHint(phraseManage.getPhrase(getApplicationContext(),
				"user.old_password"));

		newPassword = (EditText) this.findViewById(R.id.textNewPassword);
		newPassword.setHint(phraseManage.getPhrase(getApplicationContext(),
				"user.new_password"));

		retypePassword = (EditText) this.findViewById(R.id.textRetypePassword);
		retypePassword.setHint(phraseManage.getPhrase(getApplicationContext(),
				"user.confirm_password"));
	}

	/**
	 * create a blog params title content categories privacy
	 * 
	 * @author Huy Nguyen
	 */
	public class checkPasswordTask extends AsyncTask<String, Void, String> {
		String result = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			
			if (Config.CORE_URL == null) {
				URL_POST_CHECK_PASS = Config.makeUrl(user.getCoreUrl(), "changePassword", true) + "&token=" + user.getTokenkey();
			} else {
				URL_POST_CHECK_PASS = Config.makeUrl(Config.CORE_URL, "changePassword", true) + "&token=" + user.getTokenkey();
			}
			
			// Use BasicNameValuePair to store POST data

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("password", params[0]));
			pairs.add(new BasicNameValuePair("password_salt", params[1]));
			pairs.add(new BasicNameValuePair("new_password", params[2]));
			pairs.add(new BasicNameValuePair("current_password", params[3]));

			String result = network.makeHttpRequest(URL_POST_CHECK_PASS, "POST", pairs);
			Log.i("CHECKDULIEU", pairs.toString());
			return result;

		}

		@Override
		protected void onPostExecute(String result) {
			try {
				// create new message adapter
				JSONObject mainJSON = new JSONObject(result);

				Object request = mainJSON.get("output");

				if (request instanceof JSONObject) {
					JSONObject requestValue = (JSONObject) request;
					if (requestValue.has("notice")) {
						String notice = requestValue.getString("notice");

						// String notice
						Toast.makeText(getApplicationContext(), notice,
								Toast.LENGTH_LONG).show();	
						return;
					}					
					

				}
				Toast.makeText(
						getApplicationContext(),
						phraseManage.getPhrase(getApplicationContext(),
								"user.password_successfully_updated"),
						Toast.LENGTH_LONG).show();
				finish();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// We need notify the adapter that the data have been changed

			// Call onLoadMoreComplete when the LoadMore task, has finished
			// ((PullAndLoadListView) getListView()).onRefreshComplete();

			super.onPostExecute(result);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_post:
			if (checkInput()) {
				checkPasswordTask check = new checkPasswordTask();
				String new_password = newPassword.getText().toString();
				String check_password = oldPassword.getText().toString();
				check.execute(password, password_salt, new_password, check_password);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public boolean checkInput() {
		boolean bPass = true;
		if (oldPassword.getText().toString().equals("")) {
			Toast.makeText(
					getApplicationContext(),
					phraseManage.getPhrase(getApplicationContext(),
							"user.missing_old_password"), Toast.LENGTH_LONG)
					.show();
			return false;
		}
		if (newPassword.getText().toString().equals("")) {
			Toast.makeText(
					getApplicationContext(),
					phraseManage.getPhrase(getApplicationContext(),
							"user.missing_new_password"), Toast.LENGTH_LONG)
					.show();
			return false;
		}
		if (retypePassword.getText().toString().equals("")) {
			Toast.makeText(
					getApplicationContext(),
					phraseManage.getPhrase(getApplicationContext(),
							"user.missing_new_password"), Toast.LENGTH_LONG)
					.show();
			return false;
		}
		if (!retypePassword.getText().toString()
				.equals(newPassword.getText().toString())) {
			Toast.makeText(
					getApplicationContext(),
					phraseManage
							.getPhrase(getApplicationContext(),
									"user.your_confirmed_password_does_not_match_your_new_password"),
					Toast.LENGTH_LONG).show();
			return false;
		}
		return bPass;
	}
	

}
