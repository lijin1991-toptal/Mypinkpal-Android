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
import com.brodev.socialapp.android.manager.ComboBox;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.ComboBoxItem;
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
public class PrivacySettingsItems extends SherlockActivity {

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
    private LinearLayout blogsLayout, eventsLayout, songsLayout, photosLayout, pollsLayout, quizzesLayout, videosLayout;
    private ComboBox blogsChoose = null, eventsChoose = null, songsChoose = null, photosChoose = null, pollsChoose = null, quizzesChoose = null, videosChoose = null;
    ArrayList<ComboBoxItem> listBlogs = new ArrayList<ComboBoxItem>(),
            listEvents = new ArrayList<ComboBoxItem>(),
            listSongs = new ArrayList<ComboBoxItem>(),
            listPhotos = new ArrayList<ComboBoxItem>(),
            listPolls = new ArrayList<ComboBoxItem>(),
            listQuizzes = new ArrayList<ComboBoxItem>(),
            listVides = new ArrayList<ComboBoxItem>();

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private String URL_POST_USER_SETTING;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.privacy_settings_items);
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
                    PrivacySettingsItemsUpdateTask itemsUpdateTask = new PrivacySettingsItemsUpdateTask();

                    itemsUpdateTask.execute(blogsChoose.getValue(), eventsChoose.getValue(), songsChoose.getValue(),
                            photosChoose.getValue(), pollsChoose.getValue(), quizzesChoose.getValue(), videosChoose.getValue());
                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void initView() {
        getSupportActionBar().setTitle("Items");
        privacySettingsLayout = (ScrollView) this.findViewById(R.id.privacy_settings_layout);

        blogsLayout = (LinearLayout) this.findViewById(R.id.blogsComboBox);
        eventsLayout = (LinearLayout) this.findViewById(R.id.eventsComboBox);
        songsLayout = (LinearLayout) this.findViewById(R.id.songsComboBox);
        photosLayout = (LinearLayout) this.findViewById(R.id.photosComboBox);
        pollsLayout = (LinearLayout) this.findViewById(R.id.pollsComboBox);
        quizzesLayout = (LinearLayout) this.findViewById(R.id.quizzesComboBox);
        videosLayout = (LinearLayout) this.findViewById(R.id.videosComboBox);

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
                        loadPrivacySettingsItems();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPrivacySettingsItems();
    }

    private void loadPrivacySettingsItems() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                privacySettingsLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //fetch data
                new PrivacySettingsItemsTask().execute();
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
            pairs.add(new BasicNameValuePair("method", "accountapi.getPrivacySettingsItems"));

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

    public class PrivacySettingsItemsTask extends AsyncTask<Integer, Void, String> {

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
                    viewBlogs(result);
                    viewEvents(result);
                    viewSongs(result);
                    viewPhotos(result);
                    viewPolls(result);
                    viewQuizzes(result);
                    viewVideos(result);
                    loading.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    /**
     * show blogs combo box
     *
     * @param result
     */
    private void viewBlogs(String result) {
        if (blogsChoose == null) {
            getBlogs(result);
            blogsChoose = new ComboBox(this);
            blogsChoose.addComboToView(this, listBlogs, user.getPrivacy_blogs(), blogsLayout, "Blogs", null);
        }
    }

    /**
     * get blogs json
     *
     * @param resString
     */
    public void getBlogs(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject subJSON = outputJSON.getJSONObject("blog");
                Object objSub = subJSON.get("options");
                String def = subJSON.getString("default");
                if (def.equals(""))
                    def = "0";
                user.setPrivacy_blogs(def);

                if (objSub instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjCurrency = (JSONArray) objSub;
                    for (int i = 0; i < arObjCurrency.length(); i++) {
                        objCurrencyValue = arObjCurrency.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objCurrencyValue);
                        listBlogs.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show Events combo box
     *
     * @param result
     */
    private void viewEvents(String result) {
        if (eventsChoose == null) {
            getEvents(result);
            eventsChoose = new ComboBox(this);
            eventsChoose.addComboToView(this, listEvents, user.getPrivacy_events(), eventsLayout, "Events", null);
        }
    }

    /**
     * get events json
     *
     * @param resString
     */
    public void getEvents(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject subJSON = outputJSON.getJSONObject("event");
                Object objSub = subJSON.get("options");
                String def = subJSON.getString("default");
                if (def.equals(""))
                    def = "0";
                user.setPrivacy_events(def);

                if (objSub instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjCurrency = (JSONArray) objSub;
                    for (int i = 0; i < arObjCurrency.length(); i++) {
                        objCurrencyValue = arObjCurrency.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objCurrencyValue);
                        listEvents.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show songs combo box
     *
     * @param result
     */
    private void viewSongs(String result) {
        if (songsChoose == null) {
            getSongs(result);
            songsChoose = new ComboBox(this);
            songsChoose.addComboToView(this, listSongs, user.getPrivacy_songs(), songsLayout, "Songs", null);
        }
    }

    /**
     * get songs json
     *
     * @param resString
     */
    public void getSongs(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject subJSON = outputJSON.getJSONObject("music");
                Object objSub = subJSON.get("options");
                String def = subJSON.getString("default");
                if (def.equals(""))
                    def = "0";
                user.setPrivacy_songs(def);

                if (objSub instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjCurrency = (JSONArray) objSub;
                    for (int i = 0; i < arObjCurrency.length(); i++) {
                        objCurrencyValue = arObjCurrency.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objCurrencyValue);
                        listSongs.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show Photos combo box
     *
     * @param result
     */
    private void viewPhotos(String result) {
        if (photosChoose == null) {
            getPhotos(result);
            photosChoose = new ComboBox(this);
            photosChoose.addComboToView(this, listPhotos, user.getPrivacy_photos(), photosLayout, "Photos", null);
        }
    }

    /**
     * get photos json
     *
     * @param resString
     */
    public void getPhotos(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject subJSON = outputJSON.getJSONObject("photo");
                Object objSub = subJSON.get("options");
                String def = subJSON.getString("default");
                if (def.equals(""))
                    def = "0";
                user.setPrivacy_photos(def);

                if (objSub instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjCurrency = (JSONArray) objSub;
                    for (int i = 0; i < arObjCurrency.length(); i++) {
                        objCurrencyValue = arObjCurrency.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objCurrencyValue);
                        listPhotos.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show polls combo box
     *
     * @param result
     */
    private void viewPolls(String result) {
        if (pollsChoose == null) {
            getPolls(result);
            pollsChoose = new ComboBox(this);
            pollsChoose.addComboToView(this, listPolls, user.getPrivacy_polls(), pollsLayout, "Polls", null);
        }
    }

    /**
     * get polls json
     *
     * @param resString
     */
    public void getPolls(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject subJSON = outputJSON.getJSONObject("poll");
                Object objSub = subJSON.get("options");
                String def = subJSON.getString("default");
                if (def.equals(""))
                    def = "0";
                user.setPrivacy_polls(def);

                if (objSub instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjCurrency = (JSONArray) objSub;
                    for (int i = 0; i < arObjCurrency.length(); i++) {
                        objCurrencyValue = arObjCurrency.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objCurrencyValue);
                        listPolls.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show quizzes combo box
     *
     * @param result
     */
    private void viewQuizzes(String result) {
        if (quizzesChoose == null) {
            getQuizzes(result);
            quizzesChoose = new ComboBox(this);
            quizzesChoose.addComboToView(this, listQuizzes, user.getPrivacy_quizzes(), quizzesLayout, "Quizzes", null);
        }
    }

    /**
     * get quizzes json
     *
     * @param resString
     */
    public void getQuizzes(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject subJSON = outputJSON.getJSONObject("quiz");
                Object objSub = subJSON.get("options");
                String def = subJSON.getString("default");
                if (def.equals(""))
                    def = "0";
                user.setPrivacy_quizzes(def);

                if (objSub instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjCurrency = (JSONArray) objSub;
                    for (int i = 0; i < arObjCurrency.length(); i++) {
                        objCurrencyValue = arObjCurrency.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objCurrencyValue);
                        listQuizzes.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show videos combo box
     *
     * @param result
     */
    private void viewVideos(String result) {
        if (videosChoose == null) {
            getVideos(result);
            videosChoose = new ComboBox(this);
            videosChoose.addComboToView(this, listVides, user.getPrivacy_videos(), videosLayout, "Videos", null);
        }
    }

    /**
     * get videos json
     *
     * @param resString
     */
    public void getVideos(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject subJSON = outputJSON.getJSONObject("video");
                Object objSub = subJSON.get("options");
                String def = subJSON.getString("default");
                if (def.equals(""))
                    def = "0";
                user.setPrivacy_videos(def);

                if (objSub instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjCurrency = (JSONArray) objSub;
                    for (int i = 0; i < arObjCurrency.length(); i++) {
                        objCurrencyValue = arObjCurrency.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objCurrencyValue);
                        listVides.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class PrivacySettingsItemsUpdateTask extends AsyncTask<String, Void, String> {
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
            pairs.add(new BasicNameValuePair("val[blog.default_privacy_setting][blog.default_privacy_setting]", params[0]));
            pairs.add(new BasicNameValuePair("val[event.display_on_profile][event.display_on_profile]", params[1]));
            pairs.add(new BasicNameValuePair("val[music.default_privacy_setting][music.default_privacy_setting]", params[2]));
            pairs.add(new BasicNameValuePair("val[photo.default_privacy_setting][photo.default_privacy_setting]", params[3]));
            pairs.add(new BasicNameValuePair("val[poll.default_privacy_setting][poll.default_privacy_setting]", params[4]));
            pairs.add(new BasicNameValuePair("val[quiz.default_privacy_setting][quiz.default_privacy_setting]", params[5]));
            pairs.add(new BasicNameValuePair("val[video.display_on_profile][video.display_on_profile]", params[6]));

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
