package com.brodev.socialapp.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.SessionManager;
import com.brodev.socialapp.android.asyncTask.RegisterChatServer;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.facebook.FacebookConnectActivity;
import com.brodev.socialapp.facebook.LoginListener;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.facebook.FacebookRequestError;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.analytics.tracking.android.EasyTracker;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginAndJoinDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;
import com.quickblox.users.query.QueryBaseCreateUser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codechimp.apprater.AppRater;
import org.jivesoftware.smack.SmackException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends FacebookConnectActivity {

    private static final String DEBUG_TAG = "SOCIAL_APP";
    private static final String ERROR = "error";

    private EditText txtEmail, txtPassword;
    private Button btnLogin, btnFacebookLogin, signUpBtn, forgotBtn;
    private ProgressDialog progress;
    private RelativeLayout loginView;
    private PhraseManager phraseManager;

    //session manager
    private SessionManager session;
    private Context context = this;
    private String email = null;
    private String password = null;

    private Editor editor;
    private SharedPreferences pref;
    private String imageUrl = "";
    //init Network Until
    private NetworkUntil networkUntil = new NetworkUntil();

    private User user;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    /* QuickBlox ChatService */
    // bronislaw
    private QBChatService chatService;


    /**
     * Create progress
     */
    public void createProgress(ProgressDialog progress) {
        //create progress dialog
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.setMessage(phraseManager.getPhrase(getApplicationContext(), "accountapi.connecting"));
    }

    /**
     * Change color for app
     *
     * @param colorCode
     */
    private void changeColorApp(String colorCode) {
        //set color
        if ("Brown".equalsIgnoreCase(colorCode)) {
            btnLogin.setBackgroundResource(R.drawable.brown_login_button);
            signUpBtn.setBackgroundResource(R.drawable.brown_login_button);
        } else if ("Pink".equalsIgnoreCase(colorCode)) {
            btnLogin.setBackgroundResource(R.drawable.pink_login_button);
            signUpBtn.setBackgroundResource(R.drawable.pink_login_button);
        } else if ("Green".equalsIgnoreCase(colorCode)) {
            btnLogin.setBackgroundResource(R.drawable.green_login_button);
            signUpBtn.setBackgroundResource(R.drawable.green_login_button);
        } else if ("Violet".equalsIgnoreCase(colorCode)) {
            btnLogin.setBackgroundResource(R.drawable.violet_login_button);
            signUpBtn.setBackgroundResource(R.drawable.violet_login_button);
        } else if ("Red".equalsIgnoreCase(colorCode)) {
            btnLogin.setBackgroundResource(R.drawable.red_login_button);
            signUpBtn.setBackgroundResource(R.drawable.red_login_button);
        } else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
            btnLogin.setBackgroundResource(R.drawable.dark_violet_login_button);
            signUpBtn.setBackgroundResource(R.drawable.dark_violet_login_button);
        } else {
            btnLogin.setBackgroundResource(R.drawable.login_button);
            signUpBtn.setBackgroundResource(R.drawable.login_button);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getDisplayFb() == true && user.getEmail() != null) {
            checkStatusFacebook();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        user = (User) getApplication().getApplicationContext();

        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());
        phraseManager = new PhraseManager(getApplicationContext());
        user.setEmail(null);

        signUpBtn = (Button) findViewById(R.id.btnSignUp);
        signUpBtn.setText(phraseManager.getPhrase(getApplicationContext(), "user.sign_up"));
        forgotBtn = (Button) findViewById(R.id.forgot_password_button);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtEmail.setHint(phraseManager.getPhrase(getApplicationContext(), "user.email"));
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtPassword.setHint(phraseManager.getPhrase(getApplicationContext(), "user.password"));
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setText(phraseManager.getPhrase(getApplicationContext(), "user.login_button"));
        btnFacebookLogin = (Button) findViewById(R.id.login_facebook_button);

        //set color for app
        changeColorApp(user.getColor());

        //get login image
        Bundle bundle = getIntent().getExtras();
        pref = getApplicationContext().getSharedPreferences("login_background", 0);
        editor = pref.edit();
        imageUrl = pref.getString("path", "");
        Boolean enableRate = true;
        if (bundle != null) {
            if (this.getIntent().hasExtra("imageUrl")) {
                imageUrl = bundle.getString("imageUrl");
                editor.putString("path", imageUrl);
                editor.commit();
            }
            if (this.getIntent().hasExtra("enalbe_rate")) {
                enableRate = bundle.getBoolean("enalbe_rate");
            }
        }

        if (!imageUrl.equals("")) {
            ImageView loginBackground = (ImageView) findViewById(R.id.login_background);
            networkUntil.drawImageUrl(loginBackground, imageUrl, R.drawable.background);
            loginBackground.setVisibility(View.VISIBLE);
        }

        //check if have login facebook
        Log.i("Display facebook id", String.valueOf(getDisplayFb()));
        if (getDisplayFb() == true) {
            btnFacebookLogin.setVisibility(View.VISIBLE);
            checkStatusFacebook();
        } else {
            btnFacebookLogin.setVisibility(View.GONE);
        }

        loginView = (RelativeLayout) findViewById(R.id.login_view);

        if (user.getEmail() != null) {
            txtEmail.setText(user.getEmail());
            txtPassword.requestFocus();
        }

        //create progress dialog
        progress = new ProgressDialog(this);
        createProgress(progress);

        //check if logged
        if (session.checkLogin()) {
            //get user data from session
            HashMap<String, String> _user = session.getUserDetails();
            // email
            String _email = _user.get(SessionManager.KEY_EMAIL);

            Intent myIntent = new Intent(LoginActivity.this, DashboardActivity.class);
            //set email to user
            myIntent.putExtra("email", _email);
            startActivity(myIntent);
            finish();
        } else {
            if (enableRate) {
                AppRater appRater = new AppRater();
                appRater.setTextString(phraseManager.getPhrase(getApplicationContext(), "accountapi.love_this_app"), phraseManager.getPhrase(getApplicationContext(), "accountapi.rate_content"),
                        phraseManager.getPhrase(getApplicationContext(), "accountapi.rate"), phraseManager.getPhrase(getApplicationContext(), "accountapi.remind_me_later"),
                        phraseManager.getPhrase(getApplicationContext(), "accountapi.no_thanks"));
                appRater.showRateDialog(this);
                appRater.app_launched(this);
            }
        }

        //action log in with facebook
        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                connect();
            }
        });

        // action sign up
        signUpBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                /*
                // TODO Auto-generated method stub
                String url = Config.CORE_URL + Config.CORE_URL_REGISTER + "&hideHeader=1";
                Log.d("psyh", url);
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }

                Intent browserIntent = new Intent(LoginActivity.this, WebviewActivity.class);
                browserIntent.putExtra("html", url);
                browserIntent.putExtra("header", true);

                startActivity(browserIntent);
                */
                Intent signupIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signupIntent);
            }
        });

        // action login
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //get email password
                email = txtEmail.getText().toString().trim();
                password = txtPassword.getText().toString().trim();

                // check email or password null
                if (email.length() == 0 || password.length() == 0) {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.please_fill_email_password"), Toast.LENGTH_LONG).show();
                } else {
                    connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    networkInfo = connMgr.getActiveNetworkInfo();

                    if (networkInfo != null && networkInfo.isConnected()) {
                        HttpUrlAsynctask mtask = new HttpUrlAsynctask();
                        mtask.execute(email, password);
                    } else {
                        Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassActivity.class);
                startActivity(intent);
            }
        });


    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.login, menu);
//        return true;
//    }

    /**
     * Async Task login action
     */
    public class HttpUrlAsynctask extends AsyncTask<String, Void, String> {

        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            //if result is null
            if (result == null) {

                // next activity
                Intent myIntent;
                if (!session.checkLogin() && !isEmailExist(email)) {
                    myIntent = new Intent(LoginActivity.this, SkipAddFriend.class);
                } else {
                    myIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                }

                myIntent.putExtra("email", email);

                //set email to user
                user.setEmail(email);

                //create login session
                session.createLoginSession(password, email, user.getUserName(), user.getQuickbloxpswd(), user.getQuickbloxid());

                //request register chat server
                if (user.getChatKey() != null
                        && user.getChatServerUrl() != null
                        && user.getChatServerUrl() != null) {
                    connectChatServer(email);
                }

                LoginActivity.this.startActivity(myIntent);
                finish();
            } else {

                loginView.setVisibility(View.VISIBLE);

                showMessageBox(Html.fromHtml(result.toString()).toString());
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... param) {
            try {
                //get check key url
                String url_checkKey = Config.CORE_URL + Config.URL_CHECKKEY;

                // Use BasicNameValuePair to store POST data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("email", param[0]));
                pairs.add(new BasicNameValuePair("password", param[1]));

                String resultstring = networkUntil.makeHttpRequest(url_checkKey, "POST", pairs);

                Log.d("psyh", "URL_REQUEST: " + url_checkKey);
                Log.d("psyh", "RESULT: " + resultstring);

                /* bronislaw
                //set chat server info
                user.setChatKey(null);
                user.setChatSecretKey(null);
                user.setChatServerUrl(null);
                */
                user.setKey_admob(null);
                user.setCheckin(null);

                //convert to json
                JSONObject json = new JSONObject(resultstring);
                if (!json.isNull("token") && !json.isNull("user_id") && json.has("token") && json.has("user_id")) {
                    //set global value
                    Log.e("psyh:", "(WITH DATA): ");
                    user.setTokenkey(json.getString("token").toString());
                    user.setUserId(json.getString("user_id").toString());

                    if (json.has("key_admob") && !json.isNull("key_admob")) {
                        user.setKey_admob(json.getString("key_admob"));
                    }
                    if (json.has("quickbloxid") && !json.isNull("quickbloxid")) {
                        user.setQuickbloxid(json.getString("quickbloxid"));
                    }
                    if (json.has("quickbloxpswd") && !json.isNull("quickbloxpswd")) {
                        user.setQuickbloxpswd(json.getString("quickbloxpswd"));
                    }
                    if (json.has("user_name") && !json.isNull("user_name")) {
                        user.setUserName(json.getString("user_name"));
                    }

                    if (json.has("enable_check_in")) {
                        user.setCheckin(String.valueOf(json.getBoolean("enable_check_in")));
                    }

                    if (json.has("google_key")) {
                        user.setGoogleKey(json.getString("google_key"));
                    }

                    if (json.has("phrases")) {
                        phraseManager.saveJSONObject(getApplicationContext(), json.getJSONObject("phrases"));
                    }

                    Log.i(DEBUG_TAG, json.getString("token"));


                    /* bronislaw
                    //set chat server info
                    if (json.has("chat_server_key") && !json.isNull("chat_server_key"))
                        user.setChatKey(json.getString("chat_server_key").toString());

                    if (json.has("chat_server_secret") && !json.isNull("chat_server_secret"))
                        user.setChatSecretKey(json.getString("chat_server_secret").toString());

                    if (json.has("chat_server_url") && !json.isNull("chat_server_url"))
                        user.setChatServerUrl(json.getString("chat_server_url").toString());
                        */

                    //cache info
                    cacheInfo();
                } else if (!json.isNull("status") && !json.isNull("message")) {
                    Log.e("psyh:", "(WITHOUT DATA): ");
                    user.setStatus(json.getString("status").toString());
                    user.setMessage(json.getString("message").toString());
                    Log.i(DEBUG_TAG, json.getString("message"));

                    //check if status is error
                    if (ERROR.equals(user.getStatus())) {
                        return user.getMessage();
                    }
                }

            } catch (Exception ex) {
                loginView.setVisibility(View.VISIBLE);

                String error = "No Internet Connection";
                if (progress.isShowing()) {
                    progress.dismiss();
                }
                return error;
            }

            return null;
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void cacheInfo() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("token_key", user.getTokenkey());
        editor.putString("user_id", user.getUserId());
        editor.putString("core_url", Config.CORE_URL);

        /* bronislaw
        editor.putString("chat_server_key", user.getChatKey());
        editor.putString("chat_server_secret", user.getChatSecretKey());
        editor.putString("chat_server_url", user.getChatServerUrl());
        */
        editor.putString("color", user.getColor());
        editor.putString("key_admob", user.getKey_admob());

        editor.putString("email", user.getEmail());
        editor.putString("checkin", user.getCheckin());
        editor.putString("googleKey", user.getGoogleKey());

        editor.commit();
    }

    private boolean isEmailExist(String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Email_Login", Context.MODE_PRIVATE);
        String email_login = sharedPreferences.getString("value", "");
        boolean exist = false;
        if (email_login != null) {
            String[] listEmailLogin = email_login.split(", ");
            if (!Arrays.asList(listEmailLogin).contains(email)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("value", email_login + ", " + email);
                editor.commit();
            } else {
                exist = true;
            }
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("value", email);
            editor.commit();
        }
        return exist;
    }

    /**
     * Init and Log in to QuickBlox server
     */
    // bronislaw
    private void InitQBChat(String username, String qbPass) {
        if (username != null && qbPass != null) {
            Log.i(DEBUG_TAG, "QuickBlox- server login start");

            // Init Chat
            QBChatService.setDebugEnabled(true);
            QBSettings.getInstance().fastConfigInit(Config.QB_ID, Config.QBAUTH_KEY, Config.QBAUTH_SECRET);
            if (!QBChatService.isInitialized()) {
                QBChatService.init(getApplicationContext());
            }

            chatService = QBChatService.getInstance();

            // create QB user
            final QBUser qbUser = new QBUser();
            qbUser.setLogin(username);
            qbUser.setPassword(qbPass);

            QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
                @Override
                public void onSuccess(QBSession session, Bundle args) {

                    // save current user
                    //
                    qbUser.setId(session.getUserId());
                    ((User) getApplication()).setCurrentUser(qbUser);

                    // login to Chat
                    //
                    loginToChat(qbUser);
                }

                @Override
                public void onError(List<String> errors) {
                    Toast.makeText(getApplicationContext(), "Quickblox create session errors: " + errors, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void loginToChat(final QBUser user){
        chatService.login(user, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                Log.i(DEBUG_TAG, "Quickblox- login success");
                // Start sending presences
                //
                try {
                    chatService.startAutoSendPresence(Config.AUTO_PRESENCE_INTERVAL_IN_SECONDS);
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(List errors) {
                Toast.makeText(getApplicationContext(), "Quickblox chat login errors: " + errors, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Connect to chat server
     */
    public void connectChatServer(String email) {



        /* bronislaw else
        RegisterChatServer registerChatServer = new RegisterChatServer(getApplicationContext());
        registerChatServer.execute(user.getChatServerUrl(), user.getUserId(), user.getChatSecretKey(), user.getChatKey());
        try {
            String sRegisterTask = registerChatServer.get();

            if (sRegisterTask != null) {
                JSONObject out = new JSONObject(sRegisterTask);
                if (out.has("status") && !out.isNull("status")) {
                    if (out.getString("status").equals("fail")) {
                        Toast.makeText(getApplicationContext(), Html.fromHtml(out.getString("message").toString()), Toast.LENGTH_LONG).show();
                    } else if (out.getString("status").equals("success")) {
                        user.setTokenChatServer(out.getString("message").toString());
                        Log.i("TOKEN CHAT SERVER > ", user.getTokenChatServer());
                    }
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), this.getString(R.string.not_find_chat_server), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
        */
    }


    /*
     * Alert Dialog
     */
    public void showMessageBox(String message) {
        new AlertDialog.Builder(this).setPositiveButton(this.getString(R.string.ok), null).setMessage(message).show();
    }

    public void connect() {
        if (isConnected()) {
            disconnect();
        } else {
            connect(new LoginListener() {

                ProgressDialog progressDialog;

                @Override
                public void onStart() {
                    super.onStart();
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(phraseManager.getPhrase(getApplicationContext(), "accountapi.connecting_to_facebook_please_hold"));
                    progressDialog.show();
                }

                @Override
                public void onSuccess(Response response) {
                    super.onSuccess(response);
                }

                @Override
                public void onError(FacebookRequestError error) {
                    super.onError(error);
                }


                @Override
                public void onClosedLoginFailed(Session session, SessionState state, Exception exception) {
                    super.onClosedLoginFailed(session, state, exception);
                }

                @Override
                public void onCreated(Session session, SessionState state, Exception exception) {
                    super.onCreated(session, state, exception);
                }

                @Override
                public void onClosed(Session session, SessionState state, Exception exception) {
                    super.onClosed(session, state, exception);

                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    progressDialog.dismiss();
                    new ConnectFacebook().execute(getFullName(), getUserEmail(), getUserGender(), getUserBirth(), getUserId(), getAccessToken());
                }
            });
        }
    }

    private void checkStatusFacebook() {
        if (isConnected()) {
            String fbEmail = user.getEmail();

            //request register chat server
            if (user.getChatKey() != null
                    && user.getChatServerUrl() != null
                    && user.getChatServerUrl() != null) {
                connectChatServer(fbEmail);
            }

            Intent myIntent = new Intent(LoginActivity.this, DashboardActivity.class);
            //set email to user
            myIntent.putExtra("email", fbEmail);
            startActivity(myIntent);
            finish();
        }
    }

    /**
     * Class connect facebook
     *
     * @author ducpham
     */
    public class ConnectFacebook extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... param) {
            String resultstring = null;
            try {
                // get login facebook url
                String url_facebook = Config.CORE_URL + Config.URL_FACEBOOK;

                //Log params
                Log.i("FACEBOOK PARAMS", param[0] + " " + param[1] + " " + param[2] + " " + param[3] + " " + param[4] + " " + param[5]);

                // Use BasicNameValuePair to store POST data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("fullname", param[0]));
                pairs.add(new BasicNameValuePair("email", param[1]));
                pairs.add(new BasicNameValuePair("gender", (param[2].equals("male") ? "1" : "2")));
                pairs.add(new BasicNameValuePair("birthday", param[3]));
                pairs.add(new BasicNameValuePair("uid", param[4]));
                pairs.add(new BasicNameValuePair("accessToken", param[5]));

                resultstring = networkUntil.makeHttpRequest(url_facebook, "POST", pairs);
                Log.i("FACEBOOK CONNECTION", resultstring);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return resultstring;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {

                    JSONObject jsonObj = new JSONObject(result);
                    user.setKey_admob(null);
                    /* bronislaw
                    //set chat server info
                    user.setChatKey(null);
                    user.setChatSecretKey(null);
                    user.setChatServerUrl(null);
                    */
                    user.setKey_admob(null);
                    user.setCheckin(null);

                    if (jsonObj.has("token") && jsonObj.has("user_id")) {
                        user.setTokenkey(jsonObj.getString("token").toString());
                        user.setUserId(jsonObj.getString("user_id").toString());

                        /* bronislaw
                        //set chat server info
                        if (jsonObj.has("chat_server_key") && !jsonObj.isNull("chat_server_key"))
                            user.setChatKey(jsonObj.getString("chat_server_key"));
                        if (jsonObj.has("chat_server_secret") && !jsonObj.isNull("chat_server_secret"))
                            user.setChatSecretKey(jsonObj.getString("chat_server_secret"));
                        if (jsonObj.has("chat_server_url") && !jsonObj.isNull("chat_server_url"))
                            user.setChatServerUrl(jsonObj.getString("chat_server_url"));
                            */

                        if (jsonObj.has("key_admob") && !jsonObj.isNull("key_admob")) {
                            user.setKey_admob(jsonObj.getString("key_admob"));
                        }

                        if (jsonObj.has("enable_check_in")) {
                            user.setCheckin(String.valueOf(jsonObj.getBoolean("enable_check_in")));
                        }

                        if (jsonObj.has("google_key")) {
                            user.setGoogleKey(jsonObj.getString("google_key"));
                        }

                        if (jsonObj.has("phrases")) {
                            phraseManager.saveJSONObject(getApplicationContext(), jsonObj.getJSONObject("phrases"));
                        }
                        Intent myIntent;
                        String fbEmail = getUserEmail();
                        if (!session.checkLogin() && !isEmailExist(fbEmail)) {
                            myIntent = new Intent(LoginActivity.this, SkipAddFriend.class);
                        } else {
                            myIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                        }

                        myIntent.putExtra("email", fbEmail);

                        //set email to user
                        user.setEmail(fbEmail);

                        //cache info
                        cacheInfo();

                        //request register chat server
                        if (user.getChatKey() != null
                                && user.getChatServerUrl() != null
                                && user.getChatServerUrl() != null) {
                            connectChatServer(fbEmail);
                        }
                        startActivity(myIntent);
                        finish();
                    } else {
                        showMessageBox(phraseManager.getPhrase(getApplicationContext(), "accountapi.log_in_with_facebook_failed"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

}
