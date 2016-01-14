package com.brodev.socialapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.brodev.socialapp.entity.ReCaptcha;
import com.brodev.socialapp.entity.ReCaptchaException;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bebel on 3/27/15.
 */
public class SignUpActivity extends SherlockActivity implements View.OnClickListener, ReCaptcha.OnShowChallengeListener, ReCaptcha.OnVerifyAnswerListener {

    private User user;
    private NetworkUntil networkUntil = new NetworkUntil();
    private PhraseManager phraseManager;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private ScrollView signupBasicLayout;

    private ProgressBar loading, progressBar;
    private EditText displayNameEdit, usernameEdit, emailEdit, passwordEdit, cityEdit;
    private CheckBox agreeCheckBox;
    private LinearLayout locationLayout, monthLayout, dayLayout, yearLayout, genderLayout, sexualityLayout, religionLayout;
    private ComboBox locationChoose = null, monthChoose = null, dayChoose = null, yearChoose = null, genderChoose = null, sexualityChoose = null, religionChoose = null;
    ArrayList<ComboBoxItem> listLocation = new ArrayList<ComboBoxItem>(),
            listMonth = new ArrayList<ComboBoxItem>(),
            listDay = new ArrayList<ComboBoxItem>(),
            listYear = new ArrayList<ComboBoxItem>(),
            listGender = new ArrayList<ComboBoxItem>(),
            listSexuality = new ArrayList<ComboBoxItem>(),
            listReligion = new ArrayList<ComboBoxItem>();

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private String URL_POST_USER_SETTING;

    private static final String PUBLIC_KEY  = "6LeSWNoSAAAAAFHm6C2UNAhHoHkpKNZAj_yI6Koz";
    private static final String PRIVATE_KEY = "your-private-key";

    private String reCHAPChallenge;

    private ReCaptcha reCaptcha;
    private ProgressBar progress;
    private EditText    answer;
    private HashMap<String, String> publicKeyChallengeMap = new HashMap<String, String>();
    private static final String CHALLENGE_URL = "http://www.google.com/recaptcha/api/challenge?k=%s";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Back");

        phraseManager = new PhraseManager(getApplicationContext());
        user = (User) getApplication();
        colorView = new ColorView(getApplicationContext());

        initView();

    }

    void initView() {
        signupBasicLayout = (ScrollView) this.findViewById(R.id.signup_layout);

        locationLayout = (LinearLayout) this.findViewById(R.id.locationComboBox);
        monthLayout = (LinearLayout) this.findViewById(R.id.monthComboBox);
        dayLayout = (LinearLayout) this.findViewById(R.id.dayComboBox);
        yearLayout = (LinearLayout) this.findViewById(R.id.yearComboBox);
        genderLayout = (LinearLayout) this.findViewById(R.id.genderComboBox);
        sexualityLayout = (LinearLayout) this.findViewById(R.id.sexualityComboBox);
        religionLayout = (LinearLayout) this.findViewById(R.id.religionComboBox);

        displayNameEdit = (EditText) this.findViewById(R.id.displayNameEdit);
        usernameEdit = (EditText) this.findViewById(R.id.usernameEdit);
        emailEdit = (EditText) this.findViewById(R.id.emailEdit);
        passwordEdit = (EditText) this.findViewById(R.id.passwordEdit);
        cityEdit = (EditText) this.findViewById(R.id.cityEdit);

        agreeCheckBox = (CheckBox) this.findViewById(R.id.agreeCheckBox);

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
                        loadSignUp();
                    }
                }, 1000);
            }
        });

        //reCHAP
        reCaptcha = (ReCaptcha)this.findViewById(R.id.recaptcha);
        progress  = (ProgressBar)this.findViewById(R.id.progress);
        answer    = (EditText)this.findViewById(R.id.answer);

        findViewById(R.id.reload).setOnClickListener(this);
        findViewById(R.id.joinButton).setOnClickListener(this);

        showChallenge();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.reload:
                showChallenge();
                break;
            case R.id.joinButton:
                String val = checkfieldValue();
                if (!val.equals("")) {
                    Toast.makeText(this, val, Toast.LENGTH_SHORT).show();
                    break;
                }

                connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    JoinUpdateTask joinUpdateTask = new JoinUpdateTask();

                    String displayName = displayNameEdit.getText().toString().trim();
                    String username = usernameEdit.getText().toString().trim();
                    String email = emailEdit.getText().toString().trim();
                    String password = passwordEdit.getText().toString().trim();
                    String month = monthChoose.getValue();
                    String day = dayChoose.getValue();
                    String year = yearChoose.getValue();
                    String gender = genderChoose.getValue();
                    String location = locationChoose.getValue();
                    String city = cityEdit.getText().toString().trim();
                    String sexuality = sexualityChoose.getValue();
                    String religion = religionChoose.getValue();
                    String howmany = "2";
                    String challenge = reCaptcha.getChallenge();
                    String recaptchaStr = answer.getText().toString().trim();
                    String agree = "1";

                    joinUpdateTask.execute(displayName, username, email, password, month, day, year, gender,
                            location, city, religion, sexuality, howmany, agree, challenge, recaptchaStr);
                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    @Override
    public void onChallengeShown(final boolean shown) {
        this.progress.setVisibility(View.GONE);

        if (shown) {
            // If a CAPTCHA is shown successfully, displays it for the user to enter the words
            this.reCaptcha.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.show_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAnswerVerified(final boolean success) {
        if (success) {
            Toast.makeText(this, R.string.verification_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.verification_failed, Toast.LENGTH_SHORT).show();
        }

        // (Optional) Shows the next CAPTCHA
        this.showChallenge();
    }

    private String checkfieldValue() {
        if (displayNameEdit.getText().toString().equals(""))
            return "Provide your full name.";
        if (usernameEdit.getText().toString().equals(""))
            return "Provide a valid user name.";
        if (emailEdit.getText().toString().equals(""))
            return "Provide a valid email address.";
        if (passwordEdit.getText().toString().equals(""))
            return "Provide a valid password.";
        if (monthChoose.getValue().equals("0"))
            return "Select month of birth.";
        if (dayChoose.getValue().equals("default"))
            return "Select day of birth.";
        if (yearChoose.getValue().equals("default"))
            return "Select year of birth.";
        if (genderChoose.getValue().equals("default"))
            return "Select your gender.";
        if (locationChoose.getValue().equals("default"))
            return "Select current location.";
        if (sexualityChoose.getValue().equals("default"))
            return "Select your sexuality.";
        if (answer.getText().toString().equals(""))
            return "Provide a valid Captcha";
        if (agreeCheckBox.isChecked() == false)
            return "Please read and agree to the Terms of Use and Privacy Policy.";

        return "";
    }

    private void showChallenge() {
        // Displays a progress bar while downloading CAPTCHA
        progress.setVisibility(View.VISIBLE);
        reCaptcha.setVisibility(View.GONE);

        reCaptcha.showChallengeAsync(PUBLIC_KEY, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSignUp();
    }

    private void loadSignUp() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                signupBasicLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //fetch data
                new SignUpTask().execute();
            } else {
                // display error
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                signupBasicLayout.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            progressBar.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            signupBasicLayout.setVisibility(View.GONE);
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
            pairs.add(new BasicNameValuePair("mode", "getProfileOptions"));

            // url request
            String URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);

            // request GET method to server
            resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return resultstring;
    }

    public class SignUpTask extends AsyncTask<Integer, Void, String> {

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
                    viewMonth(result);
                    viewDay(result);
                    viewYear(result);
                    viewGender(result);
                    viewSexuality(result);
                    viewReligion(result);
                    loading.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
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
            locationChoose.addComboToView(this, listLocation, "0", locationLayout, null, null);
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
            Object objLocation = mainJSON.get("location");
            ComboBoxItem defaultItem = new ComboBoxItem();
            defaultItem.setName("*Location");
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
            monthChoose.addComboToView(this, listMonth, "0", monthLayout, null, null);
        }
    }

    /**
     * get month from json
     *
     * @param resString
     */
    public void getMonth(String resString) {

        ArrayList<String> monthArr = new ArrayList<String>();
        monthArr.add("*Month");
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
            dayChoose.addComboToView(this, listDay, "0", dayLayout, "/", null);
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
        defaultItem.setName("*Day");
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
            yearChoose.addComboToView(this, listYear, "0", yearLayout, "/", null);
        }
    }

    /**
     * get year from json
     *
     * @param resString
     */
    public void getYear(String resString) {

        ComboBoxItem defaultItem = new ComboBoxItem();
        defaultItem.setName("*Year");
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
            genderChoose.addComboToView(this, listGender, "0", genderLayout, null, null);
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
            Object objGender = mainJSON.get("gender");

            ComboBoxItem defaultItem = new ComboBoxItem();
            defaultItem.setName("*I am");
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
            sexualityChoose.addComboToView(this, listSexuality, "0", sexualityLayout, null, null);
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
            Object objSexuality = mainJSON.get("sexuality");

            ComboBoxItem defaultItem = new ComboBoxItem();
            defaultItem.setName("*Sexuality");
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
            religionChoose.addComboToView(this, listReligion, "0", religionLayout, null, null);
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
            Object objReligion = mainJSON.get("religion");

            ComboBoxItem defaultItem = new ComboBoxItem();
            defaultItem.setName("Religion");
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class JoinUpdateTask extends AsyncTask<String, Void, String> {
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

            URL_POST_USER_SETTING = "http://mypinkpal.com/index.php?do=/accountapi/register/";

            // Use BasicNameValuePair to store POST data
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("val[full_name]", params[0]));
            pairs.add(new BasicNameValuePair("val[user_name]", params[1]));
            pairs.add(new BasicNameValuePair("val[email]", params[2]));
            pairs.add(new BasicNameValuePair("val[password]", params[3]));
            pairs.add(new BasicNameValuePair("val[month]", params[4]));
            pairs.add(new BasicNameValuePair("val[day]", params[5]));
            pairs.add(new BasicNameValuePair("val[year]", params[6]));
            pairs.add(new BasicNameValuePair("val[gender]", params[7]));
            pairs.add(new BasicNameValuePair("val[country_iso]", params[8]));
            pairs.add(new BasicNameValuePair("val[city_location]", params[9]));
            pairs.add(new BasicNameValuePair("val[religion]", params[10]));
            pairs.add(new BasicNameValuePair("val[sexuality]", params[11]));
            pairs.add(new BasicNameValuePair("val[spam][1]", params[12]));
            pairs.add(new BasicNameValuePair("val[agree]", params[13]));
            pairs.add(new BasicNameValuePair("recaptcha_challenge_field", params[14]));
            pairs.add(new BasicNameValuePair("recaptcha_response_field", params[15]));

            System.out.println(pairs);
            String result = networkUntil.makeHttpRequest(URL_POST_USER_SETTING, "POST", pairs);

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // create new message adapter
                JSONObject mainJSON = new JSONObject(result);
                String strStatus = mainJSON.getString("status");
                if (strStatus.equals("error")) {
                    Toast.makeText(getApplicationContext(), mainJSON.getString("message"), Toast.LENGTH_LONG).show();
                } else if (strStatus.equals("success")) {
                    String user_id = mainJSON.getString("user_id");
                    Intent intent = new Intent(getApplicationContext(), VerifyEmailActivity.class);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("email", emailEdit.getText().toString());
                    startActivity(intent);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);

            super.onPostExecute(result);
        }
    }
}
