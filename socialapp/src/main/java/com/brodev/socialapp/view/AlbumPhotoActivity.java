package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.AlbumPhoto;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AlbumPhotoActivity extends SherlockListActivity {

	private User user;
	private NetworkUntil networkUtil = new NetworkUntil();
	private AlbumPhotoAdapter sa;
	private PhraseManager phraseManager;
	
	/**
	 * Broadcast Receiver 
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("album_name") && intent.hasExtra("album_id")) {
				String sAlbumName = intent.getExtras().getString("album_name");
				String sAlbumId = intent.getExtras().getString("album_id");
				AlbumPhoto albumPhoto = new AlbumPhoto(Integer.parseInt(sAlbumId), sAlbumName);
				sa.add(albumPhoto);
				sa.notifyDataSetChanged();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_photo);
		user = (User) getApplication().getApplicationContext();
		phraseManager = new PhraseManager(getApplicationContext());
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// change title
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "accountapi.select_an_album"));
		
		try {
			new AlbumTask().execute();	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter("com.brodev.socialapp.android.album.new"));
		
	}
	
	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mHandleMessageReceiver);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.album_photo, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_album:
			//action click add photo album
			Intent i = new Intent(AlbumPhotoActivity.this, CreateAlbumPhotoActivity.class);
			startActivity(i);
			return true;
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		sa.notifyDataSetChanged();
		AlbumPhoto ap = (AlbumPhoto) sa.getItem(position);
		Intent i = new Intent("com.brodev.socialapp.android.album");
		i.putExtra("album_name", ap.getName());
		i.putExtra("album_id", ap.getAlbumId());
		sendBroadcast(i);
		finish();
	}
	
	/**
	 * 
	 */
	public class AlbumTask extends AsyncTask<Void, Void, String> {
		String resultstring = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Void... params) {
			if (isCancelled()) {
				return null;
			}
			// Simulates a background task
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			
			//get result from get method
			resultstring = getResultFromGET();
			
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					sa = new AlbumPhotoAdapter(getApplicationContext());
					sa = getAlbumPhoto(sa, result);
					setListAdapter(sa);
					if (sa.getCount() == 0) {
						Toast.makeText(getApplicationContext(), 
								phraseManager.getPhrase(getApplicationContext(), "accountapi.you_don_t_have_any_album"), Toast.LENGTH_LONG).show();
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * function get result from get method
	 * @param sSearch
	 * @return result
	 */
	public String getResultFromGET() {
		
		String resultstring;
		
		//url link
		String url = null;
		if (Config.CORE_URL == null) {
			url = Config.makeUrl(user.getCoreUrl(), null, false);	
		} else {
			url = Config.makeUrl(Config.CORE_URL, null, false);
		}

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.getPhotoAlbum"));
		
		resultstring = networkUtil.makeHttpRequest(url, "GET", pairs);
		
		return resultstring;
	}
	
	/**
	 * 
	 * @param mAdapter
	 * @param resString
	 * @return
	 */
	public AlbumPhotoAdapter getAlbumPhoto(AlbumPhotoAdapter mAdapter, String resString) 
	{
		if (resString != null) 
		{
			try {
				JSONObject mainJSON = new JSONObject(resString);
				JSONArray outputJSON = mainJSON.getJSONArray("output");
				
				AlbumPhoto albumPhoto = null;
				JSONObject albumJSON = null;
				for (int i = 0; i < outputJSON.length(); i++) {
					albumPhoto = new AlbumPhoto();
					albumJSON = outputJSON.getJSONObject(i);
					
					//set album id
					albumPhoto.setAlbumId(Integer.parseInt(albumJSON.getString("album_id")));
					//set album name
					albumPhoto.setName(Html.fromHtml(albumJSON.getString("name")).toString());
					
					mAdapter.add(albumPhoto);
				}
			} catch (Exception ex) {
				return null;
			}
		}
		
		return mAdapter;
	}
	
	/**
	 * 
	 */
	public class AlbumPhotoAdapter extends ArrayAdapter<AlbumPhoto> 
	{
		public AlbumPhotoAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			AlbumPhoto item = getItem(position);
			AlbumPhotoHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.album_photo_row;
				
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				
				TextView itemTitle = (TextView) view.findViewById(R.id.item_name_album);
				view.setTag(new AlbumPhotoHolder(itemTitle));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof AlbumPhotoHolder) {
					holder = (AlbumPhotoHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				// set item title
				if (holder.name != null) {
					holder.name.setText(item.getName());
				}
			}
			
			return view;
		}
	}
	
	/**
	 *
	 */
	public class AlbumPhotoHolder {
		public final TextView name;
		public AlbumPhotoHolder(TextView name) {
			this.name = name;
		}
	}
}
