package com.brodev.socialapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.asyncTask.LikeAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Post;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ThreadActivity extends SherlockListActivity {

	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private PostAdapter pd;
	private int page, currentpos, listSize;
	private String threadId, title, postId;
	private PhraseManager phraseManager;
	private ColorView colorView;
    private ImageGetter imageGetter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread_view_fragment);
		
		user = (User) getApplication().getApplicationContext();
		phraseManager = new PhraseManager(getApplicationContext());
		colorView = new ColorView(getApplicationContext());
		postId = null;
		title = null;
		threadId = null;
        this.imageGetter = new ImageGetter(getApplicationContext());
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) 
		{
			if (getIntent().hasExtra("thread_id")) 
				threadId = bundle.getString("thread_id");
			
			if (getIntent().hasExtra("title")) 
				title = bundle.getString("title");
			
			if (getIntent().hasExtra("post_id"))
				postId = bundle.getString("post_id");
			
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (title != null) {
			getSupportActionBar().setTitle(title);
		}
		
		page = 1;
		listSize = 0;
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.thread_fragment_list_layout);
		actualListView = mPullRefreshListView.getRefreshableView();
		
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				page = 1;
				// call feed refresh task to execute
				listSize = 1;
				new PostTask().execute(threadId, postId, String.valueOf(page));
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				++page;
				new PostTask().execute(threadId, postId, String.valueOf(page));
			}
		});
		
		try {
			new PostTask().execute(threadId, postId, String.valueOf(page));
		} catch (Exception ex) {
			
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.thread, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_add_post:
				Intent intent = new Intent(this, PostThreadActivity.class);
				intent.putExtra("thread_id", threadId);
				startActivity(intent);
				return true;
			default:
		}
		return super.onOptionsItemSelected(item);
	}

	public class PostTask extends AsyncTask<String, Void, String> 
	{
		@Override
		protected String doInBackground(String... params) {
			String resultString = null;

			if (isCancelled()) {
				return null;
			}

			resultString = getResultFromGET(params[0], params[1], params[2]);

			return resultString;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					if (page == 1 || pd == null) {
						pd = new PostAdapter(getApplicationContext());
					}
					pd = getPostsAdapter(pd, result);
					listSize -= pd.getCount();
					
					if (pd != null) {
						if (page == 1) {
							actualListView.setAdapter(pd);
						} else {
							currentpos = getListView().getFirstVisiblePosition();

							actualListView.setAdapter(pd);
							getListView().setSelectionFromTop(currentpos + 1, 0);

							pd.notifyDataSetChanged();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			mPullRefreshListView.onRefreshComplete();
		}
	}
	
	/**
	 * Function get result from GET
	 * 
	 * @return string
	 */
	public String getResultFromGET(String threadId, String postId, String page) {

		String resultstring = null;

		if (pd != null && listSize <= 0) {
			return null;
		}

		try {
			// Use BasicNameValuePair to create GET data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
			
			if (threadId != null) {
				pairs.add(new BasicNameValuePair("method", "accountapi.getThreadById"));
				pairs.add(new BasicNameValuePair("thread_id", threadId));
				if (postId != null) {
					pairs.add(new BasicNameValuePair("post_id", postId));
				}
			} else {
				pairs.add(new BasicNameValuePair("method", "accountapi.getThreadByPostId"));
				pairs.add(new BasicNameValuePair("post_id", postId));
			}
			pairs.add(new BasicNameValuePair("page", page));

			// url request
			String URL = null;
			if (Config.CORE_URL == null) {
				URL = Config.makeUrl(user.getCoreUrl(), null, false);	
			} else {
				URL = Config.makeUrl(Config.CORE_URL, null, false);
			}

			// request GET method to server
			resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);

			Log.i("DEBUG", resultstring);
		} catch (Exception ex) {
			return resultstring;
		}

		return resultstring;
	}
	
	/**
	 * get post adapter
	 * @param madapter
	 * @param resString
	 * @return
	 */
	public PostAdapter getPostsAdapter(PostAdapter madapter, String resString) 
	{
		if (resString != null) 
		{
			try {
				JSONObject mainJSON = new JSONObject(resString);
				JSONObject outputJSON = mainJSON.getJSONObject("output");
				// set list size
				listSize = outputJSON.getInt("size");
				
				if (outputJSON.has("thread")) {
					JSONArray postJSON = outputJSON.getJSONArray("thread");
					
					JSONObject postObj = null;
					Post post = null;
					
					for (int i = 0; i < postJSON.length(); i++) {
						postObj = postJSON.getJSONObject(i);
						post = new Post();
						
						post = getPostFromJSON(post, postObj);
						
						madapter.add(post);
					}
				} else if (outputJSON.has("notice")) {
					Post post = new Post();
					post.setNotice(outputJSON.getString("notice"));
					madapter.add(post);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return madapter;
	}
	
	
	public Post getPostFromJSON(Post post, JSONObject jsonObj) {
		try {
			//set post id
			if (jsonObj.has("post_id") && !jsonObj.isNull("post_id"))
				post.setPostId(jsonObj.getString("post_id"));
			
			//set full name
			if (jsonObj.has("full_name") && !jsonObj.isNull("full_name"))
				post.setFullname(Html.fromHtml(jsonObj.getString("full_name")).toString());
			
			//set total post
			if (jsonObj.has("total_post") && !jsonObj.isNull("total_post"))
				post.setTotalPost(jsonObj.getString("total_post"));
			
			// set text
			if (jsonObj.has("text") && !jsonObj.isNull("text"))
				post.setText(jsonObj.getString("text"));

            if (jsonObj.has("text_html") && !jsonObj.isNull("text_html"))
                post.setText(jsonObj.getString("text_html"));
			
			// set time phrase
			if (jsonObj.has("time_phrase") && !jsonObj.isNull("time_phrase"))
				post.setTimePhrase(Html.fromHtml(jsonObj.getString("time_phrase")).toString());
			
			//set user image
			if (jsonObj.has("user_image_path") && !jsonObj.isNull("user_image_path"))
				post.setUserImagePath(jsonObj.getString("user_image_path"));
			
			//set count
			if (jsonObj.has("count") && !jsonObj.isNull("count"))
				post.setCount(jsonObj.getInt("count"));
			
			//set liked
			if (jsonObj.has("is_liked") && !jsonObj.isNull("is_liked"))
				post.setIsLiked(jsonObj.getString("is_liked"));
			
			//set total like
			if (jsonObj.has("total_like") && !jsonObj.isNull("total_like"))
				post.setTotalLike(jsonObj.getString("total_like"));
			
			//set quote
			if (jsonObj.has("quote") && !jsonObj.isNull("quote"))
				post.setQuote(Html.fromHtml(jsonObj.getString("quote")).toString());
			
			//set link post
			if (jsonObj.has("share_feed_link") && !jsonObj.isNull("share_feed_link"))
				post.setLinkSharePost(jsonObj.getString("share_feed_link"));
			
			if (jsonObj.has("share_feed_link_url") && !jsonObj.isNull("share_feed_link_url"))
				post.setLinkShareUrlPost(jsonObj.getString("share_feed_link_url"));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return post;
	}
	
	/**
	 * Post adapter
	 */
	public class PostAdapter extends ArrayAdapter<Post> 
	{
		public PostAdapter(Context context) {
			super(context, 0);
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final Post item = getItem(position);
			PostHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.thread_item_row;
				
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				
				ImageView userImage = (ImageView) view.findViewById(R.id.post_user_image);
				TextView fullname = (TextView) view.findViewById(R.id.post_full_name);
				TextView totalPost = (TextView) view.findViewById(R.id.post_total);
                TextView text = (TextView) view.findViewById(R.id.post_content);
				TextView timePhrase = (TextView) view.findViewById(R.id.post_time);
				TextView notice = (TextView) view.findViewById(R.id.notice);
				TextView countTxt = (TextView) view.findViewById(R.id.post_header_count);
				TextView like = (TextView) view.findViewById(R.id.post_like);
				TextView totalLike = (TextView) view.findViewById(R.id.post_total_like);
				ImageView likeImage = (ImageView) view.findViewById(R.id.grid_item_like_icon);
				TextView quote = (TextView) view.findViewById(R.id.post_quote);
				TextView postShare = (TextView) view.findViewById(R.id.post_share);
				TextView postReport = (TextView) view.findViewById(R.id.post_report);
				LinearLayout totalLikeLayout = (LinearLayout) view.findViewById(R.id.thread_total_like_layoout);
				TextView postTxt = (TextView) view.findViewById(R.id.post_total_txt);
				
				postTxt.setText(phraseManager.getPhrase(getApplicationContext(), "forum.posts"));
				
				quote.setText(phraseManager.getPhrase(getApplicationContext(), "core.quote"));
				
				postShare.setText(phraseManager.getPhrase(getApplicationContext(), "feed.share"));
				postReport.setText(phraseManager.getPhrase(getApplicationContext(), "feed.report"));
				
				view.setTag(new PostHolder(userImage, fullname, totalPost,
						text, timePhrase, notice, countTxt, like, totalLike,
						quote, postShare, postReport, likeImage, totalLikeLayout));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof PostHolder) {
					holder = (PostHolder) tag;
				}
			}
			
			colorView.changeColorLikeIcon(holder.likeImage, user.getColor());
			
			//if no post
			if (item.getNotice() != null) {
				view.findViewById(R.id.post_layout).setVisibility(View.GONE);
				view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
				holder.notice.setText(item.getNotice());
				colorView.changeColorText(holder.notice, user.getColor());
			} else {
				view.findViewById(R.id.post_layout).setVisibility(View.VISIBLE);
				view.findViewById(R.id.notice_layout).setVisibility(View.GONE);
			}
			
			//set user image
			if (holder.postUserImage != null) {
				networkUntil.drawImageUrl(holder.postUserImage, item.getUserImagePath(), R.drawable.loading);
			}
			
			//set full name
			if (holder.postFullname != null) {
				if (item.getFullname() != null)
					holder.postFullname.setText(item.getFullname());
			}
			
			//set total post
			if (holder.postTotal != null) {
				if (item.getTotalPost() != null) 
					holder.postTotal.setText(item.getTotalPost());
			}
			
			//set content
			if (holder.postContent != null && item.getText() != null)
            {
                // interesting part starts from here here:
                Html.ImageGetter ig = imageGetter.create(position, item.getText(), holder.postContent);

                holder.postContent.setTag(position);
                holder.postContent.setText(Html.fromHtml(item.getText(), ig, null));
			}
			
			//set time
			if (holder.postTime != null) {
				if (item.getTimePhrase() != null)
					holder.postTime.setText(item.getTimePhrase());
			}
			
			//set count
			if (holder.count != null)
				holder.count.setText("#" + String.valueOf(item.getCount()));
			
			//set total like
			if (holder.totalLike != null) {
				holder.totalLike.setText(item.getTotalLike());
				colorView.changeColorText(holder.totalLike, user.getColor());
			}
			
			//set like
			if (holder.like != null) {
				if (item.getIsLiked() != null) {
					//unlike
					holder.like.setText(phraseManager.getPhrase(getApplicationContext(), "feed.unlike"));
				} else {
					//like
					holder.like.setText(phraseManager.getPhrase(getApplicationContext(), "feed.like"));
				}
				colorView.changeColorText(holder.like, user.getColor());
				
				holder.like.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						likePost(position);
					}
				});
			}
			
			//quote post
			if (holder.quote != null) {
				holder.quote.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getApplicationContext(), PostThreadActivity.class);
						intent.putExtra("thread_id", threadId);
						intent.putExtra("quote_text", item.getQuote());
						startActivity(intent);
					}
				});
			}
			
			// action share thread post
			if (holder.postShare != null) {
				colorView.changeColorText(holder.postShare, user.getColor());
				holder.postShare.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getApplicationContext(), ShareActivity.class);
						intent.putExtra("feed_link", item.getLinkSharePost());
						intent.putExtra("feed_link_url", item.getLinkShareUrlPost());
						startActivity(intent);
					}
				});
			}
			
			//action report thread post
			if (holder.postReport != null) {
				colorView.changeColorText(holder.postReport, user.getColor());
				holder.postReport.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getApplicationContext(), WebviewActivity.class);
						String reportUrl = Config.CORE_URL + Config.URL_REPORT + "type_" + "forum_post" + "/item_" + item.getPostId();
		            	intent.putExtra("html", reportUrl);
		            	startActivity(intent);
					}
				});
			}
			
			if (Integer.parseInt(item.getTotalLike()) > 0) {
				holder.totalLikeLayout.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(ThreadActivity.this, FriendActivity.class);
						intent.putExtra("type", "forum");
						intent.putExtra("item_id", item.getPostId());
						intent.putExtra("total_like", Integer.parseInt(item.getTotalLike()));
						startActivity(intent);
					}
				});
			}
			
			return view;
		}
	}
	
	/**
	 * Action like post
	 * @param position
	 */
	protected void likePost(int position) {
		Post post = (Post) pd.getItem(position);
		
		LikeAsyncTask likeAsyncTask = new LikeAsyncTask(getApplicationContext());
		if (post.getIsLiked() != null) {
			likeAsyncTask.execute(user.getTokenkey(), post.getPostId(), "forum_post", null, "unlike");
			if ((Integer.parseInt(post.getTotalLike())) > 0)
				post.setTotalLike("" + (Integer.parseInt(post.getTotalLike()) - 1));
			
			post.setIsLiked(null);
		} else {
			likeAsyncTask.execute(user.getTokenkey(), post.getPostId(), "forum_post", null, "like");
			post.setTotalLike("" + (Integer.parseInt(post.getTotalLike()) + 1));
			post.setIsLiked("1");
		}
		
		pd.notifyDataSetChanged();
		
	}
	
	/**
	 * Class Forum holder
	 */
	public class PostHolder {
		public ImageView postUserImage;
		public TextView postFullname;
		public TextView postTotal;
		public TextView postContent;
		public TextView postTime;
		public TextView notice;
		public TextView count;
		public TextView like;
		public TextView totalLike;
		public TextView quote;
		public TextView postShare;
		public TextView postReport;
		public ImageView likeImage;
		public LinearLayout totalLikeLayout;

		public PostHolder(ImageView postUserImage, TextView postFullname,
				TextView postTotal, TextView postContent, TextView postTime,
				TextView notice, TextView count, TextView like,
				TextView totalLike, TextView quote, TextView postShare,
				TextView postReport, ImageView likeImage,
				LinearLayout totalLikeLayout) {
			super();
			this.postUserImage = postUserImage;
			this.postFullname = postFullname;
			this.postTotal = postTotal;
			this.postContent = postContent;
			this.postTime = postTime;
			this.notice = notice;
			this.count = count;
			this.like = like;
			this.totalLike = totalLike;
			this.quote = quote;
			this.postShare = postShare;
			this.postReport = postReport;
			this.likeImage = likeImage;
			this.totalLikeLayout = totalLikeLayout;
		}
	}

	@Override
	protected void onResume() {
		if (pd != null && Config.post.isContinued() == true) {
			Config.post.setCount(pd.getCount() + 1);
			pd.insert(Config.post, pd.getCount());
			pd.notifyDataSetChanged();
			Config.post.setContinued(false);
		}
		super.onResume();
	}

	
}
