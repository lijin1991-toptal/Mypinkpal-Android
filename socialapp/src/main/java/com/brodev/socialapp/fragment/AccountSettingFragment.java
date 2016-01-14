package com.brodev.socialapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.manager.ComboBox;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.ComboBoxItem;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.UserSetting;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.ChangePassword;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountSettingFragment extends SherlockFragment {

	private User user;
	private NetworkUntil networkUntil = new NetworkUntil();
	private ProgressBar loading;
	private View view;
	// Phrase manager
	private PhraseManager phraseManager;
    private ColorView colorView;

	// Currency
	ArrayList<ComboBoxItem> listCurrency = new ArrayList<ComboBoxItem>(),
			listLanguage = new ArrayList<ComboBoxItem>(),
			listTimezone = new ArrayList<ComboBoxItem>();
	private LinearLayout currencyLayout, languageLayout, timezoneLayout;
	private ComboBox currencyChoose = null, languageChoose = null, timezoneChoose = null;
	private EditText textFullname, textUsername, textEmail;
	private TextView changePassword;
	// User setting
	private UserSetting us;
	private String URL_POST_USER_SETTING;
	private ProgressBar progressBar;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private ScrollView accountSettingLayout;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    @Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		user = (User) getActivity().getApplication();
        colorView = new ColorView(getActivity().getApplicationContext());
		us = new UserSetting();
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		// disable search menu item
		MenuItem search = menu.findItem(R.id.actionBar_chat);
		search.setVisible(false);

		inflater.inflate(R.menu.poststatus, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) 
		{
			case R.id.action_post:
                connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    UserSettingUpdateTask updateUserSettingTask = new UserSettingUpdateTask();

                    String full_name = textFullname.getText().toString().trim();
                    String user_name = textUsername.getText().toString().trim();
                    String old_user_name = us.getUser_name();
                    String email = textEmail.getText().toString().trim();
                    String language_id = languageChoose.getValue();
                    String currency = currencyChoose.getValue();
                    String time_zone = timezoneChoose.getValue();
                    String current_full_name = us.getFull_name();
                    String total_user_change = String.valueOf(us.getTotal_user_change());
                    String total_full_name_change = String.valueOf(us.getTotal_full_name_change());

                    updateUserSettingTask.execute(full_name, user_name, old_user_name,
                            email, language_id, time_zone, currency, current_full_name,
                            total_user_change, total_full_name_change);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }
				break;
		}
		return false;

	}

	@Override
	public void onResume() {
		super.onResume();
		AccountSettingTask mt = new AccountSettingTask();
		mt.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// create view from friend_fragment xml
		view = inflater.inflate(R.layout.account_setting, container, false);

        accountSettingLayout = (ScrollView) view.findViewById(R.id.account_setting_layout);

		// tab
		TextView moduleName = (TextView) view.findViewById(R.id.moduleName);
		moduleName.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.account_settings"));

		currencyLayout = (LinearLayout) view.findViewById(R.id.currencyComboBox);
		languageLayout = (LinearLayout) view.findViewById(R.id.languageComboBox);
		timezoneLayout = (LinearLayout) view.findViewById(R.id.timezoneComboBox);
		
		textEmail = (EditText) view.findViewById(R.id.textEmail);
		
		TextView txtEmail = (TextView) view.findViewById(R.id.txtEmail);
		txtEmail.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.email_address"));
		
		textUsername = (EditText) view.findViewById(R.id.textUsername);
		TextView txtUserName = (TextView) view.findViewById(R.id.userName);
		
		txtUserName.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.user_name"));
		textFullname = (EditText) view.findViewById(R.id.textFullname);
		
		TextView txtFullName = (TextView) view.findViewById(R.id.fullName);
		txtFullName.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.full_name"));
		
		changePassword = (TextView) view.findViewById(R.id.changePassword);		
		changePassword.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.change_password"));

		loading = (ProgressBar) view.findViewById(R.id.content_loading);
		progressBar = (ProgressBar) view.findViewById(R.id.account_setting_loading);

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
                        //fetch data
                        loadAccountSetting();
                    }
                }, 1000);
            }
        });

		return view;
	}

    private void loadAccountSetting() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                accountSettingLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //fetch data
                new AccountSettingTask().execute();
            } else {
                // display error
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                accountSettingLayout.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            progressBar.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            accountSettingLayout.setVisibility(View.GONE);
        }
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		try {
            loadAccountSetting();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

    /**
     * function get result from get method
     * @return
     */
	public String getResultFromGET() {
		String resultstring = null;

		try {
			// Use BasicNameValuePair to create GET data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
			pairs.add(new BasicNameValuePair("method", "accountapi.getUserSetting"));

			// url request
			String URL = Config.makeUrl(user.getCoreUrl(), null, false);

			// request GET method to server
			resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		return resultstring;
	}

	public class AccountSettingTask extends AsyncTask<Integer, Void, String> {

		String resultstring = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			if (isCancelled()) {
				return null;
			}
			try {
				// get result from get method
				resultstring = getResultFromGET();
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
					getUserSetting(result);
					viewUser();
					viewCurrency(result);
					viewLanguage(result);
					viewTimezone(result);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

	}

	/**
	 * show currency combo box
	 * 
	 * @param result
	 */
	private void viewCurrency(String result) {
		if (currencyChoose == null) {
			getCurrencies(result);
			currencyChoose = new ComboBox(getActivity());

			currencyChoose.addComboToView(getActivity(), listCurrency, us.getCurrency(), currencyLayout, 
					phraseManager.getPhrase(getActivity().getApplicationContext(), "user.preferred_currency"), null);
		}

	}

	/**
	 * get currency from json
	 * 
	 * @param resString
	 */
	public void getCurrencies(String resString) {
		
		try {
			JSONObject mainJSON = new JSONObject(resString);
			Object intervention = mainJSON.get("output");

			if (intervention instanceof JSONObject) {

				JSONObject outputJSON = mainJSON.getJSONObject("output");
				Object objCurrency = outputJSON.get("Currencies");

				if (objCurrency instanceof JSONArray) {
					JSONObject objCurrencyValue = null;
					JSONArray arObjCurrency = (JSONArray) objCurrency;
					for (int i = 0; i < arObjCurrency.length(); i++) {
						objCurrencyValue = arObjCurrency.getJSONObject(i);
						ComboBoxItem item = new ComboBoxItem();
						item = item.convert(objCurrencyValue);
						listCurrency.add(item);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * show currency combo box
	 * 
	 * @param result
	 */
	private void viewLanguage(String result) {
		if (languageChoose == null) {
			getLanguages(result);	
			languageChoose = new ComboBox(getActivity());

			languageChoose.addComboToView(getActivity(), listLanguage, us.getLanguage_id(), languageLayout, 
					phraseManager.getPhrase(getActivity().getApplicationContext(), "user.primary_language"), null);
		}
	}

	/**
	 * get language from json
	 * 
	 * @param resString
	 */
	public void getLanguages(String resString) {
		try {
			JSONObject  mainJSON = new JSONObject(resString);
			Object intervention = mainJSON.get("output");

			if (intervention instanceof JSONObject) {

				JSONObject outputJSON = mainJSON.getJSONObject("output");
				Object objCurrency = outputJSON.get("Languages");

				if (objCurrency instanceof JSONArray) {
					JSONObject objCurrencyValue = null;
					JSONArray arObjCurrency = (JSONArray) objCurrency;
					for (int i = 0; i < arObjCurrency.length(); i++) {
						objCurrencyValue = arObjCurrency.getJSONObject(i);
						ComboBoxItem item = new ComboBoxItem();
						item = item.convert(objCurrencyValue);
						listLanguage.add(item);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * show currency combo box
	 * 
	 * @param result
	 */
	private void viewTimezone(String result) {
		if (timezoneChoose == null) {
			getTimezone(result);
			timezoneChoose = new ComboBox(getActivity());
			timezoneChoose.addComboToView(getActivity(), listTimezone,  us.getTime_zone(), timezoneLayout, 
					phraseManager.getPhrase(getActivity().getApplicationContext(), "user.time_zone"), null);
		}		

	}

	/**
	 * get currency from json
	 * 
	 * @param resString
	 */
	public void getTimezone(String resString) {
		try {
			JSONObject mainJSON = new JSONObject(resString);
			Object intervention = mainJSON.get("output");

			if (intervention instanceof JSONObject) {

				JSONObject outputJSON = mainJSON.getJSONObject("output");
				Object objCurrency = outputJSON.get("Timezone");

				if (objCurrency instanceof JSONArray) {
					JSONObject objCurrencyValue = null;
					JSONArray arObjCurrency = (JSONArray) objCurrency;
					for (int i = 0; i < arObjCurrency.length(); i++) {
						objCurrencyValue = arObjCurrency.getJSONObject(i);
						ComboBoxItem item = new ComboBoxItem();
						item = item.convert(objCurrencyValue);
						listTimezone.add(item);
					}
				}
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * get user setting from json
	 * 
	 * @param resString
	 */
	public void getUserSetting(String resString) {
		JSONObject mainJSON;
		try {
			mainJSON = new JSONObject(resString);
			Object intervention = mainJSON.get("output");

			if (intervention instanceof JSONObject) {

				JSONObject outputJSON = mainJSON.getJSONObject("output");
				us = us.convert(outputJSON);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * get view user
	 */
	private void viewUser() {
        textEmail.setText(us.getEmail());
        textEmail.setEnabled(us.isCan_change_email());
		textEmail.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.email_address"));
		textFullname.setText(us.getFull_name());
		textFullname.setEnabled(us.isCan_change_full_name());
		textFullname.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.full_name"));
		textUsername.setText(us.getUser_name());
		textUsername.setEnabled(us.isCan_change_user_name());
		textUsername.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.user_name"));
		changePassword.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(), ChangePassword.class);
				intent.putExtra("password", us.getPassword());
				intent.putExtra("password_salt", us.getPassword_salt());		
				startActivity(intent);
			}
		});
		loading.setVisibility(View.GONE);
	}

	/**
     * create a blog params title content categories privacy
     *
     * @author Huy Nguyen
     */
    public class UserSettingUpdateTask extends AsyncTask<String, Void, String> {
        String result = null;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            if (isCancelled()) {
                return null;
            }

            URL_POST_USER_SETTING = Config.makeUrl(user.getCoreUrl(), "updateUserSeting", true)
                    + "&token=" + user.getTokenkey();
            // Use BasicNameValuePair to store POST data

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("full_name", params[0]));
            pairs.add(new BasicNameValuePair("user_name", params[1]));
            pairs.add(new BasicNameValuePair("old_user_name", params[2]));
            pairs.add(new BasicNameValuePair("email", params[3]));
            pairs.add(new BasicNameValuePair("language_id", params[4]));
            pairs.add(new BasicNameValuePair("time_zone", params[5]));
            pairs.add(new BasicNameValuePair("default_currency", params[6]));
            pairs.add(new BasicNameValuePair("current_full_name", params[7]));
            pairs.add(new BasicNameValuePair("total_user_change", params[8]));
            pairs.add(new BasicNameValuePair("total_full_name_change", params[9]));

            System.out.println(pairs);
            String result = networkUntil.makeHttpRequest(URL_POST_USER_SETTING, "POST", pairs);

            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // create new message adapter
                JSONObject mainJSON = new JSONObject(result);

                Object request = mainJSON.get("output");

                if (request instanceof JSONObject) {
                    JSONObject requestValue = (JSONObject) request;
                    String notice = requestValue.getString("notice");

                    // String notice
                    Toast.makeText(getActivity().getApplicationContext(), Html.fromHtml(notice).toString(), Toast.LENGTH_LONG).show();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);

            super.onPostExecute(result);
        }

    }

}
