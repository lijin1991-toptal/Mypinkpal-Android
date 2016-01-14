package com.brodev.socialapp.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Display;
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
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Bebel on 3/12/15.
 */
public class PrivacySettingsProfile extends SherlockActivity {

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
    private LinearLayout viewYourWallLayout, shareOnYourWallLayout, viewYourFriendsLayout, receviedGiftsLayout;
    private LinearLayout sendYouMessageLayout, viewPhotosYourProfileLayout, canSendPokesLayout, viewYourProfileLayout;
    private LinearLayout viewYourBasicInfoLayout, viewYourProfileInfoLayout, viewYourLocationLayout;
    private LinearLayout rateYourProfileLayout, displayRSSLayout, subscribeYourRSSLayout, viewWhoYourProfileLayout, whoCanTagLayout, dateBrithLayout;
    private ComboBox viewYourWallChoose = null, shareOnYourWallChoose = null, viewYourFriendsChoose = null, receviedGiftsChoose = null;
    private ComboBox sendYouMessageChoose = null, viewPhotosYourProfileChoose = null, canSendPokesChoose = null, viewYourProfileChoose = null;
    private ComboBox viewYourBasicInfoChoose = null, viewYourProfileInfoChoose = null, viewYourLocationChoose = null;
    private ComboBox rateYourProfileChoose = null, displayRSSChoose = null, subscribeYourRSSChoose = null, viewWhoYourProfileChoose = null, whoCanTagChoose = null, dateBrithChoose = null;
    ArrayList<ComboBoxItem> listViewYourWall = new ArrayList<ComboBoxItem>(),
            listShareOnYourWall = new ArrayList<ComboBoxItem>(),
            listViewYourFriends = new ArrayList<ComboBoxItem>(),
            listReceivedGifts = new ArrayList<ComboBoxItem>(),
            listSendYouMessage = new ArrayList<ComboBoxItem>(),
            listViewPhotosYourProfile = new ArrayList<ComboBoxItem>(),
            listCanSendPokes = new ArrayList<ComboBoxItem>(),
            listViewYourProfile = new ArrayList<ComboBoxItem>(),
            listViewYourBasicInfo = new ArrayList<ComboBoxItem>(),
            listViewYourProfileInfo = new ArrayList<ComboBoxItem>(),
            listViewYourLocation = new ArrayList<ComboBoxItem>(),
            listRateYourProfile = new ArrayList<ComboBoxItem>(),
            listDisplayRSS = new ArrayList<ComboBoxItem>(),
            listSubscribeYourRSS = new ArrayList<ComboBoxItem>(),
            listViewWhoYourProfile = new ArrayList<ComboBoxItem>(),
            listWhoCanTag = new ArrayList<ComboBoxItem>(),
            listDateBrith = new ArrayList<ComboBoxItem>();

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private String URL_POST_USER_SETTING;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.privacy_settings_profile);
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
                    PrivacySettingsProfileUpdateTask profileUpdateTask = new PrivacySettingsProfileUpdateTask();

                    profileUpdateTask.execute(viewYourWallChoose.getValue(), shareOnYourWallChoose.getValue(), viewYourFriendsChoose.getValue(), receviedGiftsChoose.getValue(),
                            sendYouMessageChoose.getValue(), viewPhotosYourProfileChoose.getValue(), canSendPokesChoose.getValue(), viewYourProfileChoose.getValue(),
                            viewYourBasicInfoChoose.getValue(), viewYourProfileInfoChoose.getValue(), viewYourLocationChoose.getValue(), rateYourProfileChoose.getValue(),
                            displayRSSChoose.getValue(), subscribeYourRSSChoose.getValue(), viewWhoYourProfileChoose.getValue(),
                            whoCanTagChoose.getValue(), dateBrithChoose.getValue());
                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void initView() {
        getSupportActionBar().setTitle("Profile");
        privacySettingsLayout = (ScrollView) this.findViewById(R.id.privacy_settings_layout);

        viewYourWallLayout = (LinearLayout) this.findViewById(R.id.viewYourWallComboBox);
        shareOnYourWallLayout = (LinearLayout) this.findViewById(R.id.shareOnYourWallComboBox);
        viewYourFriendsLayout = (LinearLayout) this.findViewById(R.id.viewYourFriendsComboBox);
        receviedGiftsLayout = (LinearLayout) this.findViewById(R.id.receviedGiftsComboBox);
        sendYouMessageLayout = (LinearLayout) this.findViewById(R.id.sendYouMessageComboBox);
        viewPhotosYourProfileLayout = (LinearLayout) this.findViewById(R.id.viewPhotosYourProfileComboBox);
        canSendPokesLayout = (LinearLayout) this.findViewById(R.id.canSendPokesComboBox);
        viewYourProfileLayout = (LinearLayout) this.findViewById(R.id.viewYourProfileComboBox);
        viewYourBasicInfoLayout = (LinearLayout) this.findViewById(R.id.viewYourBasicInfoComboBox);
        viewYourProfileInfoLayout = (LinearLayout) this.findViewById(R.id.viewYourProfileInfoComboBox);
        viewYourLocationLayout = (LinearLayout) this.findViewById(R.id.viewYourLocationComboBox);
        rateYourProfileLayout = (LinearLayout) this.findViewById(R.id.rateYourProfileComboBox);
        displayRSSLayout = (LinearLayout) this.findViewById(R.id.displayRSSComboBox);
        subscribeYourRSSLayout = (LinearLayout) this.findViewById(R.id.subscribeYourRSSComboBox);
        viewWhoYourProfileLayout = (LinearLayout) this.findViewById(R.id.viewWhoYourProfileComboBox);
        whoCanTagLayout = (LinearLayout) this.findViewById(R.id.whoCanTagComboBox);
        dateBrithLayout = (LinearLayout) this.findViewById(R.id.dateBrithComboBox);

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
                        loadPrivacySettingsProfile();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        PrivacySettingsProfileTask mt = new PrivacySettingsProfileTask();
        mt.execute();
        loadPrivacySettingsProfile();
    }

    private void loadPrivacySettingsProfile() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                privacySettingsLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //fetch data
                new PrivacySettingsProfileTask().execute();
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
            pairs.add(new BasicNameValuePair("method", "accountapi.getPrivacySettingsProfile"));

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

    public class PrivacySettingsProfileTask extends AsyncTask<Integer, Void, String> {

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
                    viewViewYourWall(result);
                    viewShareOnYourWall(result);
                    viewViewYourFriends(result);
                    viewReceviedGifts(result);
                    viewSendYouMessage(result);
                    viewViewPhotosYourProfile(result);
                    viewCanSendPokes(result);
                    viewViewYourProfile(result);
                    viewViewYourBasicInfo(result);
                    viewViewYourProfileInfo(result);
                    viewViewYourLocation(result);
                    viewRateYourProfile(result);
                    viewDisplayRSS(result);
                    viewSubscribeYourRSS(result);
                    viewViewWhoYourProfile(result);
                    viewWhoCanTag(result);
                    viewDateBrith(result);
                    loading.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    /**
     * show view your wall combo box
     *
     * @param result
     */
    private void viewViewYourWall(String result) {
        if (viewYourWallChoose == null) {
            getViewYourWall(result);
            viewYourWallChoose = new ComboBox(this);
            String wall = user.getView_your_wall();
            viewYourWallChoose.addComboToView(this, listViewYourWall, wall, viewYourWallLayout, "View your wall", null);
        }
    }

    /**
     * get view your wall from json
     *
     * @param resString
     */
    public void getViewYourWall(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("View your wall");
                Object objWall = wallJSON.get("options");
                user.setView_your_wall(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listViewYourWall.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show share on your wall combo box
     *
     * @param result
     */
    private void viewShareOnYourWall(String result) {
        if (shareOnYourWallChoose == null) {
            getShareOnYourWall(result);
            shareOnYourWallChoose = new ComboBox(this);
            shareOnYourWallChoose.addComboToView(this, listShareOnYourWall, user.getShare_your_wall(), shareOnYourWallLayout, "Share on your wall", null);
        }
    }

    /**
     * get share on your wall from json
     *
     * @param resString
     */
    public void getShareOnYourWall(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject shareJSON = outputJSON.getJSONObject("Share on your wall");
                Object objShare = shareJSON.get("options");
                user.setShare_your_wall(shareJSON.getString("default"));

                if (objShare instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objShare;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listShareOnYourWall.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show view your friends combo box
     *
     * @param result
     */
    private void viewViewYourFriends(String result) {
        if (viewYourFriendsChoose == null) {
            getViewYourFriends(result);
            viewYourFriendsChoose = new ComboBox(this);
            viewYourFriendsChoose.addComboToView(this, listViewYourFriends, user.getView_your_friends(), viewYourFriendsLayout, "View your friends", null);
        }
    }

    /**
     * get view your friends from json
     *
     * @param resString
     */
    public void getViewYourFriends(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("View your friends");
                Object objWall = wallJSON.get("options");
                user.setView_your_friends(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listViewYourFriends.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show received gifts combo box
     *
     * @param result
     */
    private void viewReceviedGifts(String result) {
        if (receviedGiftsChoose == null) {
            getReceviedGifts(result);
            receviedGiftsChoose = new ComboBox(this);
            receviedGiftsChoose.addComboToView(this, listReceivedGifts, user.getReceived_gifts(), receviedGiftsLayout, "Received Gifts", null);
        }
    }

    /**
     * get received gifts from json
     *
     * @param resString
     */
    public void getReceviedGifts(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("Received Gifts");
                Object objWall = wallJSON.get("options");
                user.setReceived_gifts(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listReceivedGifts.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show send you message combo box
     *
     * @param result
     */
    private void viewSendYouMessage(String result) {
        if (sendYouMessageChoose == null) {
            getSendYouMessage(result);
            sendYouMessageChoose = new ComboBox(this);
            sendYouMessageChoose.addComboToView(this, listSendYouMessage, user.getSend_you_message(), sendYouMessageLayout, "Send you a message", null);
        }
    }

    /**
     * get send you a message from json
     *
     * @param resString
     */
    public void getSendYouMessage(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("Send you a message");
                Object objWall = wallJSON.get("options");
                user.setSend_you_message(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listSendYouMessage.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show view photos your profile combo box
     *
     * @param result
     */
    private void viewViewPhotosYourProfile(String result) {
        if (viewPhotosYourProfileChoose == null) {
            getViewPhotosYourProfile(result);
            viewPhotosYourProfileChoose = new ComboBox(this);
            viewPhotosYourProfileChoose.addComboToView(this, listViewPhotosYourProfile, user.getView_photos_your_profile(), viewPhotosYourProfileLayout, "View photos within your profile", null);
        }
    }

    /**
     * get view photos your profile from json
     *
     * @param resString
     */
    public void getViewPhotosYourProfile(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("View photos within your profile");
                Object objWall = wallJSON.get("options");
                user.setView_photos_your_profile(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listViewPhotosYourProfile.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show can send pokes combo box
     *
     * @param result
     */
    private void viewCanSendPokes(String result) {
        if (canSendPokesChoose == null) {
            getCanSendPokes(result);
            canSendPokesChoose = new ComboBox(this);
            canSendPokesChoose.addComboToView(this, listCanSendPokes, user.getCan_send_pokes(), canSendPokesLayout, "Can send pokes", null);
        }
    }

    /**
     * get can send pokes from json
     *
     * @param resString
     */
    public void getCanSendPokes(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("Can send pokes");
                Object objWall = wallJSON.get("options");
                user.setCan_send_pokes(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listCanSendPokes.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show view your profile combo box
     *
     * @param result
     */
    private void viewViewYourProfile(String result) {
        if (viewYourProfileChoose == null) {
            getViewYourProfile(result);
            viewYourProfileChoose = new ComboBox(this);
            viewYourProfileChoose.addComboToView(this, listViewYourProfile, user.getView_your_profile(), viewYourProfileLayout, "View your profile", null);
        }
    }

    /**
     * get view your profile from json
     *
     * @param resString
     */
    public void getViewYourProfile(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("View your profile");
                Object objWall = wallJSON.get("options");
                user.setView_your_profile(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listViewYourProfile.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show view your basic info combo box
     *
     * @param result
     */
    private void viewViewYourBasicInfo(String result) {
        if (viewYourBasicInfoChoose == null) {
            getViewYourBasicInfo(result);
            viewYourBasicInfoChoose = new ComboBox(this);
            viewYourBasicInfoChoose.addComboToView(this, listViewYourBasicInfo, user.getView_your_basic_information(), viewYourBasicInfoLayout, "View your basic information", null);
        }
    }

    /**
     * get view your basic from json
     *
     * @param resString
     */
    public void getViewYourBasicInfo(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("View your basic information");
                Object objWall = wallJSON.get("options");
                user.setView_your_basic_information(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listViewYourBasicInfo.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show view your profile info combo box
     *
     * @param result
     */
    private void viewViewYourProfileInfo(String result) {
        if (viewYourProfileInfoChoose == null) {
            getViewYourProfileInfo(result);
            viewYourProfileInfoChoose = new ComboBox(this);
            viewYourProfileInfoChoose.addComboToView(this, listViewYourProfileInfo, user.getView_your_profile_information(), viewYourProfileInfoLayout, "View your profile information", null);
        }
    }

    /**
     * get view your profile info from json
     *
     * @param resString
     */
    public void getViewYourProfileInfo(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("View your profile information");
                Object objWall = wallJSON.get("options");
                user.setView_your_profile_information(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listViewYourProfileInfo.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show view your location combo box
     *
     * @param result
     */
    private void viewViewYourLocation(String result) {
        if (viewYourLocationChoose == null) {
            getViewYourLocation(result);
            viewYourLocationChoose = new ComboBox(this);
            viewYourLocationChoose.addComboToView(this, listViewYourLocation, user.getView_your_location(), viewYourLocationLayout, "View your location", null);
        }
    }

    /**
     * get view your location from json
     *
     * @param resString
     */
    public void getViewYourLocation(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("View your location");
                Object objWall = wallJSON.get("options");
                user.setView_your_location(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listViewYourLocation.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show rate your profile combo box
     *
     * @param result
     */
    private void viewRateYourProfile(String result) {
        if (rateYourProfileChoose == null) {
            getRateYourProfile(result);
            rateYourProfileChoose = new ComboBox(this);
            rateYourProfileChoose.addComboToView(this, listRateYourProfile, user.getRate_your_profile(), rateYourProfileLayout, "Rate your profile", null);
        }
    }

    /**
     * get rate your profile from json
     *
     * @param resString
     */
    public void getRateYourProfile(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("Rate your profile");
                Object objWall = wallJSON.get("options");
                user.setRate_your_profile(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listRateYourProfile.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show display RSS combo box
     *
     * @param result
     */
    private void viewDisplayRSS(String result) {
        if (displayRSSChoose == null) {
            getDisplayRSS(result);
            displayRSSChoose = new ComboBox(this);
            displayRSSChoose.addComboToView(this, listDisplayRSS, user.getDisplay_rss_subscribers(), displayRSSLayout, "Display RSS subscribers count", null);
        }
    }

    /**
     * get display RSS from json
     *
     * @param resString
     */
    public void getDisplayRSS(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("Display RSS subscribers count");
                Object objWall = wallJSON.get("options");
                user.setDisplay_rss_subscribers(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listDisplayRSS.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show subscribe your RSS combo box
     *
     * @param result
     */
    private void viewSubscribeYourRSS(String result) {
        if (subscribeYourRSSChoose == null) {
            getSubscribeYourRSS(result);
            subscribeYourRSSChoose = new ComboBox(this);
            subscribeYourRSSChoose.addComboToView(this, listSubscribeYourRSS, user.getSubscribe_your_rss_feed(), subscribeYourRSSLayout, "Subscribe to your RSS feed", null);
        }
    }

    /**
     * get subscribe your RSS from json
     *
     * @param resString
     */
    public void getSubscribeYourRSS(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("Subscribe to your RSS feed");
                Object objWall = wallJSON.get("options");
                user.setSubscribe_your_rss_feed(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listSubscribeYourRSS.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show view who your profile info combo box
     *
     * @param result
     */
    private void viewViewWhoYourProfile(String result) {
        if (viewWhoYourProfileChoose == null) {
            getViewWhoYourProfile(result);
            viewWhoYourProfileChoose = new ComboBox(this);
            viewWhoYourProfileChoose.addComboToView(this, listViewWhoYourProfile, user.getView_who_your_profile(), viewWhoYourProfileLayout, "View who recently viewed your profile", null);
        }
    }

    /**
     * get view who your profile from json
     *
     * @param resString
     */
    public void getViewWhoYourProfile(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("View who recently viewed your profile");
                Object objWall = wallJSON.get("options");
                user.setView_who_your_profile(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listViewWhoYourProfile.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show who can tag combo box
     *
     * @param result
     */
    private void viewWhoCanTag(String result) {
        if (whoCanTagChoose == null) {
            getWhoCanTag(result);
            whoCanTagChoose = new ComboBox(this);
            whoCanTagChoose.addComboToView(this, listWhoCanTag, user.getWho_tag_written_contexts(), whoCanTagLayout, "Who can tag me in written contexts?", null);
        }
    }

    /**
     * get who can tag from json
     *
     * @param resString
     */
    public void getWhoCanTag(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("Who can tag me in written contexts?");
                Object objWall = wallJSON.get("options");
                user.setWho_tag_written_contexts(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listWhoCanTag.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * show date brith combo box
     *
     * @param result
     */
    private void viewDateBrith(String result) {
        if (dateBrithChoose == null) {
            getDateBrith(result);
            dateBrithChoose = new ComboBox(this);
            dateBrithChoose.addComboToView(this, listDateBrith, user.getDate_of_birth(), dateBrithLayout, "Date of Birth:", null);
        }
    }

    /**
     * get date brith from json
     *
     * @param resString
     */
    public void getDateBrith(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                JSONObject wallJSON = outputJSON.getJSONObject("Date of Birth");
                Object objWall = wallJSON.get("options");
                user.setDate_of_birth(wallJSON.getString("default"));

                if (objWall instanceof JSONArray) {
                    JSONObject objWallValue = null;
                    JSONArray arObjWall = (JSONArray) objWall;
                    for (int i = 0; i < arObjWall.length(); i++) {
                        objWallValue = arObjWall.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objWallValue);
                        listDateBrith.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class PrivacySettingsProfileUpdateTask extends AsyncTask<String, Void, String> {
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
            pairs.add(new BasicNameValuePair("val[privacy][feed.view_wall]", params[0]));
            pairs.add(new BasicNameValuePair("val[privacy][feed.share_on_wall]", params[1]));
            pairs.add(new BasicNameValuePair("val[privacy][friend.view_friend]", params[2]));
            pairs.add(new BasicNameValuePair("val[privacy][gift.show_gifts]", params[3]));
            pairs.add(new BasicNameValuePair("val[privacy][mail.send_message]", params[4]));
            pairs.add(new BasicNameValuePair("val[privacy][photo.display_on_profile]", params[5]));
            pairs.add(new BasicNameValuePair("val[privacy][poke.can_send_poke]", params[6]));
            pairs.add(new BasicNameValuePair("val[privacy][profile.view_profile]", params[7]));
            pairs.add(new BasicNameValuePair("val[privacy][profile.basic_info]", params[8]));
            pairs.add(new BasicNameValuePair("val[privacy][profile.profile_info]", params[9]));
            pairs.add(new BasicNameValuePair("val[privacy][profile.view_location]", params[10]));
            pairs.add(new BasicNameValuePair("val[privacy][rate.can_rate]", params[11]));
            pairs.add(new BasicNameValuePair("val[privacy][rss.display_on_profile]", params[12]));
            pairs.add(new BasicNameValuePair("val[privacy][rss.can_subscribe_profile]", params[13]));
            pairs.add(new BasicNameValuePair("val[privacy][track.display_on_profile]", params[14]));
            pairs.add(new BasicNameValuePair("val[privacy][user.can_i_be_tagged]", params[15]));
            pairs.add(new BasicNameValuePair("val[special][dob_setting]", params[16]));

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
