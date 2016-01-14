package com.brodev.socialapp.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.Privacy;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.manager.ComboBox;
import com.brodev.socialapp.android.manager.CustomDateTimePicker;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.ComboBoxItem;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

@SuppressLint("SimpleDateFormat")
public class CreateNewEventActivity extends SherlockActivity {

	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private PhraseManager phraseManager;
	private EditText edtEventPlan, edtEventDescription, edtEventLocation, edtEventAddress, edtEventCity, edtEventCode;
	private TextView tvEventStartTime, tvEventEndTime, tvEventAddEndTime,
			tvEventAddCountry, tvEStartTime, tvEEndTime, tvEventPrivacyTitle,
			tvEventPrivacy, tvEventPrivacyShareTitle, tvEventPrivacyShare;
	private ArrayList<ComboBoxItem> lstCountry, lstCategory;
	private ComboBox cbxCountry, cbxChidrenCountry, cbxCategory, cbxChidrenCategory;
	private String defCountry, startTime, endTime, defCategory, URL;
	private LinearLayout countryLayout, childrenCountryLayout, categoryLayout, childrenCategoryLayout;
	private ProgressBar countryProgress, childrenCountryProgress, categoryProgress, childrenCategoryProgress, eventProgress;
	private ImageView privacyImg, privacyImg1;
	private CustomDateTimePicker pickStartTime, pickEndTime;
	private Calendar cal = Calendar.getInstance();
	private DateFormat dateFormat, dateFormatRequest;
	private Privacy privacyEvent, privacyEventShare;	
	private String[] aStartTime, aEndTime;
	private ColorView colorView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_event);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		user = (User) getApplicationContext();
		colorView = new ColorView(getApplicationContext());
		phraseManager = new PhraseManager(getApplicationContext());
		lstCountry = new ArrayList<ComboBoxItem>();
		lstCategory = new ArrayList<ComboBoxItem>();
		defCountry = null;
		defCategory = null;
		startTime = null;
		URL = null;
		cbxCountry = new ComboBox(CreateNewEventActivity.this);		
		cbxChidrenCountry = new ComboBox(CreateNewEventActivity.this);	
		cbxCategory = new ComboBox(CreateNewEventActivity.this);
		cbxChidrenCategory = new ComboBox(CreateNewEventActivity.this);
		dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		dateFormatRequest = new SimpleDateFormat("MM/dd/yyyy/HH/mm");		
		
		//set title
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "event.create_new_event"));
		registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_REQUEST_CHILDREN_COUNTRY));
		registerReceiver(mHandleCategoryMessageReceiver, new IntentFilter(Config.DISPLAY_REQUEST_CHILDREN_CATEGORY));
		
		countryLayout = (LinearLayout) findViewById(R.id.event_country);
		childrenCountryLayout = (LinearLayout) findViewById(R.id.event_children_country);
		categoryLayout = (LinearLayout) findViewById(R.id.event_category);
		childrenCategoryLayout = (LinearLayout) findViewById(R.id.event_children_category);
		countryProgress = (ProgressBar) findViewById(R.id.loading_country_progress);
		childrenCountryProgress = (ProgressBar) findViewById(R.id.loading_children_country_progress);
		categoryProgress = (ProgressBar) findViewById(R.id.loading_category_progress);
		childrenCategoryProgress = (ProgressBar) findViewById(R.id.loading_children_category_progress);
		
		eventProgress = (ProgressBar) findViewById(R.id.create_event_loading);
		eventProgress.setVisibility(View.GONE);
		 
		tvEStartTime = (TextView) findViewById(R.id.event_startTime_tv);
		tvEStartTime.setText("" + dateFormat.format(cal.getTime()));
		startTime = dateFormatRequest.format(cal.getTime()).toString();
		
		tvEEndTime = (TextView) findViewById(R.id.EndTime);
		cal.add(Calendar.HOUR_OF_DAY, 3);
		tvEEndTime.setText("" + dateFormat.format(cal.getTime()));
		endTime = dateFormatRequest.format(cal.getTime()).toString();
		
		edtEventPlan = (EditText) findViewById(R.id.event_plan_tv);
		edtEventPlan.setHint(phraseManager.getPhrase(getApplicationContext(), "event.what_are_you_planning"));
		
		edtEventDescription = (EditText) findViewById(R.id.event_description);
		edtEventDescription.setHint(phraseManager.getPhrase(getApplicationContext(), "event.description"));
		
		tvEventStartTime = (TextView) findViewById(R.id.event_startTime);
		tvEventStartTime.setText(phraseManager.getPhrase(getApplicationContext(), "event.start_time"));
		
		tvEventAddEndTime = (TextView) findViewById(R.id.event_add_endTime);
		tvEventAddEndTime.setText(phraseManager.getPhrase(getApplicationContext(), "event.add_end_time"));
		
		tvEventEndTime = (TextView) findViewById(R.id.event_endTime_tv);
		tvEventEndTime.setText(phraseManager.getPhrase(getApplicationContext(), "event.end_time"));
		
		edtEventLocation = (EditText) findViewById(R.id.event_location);
		edtEventLocation.setHint(phraseManager.getPhrase(getApplicationContext(), "event.location_venue"));
		
		tvEventAddCountry = (TextView) findViewById(R.id.event_add_country);
		tvEventAddCountry.setText(phraseManager.getPhrase(getApplicationContext(), "event.add_address_city_zip_country"));
		
		edtEventAddress = (EditText) findViewById(R.id.event_address);
		edtEventAddress.setHint(phraseManager.getPhrase(getApplicationContext(), "event.address"));
		
		edtEventCity = (EditText) findViewById(R.id.event_city);
		edtEventCity.setHint(phraseManager.getPhrase(getApplicationContext(), "event.city"));
		
		edtEventCode = (EditText) findViewById(R.id.event_code);
		edtEventCode.setHint(phraseManager.getPhrase(getApplicationContext(), "event.zip_postal_code"));
		
		tvEventPrivacyTitle = (TextView) findViewById(R.id.event_privacy_title);
		tvEventPrivacyTitle.setText(phraseManager.getPhrase(getApplicationContext(), "event.event_privacy"));
		
		tvEventPrivacy = (TextView) findViewById(R.id.event_privacy_tv);	
		tvEventPrivacy.setText(phraseManager.getPhrase(getApplicationContext(), "privacy.everyone"));
		
		tvEventPrivacyShareTitle = (TextView) findViewById(R.id.event_privacy_share_title);	
		tvEventPrivacyShareTitle.setText(phraseManager.getPhrase(getApplicationContext(), "event.share_privacy"));
		
		tvEventPrivacyShare = (TextView) findViewById(R.id.event_privacy_share_tv);	
		tvEventPrivacyShare.setText(phraseManager.getPhrase(getApplicationContext(), "privacy.everyone"));
		
		privacyImg = (ImageView) findViewById(R.id.event_privacy_img);
		privacyImg1 = (ImageView) findViewById(R.id.event_privacy_share_img);
		colorView.changeColorPrivacy(privacyImg, user.getColor());
		colorView.changeColorPrivacy(privacyImg1, user.getColor());
		
		privacyEvent = new Privacy(CreateNewEventActivity.this);
		privacyEvent.setTextView(tvEventPrivacy);
		tvEventPrivacy.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					privacyEvent.showMessage();
				} catch (Exception e) {

				}
			}
		});
		
		privacyEventShare = new Privacy(CreateNewEventActivity.this);
		privacyEventShare.setTextView(tvEventPrivacyShare);
		tvEventPrivacyShare.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					privacyEventShare.showMessage();
				} catch (Exception e) {

				}
			}
		});
		
		//action click
		tvEventAddEndTime.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				tvEventAddEndTime.setVisibility(View.GONE);
				findViewById(R.id.event_endTime_layout).setVisibility(View.VISIBLE);
			}
		});
		
		tvEventAddCountry.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tvEventAddCountry.setVisibility(View.GONE);
				findViewById(R.id.event_add_country_layout).setVisibility(View.VISIBLE);
			}
		});
		
		try {
			new RequestCountry().execute(user.getTokenkey(), null);
			new RequestCategory().execute(user.getTokenkey(), null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		pickStartTime = new CustomDateTimePicker(this, new CustomDateTimePicker.ICustomDateTimeListener() {

			@Override
			public void onSet(Dialog dialog, Calendar calendarSelected,
					Date dateSelected, int year, String monthFullName,
					String monthShortName, int monthNumber, int date,
					String weekDayFullName, String weekDayShortName,
					int hour24, int hour12, int min, int sec,
					String AM_PM) {
				monthNumber++;
				startTime = monthNumber
						+ "/"
						+ calendarSelected
						.get(Calendar.DAY_OF_MONTH)
						+ "/" + year + "/" + hour24 + "/" + min;
				tvEStartTime.setText(monthNumber
						+ "/"
						+ calendarSelected
						.get(Calendar.DAY_OF_MONTH)
						+ "/" + year + " " + hour24 + ":" + min);
			}

			@Override
			public void onCancel() {
			}
		});
		pickStartTime.set24HourFormat(true);
		pickStartTime.setDate(Calendar.getInstance());
		findViewById(R.id.event_start_time_layout).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pickStartTime.showDialog();
			}
		});
		
		pickEndTime = new CustomDateTimePicker(this, new CustomDateTimePicker.ICustomDateTimeListener() {

			@Override
			public void onSet(Dialog dialog, Calendar calendarSelected,
					Date dateSelected, int year, String monthFullName,
					String monthShortName, int monthNumber, int date,
					String weekDayFullName, String weekDayShortName,
					int hour24, int hour12, int min, int sec,
					String AM_PM) {
				monthNumber++;
				endTime = monthNumber
						+ "/"
						+ calendarSelected
						.get(Calendar.DAY_OF_MONTH)
						+ "/" + year + "/" + hour24 + "/" + min;
				tvEEndTime.setText(monthNumber
						+ "/"
						+ calendarSelected
						.get(Calendar.DAY_OF_MONTH)
						+ "/" + year + " " + hour24 + ":" + min);
			}

			@Override
			public void onCancel() {
			}
		});
		
		pickEndTime.set24HourFormat(true);
		pickEndTime.setDate(Calendar.getInstance());
		findViewById(R.id.event_end_time_layout).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pickEndTime.showDialog();
			}
		});
	}
	
	/**
	 * Receiving message
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			String bRequest = intent.getExtras().getString("request");
			if (bRequest != null && !bRequest.equals(defCountry) && !isNumeric(bRequest)) {
				new RequestCountry().execute(user.getTokenkey(), bRequest);
			}
		}
	};
	
	/**
	 * Check String is numeric
	 * @param str
	 * @return
	 */
	public boolean isNumeric(String str)  
	{  
		try {
			int d = Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
	  return true;  
	}
	
	/**
	 * Receiving message
	 */
	private final BroadcastReceiver mHandleCategoryMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			String bRequest = intent.getExtras().getString("request");
			
			if (bRequest != null && !bRequest.equals(defCategory) ) {					
				new RequestCategory().execute(user.getTokenkey(), bRequest);
			} 
			
			if ("0".equals(defCategory)) {
				childrenCategoryLayout.setVisibility(View.GONE);
				childrenCategoryLayout.removeAllViews();
			}
			
		}
	};
	
	@Override
	public void onDestroy() {
		try{
			unregisterReceiver(mHandleMessageReceiver);
			unregisterReceiver(mHandleCategoryMessageReceiver);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.poststatus, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.action_post:
				if (checkValue()) {
					new CreateNewEvent().execute(
						edtEventPlan.getText().toString().trim(),
						edtEventDescription.getText().toString(),
						startTime,
						endTime,
						edtEventLocation.getText().toString().trim(),
						(edtEventAddress.getText().toString().trim().length() != 0) ? edtEventAddress.getText().toString().trim() : null,
						(edtEventCity.getText().toString().trim().length() != 0) ? edtEventCity.getText().toString().trim() : null,
						(edtEventCode.getText().toString().trim().length() != 0) ? edtEventCode.getText().toString().trim() : null,		
						cbxCountry.getValue(),
						(!"0".equals(cbxChidrenCountry.getValue())) ? cbxChidrenCountry.getValue() : null,
						privacyEvent.getValue(),
						privacyEventShare.getValue(),
						(!"0".equals(cbxCategory.getValue())) ? cbxCategory.getValue() : null,
						(!"0".equals(cbxChidrenCategory.getValue())) ? cbxChidrenCategory.getValue() : null
					);
					
				}
				break;
			default:
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Check value in form
	 * @return
	 */
	private boolean checkValue() {
		
		if (edtEventPlan.getText().toString().trim().length() == 0) {
			Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "event.provide_a_name_for_this_event"),
					Toast.LENGTH_LONG).show();
			
			return false;
		} else if (edtEventLocation.getText().toString().trim().length() == 0) {
			Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "event.provide_a_location_for_this_event"),
					Toast.LENGTH_LONG).show();
			
			return false;
		}
		return true;
	}
	
	/**
	 * Class request create new event
	 */
	public class CreateNewEvent extends AsyncTask<String, Void, String> {
		
		@Override
		protected void onPreExecute() {
			eventProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String resultString = null;
			try {
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), "createNewEvent", true) + "&token=" + user.getTokenkey();
				} else {
					URL = Config.makeUrl(Config.CORE_URL, "createNewEvent", true) + "&token=" + user.getTokenkey();
				}
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("title", params[0]));
				pairs.add(new BasicNameValuePair("description", params[1]));
				
				aStartTime = params[2].split("/");
				if (aStartTime.length > 0) {
					pairs.add(new BasicNameValuePair("start_month", aStartTime[0]));
			        pairs.add(new BasicNameValuePair("start_day", aStartTime[1]));
			        pairs.add(new BasicNameValuePair("start_year", aStartTime[2]));
			        pairs.add(new BasicNameValuePair("start_hour", aStartTime[3]));
			        pairs.add(new BasicNameValuePair("start_minute", aStartTime[4]));
				}
				
				aEndTime = params[3].split("/");
				if (aEndTime.length > 0) {
					pairs.add(new BasicNameValuePair("end_month", aEndTime[0]));
			        pairs.add(new BasicNameValuePair("end_day", aEndTime[1]));
			        pairs.add(new BasicNameValuePair("end_year", aEndTime[2]));
			        pairs.add(new BasicNameValuePair("end_hour", aEndTime[3]));
			        pairs.add(new BasicNameValuePair("end_minute", aEndTime[4]));
				}
				
				pairs.add(new BasicNameValuePair("location", params[4]));
				//address
				if (params[5] != null) {
					
					pairs.add(new BasicNameValuePair("address", params[5]));
				}
				//city
				if (params[6] != null) {
					pairs.add(new BasicNameValuePair("city", params[6]));
				}
				//Zip/Postal Code
				if (params[7] != null) {
					pairs.add(new BasicNameValuePair("postal_code", params[7]));
				}
				
				pairs.add(new BasicNameValuePair("country_iso", params[8]));
				
				if (params[9] != null) {
					pairs.add(new BasicNameValuePair("country_child_id", params[9]));
				}
				
				pairs.add(new BasicNameValuePair("privacy", params[10]));
				pairs.add(new BasicNameValuePair("privacy_comment", params[11]));
				
				String category = null;
				if (params[12] != null) {
					category = params[12];
					if (params[13] != null) {
						category += "," + params[13];
					}
				}
				if (category != null) {
					pairs.add(new BasicNameValuePair("categories", category));	
				}
				
				resultString = networkUntil.makeHttpRequest(URL, "POST", pairs);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			
			return resultString;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					eventProgress.setVisibility(View.GONE);
					JSONObject mainJson = new JSONObject(result);
					Object intervention = mainJson.get("output");
					if (intervention instanceof JSONObject) {
						JSONObject output = (JSONObject) intervention;
						Toast.makeText(getApplicationContext(), Html.fromHtml(output.getString("notice")).toString(), Toast.LENGTH_LONG).show();
					}
					finish();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * Class request get category
	 */
	public class RequestCategory extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			lstCategory = new ArrayList<ComboBoxItem>();
			lstCategory.clear();	
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String resultstring = null;
			try {
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", params[0]));
				pairs.add(new BasicNameValuePair("method", "accountapi.getEventCategoryForAdd"));
				if (params[1] != null) {
					pairs.add(new BasicNameValuePair("category_id", "" + params[1]));
				}
				resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					categoryProgress.setVisibility(View.GONE);
					
					JSONObject mainJson = new JSONObject(result);
					JSONObject outputJson = mainJson.getJSONObject("output");
					JSONArray categoryJson = outputJson.getJSONArray("category");
					
					boolean bChildren = outputJson.getBoolean("children");
					
					ComboBoxItem item = null;
					JSONObject cbxItem = null;
				
					for (int i = 0; i < categoryJson.length(); i++) {
						item = new ComboBoxItem();
						cbxItem = categoryJson.getJSONObject(i);
						if (cbxItem.getBoolean("default") && !bChildren) {
							defCategory = cbxItem.getString("value");
						}
						item = item.convert(cbxItem);
						lstCategory.add(item);
					}
					
					if (!bChildren && defCategory != null ) {
						cbxCategory.addComboToView(CreateNewEventActivity.this, lstCategory, defCategory, categoryLayout, 
								phraseManager.getPhrase(getApplicationContext().getApplicationContext(), "event.category"), "category");
					} else {
						if (lstCategory.size() > 0) {
							childrenCategoryLayout.setVisibility(View.VISIBLE);
							childrenCategoryProgress.setVisibility(View.GONE);
							
							cbxChidrenCategory.addComboToView(CreateNewEventActivity.this, lstCategory, defCategory, childrenCategoryLayout, null, null);
						} 
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * Class request get countries
	 */
	public class RequestCountry extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			lstCountry = new ArrayList<ComboBoxItem>();
			lstCountry.clear();	
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String resultstring = null;
			try {
				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), null, false);	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, null, false);
				}
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", params[0]));
				pairs.add(new BasicNameValuePair("method", "accountapi.getCountries"));
				if (params[1] != null) {
					pairs.add(new BasicNameValuePair("iso", params[1]));
				}
				resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
				
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			return resultstring;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					countryProgress.setVisibility(View.GONE);
					
					JSONObject mainJson = new JSONObject(result);
					JSONObject outputJson = mainJson.getJSONObject("output");
					JSONArray countryJson = outputJson.getJSONArray("country");
					
					boolean bChildren = outputJson.getBoolean("children");
					
					ComboBoxItem item = null;
					JSONObject cbxItem = null;
					
					for (int i = 0; i < countryJson.length(); i++) {
						item = new ComboBoxItem();
						cbxItem = countryJson.getJSONObject(i);
						if (cbxItem.getBoolean("default")) {
							defCountry = cbxItem.getString("value");
						}
						item = item.convert(cbxItem);
						lstCountry.add(item);
					}
					
					if (!bChildren && defCountry != null) {
						
						cbxCountry.addComboToView(CreateNewEventActivity.this, lstCountry, defCountry, countryLayout, 
								phraseManager.getPhrase(getApplicationContext().getApplicationContext(), "event.country"), "country");
						
						new RequestCountry().execute(user.getTokenkey(), defCountry);
					} else {
						if (lstCountry.size() > 0) {
							childrenCountryLayout.setVisibility(View.VISIBLE);
							childrenCountryProgress.setVisibility(View.GONE);
							childrenCountryLayout.removeAllViews();
							cbxChidrenCountry.addComboToView(CreateNewEventActivity.this, lstCountry, defCountry, childrenCountryLayout, null, "country");
						} else {
							childrenCountryLayout.setVisibility(View.GONE);
							childrenCountryLayout.removeAllViews();
						}
					}
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
		
	}
}
