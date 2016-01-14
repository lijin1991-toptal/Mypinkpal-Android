package com.brodev.socialapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Album;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.PhotoGridActivity;
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

/**
 * Class album fragment
 * @author Nguyen Dat
 */
public class AlbumsFragment extends SherlockListFragment {
	
	private NetworkUntil network = new NetworkUntil();
	private AlbumAdapter ma;
	private int page = 1;
	private int totalItem = 10;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private User user;
	private int currentPos;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private FrameLayout searchLayout;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
	
	//phrase manager
	private PhraseManager phraseManager;
	private ColorView colorView;
	
	String user_id = null;
	String page_id = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
        user = (User) getActivity().getApplicationContext();

		super.onCreate(savedInstanceState);
	}	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		super.onActivityCreated(savedInstanceState);
		
		actualListView = mPullRefreshListView.getRefreshableView();
		
		Bundle bundle = getActivity().getIntent().getExtras();
		
		//init value
		user_id = null;
		page_id = null;
		currentPos = 0;
		
		if (bundle != null){
			if(getActivity().getIntent().hasExtra("user_id")){
				user_id = bundle.getString("user_id");
			}else if(getActivity().getIntent().hasExtra("page_id")){
				page_id = bundle.getString("page_id");
			}
		}
		
		try {
            loadPhoto();
		} catch(Exception ex) {
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//create view from friend_fragment xml
		View view = inflater.inflate(R.layout.album_fragment, container, false);
		
		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.album_fragment_list);

		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				page = 1;
				totalItem = 10;
				//call album refresh task to execute
				new AlbumRefreshTask().execute(page);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				++page;
				new AlbumsLoadMoreTask().execute(page);
			}

		});

        //no internet connection
        noInternetLayout = (RelativeLayout) view.findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) view.findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) view.findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) view.findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) view.findViewById(R.id.no_internet_image);

        //change color for no internet
        colorView.changeImageForNoInternet(noInternetImg, noInternetBtn, user.getColor());

        //set text for no internet element
        noInternetBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.try_again"));
        noInternetTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_title"));
        noInternetContent.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_content"));

        //action click load try again
        noInternetBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadPhoto();
                    }
                }, 2000);
            }
        });
		
		return view;
	}

    private void loadPhoto() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                mPullRefreshListView.setVisibility(View.VISIBLE);

                //fetch data
                new AlbumsLoadMoreTask().execute(page);
            } else {
                // display error
                mPullRefreshListView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mPullRefreshListView.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }
	
	Album album = null;
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) 
	{
		album = (Album) actualListView.getAdapter().getItem(position);
		//init intent
		if (album.getNotice() == null) {
			Intent intent = new Intent(this.getActivity(), PhotoGridActivity.class);
			
			if (album.getModule_id() != null) {
				intent.putExtra("module_id", album.getModule_id());
				intent.putExtra("group_id", album.getGroup_id());
			} else {
				intent.putExtra("album_user_id", album.getUser_id());
			}
			intent.putExtra("album_name", album.getName());
			intent.putExtra("total_photo", album.getAlbum_total());
			intent.putExtra("album_id", album.getAlbum_id());
			intent.putExtra("page", 1);
			
			startActivity(intent);
		}
		
	}

	/**
	 * function get result from get method
	 * @param page
	 * @return string result
	 */
	public String getResultFromGET(int page) 
	{
		String resultstring;
		
		if (totalItem < 10) {
			return null;
		}
		
		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getPhotoAlbums"));
		if(user_id != null){
			pairs.add(new BasicNameValuePair("user_id", "" + user_id));
		} else if(page_id!= null) {
			pairs.add(new BasicNameValuePair("module", "pages"));
			pairs.add(new BasicNameValuePair("item_id", "" + page_id));
		}
		pairs.add(new BasicNameValuePair("limit", "10"));
		pairs.add(new BasicNameValuePair("page", "" + page));
		
		// url request
		String URL = Config.makeUrl(user.getCoreUrl(), null, false);
		// request GET method to server
		resultstring = network.makeHttpRequest(URL, "GET", pairs);
					
		return resultstring;
	}
	
	/**
	 * Function create album adapter
	 * @return album Adapter
	 */
	public AlbumAdapter getAlbumAdapter(AlbumAdapter madapter, String resString) 
	{
		if (resString != null) 
		{	
			try {
				JSONObject mainJSON = new JSONObject(resString);
				
				//get api JSON
				JSONArray outJson = mainJSON.getJSONArray("output");
				
				Album album = null;	
				totalItem = outJson.length();
				
				//if get albums list empty 
				if (outJson.length() == 0 && page == 1) {
					album = new Album();
					album.setNotice(phraseManager.getPhrase(getActivity().getApplicationContext(), "photo.no_albums_found_here"));
					madapter.add(album);
				}
				
				for (int i = 0; i < outJson.length(); i++) {
						
					JSONObject JsonAlbum = outJson.getJSONObject(i);
					
					album = new Album();
					
					if(JsonAlbum.has("album_id")){
						album.setAlbum_id(JsonAlbum.getString("album_id"));
					}
					
					if(JsonAlbum.has("user_id")){
						album.setUser_id(JsonAlbum.getString("user_id"));
					}
					
					if(JsonAlbum.has("name")){
						album.setName(JsonAlbum.getString("name"));
					}
					
					if(JsonAlbum.has("description")){
						album.setDescription(JsonAlbum.getString("description"));
					}
					
					if(JsonAlbum.has("time_phrase")){
						album.setTime_phrase(JsonAlbum.getString("time_phrase"));
					}
					
					if(JsonAlbum.has("total_photo")){
						album.setAlbum_total(JsonAlbum.getString("total_photo"));
					}
					
					if(JsonAlbum.has("photo_sizes")){
						album.setAlbum_pic(JsonAlbum.getJSONObject("photo_sizes").getString("100"));
					}
					
					if(JsonAlbum.has("module_id") && !JsonAlbum.isNull("module_id")){
						album.setModule_id(JsonAlbum.getString("module_id"));
					}
					
					if(JsonAlbum.has("group_id") && !JsonAlbum.isNull("group_id")){
						album.setGroup_id(JsonAlbum.getString("group_id"));
					}
					
					madapter.add(album);

				}
				
			} catch(Exception ex) {
				ex.printStackTrace();
                mPullRefreshListView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
				return null;
			}
		}
		return madapter;
		
	}
	
	
	/**
	 * Load more album list of logged user 
	 * @author Nguyen Dat
	 */
	public class AlbumsLoadMoreTask extends AsyncTask<Integer, Void, String> 
	{
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

            try {
                //get result from get method
                result = getResultFromGET(params[0]);
            } catch (Exception ex) {
                return null;
            }
			
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			try {
                if (result != null) {
                    if (ma == null) {
                        ma = new AlbumAdapter(getActivity());
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.view_all_photo, getListView(), false);

                        RelativeLayout Recent_photo = (RelativeLayout)header.findViewById(R.id.all_photo_relativeLayout);
                        TextView photoAllText = (TextView) header.findViewById(R.id.all_photo_text);
                        photoAllText.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "photo.recent_photos"));

                        TextView photoClickText = (TextView) header.findViewById(R.id.all_photo_text_click);
                        photoClickText.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.touch_here_to_view_all_photo"));

                        //change text color
                        colorView.changeColorText(photoAllText, user.getColor());

                        Recent_photo.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), PhotoGridActivity.class);
                                if(user_id != null || page_id != null) {
                                    if (page_id != null) {
                                        intent.putExtra("page_id", page_id);
                                    } else {
                                        intent.putExtra("user_id", user_id);
                                    }
                                }
                                intent.putExtra("album_name", phraseManager.getPhrase(getActivity().getApplicationContext(), "photo.recent_photos"));
                                startActivity(intent);
                            }
                        });
                        actualListView.addHeaderView(header, null, false);
                    }

                    ma = getAlbumAdapter(ma, result);

                    if (ma != null) {
                        currentPos = getListView().getFirstVisiblePosition();
                        actualListView.setAdapter(ma);
                        getListView().setSelectionFromTop(currentPos + 1, 0);
                    }

                    // We need notify the adapter that the data have been changed
                    ma.notifyDataSetChanged();
                }
				// Call onLoadMoreComplete when the LoadMore task, has finished
				mPullRefreshListView.onRefreshComplete();
				
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
			super.onPostExecute(result);
		}
		

	}
	
	/**
	 * pull to refresh album list 
	 * @author Nguyen Dat
	 */
	public class AlbumRefreshTask extends AsyncTask<Integer, Void, String> 
	{
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

            try {
                //get result from get method
                result = getResultFromGET(params[0]);
            } catch (Exception ex) {
                return null;
            }

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			try {
                if (result != null) {
                    //create new album adapter
                    ma = new AlbumAdapter(getActivity());

                    ma = getAlbumAdapter(ma, result);

                    if(ma != null) {
                        actualListView.setAdapter(ma);
                    }
                }

				// Call onLoadMoreComplete when the LoadMore task, has finished
				mPullRefreshListView.onRefreshComplete();
				
			} catch(Exception ex) {
				ex.printStackTrace();
			}

			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * Class album view holder
	 * @author Nguyen Dat
	 */
	public class AlbumViewHolder {
		public final ImageView imageHolder;
		public final TextView name;
		public final TextView description;
		public final TextView time_phrase;
		public final TextView total_photo;
		//notice
		public final TextView notice;

		public AlbumViewHolder(ImageView icon, TextView name, TextView description, TextView time_phrase, TextView total_photo, TextView notice) {
			this.imageHolder = icon;
			this.name = name;
			this.description = description;
			this.time_phrase = time_phrase;
			this.total_photo = total_photo;
			this.notice = notice;
		}
	}
	
	/**
	 * Class Email Adapter
	 * @author Nguyen Dat
	 */
	public class AlbumAdapter extends ArrayAdapter<Album> 
	{
		public AlbumAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View view = convertView;
			Album item = getItem(position);
			AlbumViewHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.photo_album_row;
				
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				//call element from xml
				ImageView icon = (ImageView) view.findViewById(R.id.album_image);
				TextView name = (TextView) view.findViewById(R.id.album_name);
				TextView description = (TextView) view.findViewById(R.id.album_description);
				TextView time_phrase = (TextView) view.findViewById(R.id.album_time_phrase);
				TextView total_photo = (TextView) view.findViewById(R.id.album_total);
				TextView notice = (TextView) view.findViewById(R.id.notice);
				
				view.setTag(new AlbumViewHolder(icon, name, description, time_phrase, total_photo, notice));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof AlbumViewHolder) {
					holder = (AlbumViewHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				
				if (item.getNotice() != null) {
					//invisible
					view.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
					//show notice
					view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
					holder.notice.setText(item.getNotice());
					colorView.changeColorText(holder.notice, user.getColor());
				}
				
				//set image friend;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getAlbum_pic())) {
						network.drawImageUrl(holder.imageHolder, item.getAlbum_pic(), R.drawable.loading);
					}
				}
				//set full name;
				if (holder.name != null) {
					holder.name.setText(item.getName());
				}
				//set gender
				if (holder.description != null) {
					holder.description.setText(item.getDescription());
				}
				//set birthday
				if (holder.time_phrase != null) {
					holder.time_phrase.setText(item.getTime_phrase());
				}
				
				if (holder.total_photo != null) {
					holder.total_photo.setText("(" + item.getAlbum_total() + ")");
					colorView.changeColorText(holder.total_photo, user.getColor());
				}
			}
			
			return view;
		}
	}
	
}
