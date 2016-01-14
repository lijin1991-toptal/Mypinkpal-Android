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
 * Created by Bebel on 3/11/15.
 */
public class EditProfileDetails extends SherlockActivity {

    private User user;
    private NetworkUntil networkUntil = new NetworkUntil();
    private PhraseManager phraseManager;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private ScrollView editProfileDetailsLayout;

    private ProgressBar loading, progressBar;
    private LinearLayout smokerLayout, drinkerLayout;
    private ComboBox smokerChoose = null, drinkerChoose = null;
    ArrayList<ComboBoxItem> listSmoker = new ArrayList<ComboBoxItem>(),
            listDrinker = new ArrayList<ComboBoxItem>();

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private String URL_POST_USER_SETTING;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_profile_details);
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
                    EditProfileDetailsUpdateTask editProfileTask = new EditProfileDetailsUpdateTask();

                    String smoker = smokerChoose.getValue();
                    String drinker = drinkerChoose.getValue();

                    editProfileTask.execute(smoker, drinker);

                    UpdateDataTask updateDataTask = new UpdateDataTask();
                    updateDataTask.execute();
                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        getSupportActionBar().setTitle("Details");
        editProfileDetailsLayout = (ScrollView) this.findViewById(R.id.edit_profile_layout);

        smokerLayout = (LinearLayout) this.findViewById(R.id.smokerComboBox);
        drinkerLayout = (LinearLayout) this.findViewById(R.id.drinkerComboBox);

        loading = (ProgressBar) this.findViewById(R.id.content_loading);
        progressBar = (ProgressBar) this.findViewById(R.id.edit_profile_basic_loading);

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
                        loadEditProfileDetails();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        EditProfileDetailsTask mt = new EditProfileDetailsTask();
        mt.execute();
        loadEditProfileDetails();

    }

    private void loadEditProfileDetails() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                editProfileDetailsLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                //fetch data
                new EditProfileDetailsTask().execute();
            } else {
                // display error
                progressBar.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                editProfileDetailsLayout.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            progressBar.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            editProfileDetailsLayout.setVisibility(View.GONE);
        }
    }


    public class EditProfileDetailsUpdateTask extends AsyncTask<String, Void, String> {
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

            URL_POST_USER_SETTING = Config.makeUrl(user.getCoreUrl(), "updateProfile", true)
                    + "&token=" + user.getTokenkey();
            // Use BasicNameValuePair to store POST data

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("custom[6]", params[0]));
            pairs.add(new BasicNameValuePair("custom[7]", params[1]));

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
            pairs.add(new BasicNameValuePair("method", "accountapi.getProfileOptions"));

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

    public class EditProfileDetailsTask extends AsyncTask<Integer, Void, String> {

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
                    viewSmoker(result);
                    viewDrinker(result);
                    loading.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    /**
     * show smoker combo box
     *
     * @param result
     */
    private void viewSmoker(String result) {
        if (smokerChoose == null) {
            getSmoker(result);
            smokerChoose = new ComboBox(this);
            String selected = user.getCustom_smoker();
            smokerChoose.addComboToView(this, listSmoker, selected, smokerLayout, "Smoker", null);
        }
    }

    /**
     * get smoker from json
     *
     * @param resString
     */
    public void getSmoker(String resString) {
        ArrayList<String> smokerArr = new ArrayList<String>();
        smokerArr.add("Select:");
        smokerArr.add("Sometimes");
        smokerArr.add("No");
        smokerArr.add("Yes");

        for (int i = 0; i < smokerArr.size(); i++) {
            ComboBoxItem item = new ComboBoxItem();
            item.setName(smokerArr.get(i));
            if (i == 0)
                item.setValue("");
            else
                item.setValue(String.valueOf(i));
            listSmoker.add(item);
        }
    }

    /**
     * show Drinker combo box
     *
     * @param result
     */
    private void viewDrinker(String result) {
        if (drinkerChoose == null) {
            getDrinker(result);
            drinkerChoose = new ComboBox(this);
            String selected = user.getCustom_drinker();
            drinkerChoose.addComboToView(this, listDrinker, selected, drinkerLayout, "Drinker", null);
        }
    }

    /**
     * get drinker from json
     *
     * @param resString
     */
    public void getDrinker(String resString) {

        ArrayList<String> drinkerArr = new ArrayList<String>();
        drinkerArr.add("Select:");
        drinkerArr.add("Yes");
        drinkerArr.add("No");
        drinkerArr.add("Sometimes");

        for (int i = 0; i < drinkerArr.size(); i++) {
            ComboBoxItem item = new ComboBoxItem();
            item.setName(drinkerArr.get(i));
            if (i == 0)
                item.setValue("");
            else
                item.setValue(String.valueOf(i));
            listDrinker.add(item);
        }
    }


    private class UpdateDataTask extends AsyncTask<String, Void, String> {
        String resultstring;
        JSONObject mainJson;
        JSONObject socialappJSON, notify;

        @Override
        protected String doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }
            try {

                // Use BasicNameValuePair to create GET data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                pairs.add(new BasicNameValuePair("method", "accountapi.getUserInfo"));
                pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
                pairs.add(new BasicNameValuePair("login", "1"));

                // url request
                String URL = null;
                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), null, false);
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, null, false);
                }

                // request GET method to server
                resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);

            } catch (Exception ex) {
            }
            return resultstring;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(String result) {
            try {
                mainJson = new JSONObject(result);
                JSONObject outputJson = mainJson.getJSONObject("output");

                socialappJSON = mainJson.getJSONObject("social_app");
                notify = socialappJSON.getJSONObject("notify");

                /* Edit profile */
                if (outputJson.has("country_iso")) {
                    user.setCountry_iso(Html.fromHtml(outputJson.getString("country_iso")).toString());
                }

                if (outputJson.has("city_location")) {
                    String city = Html.fromHtml(outputJson.getString("city_location")).toString();
                    if (city == null || city.equals("null"))
                        user.setCity_location("");
                    else
                        user.setCity_location(city);
                }

                if (outputJson.has("postal_code")) {
                    String code = Html.fromHtml(outputJson.getString("postal_code")).toString();
                    if (code == null || code.equals("null"))
                        user.setPostal_code("");
                    else
                        user.setPostal_code(code);
                }

                if (outputJson.has("birthday_time_stamp")) {
                    user.setBirthday_time_stamp(Html.fromHtml(outputJson.getString("birthday_time_stamp")).toString());
                }

                if (outputJson.has("gender")) {
                    user.setUserGender(Html.fromHtml(outputJson.getString("gender")).toString());
                }

                if (outputJson.has("sexuality")) {
                    user.setSexuality(Html.fromHtml(outputJson.getString("sexuality")).toString());
                }

                if (outputJson.has("religion")) {
                    user.setReligion(Html.fromHtml(outputJson.getString("religion")).toString());
                }

                if (outputJson.has("relation_id")) {
                    user.setRelation_id(Html.fromHtml(outputJson.getString("relation_id")).toString());
                }

                if (outputJson.has("relation_with")) {
                    user.setRelation_with(Html.fromHtml(outputJson.getString("relation_with")).toString());
                }

                if (outputJson.has("relation")) {
                    user.setRelation(Html.fromHtml(outputJson.getString("relation")).toString());
                }

                if (outputJson.has("signature")) {
                    String signature = Html.fromHtml(outputJson.getString("signature")).toString();
                    if (signature == null || signature.equals("null"))
                        user.setSignature("");
                    else
                        user.setSignature(signature);
                }

                if (outputJson.has("use_timeline")) {
                    user.setUse_timeline(Html.fromHtml(outputJson.getString("use_timeline")).toString());
                }

                if (outputJson.has("custom")) {
                    JSONObject custom = outputJson.getJSONObject("custom");
                    if (custom.has("about_me")) {
                        String aboutme = Html.fromHtml(custom.getString("about_me")).toString();
                        if (aboutme == null || aboutme.equals("null"))
                            user.setCustom_aboutme("");
                        else
                            user.setCustom_aboutme(aboutme);
                    }
                    if (custom.has("who_i_d_like_to_meet")) {
                        String whoMeet = Html.fromHtml(custom.getString("who_i_d_like_to_meet")).toString();
                        if (whoMeet == null || whoMeet.equals("null"))
                            user.setCustom_whomeet("");
                        else
                            user.setCustom_whomeet(whoMeet);
                    }
                    if (custom.has("movies")) {
                        String movies = Html.fromHtml(custom.getString("movies")).toString();
                        if (movies == null || movies.equals("null"))
                            user.setCustom_movies("");
                        else
                            user.setCustom_movies(movies);
                    }
                    if (custom.has("interests")) {
                        String interest = Html.fromHtml(custom.getString("interests")).toString();
                        if (interest == null || interest.equals("null"))
                            user.setCustom_interests("");
                        else
                            user.setCustom_interests(interest);
                    }
                    if (custom.has("music")) {
                        String music = Html.fromHtml(custom.getString("music")).toString();
                        if (music == null || music.equals("null"))
                            user.setCustom_music("");
                        else
                            user.setCustom_music(music);
                    }
                    if (custom.has("smoker")) {
                        String smoker = Html.fromHtml(custom.getString("smoker")).toString();
                        if (smoker == null || smoker.equals("null"))
                            user.setCustom_smoker("");
                        else
                            user.setCustom_smoker(smoker);
                    }
                    if (custom.has("drinker")) {
                        String drinker = Html.fromHtml(custom.getString("drinker")).toString();
                        if (drinker == null || drinker.equals("null"))
                            user.setCustom_drinker("");
                        else
                            user.setCustom_drinker(drinker);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.onPostExecute(result);
        }
    }

}