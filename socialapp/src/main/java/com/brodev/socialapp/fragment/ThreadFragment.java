package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.ForumThread;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.ThreadActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

@SuppressLint("ValidFragment")
public class ThreadFragment extends SherlockListFragment {

	private PhraseManager phraseManager;
	private NetworkUntil networkUntil = new NetworkUntil();
	private User user;
	private int page, currentpos, listSize;
	private ThreadAdapter ta = null;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private String method = null;
	private int forumId;
	private ColorView colorView;
	private boolean bNotice;

	public ThreadFragment(String method) {
		this.method = method;
	}
	
	/**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ThreadFragment newInstance(int index) 
    {
    	ThreadFragment f = new ThreadFragment(null);

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("forum_id", index);
        f.setArguments(args);

        return f;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.thread_fragment, container, false);

		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.thread_fragment_list);

		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				page = 1;
				// call feed refresh task to execute
				listSize = 1;
				new ThreadForumTask().execute(String.valueOf(page));
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				// Do the work to load more items at the end of list
				// here
				++page;
				new ThreadForumTask().execute(String.valueOf(page));
			}
		});
		return view;
	}

	@Override
	public View getView() {
		return super.getView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		user = (User) getView().getContext().getApplicationContext();

		actualListView = mPullRefreshListView.getRefreshableView();
		
		try {
			new ThreadForumTask().execute(String.valueOf(page));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
		page = 1;
		listSize = 0;
		bNotice = false;
		
		Bundle args = getArguments();
        if (args != null) {
            forumId = args.getInt("forum_id");
        }
        
		super.onCreate(savedInstanceState);
	}

	/**
	 * Function get result from GET
	 * 
	 * @return string
	 */
	public String getResultFromGET(String page) {

		String resultstring = null;

		if (ta != null && listSize <= 0) {
			return null;
		}

		try {
			// Use BasicNameValuePair to create GET data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
			pairs.add(new BasicNameValuePair("method", "accountapi.getMyThreads"));

			if (method != null)
				pairs.add(new BasicNameValuePair("thread", method));
			else
				pairs.add(new BasicNameValuePair("forum_id", "" + forumId));

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

		} catch (Exception ex) {
			return resultstring;
		}

		return resultstring;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		ForumThread thread = (ForumThread) actualListView.getAdapter().getItem(position);
		if (thread.getNotice() == null) {
			Intent intent = new Intent(this.getActivity(), ThreadActivity.class);
			intent.putExtra("thread_id", thread.getThreadId());
			intent.putExtra("title", thread.getThreadTitle());
			
			startActivity(intent);
		}
		
		super.onListItemClick(l, v, position, id);
	}

	public class ThreadForumTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String resultString = null;

			if (isCancelled()) {
				return null;
			}

			resultString = getResultFromGET(params[0]);

			return resultString;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					if (page == 1 || ta == null) {
						ta = new ThreadAdapter(getActivity().getApplicationContext());
					}

					ta = getThreadsAdapter(ta, result);
					listSize -= ta.getCount();

					if (ta != null) {
						if (page == 1) {
							actualListView.setAdapter(ta);
						} else {
							currentpos = getListView().getFirstVisiblePosition();

							actualListView.setAdapter(ta);
							getListView().setSelectionFromTop(currentpos + 1, 0);

							ta.notifyDataSetChanged();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			mPullRefreshListView.onRefreshComplete();
		}
	}

	public ThreadAdapter getThreadsAdapter(ThreadAdapter madapter, String resString) {
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);
				JSONObject outputJSON = mainJSON.getJSONObject("output");
				Object intervention = outputJSON.get("thread");

				// set list size
				listSize = outputJSON.getInt("size");

				// if intervention instance of json array
				if (intervention instanceof JSONArray) {
					JSONArray outJson = (JSONArray) intervention;

					JSONObject threadObj = null;
					ForumThread thread = null;

					for (int i = 0; i < outJson.length(); i++) {
						threadObj = outJson.getJSONObject(i);
						thread = new ForumThread();

						thread = getThreadFromJSON(thread, threadObj);

						madapter.add(thread);
					}
				} else if (intervention instanceof JSONObject) {
					JSONObject outJson = (JSONObject) intervention;

					if (outJson.has("notice") && !outJson.isNull("notice")) {
						ForumThread thread = new ForumThread();
						thread.setNotice(Html.fromHtml(outJson.getString("notice")).toString());
						madapter.add(thread);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return madapter;
	}

	/**
	 * Get thread from json
	 * 
	 * @param thread
	 * @param threadObj
	 * @return thread
	 */
	public ForumThread getThreadFromJSON(ForumThread thread, JSONObject threadObj) {
		try {
			// set thread id
			if (threadObj.has("thread_id") && !threadObj.isNull("thread_id"))
				thread.setThreadId(threadObj.getString("thread_id"));

			// set thread title
			if (threadObj.has("title") && !threadObj.isNull("title"))
				thread.setThreadTitle(Html.fromHtml(threadObj.getString("title")).toString());

			// set thread phrase
			if (threadObj.has("phrase") && !threadObj.isNull("phrase"))
				thread.setThreadPhrase(Html.fromHtml(threadObj.getString("phrase")).toString());

			// set thread reply
			if (threadObj.has("total_post") && !threadObj.isNull("total_post"))
				thread.setTotalReply(threadObj.getString("total_post"));

			// set thread view
			if (threadObj.has("total_view") && !threadObj.isNull("total_view"))
				thread.setTotalView(threadObj.getString("total_view"));

			// set user image
			if (threadObj.has("user_image_path") && !threadObj.isNull("user_image_path"))
				thread.setUserImage(threadObj.getString("user_image_path"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return thread;
	}
	
	/**
	 * Thread adapter
	 */
	public class ThreadAdapter extends ArrayAdapter<ForumThread> {
		public ThreadAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ForumThread item = getItem(position);
			ThreadHolder holder = null;

			if (view == null) {
				int layout = R.layout.thread_list_row;
				view = LayoutInflater.from(getContext()).inflate(layout, null);

				ImageView threadImage = (ImageView) view.findViewById(R.id.thread_image);
				TextView threadTitle = (TextView) view.findViewById(R.id.thread_title);
				TextView threadPhrase = (TextView) view.findViewById(R.id.thread_phrase);
				TextView totalReply = (TextView) view.findViewById(R.id.thread_total_reply);
				TextView totalView = (TextView) view.findViewById(R.id.thread_total_view);
				TextView notice = (TextView) view.findViewById(R.id.notice);

				// set phrase
				TextView replyTxt = (TextView) view.findViewById(R.id.thread_reply);
				replyTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "forum.replies"));

				TextView viewTxt = (TextView) view.findViewById(R.id.thread_view);
				viewTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "forum.views"));

				view.setTag(new ThreadHolder(threadImage, threadTitle,
						threadPhrase, totalReply, totalView, notice));

			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof ThreadHolder) {
					holder = (ThreadHolder) tag;
				}
			}

			if (item.getNotice() != null) {
				view.findViewById(R.id.thread_layout).setVisibility(View.GONE);
				view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
				holder.notice.setText(item.getNotice());
				bNotice = true;
				colorView.changeColorText(holder.notice, user.getColor());
			} else {
				view.findViewById(R.id.thread_layout).setVisibility(View.VISIBLE);
				view.findViewById(R.id.notice_layout).setVisibility(View.GONE);
			}

			if (holder.threadImage != null) {
				if (method != null && (method.equals("my-thread") || method.equals("new")))
					networkUntil.drawImageUrl(holder.threadImage, user.getUserImage(), R.drawable.loading);
				else
					networkUntil.drawImageUrl(holder.threadImage, item.getUserImage(), R.drawable.loading);
			}

			if (holder.threadTitle != null) {
				if (item.getThreadTitle() != null) {
					holder.threadTitle.setText(item.getThreadTitle());
					colorView.changeColorText(holder.threadTitle, user.getColor());
				}
			}

			if (holder.threadPhrase != null) {
				if (item.getThreadPhrase() != null) {
					holder.threadPhrase.setText(item.getThreadPhrase());
				}
			}

			if (holder.totalReply != null) {
				if (item.getTotalReply() != null) {
					holder.totalReply.setText(item.getTotalReply());
				}
			}

			if (holder.totalView != null) {
				if (item.getTotalView() != null) {
					holder.totalView.setText(item.getTotalView());
				}
			}

			return view;
		}
	}

	/**
	 * class thread holder
	 */
	public class ThreadHolder {
		public final ImageView threadImage;
		public final TextView threadTitle;
		public final TextView threadPhrase;
		public final TextView totalReply;
		public final TextView totalView;
		public final TextView notice;

		public ThreadHolder(ImageView threadImage, TextView threadTitle,
				TextView threadPhrase, TextView totalReply, TextView totalView,
				TextView notice) {
			super();
			this.threadImage = threadImage;
			this.threadTitle = threadTitle;
			this.threadPhrase = threadPhrase;
			this.totalReply = totalReply;
			this.totalView = totalView;
			this.notice = notice;
		}
	}

	@Override
	public void onResume() {
		if (ta != null && Config.forumThread.isContinued() == true) {
			if (bNotice) {
				ta.clear();	
			}
			ta.insert(Config.forumThread, 0);
			ta.notifyDataSetChanged();
			bNotice = false;
			Config.forumThread.setContinued(false);
		}
		super.onResume();
	}
}
