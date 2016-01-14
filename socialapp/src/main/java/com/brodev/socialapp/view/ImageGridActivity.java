/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;


import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import android.widget.TextView;


import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.config.Config;

import com.brodev.socialapp.entity.Album;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImageGridActivity extends SherlockFragmentActivity {

	String[] imageUrls;
	
	String[] imagePager;
	
	String[] imagePhotoIds;
	
	String[] imageHasLike;
	
	String[] imageFeedisLike;
	
	String[] imageTotal_like;
	
	String[] imageTotal_comment;
	
	String[] imageItemid;
	
	String[] imageType;
	
	NetworkUntil networkUntil = new NetworkUntil();

	int page = 1;
	
	LinearLayout scroll_photo;
	
	AlbumAdapter albuma;
	
	ArrayList<String> stringArrayList;
	
	ArrayList<String> PagerList;
	
	ArrayList<String> ImagesId;
	
	ArrayList<String> HasLike;
	
	ArrayList<String> FeedisLike;
	
	ArrayList<String> Total_like;
	
	ArrayList<String> Total_comment;
	
	ArrayList<String> Itemid;
	
	ArrayList<String> Type;
	
	
	List<Album> arrayOfList;
	
	int viewmore = 0;
	
	private ImageAdapter adapter;
	
	String album_user_id = null;

	int imageCount = 0;
	
	int itemView = 20;
	
	int HeighRow = 0;
	
	float countImage = 1;
	
	String user_id;
	
	String page_id;
	
	String module_id = null;
	
	String group_id = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photos_index);
		
		String album_id = null;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		scroll_photo = (LinearLayout)findViewById(R.id.scroll_photo);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null){
			if(this.getIntent().hasExtra("user_id")){
				user_id = bundle.getString("user_id");
				new ShowAlbumPhoto().execute(user_id, null);
				new ShowGridPhoto().execute(user_id, null);
			}else if(this.getIntent().hasExtra("album_user_id")){
				album_user_id = bundle.getString("album_user_id");
				album_id = bundle.getString("album_id");
				new ShowGridPhoto().execute(album_user_id, album_id);
			}else if(this.getIntent().hasExtra("page_id"))	{
				page_id = bundle.getString("page_id");
				new ShowAlbumPhoto().execute(page_id, null);
				new ShowGridPhoto().execute(page_id, null);
			}else if(this.getIntent().hasExtra("module_id")){
				album_id = bundle.getString("album_id");
				module_id = bundle.getString("module_id");
				group_id = bundle.getString("group_id");
				new ShowGridPhoto().execute(group_id, album_id);
			}
			
		}else{
			new ShowAlbumPhoto().execute(null, null);
			new ShowGridPhoto().execute(null, null);
		}	
		
		
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
	
	public class ShowGridPhoto extends AsyncTask<String, Void, String> {
		User user = (User) getApplicationContext().getApplicationContext();
		String resultstring = null;
		JSONObject mainJSON = null;
		JSONArray outJson = null;
		JSONObject total = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				
				// Use BasicNameValuePair to create GET data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method", "accountapi.getPhotos"));
				if(page > 1){
					pairs.add(new BasicNameValuePair("page", "" + page));
				}else{
					pairs.add(new BasicNameValuePair("page", "undefined"));
					
				}
				
				if(params[0] != null){
					if(user_id != null){
						pairs.add(new BasicNameValuePair("user_id", "" + params[0]));
					}else if(page_id != null){
						pairs.add(new BasicNameValuePair("module", "pages"));
						pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
					}else if(module_id !=null){
						pairs.add(new BasicNameValuePair("module", "pages"));
						pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
					}
					if(params[1] != null){
						pairs.add(new BasicNameValuePair("album_id", "" + params[1]));
					}
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
				
				
				JSONObject mainJSON = new JSONObject(resultstring);
				JSONArray outJson = mainJSON.getJSONArray("output");
				
				if(stringArrayList == null){
					stringArrayList = new ArrayList<String>();
					PagerList = new ArrayList<String>();
					ImagesId = new ArrayList<String>();
					
					HasLike = new ArrayList<String>();
					FeedisLike = new ArrayList<String>();
					Total_like = new ArrayList<String>();
					Total_comment = new ArrayList<String>();
					Itemid = new ArrayList<String>();
					Type = new ArrayList<String>();
				}
				
				
				if(outJson.length() < 20){
					viewmore = 1;
				}else{
					float leng = outJson.length();
					float itemv = itemView;
					countImage = leng/itemv;
					
				}
				
				System.out.println(outJson.length());
				System.out.println(countImage);				
							
				for (int i = 0; i < outJson.length(); i++) {
						
					JSONObject JsonPic = outJson.getJSONObject(i);
					
					stringArrayList.add(JsonPic.getJSONObject("photo_sizes").getString("100"));
					
					ImagesId.add(JsonPic.getString("photo_id"));
					
					if(JsonPic.has("feed_total_like")){
						HasLike.add(JsonPic.getString("feed_total_like"));
						Total_like.add(JsonPic.getString("feed_total_like"));
					}
					
					if(JsonPic.has("item_id")){
						Itemid.add(JsonPic.getString("item_id"));
					}
					if(!JsonPic.isNull("feed_is_liked") && JsonPic.getString("feed_is_liked") != "false"){
						if(!"".equals(JsonPic.getString("feed_is_liked"))){
							FeedisLike.add("feed_is_liked");
						}else{
							FeedisLike.add("null");
						}
						
					}
					if(JsonPic.has("total_comment")){
						Total_comment.add(JsonPic.getString("total_comment"));
					}
					
					Type.add(JsonPic.getJSONObject("social_app").getString("type_id"));
					
					if(Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD){
						
						PagerList.add(JsonPic.getJSONObject("photo_sizes").getString("240"));
					}else{
						
						PagerList.add(JsonPic.getJSONObject("photo_sizes").getString("500"));
					}
						
				}
				
				imagePhotoIds = ImagesId.toArray(new String[ImagesId.size()]);
				imageUrls = stringArrayList.toArray(new String[stringArrayList.size()]);
				imageCount = stringArrayList.size();
				imagePager = PagerList.toArray(new String[stringArrayList.size()]);
				
				imageHasLike = HasLike.toArray(new String[HasLike.size()]);
				
				imageFeedisLike = FeedisLike.toArray(new String[FeedisLike.size()]);
				
				imageTotal_like = Total_like.toArray(new String[Total_like.size()]);
				
				imageTotal_comment = Total_comment.toArray(new String[Total_comment.size()]);
				
				imageItemid = Itemid.toArray(new String[Itemid.size()]);
				
				imageType = Type.toArray(new String[Type.size()]);
				
				imageType = Type.toArray(new String[Type.size()]);
				
			} catch(Exception ex) {
			}	
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {				
				
				if(adapter == null){
					adapter = new ImageAdapter();
				}
			    		
				final GridView listView = (GridView)findViewById(R.id.gridview);
				
				float RowG = imageCount;
				float RowF = itemView;
				final float Row12 = RowG/4;
				final float Row3 = RowF/4;
				if(adapter != null){
					listView.setAdapter(adapter);
				}
				
				//dynamic gridview height
				if(HeighRow == 0){
					if(album_user_id != null && viewmore == 0){
						listView.getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() 
			            {
			            @SuppressWarnings("deprecation")
						@Override
			            public void onGlobalLayout() 
			                {
			            	listView.getViewTreeObserver().removeGlobalOnLayoutListener( this );
			                View lastChild = listView.getChildAt( listView.getChildCount() - 1 );
			                HeighRow = lastChild.getBottom();
			                listView.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, (int) (lastChild.getBottom()*Math.round(Row3 + 0.4)) ));
			                }
			            });
					}else{
						listView.getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() 
			            {
			            @SuppressWarnings("deprecation")
						@Override
			            public void onGlobalLayout() 
			                {
			            	listView.getViewTreeObserver().removeGlobalOnLayoutListener( this );
			                View lastChild = listView.getChildAt( listView.getChildCount() - 1 );
			                HeighRow = lastChild.getBottom();
			                listView.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, (int) (lastChild.getBottom()*Math.round(Row12 + 0.4)) ));
			                }
			            });
					}
					
				}else{
					listView.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, (int) (HeighRow*Math.round(Row12 + 0.4)) )) ;
				}
			
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						startImagePagerActivity(position);
					}
				});
				
			}
		}
	}
	
	public class ImageAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			if(album_user_id != null && viewmore == 0)return itemView;
				return imageUrls.length;
			
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ImageView imageView;
			if (convertView == null) {
				imageView = (ImageView) getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
			} else {
				imageView = (ImageView) convertView;
			}
			networkUntil.drawImageUrl(imageView, imageUrls[position], R.drawable.loading);

			return imageView;
		}
	}
	
	
	public class ShowAlbumPhoto extends AsyncTask<String, Void, String> {
		User user = (User) getApplicationContext().getApplicationContext();
		String resultstring = null;
		JSONObject mainJSON = null;
		JSONArray outJson = null;
		JSONObject total = null;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				
				// Use BasicNameValuePair to create GET data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method", "accountapi.getPhotoAlbums"));
				
				if(params[0] != null){
					if(user_id != null){
						pairs.add(new BasicNameValuePair("user_id", "" + params[0]));
					}else if(page_id != null){
						pairs.add(new BasicNameValuePair("module", "pages"));
						pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
					}
				}
				pairs.add(new BasicNameValuePair("limit", "2"));
				pairs.add(new BasicNameValuePair("page", "0"));
				
				// url request
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}
				
				// request GET method to server
				
				resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
				
				JSONObject mainJSON = new JSONObject(resultstring);
				JSONArray outJson = mainJSON.getJSONArray("output");
				
				arrayOfList = new ArrayList<Album>();
						
				for (int i = 0; i < outJson.length(); i++) {
						
					JSONObject JsonAlbum = outJson.getJSONObject(i);
					
					Album album = new Album();
					
					album.setAlbum_id(JsonAlbum.getString("album_id"));
					
					album.setUser_id(JsonAlbum.getString("user_id"));
					
					album.setName(JsonAlbum.getString("name"));
					
					album.setDescription(JsonAlbum.getString("description"));
					
					album.setTime_phrase(JsonAlbum.getString("time_phrase"));
					
					album.setAlbum_total(JsonAlbum.getString("total_photo"));
					
					album.setAlbum_pic(JsonAlbum.getJSONObject("photo_sizes").getString("100"));
					
					arrayOfList.add(album);
					
					
				}
				 
			} catch(Exception ex) {
				//Log.i("comment item", ex.getMessage());
			}

			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				
			    
				ListView AlbumList = (ListView)findViewById(R.id.photo_listview);
				
				//AlbumList.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, GridHeight));
				if(new AlbumAdapter() != null){
					albuma = new AlbumAdapter();
				}
				
				AlbumList.setAdapter(albuma);
				
				int list_height = getListViewHeight(AlbumList);

				AlbumList.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, list_height));
			   
				//int ListHeight = getItemHeightofListView(AlbumList);
				
				System.out.println(list_height);
				
				AlbumList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Album item = arrayOfList.get(position);
						//init intent
						Intent intent = new Intent(getApplicationContext(), ImageGridActivity.class);
						intent.putExtra("album_user_id", item.getUser_id());
						intent.putExtra("album_id", item.getAlbum_id());
						intent.putExtra("page", 1);
						
						startActivity(intent);
					}
				});

			}
			
		}
		
		 private int getListViewHeight(ListView list) {
	          ListAdapter adapter = list.getAdapter();

	          int listviewHeight = 0;

	          list.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED), 
	                       MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

	          listviewHeight = list.getMeasuredHeight() * adapter.getCount() + (adapter.getCount() * list.getDividerHeight());

	          return listviewHeight;
	    }

	}
	
	
	private void startImagePagerActivity(int position) {
		Intent intent = new Intent(this, ImagePagerActivity.class);
		intent.putExtra("image", imagePager);
		intent.putExtra("photo_id", imagePhotoIds);		
		intent.putExtra("HasLike", imageHasLike);
		intent.putExtra("FeedisLike", imageFeedisLike);
		intent.putExtra("Total_like", imageTotal_like);
		intent.putExtra("Total_comment", imageTotal_comment);
		intent.putExtra("Itemid", imageItemid);
		intent.putExtra("Type", imageType);
		
		intent.putExtra("position", position);
		startActivity(intent);
	}
	
	public class AlbumAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return arrayOfList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			AlbumViewHolder holder = null;
			Album item = arrayOfList.get(position);
			if (view == null) {	
				view = getLayoutInflater().inflate(R.layout.photo_album_row, null);
				
				ImageView icon = (ImageView) view.findViewById(R.id.album_image);
				TextView name = (TextView) view.findViewById(R.id.album_name);
				TextView description = (TextView) view.findViewById(R.id.album_description);
				TextView time_phrase = (TextView) view.findViewById(R.id.album_time_phrase);
				TextView total_photo = (TextView) view.findViewById(R.id.album_total);
				view.setTag(new AlbumViewHolder(icon, name, description, time_phrase, total_photo));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof AlbumViewHolder) {
					holder = (AlbumViewHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				//set image friend;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getAlbum_pic())) {
						networkUntil.drawImageUrl(holder.imageHolder, item.getAlbum_pic(), R.drawable.loading);
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
				}
				
			}
			
			
			
			return view;
		}
	}
	
	public class AlbumViewHolder {
		public final ImageView imageHolder;
		public final TextView name;
		public final TextView description;
		public final TextView time_phrase;
		public final TextView total_photo;

		public AlbumViewHolder(ImageView icon, TextView name, TextView description, TextView time_phrase, TextView total_photo) {
			this.imageHolder = icon;
			this.name = name;
			this.description = description;
			this.time_phrase = time_phrase;
			this.total_photo = total_photo;
		}
	}
	
}