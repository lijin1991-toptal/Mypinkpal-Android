package com.brodev.socialapp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.manager.ComboBox;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.ComboBoxItem;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.UserProfile;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bebel on 3/11/15.
 */
public class EditProfileBasic extends SherlockActivity {

    private User user;
    private NetworkUntil networkUntil = new NetworkUntil();
    private PhraseManager phraseManager;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private ScrollView editProfileBasicLayout;

    private ProgressBar loading, progressBar;
    private EditText cityEdit, zipEdit, signatureEdit;
    private LinearLayout locationLayout, monthLayout, dayLayout, yearLayout, genderLayout, sexualityLayout, religionLayout, relationshipLayout, timelineLayout;
    private ComboBox locationChoose = null, monthChoose = null, dayChoose = null, yearChoose = null, genderChoose = null, sexualityChoose = null, religionChoose = null, relationshipChoose = null, timelineChoose = null;
    ArrayList<ComboBoxItem> listLocation = new ArrayList<ComboBoxItem>(),
            listMonth = new ArrayList<ComboBoxItem>(),
            listDay = new ArrayList<ComboBoxItem>(),
            listYear = new ArrayList<ComboBoxItem>(),
            listGender = new ArrayList<ComboBoxItem>(),
            listSexuality = new ArrayList<ComboBoxItem>(),
            listReligion = new ArrayList<ComboBoxItem>(),
            listRelationship = new ArrayList<ComboBoxItem>(),
            listTimeline = new ArrayList<ComboBoxItem>();

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private String URL_POST_USER_SETTING;
    private String gPrevious_with = "";
    private String gPrevious_type = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_profile_basic);
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
                    EditProfileUpdateTask editProfileUpdateTask = new EditProfileUpdateTask();

                    String country_iso = locationChoose.getValue();
                    String city = cityEdit.getText().toString().trim();
                    String postal = zipEdit.getText().toString().trim();
                    String month = monthChoose.getValue();
                    String day = dayChoose.getValue();
                    String year = yearChoose.getValue();
                    String gender = genderChoose.getValue();
                    String sexuality = sexualityChoose.getValue();
                    String religion = religionChoose.getValue();
                    String relation = relationshipChoose.getValue();
                    String relation_with = "";
                    String previous_relation_with = gPrevious_with;
                    String previous_relation_type = gPrevious_type;
                    String signature = signatureEdit.getText().toString().trim();
                    String timeline = timelineChoose.getValue();

                    editProfileUpdateTask.execute(country_iso, city, postal, month, day, year, gender, sexuality, religion, relation, relation_with,
                            previous_relation_with, previous_relation_type, signature, timeline);

                    UpdateDataTask updateDataTask = new UpdateDataTask();
                    updateDataTask.execute();
                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void initView() {
        getSupportActionBar().setTitle("Basic Information");
        editProfileBasicLayout = (ScrollView) this.findViewById(R.id.edit_profile_layout);

        locationLayout = (LinearLayout) this.findViewById(R.id.locationComboBox);
        monthLayout = (LinearLayout) this.findViewById(R.id.monthComboBox);
        dayLayout = (LinearLayout) this.findViewById(R.id.dayComboBox);
        yearLayout = (LinearLayout) this.findViewById(R.id.yearComboBox);
        genderLayout = (LinearLayout) this.findViewById(R.id.genderComboBox);
        sexualityLayout = (LinearLayout) this.findViewById(R.id.sexualityComboBox);
        religionLayout = (LinearLayout) this.findViewById(R.id.religionComboBox);
        relationshipLayout = (LinearLayout) this.findViewById(R.id.relationshipComboBox);
        timelineLayout = (LinearLayout) this.findViewById(R.id.timelineComboBox);

        cityEdit = (EditText) this.findViewById(R.id.profileCityEdit);
        zipEdit = (EditText) this.findViewById(R.id.profileZipEdit);
        signatureEdit = (EditText) this.findViewById(R.id.profileSignatureEdit);

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
                        loadEditProfileBasic();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        EditProfileBasicTask mt = new EditProfileBasicTask();
        mt.execute();
        loadEditProfileBasic();

    }

    private void loadEditProfileBasic() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                editProfileBasicLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //fetch data
                new EditProfileBasicTask().execute();
            } else {
                // display error
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                editProfileBasicLayout.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            progressBar.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            editProfileBasicLayout.setVisibility(View.GONE);
        }
    }


    public class EditProfileUpdateTask extends AsyncTask<String, Void, String> {
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
            pairs.add(new BasicNameValuePair("val[country_iso]", params[0]));
            pairs.add(new BasicNameValuePair("val[city_location]", params[1]));
            pairs.add(new BasicNameValuePair("val[postal_code]", params[2]));
            pairs.add(new BasicNameValuePair("val[month]", params[3]));
            pairs.add(new BasicNameValuePair("val[day]", params[4]));
            pairs.add(new BasicNameValuePair("val[year]", params[5]));
            pairs.add(new BasicNameValuePair("val[gender]", params[6]));
            pairs.add(new BasicNameValuePair("val[sexuality]", params[7]));
            pairs.add(new BasicNameValuePair("val[religion]", params[8]));
            pairs.add(new BasicNameValuePair("val[relation]", params[9]));
            pairs.add(new BasicNameValuePair("val[relation_with]", params[10]));
            pairs.add(new BasicNameValuePair("val[previous_relation_with]", params[11]));
            pairs.add(new BasicNameValuePair("val[previous_relation_type]", params[12]));
            pairs.add(new BasicNameValuePair("val[signature]", params[13]));
            pairs.add(new BasicNameValuePair("val[use_timeline]", params[14]));

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

    public class EditProfileBasicTask extends AsyncTask<Integer, Void, String> {

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
                    viewLocation(result);
                    viewCity_zip_signature(result);
                    viewMonth(result);
                    viewDay(result);
                    viewYear(result);
                    viewGender(result);
                    viewSexuality(result);
                    viewReligion(result);
                    viewRelationship(result);
                    viewTimeline(result);
                    loading.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    private void viewCity_zip_signature(String result) {
        cityEdit.setText(user.getCity_location());
        zipEdit.setText(user.getPostal_code());
        signatureEdit.setText(user.getSignature());
    }


    /**
     * show location combo box
     *
     * @param result
     */
    private void viewLocation(String result) {
        if (locationChoose == null) {
            getLocation(result);
            locationChoose = new ComboBox(this);
            locationChoose.addComboToView(this, listLocation, user.getCountry_iso(), locationLayout, "Location:", null);
        }
    }

    /**
     * get location from json
     *
     * @param resString
     */
    public void getLocation(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                Object objLocation = outputJSON.get("location");
                ComboBoxItem defaultItem = new ComboBoxItem();
                defaultItem.setName("Select:");
                defaultItem.setValue("default");
                listLocation.add(defaultItem);

                if (objLocation instanceof JSONArray) {
                    JSONObject objLocationValue = null;
                    JSONArray arObjLocation = (JSONArray) objLocation;
                    for (int i = 0; i < arObjLocation.length(); i++) {
                        objLocationValue = arObjLocation.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convertLocation(objLocationValue);
                        listLocation.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * show month combo box
     *
     * @param result
     */
    private void viewMonth(String result) {
        if (monthChoose == null) {
            getMonth(result);
            monthChoose = new ComboBox(this);
            String birth = user.getBirthday_time_stamp();
            String subBirth = birth.substring(0,2);
            String month = "";
            int time = Integer.parseInt(subBirth);
            for (int i = 0; i < listMonth.size(); i++) {
                ComboBoxItem item;
                item = listMonth.get(i);
                if (item.getValue().equals(String.valueOf(time))) {
                    month = item.getValue();
                    break;
                }
            }
            monthChoose.addComboToView(this, listMonth, month, monthLayout, "", null);
        }
    }

    /**
     * get month from json
     *
     * @param resString
     */
    public void getMonth(String resString) {

        ArrayList<String> monthArr = new ArrayList<String>();
        monthArr.add("Month:");
        monthArr.add("January");
        monthArr.add("February");
        monthArr.add("March");
        monthArr.add("April");
        monthArr.add("May");
        monthArr.add("June");
        monthArr.add("July");
        monthArr.add("August");
        monthArr.add("September");
        monthArr.add("October");
        monthArr.add("November");
        monthArr.add("December");

        int i;
        for (i = 0; i <= 12; i++) {
            ComboBoxItem item = new ComboBoxItem();
            item.setName(monthArr.get(i));
            item.setValue(String.valueOf(i));
            listMonth.add(item);
        }
    }


    /**
     * show day combo box
     *
     * @param result
     */
    private void viewDay(String result) {
        if (dayChoose == null) {
            getDay(result);
            dayChoose = new ComboBox(this);
            String birth = user.getBirthday_time_stamp();
            String subBirth = birth.substring(2, 4);
            int time = Integer.parseInt(subBirth);
            dayChoose.addComboToView(this, listDay, String.valueOf(time), dayLayout, "/", null);
        }
    }

    /**
     * get day from json
     *
     * @param resString
     */
    public void getDay(String resString) {

        ComboBoxItem defaultItem = new ComboBoxItem();
        defaultItem.setValue("default");
        defaultItem.setName("Day:");
        listDay.add(defaultItem);

        int i;
        for (i = 1; i <= 31; i++) {
            ComboBoxItem item = new ComboBoxItem();
            item.setName(String.valueOf(i));
            item.setValue(String.valueOf(i));
            listDay.add(item);
        }
    }

    /**
     * show year combo box
     *
     * @param result
     */
    private void viewYear(String result) {
        if (yearChoose == null) {
            getYear(result);
            yearChoose = new ComboBox(this);
            String birth = user.getBirthday_time_stamp();
            String subBirth = birth.substring(4,8);
            yearChoose.addComboToView(this, listYear, subBirth, yearLayout, "/", null);
        }
    }

    /**
     * get year from json
     *
     * @param resString
     */
    public void getYear(String resString) {

        ComboBoxItem defaultItem = new ComboBoxItem();
        defaultItem.setName("Year:");
        defaultItem.setValue("default");
        listYear.add(defaultItem);

        int i;
        for (i = 1995; i >= 1900; i--) {
            ComboBoxItem item = new ComboBoxItem();
            item.setName(String.valueOf(i));
            item.setValue(String.valueOf(i));
            listYear.add(item);
        }
    }


    /**
     * show gender combo box
     *
     * @param result
     */
    private void viewGender(String result) {
        if (genderChoose == null) {
            getGender(result);
            genderChoose = new ComboBox(this);
            String gender = user.getUserGender();
            genderChoose.addComboToView(this, listGender, gender, genderLayout, "Gender:", null);
        }
    }

    /**
     * get gender from json
     *
     * @param resString
     */
    public void getGender(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                Object objGender = outputJSON.get("gender");

                ComboBoxItem defaultItem = new ComboBoxItem();
                defaultItem.setName("Select:");
                defaultItem.setValue("default");
                listGender.add(defaultItem);

                if (objGender instanceof JSONArray) {
                    JSONObject objGenderValue = null;
                    JSONArray arObjGender = (JSONArray) objGender;
                    for (int i = 0; i < arObjGender.length(); i++) {
                        objGenderValue = arObjGender.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objGenderValue);
                        listGender.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * show sexuality combo box
     *
     * @param result
     */
    private void viewSexuality(String result) {
        if (sexualityChoose == null) {
            getSexuality(result);
            sexualityChoose = new ComboBox(this);
            String sex = user.getSexuality();
            sexualityChoose.addComboToView(this, listSexuality, sex, sexualityLayout, "Sexuality:", null);
        }
    }

    /**
     * get sexuality from json
     *
     * @param resString
     */
    public void getSexuality(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                Object objSexuality = outputJSON.get("sexuality");

                ComboBoxItem defaultItem = new ComboBoxItem();
                defaultItem.setName("Select:");
                defaultItem.setValue("default");
                listSexuality.add(defaultItem);

                if (objSexuality instanceof JSONArray) {
                    JSONObject objSexualityValue = null;
                    JSONArray arObjSexuality = (JSONArray) objSexuality;
                    for (int i = 0; i < arObjSexuality.length(); i++) {
                        objSexualityValue = arObjSexuality.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objSexualityValue);
                        listSexuality.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * show religion combo box
     *
     * @param result
     */
    private void viewReligion(String result) {
        if (religionChoose == null) {
            getReligion(result);
            religionChoose = new ComboBox(this);
            String religion = user.getReligion();
            religionChoose.addComboToView(this, listReligion, religion, religionLayout, "Religion:", null);
        }
    }

    /**
     * get religion from json
     *
     * @param resString
     */
    public void getReligion(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                Object objReligion = outputJSON.get("religion");

                ComboBoxItem defaultItem = new ComboBoxItem();
                defaultItem.setName("Select:");
                defaultItem.setValue("default");
                listReligion.add(defaultItem);

                if (objReligion instanceof JSONArray) {
                    JSONObject objReligionValue = null;
                    JSONArray arObjReligion = (JSONArray) objReligion;
                    for (int i = 0; i < arObjReligion.length(); i++) {
                        objReligionValue = arObjReligion.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objReligionValue);
                        listReligion.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * show relationship combo box
     *
     * @param result
     */
    private void viewRelationship(String result) {
        if (relationshipChoose == null) {
            getRelationship(result);
            relationshipChoose = new ComboBox(this);
            String relation = user.getRelation_id();
            gPrevious_type = relation;
            relationshipChoose.addComboToView(this, listRelationship, relation, relationshipLayout, "Relationship Status:", null);
        }
    }

    /**
     * get relationship from json
     *
     * @param resString
     */
    public void getRelationship(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                Object objRelation = outputJSON.get("relation");

                if (objRelation instanceof JSONArray) {
                    JSONObject objRelationValue = null;
                    JSONArray arObjRelation = (JSONArray) objRelation;
                    for (int i = 0; i < arObjRelation.length(); i++) {
                        objRelationValue = arObjRelation.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objRelationValue);
                        listRelationship.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * show timeline combo box
     *
     * @param result
     */
    private void viewTimeline(String result) {
        if (timelineChoose == null) {
            getTimeline(result);
            timelineChoose = new ComboBox(this);
            String timeline = user.getUse_timeline();
            timelineChoose.addComboToView(this, listTimeline, timeline, timelineLayout, "Timeline:", null);
        }
    }

    /**
     * get timeline from json
     *
     * @param resString
     */
    public void getTimeline(String resString) {

        try {
            JSONObject mainJSON = new JSONObject(resString);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {

                JSONObject outputJSON = mainJSON.getJSONObject("output");
                Object objTimeline = outputJSON.get("timeline");

                if (objTimeline instanceof JSONArray) {
                    JSONObject objCurrencyValue = null;
                    JSONArray arObjTimeline = (JSONArray) objTimeline;
                    for (int i = 0; i < arObjTimeline.length(); i++) {
                        objCurrencyValue = arObjTimeline.getJSONObject(i);
                        ComboBoxItem item = new ComboBoxItem();
                        item = item.convert(objCurrencyValue);
                        listTimeline.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
