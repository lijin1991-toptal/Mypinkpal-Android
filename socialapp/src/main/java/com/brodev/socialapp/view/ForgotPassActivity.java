package com.brodev.socialapp.view;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.entity.ReCaptcha;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bebel on 3/30/15.
 */
public class ForgotPassActivity extends SherlockActivity implements View.OnClickListener, ReCaptcha.OnShowChallengeListener, ReCaptcha.OnVerifyAnswerListener {

    private NetworkUntil networkUntil = new NetworkUntil();

    private ScrollView forgetLayout;

    private EditText emailEdit;
    private ProgressBar progressBar;

    private String URL_POST_USER_SETTING;

    private static final String PUBLIC_KEY  = "6LeSWNoSAAAAAFHm6C2UNAhHoHkpKNZAj_yI6Koz";
    private static final String PRIVATE_KEY = "your-private-key";

    private String reCHAPChallenge;
    private ReCaptcha   reCaptcha;
    private ProgressBar progress;
    private EditText answer;
    private HashMap<String, String> publicKeyChallengeMap = new HashMap<String, String>();
    private static final String CHALLENGE_URL = "http://www.google.com/recaptcha/api/challenge?k=%s";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Back");

        initView();

    }

    void initView() {

        emailEdit = (EditText) this.findViewById(R.id.emailEdit);
        progressBar = (ProgressBar) this.findViewById(R.id.edit_profile_basic_loading);

        //reCHAP
        reCaptcha = (ReCaptcha)this.findViewById(R.id.recaptcha);
        progress  = (ProgressBar)this.findViewById(R.id.progress);
        answer    = (EditText)this.findViewById(R.id.answer);

        findViewById(R.id.reload).setOnClickListener(this);
        findViewById(R.id.requestPassword).setOnClickListener(this);

        showChallenge();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.reload:
                showChallenge();
                break;
            case R.id.requestPassword:
                ForgotPasswordTask forgotPasswordTask = new ForgotPasswordTask();
                forgotPasswordTask.execute(emailEdit.getText().toString(), reCaptcha.getChallenge(), answer.getText().toString());
                break;
        }
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

    private void showChallenge() {
        // Displays a progress bar while downloading CAPTCHA
        this.progress.setVisibility(View.VISIBLE);
        this.reCaptcha.setVisibility(View.GONE);

        this.reCaptcha.showChallengeAsync(PUBLIC_KEY, this);
    }

    public class ForgotPasswordTask extends AsyncTask<String, Void, String> {
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

            URL_POST_USER_SETTING = "http://mypinkpal.com/index.php?do=/accountapi/requestpassword/";

            // Use BasicNameValuePair to store POST data
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("email", params[0]));
            pairs.add(new BasicNameValuePair("recaptcha_challenge_field", params[1]));
            pairs.add(new BasicNameValuePair("recaptcha_response_field", params[2]));

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
                Toast.makeText(getApplicationContext(), mainJSON.getString("message"), Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);

            super.onPostExecute(result);
        }
    }
}
