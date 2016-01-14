package com.brodev.socialapp.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.CustomMultiPartEntity;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.manager.PrivacyManager;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Feed;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("DefaultLocale")
public class ImageUpload extends SherlockActivity {
	
	private ImageView imgView, userImg;
	private RelativeLayout takePhoto, ibAlbum, checkinBtn;
	private EditText caption;
	private TextView photoTxt, albumTxt, checkInTxt, checkInLocation;
	private ArrayList<Bitmap> aBitmap;
	private LinearLayout horizontalView;
	private ProgressDialog dialog;
	private RelativeLayout privacyLayout;
	private PrivacyManager privacyManager;
	private NetworkUntil networkUntil = new NetworkUntil();
	private User user;
	private static  String page_id = null;
	private static  String user_id = null;
	private static  String event_id = null;
	private String privacy_value, owner_user_id, page_title, fullname, profile_page_id, sImagePages, sAlbumName, iAlbumId;
	private String sharePhoto, sharePhotoAlbum;
	private ArrayList<String> selectedPhoto;
	
	private ProfilePicUtil profilePicUtil = new ProfilePicUtil();
	
	//phrase manager
	private PhraseManager phraseManager;
	private int selected;
    private long totalSizeImage;
	private String sPrivacy, locationLat, locationLng, locationName;
	private String colorCode;
	private ColorView colorView;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
	
	/**
	 * Broadcast Receiver 
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("album_name") && intent.hasExtra("album_id")) {
				sAlbumName = intent.getExtras().getString("album_name");
				iAlbumId = String.valueOf(intent.getExtras().getInt("album_id"));
			}
		}
	};
	
	/** Called when the activity is first created. */
	public void InitializeUI() {
		// get views from ID's
		imgView = (ImageView) findViewById(R.id.ImageView);
		takePhoto = (RelativeLayout) findViewById(R.id.status_photo);
		
		ibAlbum = (RelativeLayout) findViewById(R.id.status_album);
		caption = (EditText) findViewById(R.id.Caption);
		caption.setHint(phraseManager.getPhrase(getApplicationContext(), "feed.what_s_on_your_mind"));
		
		userImg = (ImageView) findViewById(R.id.status_user_image);
		horizontalView = (LinearLayout) findViewById(R.id.horizonalImage);
		privacyLayout = (RelativeLayout) findViewById(R.id.status_privacy_all);
		
		photoTxt = (TextView) findViewById(R.id.photoTxt);
		albumTxt = (TextView) findViewById(R.id.albumTxt);
        checkInTxt = (TextView) findViewById(R.id.upload_check_in_Txt);

		//set phrase
		photoTxt.setText(phraseManager.getPhrase(getApplicationContext(), "user.photo"));
		albumTxt.setText(phraseManager.getPhrase(getApplicationContext(), "profile.albums"));
        checkInTxt.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.check_in"));

        checkinBtn = (RelativeLayout) findViewById(R.id.upload_status_check_in);
        checkInLocation = (TextView) findViewById(R.id.upload_check_in_name);

        if (locationName != null) {
            checkInLocation.setText(phraseManager.getPhrase(getApplicationContext(), "feed.at_location").replace("{location}", locationName));
        }

		aBitmap = new ArrayList<Bitmap>();

		colorView = new ColorView(getApplicationContext());
		colorCode = colorView.getColorCode(getApplicationContext(), user);

		// change title
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "accountapi.post_status"));

		networkUntil.drawImageUrl(userImg, user.getUserImage(), R.drawable.loading);

		if (selectedPhoto.size() > 0) {
			findViewById(R.id.line).setVisibility(View.VISIBLE);
			//if select only image
			if (selectedPhoto.size() == 1) {
				Bitmap bitmap = profilePicUtil.decodeFile(selectedPhoto.get(0));
				//add bitmap to array
				aBitmap.add(bitmap);
				imgView.setImageBitmap(bitmap);
				
				imgView.getLayoutParams().height = (int)getResources().getDimension(R.dimen.marketplace_image);
				imgView.getLayoutParams().width = (int)getResources().getDimension(R.dimen.marketplace_image);
				imgView.setVisibility(View.VISIBLE);
			} else {
				imgView.setVisibility(View.GONE);
				sharePhoto = phraseManager.getPhrase(getApplicationContext(), "accountapi.full_name_shared_a_few_photos");
				//replace full name
				caption.setText(sharePhoto.replace("{full_name}", user.getFullname()));
				
				caption.setEnabled(false);
				horizontalView.setVisibility(View.VISIBLE);
				privacyLayout.setVisibility(View.GONE);

				for (int i = 0; i < selectedPhoto.size(); i++) {

					ImageView image = new ImageView(getApplicationContext());

					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.marketplace_image), 
							(int) getResources().getDimension(R.dimen.marketplace_image));
					lp.setMargins(15, 15, 15, 0);

					image.setLayoutParams(lp);
					image.setBackgroundResource(R.drawable.border_image);

					image.setScaleType(ImageView.ScaleType.CENTER_CROP);
					
					Bitmap bitmap = profilePicUtil.decodeFile(selectedPhoto.get(i));
					bitmap = profilePicUtil.setPic(getApplicationContext(), bitmap, null, selectedPhoto.get(i));

					aBitmap.add(bitmap);
					image.setImageBitmap(bitmap);

					horizontalView.addView(image);
				}
			}
		} else {
			findViewById(R.id.line).setVisibility(View.GONE);
		}
		
		if (page_id == null) {
			if (privacy_value == null) {
				privacy_value = "0";
				sPrivacy = phraseManager.getPhrase(getApplicationContext(), "privacy.everyone");
			}
		} else {
			privacy_value = "0";
			sPrivacy = page_title;
		}

		//choose photo
		takePhoto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ImageUpload.this, AlbumSelectedActivity.class);
				if (user_id != null) {
					i.putExtra("user_id", user_id);
				}
				if (page_id != null) {
					i.putExtra("page_id", page_id);
					i.putExtra("owner_user_id", owner_user_id);
					i.putExtra("page_title", page_title);
					i.putExtra("fullname", fullname);
					i.putExtra("profile_page_id", profile_page_id);
				}
				if (event_id != null) {
					i.putExtra("event_id", event_id);
				}
				startActivity(i);
				finish();
			}
		});

        checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ImageUpload.this, CheckInActivity.class);
                if (user_id != null) {
                    i.putExtra("user_id", user_id);
                }
                startActivity(i);
                finish();
            }
        });

		//choose privacy
		privacyLayout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showMessageBox();
			}
		});
		
		//select an album
		ibAlbum.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(ImageUpload.this, AlbumPhotoActivity.class);
                checkInLocation.setText(null);
                locationLng = locationLat = locationName = null;
				startActivity(i);
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		//if add new photos to album
		if (sAlbumName != null && iAlbumId != null) {
			
			caption.setEnabled(false);
			privacyLayout.setVisibility(View.GONE);
			
			sharePhotoAlbum = phraseManager.getPhrase(getApplicationContext(), "accountapi.full_name_shared_a_few_photos_in_this_album_album_name");
			caption.setText(sharePhotoAlbum.replace("{full_name}", user.getFullname()).replace("{album_name}", sAlbumName));
		}
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageupload);
		Bundle extras = getIntent().getExtras();
		
		phraseManager = new PhraseManager(getApplicationContext());
		privacyManager = new PrivacyManager(getApplicationContext());
        dialog = new ProgressDialog(ImageUpload.this);
        user = (User) getApplicationContext();

		//init value
		user_id = null;
		page_id = null;
		owner_user_id = null;
		page_title = null;
		fullname = null;
		profile_page_id = null;
		sImagePages = null;
		sAlbumName = null;
		iAlbumId = null;
		selected = 0; // select at 0
        totalSizeImage = 0;
		sPrivacy = null;
		sharePhoto = null;
		sharePhotoAlbum = null;
        locationLat = null;
        locationLng = null;
        locationName = null;
		selectedPhoto = new ArrayList<String>();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		registerReceiver(mHandleMessageReceiver, new IntentFilter("com.brodev.socialapp.android.album"));
		
		if (extras != null) {
			if (getIntent().hasExtra("user_id")) {
				user_id = extras.getString("user_id");
			} else if (getIntent().hasExtra("page_id")) {
				page_id = extras.getString("page_id");
				owner_user_id = extras.getString("owner_user_id");
				page_title = Html.fromHtml(extras.getString("page_title")).toString();
				fullname = Html.fromHtml(extras.getString("fullname")).toString();
				profile_page_id = extras.getString("profile_page_id");
				sImagePages = extras.getString("pages_image");
			} else if (getIntent().hasExtra("event_id")) {
				event_id = extras.getString("event_id");
			}
			
			if (getIntent().hasExtra("selected_photo")) {
				selectedPhoto = extras.getStringArrayList("selected_photo");	
			}

            if (getIntent().hasExtra("location_name") && getIntent().hasExtra("location_lat") && getIntent().hasExtra("location_lng"))
            {
                locationLat = extras.getString("location_lat");
                locationLng = extras.getString("location_lng");
                locationName = extras.getString("location_name");
            }
		}
		InitializeUI();
		
		if (user_id != null && !user_id.equals(user.getUserId())) {
			privacyLayout.setVisibility(View.GONE);			
		}
		
		if (user_id != null || page_id != null || event_id != null ) {
			ibAlbum.setVisibility(View.GONE);
		}
		if (event_id != null || (page_id != null && !owner_user_id.equals(user.getUserId()))) {
			privacyLayout.setVisibility(View.GONE);
			sPrivacy = null;
			privacy_value = null;
		}

        if (Boolean.parseBoolean(user.getCheckin()) && ((user_id != null && user_id.equals(user.getUserId())) || user_id == null))
        {
            checkinBtn.setVisibility(View.VISIBLE);
        }
        else {
            checkinBtn.setVisibility(View.GONE);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.poststatus, menu);

		return true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_post:
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected())
            {
                if (aBitmap.size() == 0)
                {
                    if ("".equals(caption.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.please_insert_text"), Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.setTitle(phraseManager.getPhrase(getApplicationContext(), "accountapi.posting"));
                        dialog.setMessage(phraseManager.getPhrase(getApplicationContext(), "accountapi.please_wait"));
                        dialog.show();
                        new posttext().execute();
                    }
                } else {
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.setTitle(phraseManager.getPhrase(getApplicationContext(), "accountapi.uploading"));
                    dialog.setMessage(phraseManager.getPhrase(getApplicationContext(), "accountapi.please_wait"));
                    dialog.show();
                    if (aBitmap.size() == 1 && iAlbumId == null) {
                        new ImageUploadTask().execute();
                    } else {
                        new MultiImageUploadTask().execute();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                finish();
            }

			return true;
		case android.R.id.home:
			finish();
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Show dialog choose privacy
	 */
	private void showMessageBox() 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		if (page_id == null) {
			builder.setSingleChoiceItems(privacyManager.getValue(), selected,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// The 'which' argument contains the index position
							// of the selected item
							if (which == 0) {
								sPrivacy = phraseManager.getPhrase(getApplicationContext(), "privacy.everyone");
								privacy_value = "0";
							} else if (which == 1) {
								sPrivacy = phraseManager.getPhrase(getApplicationContext(), "privacy.friends");
								privacy_value = "1";
							} else if (which == 2) {
								sPrivacy = phraseManager.getPhrase(getApplicationContext(), "privacy.friends_of_friends");
								privacy_value = "2";
							} else if (which == 3) {
								sPrivacy = phraseManager.getPhrase(getApplicationContext(), "privacy.only_me");
								privacy_value = "3";
							}
							selected = which;  
							dialog.cancel(); 
						}
					});
		} else {
			final CharSequence[] items = { page_title, fullname };
			builder.setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// The 'which' argument contains the index position
					// of the selected item
					if (which == 0) {
						sPrivacy = page_title;
						privacy_value = "0";
					} else if (which == 1) {
						sPrivacy = fullname;
						privacy_value = "1";
					}
					selected = which;  
					dialog.cancel(); 
				}
			});
		}

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();

	}

	public class posttext extends AsyncTask<String, Void, String> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			String postrequest = null;
			JSONObject json = new JSONObject();
			String URL = null;
			if (Config.CORE_URL == null) {
				URL = Config.makeUrl(user.getCoreUrl(), "updateStatus", true) + "&token=" + user.getTokenkey();
			} else {
				URL = Config.makeUrl(Config.CORE_URL, "updateStatus", true) + "&token=" + user.getTokenkey();
			}
			
			// Use BasicNameValuePair to create GET data
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				if (page_id != null) {
					
					String customPage = page_id;
					if (fullname.equals(sPrivacy)) {
						customPage = String.valueOf(0);
					}
					if (privacy_value != null) {
						if (Config.CORE_URL == null) {
							URL = Config.makeUrl(user.getCoreUrl(), "updateStatus", true) + "&custom_pages_post_as_page=" + customPage + "&token=" + user.getTokenkey();
						} else {
							URL = Config.makeUrl(Config.CORE_URL, "updateStatus", true) + "&custom_pages_post_as_page=" + customPage + "&token=" + user.getTokenkey();
						}
					}
					
					json.put("callback_item_id", page_id);
					json.put("callback_module", "pages");
					json.put("parent_user_id", page_id);
					json.put("user_status", caption.getText().toString());
					json.put("status_info", caption.getText().toString());
					if (privacy_value != null && "0".equals(privacy_value)) {
						json.put("owner_user_id", profile_page_id);

					} else {
						json.put("owner_user_id", user.getUserId());
					}
					pairs.add(new BasicNameValuePair("is_callback", "1"));
					pairs.add(new BasicNameValuePair("val", json.toString()));
				}
				if (page_id == null) {
					pairs.add(new BasicNameValuePair("status_info", caption.getText().toString()));
					pairs.add(new BasicNameValuePair("owner_user_id", user.getUserId()));
					pairs.add(new BasicNameValuePair("privacy", privacy_value));
					if (user_id != null && !user_id.equals(user.getUserId())) {
						pairs.add(new BasicNameValuePair("parent_user_id", user_id));
					}

                    if (locationName != null && (user_id == null || (user_id != null && user_id.equals(user.getUserId())))) {
                        pairs.add(new BasicNameValuePair("Latlng", locationLat + "," + locationLng));
                        pairs.add(new BasicNameValuePair("name", locationName));
                    }
				}
				if (event_id != null) {
					if (Config.CORE_URL == null) {
						URL = Config.makeUrl(user.getCoreUrl(), "updateStatus", true) + "&token=" + user.getTokenkey();
					} else {
						URL = Config.makeUrl(Config.CORE_URL, "updateStatus", true) + "&token=" + user.getTokenkey();
					}
					
					json.put("callback_item_id", event_id);
					json.put("callback_module", "event");
					json.put("parent_user_id", event_id);
					json.put("user_status", caption.getText().toString());
					json.put("status_info", caption.getText().toString());	
					
					pairs.add(new BasicNameValuePair("is_event", "1"));
					pairs.add(new BasicNameValuePair("val", json.toString()));
				}
				
				postrequest = networkUntil.makeHttpRequest(URL, "POST", pairs);

			} catch (JSONException e) {
				e.printStackTrace();
				if (dialog.isShowing())
					dialog.dismiss();
				finish();
			}
			return postrequest;
		}
		
		@Override
		protected void onPostExecute(String sResponse) {
			try {
				
				if (dialog.isShowing())
					dialog.dismiss();
				
				if (sResponse != null) {
					getSingleFeed(sResponse);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finish();
		}

	}

	/**
	 * Class upload multiple images
	 */
	public class MultiImageUploadTask extends AsyncTask<Void, Integer, String> 
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Void... unsued) {
			String sResponse = null;
			try {
				
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();

				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), "addMultiPhoto", true) + "&token=" + user.getTokenkey();
				} else {
					URL = Config.makeUrl(Config.CORE_URL, "addMultiPhoto", true) + "&token=" + user.getTokenkey();
				}
				
				HttpPost httpPost = new HttpPost(URL);

                CustomMultiPartEntity entity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, new CustomMultiPartEntity.ProgressListener()
                {
                    @Override
                    public void transferred(long num)
                    {
                        publishProgress((int) ((num / (float) totalSizeImage) * 100));
                    }
                });
				
				if (iAlbumId != null) {
					entity.addPart("album_id", new StringBody(iAlbumId));
				}
				
				for (int i = 0; i < aBitmap.size(); i++) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					Bitmap bitmap = aBitmap.get(i);
					bitmap.compress(CompressFormat.PNG, 70, bos);
					byte[] data = bos.toByteArray();
					
					entity.addPart("image[]", new ByteArrayBody(data, "myImage" + i + ".jpg"));
				}
                totalSizeImage = entity.getContentLength();

				httpPost.setEntity(entity);
				HttpResponse response = httpClient.execute(httpPost, localContext);
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

				sResponse = reader.readLine();
				
			} catch (Exception ex) {
				aBitmap = new ArrayList<Bitmap>();
				if (dialog.isShowing())
					dialog.dismiss();

                finish();
				return null;
			}
			return sResponse;
		}

        @Override
        protected void onProgressUpdate(Integer... progress) {
            dialog.setProgress((int) (progress[0]));
        }

		@Override
		protected void onPostExecute(String ui)
		{
			if (dialog.isShowing())
				dialog.dismiss();
			finish();
			Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.please_pull_to_refresh_your_newsfeed"), Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Class upload image
	 */
	class ImageUploadTask extends AsyncTask<Void, Integer, String> {

		@Override
		protected String doInBackground(Void... unsued) {
			JSONObject json = new JSONObject();
			String sResponse = null;
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();
				
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), "addPhoto", true) + "&token=" + user.getTokenkey();	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, "addPhoto", true) + "&token=" + user.getTokenkey();
				}
				
				HttpPost httpPost = new HttpPost(URL);

                CustomMultiPartEntity entity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, new CustomMultiPartEntity.ProgressListener()
                {
                    @Override
                    public void transferred(long num)
                    {
                        publishProgress((int) ((num / (float) totalSizeImage) * 100));
                    }
                });

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
					
				Bitmap bitmap = aBitmap.get(0);
				bitmap.compress(CompressFormat.PNG, 70, bos);
				byte[] data = bos.toByteArray();

				String gettext = Html.toHtml(caption.getText());

				gettext = gettext.replace("<p dir=rtl>", "");
				gettext = gettext.replace("<p dir=ltr>", "");
				gettext = gettext.replace("</p>", "");
				gettext = gettext.replace("<u>", "");
				gettext = gettext.replace("</u>", "");
				
				if (page_id != null)
                {

					json.put("callback_item_id", page_id);
					json.put("callback_module", "pages");
					json.put("parent_user_id", page_id);
					json.put("user_status", caption.getText().toString());
					json.put("status_info", caption.getText().toString());
					if (privacy_value != null && "0".equals(privacy_value))
                    {
						json.put("owner_user_id", profile_page_id);
					} else {
						json.put("owner_user_id", user.getUserId());
					}
					
					entity.addPart("is_callback", new StringBody("1"));
					
					entity.addPart("val", new StringBody(json.toString()));
				}
				
				if (page_id == null && event_id == null)
                {
					entity.addPart("status_info", new StringBody(gettext));
					entity.addPart("owner_user_id", new StringBody("1"));
					entity.addPart("privacy", new StringBody(privacy_value));
					if(user_id != null){
						entity.addPart("parent_user_id", new StringBody(user_id));
					}
				}

				if (event_id != null)
                {
					json.put("callback_item_id", event_id);
					json.put("callback_module", "event");
					json.put("parent_user_id", event_id);
					json.put("user_status", caption.getText().toString());
					json.put("status_info", caption.getText().toString());
					json.put("owner_user_id", user.getUserId());
					entity.addPart("is_event", new StringBody("1"));
					
					entity.addPart("val", new StringBody(json.toString()));
				}
                entity.addPart("image", new ByteArrayBody(data, "myImage.jpg"));
                totalSizeImage = entity.getContentLength();

				httpPost.setEntity(entity);
				HttpResponse response = httpClient.execute(httpPost, localContext);
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

				sResponse = reader.readLine();
				
				return sResponse;
			} catch (Exception e) {
				if (dialog.isShowing())
					dialog.dismiss();
			
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
            dialog.setProgress((int) (progress[0]));
		}

		@Override
		protected void onPostExecute(String sResponse) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();
					
				if (sResponse != null) {
					getSingleFeed(sResponse);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finish();
		}
	}
	
	
	/**
	 * Get Single feed after sharing
	 * @param sResponse
	 */
	public void getSingleFeed(String sResponse) {
		
		try {
			if (sResponse != null) {

				JSONObject apiJSON = new JSONObject(sResponse);
				
				Object inter = apiJSON.get("output");
				
				if (inter instanceof JSONArray) {
					Config.feed = new Feed();
					JSONArray output = (JSONArray) inter;
					
					if (output.length() > 0) {
						JSONObject pagesObj = output.getJSONObject(0);
						
						Config.feed.setFeedId(pagesObj.getString("feed_id"));
						
						Config.feed.setContinueFeed(true);
						
						if(pagesObj.has("item_id")){
							Config.feed.setItemId(pagesObj.getString("item_id"));
						}
						
						if (pagesObj.has("social_app")) {
							Config.feed.setType(pagesObj.getJSONObject("social_app").getString("type_id"));	
						} else {
							Config.feed.setType(pagesObj.getString("type_id"));
						}
						
						if (pagesObj.has("full_name")) {
							Config.feed.setFullName(Html.fromHtml(pagesObj.getString("full_name")).toString());
						} else {
							Config.feed.setFullName(user.getFullname());	
						}

                        Config.feed.setTitle(pagesObj.getString("title_phrase_html"));

                        if (pagesObj.has("parent_user") && !pagesObj.isNull("parent_user")) {
                            JSONObject userObj = pagesObj.getJSONObject("parent_user");
                            if (userObj.has("parent_full_name") && !userObj.isNull("parent_full_name")) {
                                Config.feed.setTitle(pagesObj.getString("title_phrase_html") + " &raquo; " + "<b><font color=\"" + colorCode + "\">" + userObj.getString("parent_full_name")+ "</font></b>");
                            }
                        }
						
						if (pagesObj.has("user_id")) {
							Config.feed.setUserId(pagesObj.getString("user_id"));
						} else {
							Config.feed.setUserId(user.getUserId());	
						}
						
						//set time phrase	
						Config.feed.setTime(phraseManager.getPhrase(getApplicationContext(), "accountapi.1_second_ago"));
						
						Config.feed.setIcon(pagesObj.getString("feed_icon"));
						if (sImagePages != null && sPrivacy != null && !fullname.equals(sPrivacy)) {
							Config.feed.setUserImage(sImagePages);
						} else {
							Config.feed.setUserImage(user.getUserImage());	
						}
						
						if (pagesObj.has("no_share")) {
							Config.feed.setNo_share(pagesObj.getBoolean("no_share"));
						} else {
							Config.feed.setNo_share(false);
						}
						
						if (pagesObj.has("feed_title")) {
							Config.feed.setTitleFeed(Html.fromHtml(pagesObj.getString("feed_title")).toString());
						}
						
						Config.feed.setFeedLink(pagesObj.getString("feed_link"));
						if ("link".equals(pagesObj.getString("type_id")) && pagesObj.has("feed_link_actual")) {
							Config.feed.setFeedLink(pagesObj.getString("feed_link_actual"));
						}
						if (pagesObj.has("parent_module_id") && !pagesObj.isNull("parent_module_id")) {
							Config.feed.setModule(pagesObj.getString("parent_module_id"));
						}
						
						if (pagesObj.has("enable_like")) {
							if(!pagesObj.isNull("feed_is_liked") && !"false".equals(pagesObj.getString("feed_is_liked"))) {
								Config.feed.setFeedIsLiked("feed_is_liked");
							}
							Config.feed.setEnableLike(pagesObj.getBoolean("enable_like"));
						} else {
							Config.feed.setEnableLike(false);
						}

						if (pagesObj.has("can_post_comment")) {
							Config.feed.setCanPostComment(pagesObj.getBoolean("can_post_comment"));
						} else {
							Config.feed.setCanPostComment(false);
						}
						
						if (pagesObj.has("total_comment")) {
							Config.feed.setTotalComment(pagesObj.getString("total_comment"));
						}
						
						if (pagesObj.has("comment_type_id")) {
							Config.feed.setComment_type_id(pagesObj.getString("comment_type_id"));
						}
						
						if (pagesObj.has("type_id")) {
							Config.feed.setType(pagesObj.getString("type_id"));
						}
						
						if (pagesObj.has("feed_title_extra")) {
							Config.feed.setFeedTitleExtra(Html.fromHtml(pagesObj.getString("feed_title_extra")).toString());
						}

						if (pagesObj.has("feed_content")) {
							Config.feed.setFeedContent(pagesObj.getString("feed_content"));
						}

                        if (pagesObj.has("feed_content_html")) {
                            Config.feed.setFeedContent(pagesObj.getString("feed_content_html"));
                        }

						if (pagesObj.has("profile_page_id")) {
							Config.feed.setProfile_page_id(pagesObj.getString("profile_page_id"));
						}
						
						if (pagesObj.has("feed_total_like")) {
							Config.feed.setHasLike(pagesObj.getString("feed_total_like"));
							Config.feed.setTotalLike(Integer.parseInt(pagesObj.getString("feed_total_like")));
						}
						
						if (pagesObj.has("feed_status")) {
							Config.feed.setStatus(pagesObj.getString("feed_status"));
						}

                        if (pagesObj.has("feed_status_html")) {
                            Config.feed.setStatus(pagesObj.getString("feed_status_html"));
                        }
						
						if (pagesObj.has("can_share_item_on_feed")) {
							Config.feed.setCan_share_item_on_feed(pagesObj.getBoolean("can_share_item_on_feed"));
						}
						
						if (pagesObj.has("like_type_id")) {
							Config.feed.setLikeTypeId(pagesObj.getString("like_type_id"));	
						}
						
						if (pagesObj.has("like_item_id")) {
							Config.feed.setItemId(pagesObj.getString("like_item_id"));
						}
						
						if (pagesObj.has("social_app")) {
							JSONObject socialObj = pagesObj.getJSONObject("social_app");
							Object intervention = socialObj.get("link");
							
							if (intervention instanceof JSONObject) {
								JSONObject requestObj = socialObj.getJSONObject("link").getJSONObject("request");
								
								if (requestObj.has("page_id")) {
									Config.feed.setPage_id_request(requestObj.getString("page_id"));
								} else if (requestObj.has("user_id")) {
									Config.feed.setUser_id_request(requestObj.getString("user_id"));
								} else if (requestObj.has("photo_id")) {
									Config.feed.setPhoto_id_request(requestObj.getString("photo_id"));
								}
							}
						}
						
						if (!pagesObj.isNull("feed_image")) {
							ArrayList<String> Images_feed = new ArrayList<String>();

							Object imageObj = pagesObj.get("feed_image");
							if (imageObj instanceof JSONArray) {
								Images_feed.add(pagesObj.getJSONArray("feed_image").getString(0));	

								Config.feed.setImage1(pagesObj.getJSONArray("feed_image").getString(0));
								Config.feed.setFeed_Image(Images_feed);
							}
						}
						
						if (pagesObj.has("custom_data_cache")) {
							Object customData = pagesObj.get("custom_data_cache");
							if (customData instanceof JSONObject) {
								JSONObject cus = (JSONObject) customData;
								if (cus.has("image") && !cus.isNull("image") && !"".equals(cus.getString("image")))
									Config.feed.setImage1(cus.getString("image"));	
							}
							
						}

                        if (pagesObj.has("location_img") && !pagesObj.isNull("location_img"))
                        {
                            Config.feed.setLocationImg(pagesObj.getString("location_img"));
                        }

                        if (pagesObj.has("location_link") && !pagesObj.isNull("location_link"))
                        {
                            Config.feed.setLocationLink(pagesObj.getString("location_link"));
                        }
					}
				}
				
				if (inter instanceof JSONObject) {
					JSONObject outJson = (JSONObject) inter;
					if (outJson.has("error_message"))
						Toast.makeText(getApplicationContext(), Html.fromHtml(outJson.getString("error_message")).toString(), Toast.LENGTH_LONG).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}