package com.brodev.socialapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;

/**
 * Created by Bebel on 4/1/15.
 */
public class VerifyEmailActivity extends SherlockActivity implements View.OnClickListener {

    private NetworkUntil networkUntil = new NetworkUntil();

    private ScrollView resendEmailLayout;

    private ProgressBar progressBar;
    private TextView yellowText, generalText;
    private Button resendEmailBtn;

    private String URL_POST_USER_SETTING;
    private String userID;
    private String email;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_verify_email);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Back");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (getIntent().hasExtra("email"))
                email = bundle.getString("email");
            if (getIntent().hasExtra("user_id"))
                userID = bundle.getString("user_id");
        }

        initView();

    }

    void initView() {
        resendEmailLayout = (ScrollView) this.findViewById(R.id.resendEmail_layout);

        yellowText = (TextView) this.findViewById(R.id.yellowText);
        generalText = (TextView) this.findViewById(R.id.generalText);
        resendEmailBtn = (Button) this.findViewById(R.id.resendEmailBtn);

        progressBar = (ProgressBar) this.findViewById(R.id.edit_profile_basic_loading);

        String txt = "You need to verify your email address before logging in. We sent a verification code to: " + email;
        yellowText.setText(txt);
        generalText.setText("This app is very concerned about security and therefore it requires you to verify your " +
                "email address. An email has been sent to you, it contains a special link which verifies " +
                "you and allows you to log in freely. This verification is required only once for this " +
                "email address. Please check your email and verify your email address now.");

//        loading = (ProgressBar) this.findViewById(R.id.content_loading);

        findViewById(R.id.resendEmailBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.resendEmailBtn:
                VerifyEmailTask verifyEmailTask = new VerifyEmailTask();
                verifyEmailTask.execute(userID);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class VerifyEmailTask extends AsyncTask<String, Void, String> {
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

            URL_POST_USER_SETTING = "http://mypinkpal.com/mypinkpalapi.php?mode=sendVerifyEmail";

            // Use BasicNameValuePair to store POST data
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("userId", params[0]));

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
