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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Blog;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.CommentFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public final class BlogDetail extends SherlockFragmentActivity {

	static final int MENU_SET_MODE = 0;
	private User user;
	Blog blog;
	
	private NetworkUntil networkUntil = new NetworkUntil();
	private ColorView colorView;
    private ImageGetter imageGetter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_content_view);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle bundle = getIntent().getExtras();
		blog = (Blog) bundle.get("blog");
		
		user = (User) getApplicationContext();
		colorView = new ColorView(getApplicationContext());
        this.imageGetter = new ImageGetter(getApplicationContext());
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		
		if (blog.getTime_stamp().equals("0")) {
			this.getBlogAdapter();
		}
		
		initView();
		// get comment fragment
		Bundle comment = new Bundle();
		comment.putString("type", "blog");
		comment.putInt("itemId", blog.getBlog_id());
		comment.putInt("totalComment", blog.getTotal_comment());
		comment.putInt("total_like", blog.getTotal_like());
		comment.putBoolean("no_share", blog.getShare());
		comment.putBoolean("is_liked", blog.getIs_like());
		comment.putBoolean("can_post_comment", blog.isCanPostComment());
		
		CommentFragment commentFragment = new CommentFragment();
		commentFragment.setArguments(comment);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.commentfragment_wrap, commentFragment).commit();
	}

	private void initView() {
		ImageView userImage = (ImageView) this.findViewById(R.id.image_user);
		
		if (!"".equals(blog.getUser_image_path())) {
			networkUntil.drawImageUrl(userImage, blog.getUser_image_path(), R.drawable.loading);
		}
		// set title
		TextView title = (TextView) this.findViewById(R.id.title);
		title.setText(blog.getTitle());
		colorView.changeColorText(title, user.getColor());
		
		// set content
        TextView content = (TextView) this.findViewById(R.id.blog_content_detail);

        // interesting part starts from here here:
        Html.ImageGetter ig = imageGetter.create(0, blog.getText(), content);
        content.setTag(0);
        content.setText(Html.fromHtml(blog.getText(), ig, null));

        TextView timestamp = (TextView) findViewById(R.id.time_stamp);
		timestamp.setText(blog.getTime_stamp());

		// set short text
		TextView shortText = (TextView) findViewById(R.id.fullName);
		shortText.setText(blog.getFull_name());
		
		TextView total_like = (TextView) findViewById(R.id.total_like);
		total_like.setText(String.valueOf(blog.getTotal_like()));
		colorView.changeColorText(total_like, user.getColor());
		
		TextView total_comment = (TextView) findViewById(R.id.total_comment);
		total_comment.setText(String.valueOf(blog.getTotal_comment()));
		colorView.changeColorText(total_comment, user.getColor());
		
		ImageView likeImg = (ImageView) this.findViewById(R.id.likes_feed_txt);
		ImageView commentImg = (ImageView) this.findViewById(R.id.comments_feed_txt);
		colorView.changeColorLikeCommnent(likeImg, commentImg, user.getColor());
		
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
     * function get result from get method
     * @return
     */
	public String getResultFromGET() {
		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getBlog"));
		pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
		pairs.add(new BasicNameValuePair("blogId", "" + blog.getBlog_id()));

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
	 * Function create Music adapter
	 * 
	 * @return Music Adapter
	 */
	public void getBlogAdapter() {

		String resString = this.getResultFromGET();
		try {
			JSONObject mainJSON = new JSONObject(resString);

			Object intervention = mainJSON.get("output");
			if (intervention instanceof JSONObject) {
				JSONObject outputJSON = mainJSON.getJSONObject("output");
				// set title
				blog.setTitle(outputJSON.getString("title"));
				// set text
				blog.setText(outputJSON.getString("text"));				
				// set is like
				if (!outputJSON.isNull("is_liked")) {
					blog.setIs_like(true);
				}
				// set total like
				blog.setTotal_like(outputJSON.getInt("total_like"));
				// set total comment
				blog.setTotal_comment(outputJSON.getInt("total_comment"));
				// set user name
				blog.setFull_name(outputJSON.getString("full_name"));
				// set user image path
				blog.setUser_image_path(outputJSON.getString("user_image_path"));
				// set time stamp
				blog.setTime_stamp(outputJSON.getString("time_stamp"));
				

				//set is liked
				if (outputJSON.has("is_liked") && !outputJSON.isNull("is_liked")) {
					Object inte = outputJSON.get("is_liked");
					if (inte instanceof String) 
						blog.setIs_like(true);
				}
					
				//set share
				if (outputJSON.has("no_share")) 
					blog.setShare(true);
				
				//set can post comment
				if (outputJSON.has("can_post_comment")) 
					blog.setCanPostComment(outputJSON.getBoolean("can_post_comment"));
				else 
					blog.setCanPostComment(true);

			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}
}
