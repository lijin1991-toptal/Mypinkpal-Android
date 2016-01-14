package com.brodev.socialapp.fragment;

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

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Forum;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.ForumActivity;
import com.brodev.socialapp.view.ThreadActivity;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForumFragment extends SherlockListFragment {

	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private ForumAdapter fa;
	private PhraseManager phraseManager;
	private int forumId;
	private ColorView colorView;
	
	/**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ForumFragment newInstance(int index) 
    {
    	ForumFragment f = new ForumFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("forum_id", index);
        f.setArguments(args);

        return f;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
		user = (User) getActivity().getApplicationContext();
		
		Bundle args = getArguments();
        if (args != null) {
            forumId = args.getInt("forum_id", 0);
        }
        
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		try {
			new GetForumsTask().execute();
		} catch (Exception ex) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// create view from forum_fragment xml
		View view = inflater.inflate(R.layout.forum_fragment, container, false);

		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Forum forum = (Forum) getListAdapter().getItem(position);
		if (forum.getNotice() == null) {
			Intent intent = new Intent(this.getActivity(), ForumActivity.class);
			intent.putExtra("forum_id", forum.getForumId());
			intent.putExtra("sub_forum", String.valueOf(forum.getIsCategory()));
			intent.putExtra("category", forum.getName());

			startActivity(intent);
		}
		

		super.onListItemClick(l, v, position, id);
	}

	/**
	 * Class get forums
	 * 
	 * @author ducpham
	 */
	public class GetForumsTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			String resultString = null;

			if (isCancelled()) {
				return null;
			}
			// Simulates a background task
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			resultString = getResultFromGET();

			return resultString;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					fa = new ForumAdapter(getActivity());

					fa = getForumsAdapter(fa, result);

					if (fa != null) {
						setListAdapter(fa);
					}
				} catch (Exception ex) {

				}
			}
		}
	}

	/**
	 * Function get result from GET
	 * 
	 * @return string
	 */
	public String getResultFromGET() {

		String resultstring = null;

		try {
			// Use BasicNameValuePair to create GET data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
			if (forumId != 0) {
				pairs.add(new BasicNameValuePair("method", "accountapi.getSubForums"));
				pairs.add(new BasicNameValuePair("forum_id", "" + forumId));
			} else {
				pairs.add(new BasicNameValuePair("method", "accountapi.getForums"));
			}
			
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
     *
     * @param madapter
     * @param resString
     * @return
     */
	public ForumAdapter getForumsAdapter(ForumAdapter madapter, String resString) {

		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);

				Object intervention = mainJSON.get("output");

				// if intervention instance of json array
				if (intervention instanceof JSONArray) {
					JSONArray outJson = (JSONArray) intervention;

					JSONObject forumObj = null;
					Forum forum = null;

					for (int i = 0; i < outJson.length(); i++) {
						forumObj = outJson.getJSONObject(i);
						forum = new Forum();

						forum = getForumFromJSON(forum, forumObj);

						madapter.add(forum);

						if (forumId == 0) {
						// get sub forum
						JSONArray subForumObj = forumObj.getJSONArray("sub_forum");
						
							for (int j = 0; j < subForumObj.length(); j++) {
								JSONObject _subForumObj = subForumObj.getJSONObject(j);
	
								Forum subforum = new Forum();
								subforum = getForumFromJSON(subforum, _subForumObj);
								madapter.add(subforum);
							}
						}
					}
				} else if (intervention instanceof JSONObject) {
					JSONObject outputJSON = (JSONObject) intervention;
					Forum forum = new Forum();
					forum.setNotice(Html.fromHtml(outputJSON.getString("notice")).toString());
					
					madapter.add(forum);
				}
			} catch (Exception ex) {
				return null;
			}
		}
		return madapter;
	}

	public Forum getForumFromJSON(Forum forum, JSONObject forumObj) {
		try {
			// set forum id
			forum.setForumId(Integer.parseInt(forumObj.getString("forum_id")));

			// set is category
			forum.setIsCategory(Integer.parseInt(forumObj.getString("is_category")));

			// set parent id
			forum.setParentId(Integer.parseInt(forumObj.getString("parent_id")));

			// set name
			forum.setName(Html.fromHtml(forumObj.getString("name")).toString());

			// set total thread
			forum.setTotalThread(Integer.parseInt(forumObj.getString("total_thread")));

			// set total post
			forum.setTotalPost(Integer.parseInt(forumObj.getString("total_post")));

			// set thread id in forum
			if (forumObj.has("thread_id") && !forumObj.isNull("thread_id"))
				forum.setThreadId(forumObj.getString("thread_id"));

			// set thread title in forum
			if (forumObj.has("thread_title")
					&& !forumObj.isNull("thread_title"))
				forum.setThreadTitle(Html.fromHtml(forumObj.getString("thread_title")).toString());

			// set phrase of thread in forum
			if (forumObj.has("phrase") && !forumObj.isNull("phrase"))
				forum.setPhraseThread(Html.fromHtml(forumObj.getString("phrase")).toString());

		} catch (Exception ex) {

		}

		return forum;
	}

	/**
	 * Change color image
	 * @param blogImg
	 * @param colorCode
	 */
	private void changeColorImage(ImageView blogImg, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			blogImg.setImageResource(R.drawable.brown_blog_icon);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			blogImg.setImageResource(R.drawable.pink_blog_icon);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			blogImg.setImageResource(R.drawable.green_blog_icon);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			blogImg.setImageResource(R.drawable.violet_blog_icon);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			blogImg.setImageResource(R.drawable.red_blog_icon);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			blogImg.setImageResource(R.drawable.dark_violet_blog_icon);
		} else {
			blogImg.setImageResource(R.drawable.blog_icon);
		}
	}
	
	/**
	 * Forum adapter
	 */
	public class ForumAdapter extends ArrayAdapter<Forum> {
		public ForumAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final Forum item = getItem(position);
			ForumHolder holder = null;

			if (view == null) {
				int layout = R.layout.forum_list_row;
				view = LayoutInflater.from(getContext()).inflate(layout, null);

				TextView headerTxt = (TextView) view.findViewById(R.id.forum_header_txt);
				ImageView imageForum = (ImageView) view.findViewById(R.id.forum_image);
				TextView nameTxt = (TextView) view.findViewById(R.id.forum_content_txt);
				TextView totalThread = (TextView) view.findViewById(R.id.forum_total_thread);
				TextView totalPost = (TextView) view.findViewById(R.id.forum_total_post);
				LinearLayout threadLayout = (LinearLayout) view.findViewById(R.id.thread_layout);
				TextView threadTitle = (TextView) view.findViewById(R.id.forum_thread_title);
				TextView threadPhrase = (TextView) view.findViewById(R.id.forum_thread_phrase);

				// set phrase threads
				TextView threadTxt = (TextView) view.findViewById(R.id.forum_threads_txt);
				threadTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "forum.threads"));

				TextView postTxt = (TextView) view.findViewById(R.id.forum_post_txt);
				postTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "forum.posts"));

				TextView notice = (TextView) view.findViewById(R.id.notice);
				
				view.setTag(new ForumHolder(headerTxt, imageForum, nameTxt,
						totalThread, totalPost, threadLayout, threadTitle,
						threadPhrase, notice));

			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof ForumHolder) {
					holder = (ForumHolder) tag;
				}
			}

			if (item != null && holder != null) {
				//change color 
				changeColorImage(holder.imageView, user.getColor());
				
				//set notice 
				if (item.getNotice() != null) {
					view.findViewById(R.id.content_layout).setVisibility(View.GONE);
					view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
					colorView.changeColorText(holder.notice, user.getColor());
				} else {
					view.findViewById(R.id.content_layout).setVisibility(View.VISIBLE);
					view.findViewById(R.id.notice_layout).setVisibility(View.GONE);
				}
				
				if (item.getIsCategory() == 1) {
					view.findViewById(R.id.header_layout_view).setVisibility(View.VISIBLE);
					view.findViewById(R.id.content_layout_view).setVisibility(View.GONE);
					if (holder.headerName != null) {
						holder.headerName.setText(item.getName());
					}
				} else {
					view.findViewById(R.id.header_layout_view).setVisibility(View.GONE);
					view.findViewById(R.id.content_layout_view).setVisibility(View.VISIBLE);

					if (holder.name != null) {
						holder.name.setText(item.getName());
						colorView.changeColorText(holder.name, user.getColor());
					}

					if (holder.totalThread != null) {
						holder.totalThread.setText(String.valueOf(item.getTotalThread()));
					}

					if (holder.totalPost != null) {
						holder.totalPost.setText(String.valueOf(item.getTotalPost()));
					}

					if (holder.threadLayout != null) {
						if (item.getThreadTitle() != null) {
							holder.threadLayout.setVisibility(View.VISIBLE);
							holder.threadTitle.setText(item.getThreadTitle());
							holder.threadPhrase.setText(item.getPhraseThread());
							//action click thread in forum
							holder.threadLayout.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(getActivity(), ThreadActivity.class);
									intent.putExtra("thread_id", item.getThreadId());
									intent.putExtra("title", item.getThreadTitle());
									
									startActivity(intent);
								}
							});
						} else {
							holder.threadLayout.setVisibility(View.GONE);
						}
					}

				}

			}

			return view;
		}
	}

	/**
	 * Class Forum holder
	 */
	public class ForumHolder {
		public TextView headerName;
		public ImageView imageView;
		public TextView name;
		public TextView totalThread;
		public TextView totalPost;
		public LinearLayout threadLayout;
		public TextView threadTitle;
		public TextView threadPhrase;
		public TextView notice;

		public ForumHolder(TextView headerName, ImageView imageView,
				TextView name, TextView totalThread, TextView totalPost,
				LinearLayout threadLayout, TextView threadTitle,
				TextView threadPhrase, TextView notice) {
			this.headerName = headerName;
			this.imageView = imageView;
			this.name = name;
			this.totalThread = totalThread;
			this.totalPost = totalPost;
			this.threadLayout = threadLayout;
			this.threadTitle = threadTitle;
			this.threadPhrase = threadPhrase;
			this.notice = notice;
		}
	}
}
