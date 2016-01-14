package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragment;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.StickerManager;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Sticker;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

public class StickerFragment extends SherlockFragment {

	private User user;
	private String stickerId, userId;
	public static final String STICKER_ID = "STICKER_ID";
	public static final String USER_ID = "USER_ID";
	private NetworkUntil networkUntil = new NetworkUntil();
	private StickerAdapter sa = null;
	private GridView grid;
	private View view;
	private ProgressBar loading;
	private StickerManager stickerManager;
	private JSONArray outputJSON;
	
	public static final StickerFragment newInstance(String userId, String stickerId) {
		StickerFragment f = new StickerFragment();
		Bundle bdl = new Bundle(1);
		bdl.putString(STICKER_ID, stickerId);
		bdl.putString(USER_ID, userId);
		f.setArguments(bdl);
		return f;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return super.getView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		user = (User) getActivity().getApplicationContext();
		stickerId = getArguments().getString(STICKER_ID);
		userId = getArguments().getString(USER_ID);
		
		try {
			if (stickerManager.loadSticker(getActivity().getApplicationContext(), stickerId).length() > 0 
					&& (System.currentTimeMillis() - stickerManager.loadTimeSticker(getActivity().getApplicationContext(), stickerId)) < Config.TIME_STICKER) {
				outputJSON = stickerManager.loadSticker(getActivity().getApplicationContext(), stickerId);
				setStickerGrid(outputJSON);
			} else {
				if (stickerManager.loadSticker(getActivity().getApplicationContext(), stickerId).length() > 0) {
					stickerManager.clearSticker(getActivity().getApplicationContext(), stickerId);
				}
				new GetStickerAsyncTask().execute(user.getChatServerUrl(), user.getChatKey(), stickerId, String.valueOf(0));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		super.onActivityCreated(savedInstanceState);
	}
	
	/**
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 * 
	 * @param context
	 * @param message
	 */
	public static void displayMessage(Context context, String type,
			String userId, String message, String image, String fullname, String stickerType) {
		Intent intent = new Intent(Config.DISPLAY_CHAT_ACTION);
		intent.putExtra("type", type);

		intent.putExtra("userId", userId);
		intent.putExtra("message", message);
		intent.putExtra("image", image);
		intent.putExtra("fullname", fullname);
		intent.putExtra("sticker_type", stickerType);

		context.sendBroadcast(intent);
	}
	
	public static void displaySticker(Context context, String type, String url, int height, int width, String stickerType) {
		Intent intent = new Intent(Config.DISPLAY_SHOW_STICKER_ACTION);
		intent.putExtra("type", type);

		intent.putExtra("stickerUrl", url);
		intent.putExtra("stickerHeight", height);
		intent.putExtra("stickerWidth", width);
		intent.putExtra("sticker_type", stickerType);

		context.sendBroadcast(intent);
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		stickerManager = new StickerManager(getActivity().getApplicationContext());
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.sticker_grid, container, false);
		
		loading = (ProgressBar) view.findViewById(R.id.sticker_loading);
		grid = (GridView) view.findViewById(R.id.sticker_grid);
		
		return view;
	}
	
	/**
	 * Class request get stickers by group id
	 */
	public class GetStickerAsyncTask extends AsyncTask<String, Void, String> {
		
		@Override
		protected String doInBackground(String... params) {
			
			if (isCancelled()) {
				return null;
			}
			
			String result = null;
			
			try {
				String getStickerUrl = params[0] + "/" + params[1] + Config.CHAT_STICKER_GROUP + params[2] + "/";
		
				if (!params[0].startsWith("http://"))
					getStickerUrl = "http://" + getStickerUrl;

				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("size", params[3]));
				
				result = networkUntil.makeHttpRequest(getStickerUrl, "GET", pairs);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			getSticker(result);
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * Get Sticker
	 */
	public void getSticker(String resSticker) {
		try {
			
			if (resSticker != null) {
				outputJSON = new JSONArray(resSticker);
				stickerManager.saveSticker(getActivity().getApplicationContext(), stickerId, outputJSON, System.currentTimeMillis());
			}
			
			setStickerGrid(outputJSON);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setStickerGrid(JSONArray outputJSON) {
		try {
			sa = new StickerAdapter(getActivity().getApplicationContext());
			JSONObject stickerJson = null;
			Sticker sticker = null;
			
			for (int i = 0; i < outputJSON.length(); i++) {
				sticker = new Sticker();
				stickerJson = outputJSON.getJSONObject(i);
				sticker.setStickerId(stickerJson.getString("id"));
				sticker.setStickerName(Html.fromHtml(stickerJson.getString("name")).toString());
				sticker.setStickerUrl(stickerJson.getString("url"));
				sticker.setStickerUrl100(stickerJson.getString("url_100"));
				
				if (stickerJson.has("height")) {
					sticker.setStickerHeight(stickerJson.getInt("height"));
				} else {
					sticker.setStickerHeight(60);
				}
				
				if (stickerJson.has("width")) {
					sticker.setStickerWidth(stickerJson.getInt("width"));
				} else {
					sticker.setStickerHeight(60);
				}
				
				if (stickerJson.has("type")) 
					sticker.setStickerType(stickerJson.getString("type"));
				
				sa.add(sticker);
			}
			
			loading.setVisibility(View.GONE);
			
			grid.setAdapter(sa);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public class StickerAdapter extends ArrayAdapter<Sticker> {

		public StickerAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final Sticker item = getItem(position);
			StickerViewHolder holder = null;

			if (view == null) {
				int layout = R.layout.sticker_item;

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				ImageView sticker = (ImageView) view.findViewById(R.id.sticker_item);

				view.setTag(new StickerViewHolder(sticker));

			}

			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof StickerViewHolder) {
					holder = (StickerViewHolder) tag;
				}
			}

			if (item != null && holder != null) {
				// set pages image
				if (holder.sticker != null) {
					if (item.getStickerUrl() != null) {
						networkUntil.drawImageUrl(holder.sticker, item.getStickerUrl(), R.drawable.loading);
					}
					holder.sticker.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							displayMessage(getActivity().getApplicationContext(), "send_sticker", userId, item.getStickerId(), user.getUserImage(), user.getFullname(), item.getStickerType());
							displaySticker(getActivity().getApplicationContext(), "send_sticker",  item.getStickerUrl(), item.getStickerHeight(), item.getStickerWidth(), item.getStickerType());
						}
					});
				}
			}
			return view;
		}
	}
	
	/**
	 * Class conversation view holder
	 */
	public class StickerViewHolder {
		public final ImageView sticker;

		public StickerViewHolder(ImageView sticker) {
			this.sticker = sticker;
		}
	}

}
