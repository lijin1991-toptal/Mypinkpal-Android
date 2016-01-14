package com.brodev.socialapp.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bebel on 3/12/15.
 */
public class PrivacySettingsBlockedUsers extends SherlockActivity {

    private User user;
    private NetworkUntil networkUntil = new NetworkUntil();
    private PhraseManager phraseManager;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private ScrollView privacySettingsLayout;

    private ProgressBar loading, progressBar;
    private LinearLayout verticalLinear, horizonLinear;

    private ArrayList<CheckBox> checkBoxArrayList;
    private ArrayList<String> idArrayList;
    private ArrayList<String> selectedArrayList;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private String URL_POST_USER_SETTING;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.privacy_settings_blocked_users);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phraseManager = new PhraseManager(getApplicationContext());
        user = (User) getApplication();
        colorView = new ColorView(getApplicationContext());

        initView();
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
                return true;
            case R.id.action_post:
                connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                networkInfo = connMgr.getActiveNetworkInfo();

                selectedArrayList = new ArrayList<String>();

                if (networkInfo != null && networkInfo.isConnected()) {
                    PrivacySettingsBlockedUsersUpdateTask blockUpdateTask = new PrivacySettingsBlockedUsersUpdateTask();
                    for (int i = 0; i < checkBoxArrayList.size(); i++) {
                        CheckBox selected = checkBoxArrayList.get(i);
                        if (selected.isChecked())
                            selectedArrayList.add(idArrayList.get(i));
                    }

                    if (selectedArrayList.size() > 0) {
                        blockUpdateTask.execute(selectedArrayList);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void initView() {
        getSupportActionBar().setTitle("Blocked Users");
        privacySettingsLayout = (ScrollView) this.findViewById(R.id.privacy_settings_layout);
        verticalLinear = (LinearLayout) this.findViewById(R.id.verticalLinearLayout);

        loading = (ProgressBar) this.findViewById(R.id.content_loading);
        progressBar = (ProgressBar) this.findViewById(R.id.privacy_settings_loading);

        //no internet connection
        noInternetLayout = (RelativeLayout) this.findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) this.findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) this.findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) this.findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) this.findViewById(R.id.no_internet_image);

        //change color for no internet
        colorView.changeImageForNoInternet(noInternetImg, noInternetBtn, user.getColor());

        //set text for no internet element
        noInternetBtn.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.try_again"));
        noInternetTitle.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_title"));
        noInternetContent.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"));

        //action click load try again
        noInternetBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //fetch data
                        loadPrivacySettingsBlockedUsers();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPrivacySettingsBlockedUsers();
    }

    private void loadPrivacySettingsBlockedUsers() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                privacySettingsLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //fetch data
                new PrivacySettingsBlockedUsersTask().execute();
            } else {
                // display error
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                privacySettingsLayout.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            progressBar.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            privacySettingsLayout.setVisibility(View.GONE);
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
            pairs.add(new BasicNameValuePair("method", "accountapi.getPrivacySettingsBlockedUsers"));

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

    public class PrivacySettingsBlockedUsersTask extends AsyncTask<Integer, Void, String> {

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
                    viewBlockedUsers(result);
                    loading.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * show Blocked Users
     */
    private void viewBlockedUsers(String result) {
        if (checkBoxArrayList == null) {
            try {
                JSONObject mainJSON = new JSONObject(result);
                Object intervention = mainJSON.get("output");

                if (intervention instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjCurrency = (JSONArray) intervention;
                    checkBoxArrayList = new ArrayList<CheckBox>();
                    idArrayList = new ArrayList<String>();

                    for (int i = 0; i < arObjCurrency.length(); i++) {
                        objCurrencyValue = arObjCurrency.getJSONObject(i);
                        CheckBox userCheckBox = new CheckBox(this);
                        TextView userTextView = new TextView(this);
                        userTextView.setText(objCurrencyValue.getString("full_name"));
                        horizonLinear = new LinearLayout(this);
                        horizonLinear.addView(userCheckBox);
                        horizonLinear.addView(userTextView);
                        verticalLinear.addView(horizonLinear);

                        checkBoxArrayList.add(userCheckBox);
                        idArrayList.add(objCurrencyValue.getString("user_id"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class PrivacySettingsBlockedUsersUpdateTask extends AsyncTask<ArrayList<String>, Void, String> {
        String result = null;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ArrayList<String>... params) {

            if (isCancelled()) {
                return null;
            }
            ArrayList<String> arr = params[0];

            URL_POST_USER_SETTING = Config.makeUrl(user.getCoreUrl(), "updatePrivacySettings", true)
                    + "&token=" + user.getTokenkey();
            // Use BasicNameValuePair to store POST data

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            for (int i = 0; i < arr.size(); i++) {
                pairs.add(new BasicNameValuePair("val[blocked][" + String.valueOf(i) + "]", arr.get(i)));
            }

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
                    Toast.makeText(getApplicationContext(), Html.fromHtml(notice).toString(), Toast.LENGTH_LONG).show();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            progressBar.setVisibility(View.GONE);

            super.onPostExecute(result);
        }
    }
}
