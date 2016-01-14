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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bebel on 3/12/15.
 */
public class PrivacySettingsInvisibleMode extends SherlockActivity {

    private User user;
    private NetworkUntil networkUntil = new NetworkUntil();
    private PhraseManager phraseManager;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;

    private ProgressBar loading, progressBar;
    private Switch enableInvisibleSwitch;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private String URL_POST_USER_SETTING;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.privacy_settings_invisible_mode);
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

                if (networkInfo != null && networkInfo.isConnected()) {
                    PrivacySettingsInvisibleModeUpdateTask invisibleModeUpdateTask = new PrivacySettingsInvisibleModeUpdateTask();
                    String invisibleMode;

                    if (enableInvisibleSwitch.isChecked())
                        invisibleMode = "0";
                    else
                        invisibleMode = "1";

                    invisibleModeUpdateTask.execute(invisibleMode);
                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void initView() {
        getSupportActionBar().setTitle("Invisible Mode");

        enableInvisibleSwitch = (Switch) this.findViewById(R.id.enableInvisibleSwitch);

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
                        loadPrivacySettingsInvisibleMode();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPrivacySettingsInvisibleMode();
    }

    private void loadPrivacySettingsInvisibleMode() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //fetch data
                new PrivacySettingsInvisibleModeTask().execute();
            } else {
                // display error
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            progressBar.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
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
            pairs.add(new BasicNameValuePair("method", "accountapi.getPrivacySettingsInvisibleMode"));

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

    public class PrivacySettingsInvisibleModeTask extends AsyncTask<Integer, Void, String> {

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
                    viewInvisibleMode(result);
                    loading.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    /**
     * show Invisible Mode
     */
    private void viewInvisibleMode(String result) {
        try {
            JSONObject mainJSON = new JSONObject(result);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {
                JSONObject outputJSON = mainJSON.getJSONObject("output");
                String invisibleMode = outputJSON.getString("is_invisible");

                if (invisibleMode.equals("1"))
                    enableInvisibleSwitch.setChecked(false);
                else
                    enableInvisibleSwitch.setChecked(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class PrivacySettingsInvisibleModeUpdateTask extends AsyncTask<String, Void, String> {
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

            URL_POST_USER_SETTING = Config.makeUrl(user.getCoreUrl(), "updatePrivacySettings", true)
                    + "&token=" + user.getTokenkey();
            // Use BasicNameValuePair to store POST data

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("val[invisible]", params[0]));

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
