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
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.widget.LinearLayout;
import android.widget.ProgressBar;

import android.widget.TextView;
import com.brodev.socialapp.http.NetworkUntil;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Blog;
import com.brodev.socialapp.entity.BlogCategory;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.Privacy;
import com.brodev.socialapp.android.manager.ColorView;

public final class BlogPostNew extends SherlockActivity {

	static final int MENU_SET_MODE = 0;
	private User user;
	Blog blog;
	PhraseManager phraseManage;
	CheckBox chkCategory;
	NetworkUntil networkUntil = new NetworkUntil();
	BlogCategoryAdapter bca;
	private LinearLayout getCategory;
	private List<BlogCategory> lstCategory;
	private ProgressBar progressBlog;
	private String URL_POST_BLOG = null, categoryValues = "";
	private NetworkUntil network = new NetworkUntil();
	Privacy privacy;
	private ColorView colorView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_add_new);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		phraseManage = new PhraseManager(getApplicationContext());
		user = (User) getApplicationContext();
		colorView = new ColorView(getApplicationContext());
		initView();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.poststatus, menu);

		return true;
	}

	private void initView() {
		TextView moduleName = (TextView) this.findViewById(R.id.moduleName);
		moduleName.setText(phraseManage.getPhrase(getApplicationContext(), "blog.add_a_new_blog"));

		TextView title = (TextView) this.findViewById(R.id.textTitle);
		title.setHint(phraseManage.getPhrase(getApplicationContext(), "blog.title"));

		TextView post = (TextView) this.findViewById(R.id.textCotent);
		post.setHint(phraseManage.getPhrase(getApplicationContext(), "blog.post"));
		
		ImageView privacyImg = (ImageView) this.findViewById(R.id.post_stt_privacy_img);
		colorView.changeColorPrivacy(privacyImg, user.getColor());
		
		// category
		TextView categories = (TextView) this.findViewById(R.id.categoryView);
		categories.setText(phraseManage.getPhrase(getApplicationContext(), "blog.categories"));

		getCategory = (LinearLayout) this.findViewById(R.id.listCategories);
		BlogCategoryTask showBlogCategory = new BlogCategoryTask();

		progressBlog = (ProgressBar) this.findViewById(R.id.progress_blog);

		showBlogCategory.execute();

		// show privacy
		TextView postPrivacy = (TextView) this.findViewById(R.id.postPrivacy);
		postPrivacy.setText(phraseManage.getPhrase(getApplicationContext(), "blog.privacy"));
		
		final TextView privacy_status = (TextView) this.findViewById(R.id.status_privacy);
		privacy_status.setText(phraseManage.getPhrase(getApplicationContext(), "privacy.everyone"));

		privacy = new Privacy(this);
		privacy.setTextView(privacy_status);
		privacy_status.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					privacy.showMessage();
				} catch (Exception e) {

				}
			}
		});

	}

	/**
	 * function get result from get method
	 * 
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET() {
		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getAllBlogCategories"));
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

		return resultstring;
	}

	/**
	 * get blog category
	 * 
	 * @author Huy Nguyen
	 */
	public class BlogCategoryTask extends AsyncTask<Integer, Void, String> {
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
			result = getResultFromGET();

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				// create new message adapter
				lstCategory = new ArrayList<BlogCategory>();

				lstCategory = getBlogCategoryAdapter(lstCategory, result);

				if (lstCategory != null && lstCategory.size() > 0) {
					bca = new BlogCategoryAdapter(getApplicationContext(), R.layout.checkbox, lstCategory);

					for (int i = 0; i < bca.getCount(); i++) {
						View itemCate = bca.getView(i, null, null);
						getCategory.addView(itemCate);
					}
				}
				progressBlog.setVisibility(View.GONE);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// We need notify the adapter that the data have been changed

			// Call onLoadMoreComplete when the LoadMore task, has finished
			// ((PullAndLoadListView) getListView()).onRefreshComplete();

			super.onPostExecute(result);
		}

	}

	/**
	 * Function create Friend adapter
	 * 
	 * @return Friend Adapter
	 */
	public List<BlogCategory> getBlogCategoryAdapter(
			List<BlogCategory> madapter, String resString) {
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);

				Object intervention = mainJSON.get("output");

				if (intervention instanceof JSONArray) {

					JSONArray outJson = (JSONArray) intervention;

					JSONObject outputJson = null;
					BlogCategory category = null;

					for (int i = 0; i < outJson.length(); i++) {
						outputJson = outJson.getJSONObject(i);
						category = new BlogCategory();
						// set category id
						category.setCategory_id(outputJson.getInt("category_id"));
						// set full name
						category.setName(outputJson.getString("name"));
						Log.i("CATEGORYNAME", outputJson.getString("name"));
						madapter.add(category);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return madapter;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_post:
			BlogAddTask addBlogTask = new BlogAddTask();
			// get title
			EditText title = (EditText) this.findViewById(R.id.textTitle);
			String titleValue = title.getText().toString().trim();
			// get content
			EditText content = (EditText) this.findViewById(R.id.textCotent);
			String contentValue = content.getText().toString().trim();
			// get privacy
			String privacyValue = privacy.getValue();
			// get category
			getCategoryValue();
			
			if (titleValue.length() <= 0) {
				Toast.makeText(getApplicationContext(), phraseManage.getPhrase(getApplicationContext(), "blog.fill_title_for_blog"), Toast.LENGTH_LONG).show();
			} else if (contentValue.length() <= 0) {
				Toast.makeText(getApplicationContext(), phraseManage.getPhrase(getApplicationContext(), "blog.add_content_to_blog"), Toast.LENGTH_LONG).show();
			} else if (titleValue.length() > 0 && contentValue.length() > 0){
				addBlogTask.execute(titleValue, contentValue, categoryValues, privacyValue);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private class BlogCategoryAdapter extends ArrayAdapter<BlogCategory> {

		private List<BlogCategory> blogCategorys;

		public BlogCategoryAdapter(Context context, int textViewResourceId,
				List<BlogCategory> blogCategory) {
			super(context, textViewResourceId, blogCategory);
			this.blogCategorys = new ArrayList<BlogCategory>();
			this.blogCategorys.addAll(blogCategory);
		}

		private class ViewHolder {
			CheckBox name;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			Log.v("ConvertView", String.valueOf(position));

			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.checkbox, null);

				holder = new ViewHolder();
				holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
				convertView.setTag(holder);

				holder.name.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						BlogCategory blogCategory = (BlogCategory) cb.getTag();
						blogCategory.setIsChecked(cb.isChecked());
						changeColor(cb, user.getColor());
					}
				});
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			BlogCategory blogCategory = blogCategorys.get(position);
			holder.name.setText(blogCategory.getName());
			holder.name.setChecked(false);
			holder.name.setTag(blogCategory);

			return convertView;
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
			} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.dark_violet_checkbox_selector);
			} else {
				checkbox.setButtonDrawable(R.drawable.checkbox_selector);
			}
		}

	}

	/**
	 * create a blog params title content categories privacy
	 * 
	 * @author Huy Nguyen
	 */
	public class BlogAddTask extends AsyncTask<String, Void, String> {
		String result = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			
			if (Config.CORE_URL == null) {
				URL_POST_BLOG = Config.makeUrl(user.getCoreUrl(), "postBlog", true) + "&token=" + user.getTokenkey();
			} else {
				URL_POST_BLOG = Config.makeUrl(Config.CORE_URL, "postBlog", true) + "&token=" + user.getTokenkey();
			}
			
			// Use BasicNameValuePair to store POST data

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("title", params[0]));
			pairs.add(new BasicNameValuePair("content", params[1]));
			pairs.add(new BasicNameValuePair("categories", params[2]));
			pairs.add(new BasicNameValuePair("privacy", params[3]));

			String result = network.makeHttpRequest(URL_POST_BLOG, "POST", pairs);

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
					String notice = requestValue.getString("notice");
					boolean success = requestValue.getBoolean("success");
					// String notice
					Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_LONG).show();
					// boolean
					if (success) {
						finish();
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// We need notify the adapter that the data have been changed

			// Call onLoadMoreComplete when the LoadMore task, has finished
			// ((PullAndLoadListView) getListView()).onRefreshComplete();

			super.onPostExecute(result);
		}

	}

	private void getCategoryValue() {
		categoryValues = "";
		if (lstCategory != null && lstCategory.size() > 0) {
			for (int i = 0; i < lstCategory.size(); i++) {
				BlogCategory item = lstCategory.get(i);
				if (item.getIsChecked()) {
					categoryValues += String.valueOf(item.getCategory_id()) + ",";

				}
			}
		}
	}

}
