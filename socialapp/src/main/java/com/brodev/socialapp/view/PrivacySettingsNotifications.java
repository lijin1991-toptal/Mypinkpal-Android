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
import android.widget.ScrollView;
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
import java.util.Objects;

/**
 * Created by Bebel on 3/12/15.
 */
public class PrivacySettingsNotifications extends SherlockActivity {

    private User user;
    private NetworkUntil networkUntil = new NetworkUntil();
    private PhraseManager phraseManager;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private ScrollView privacySettingsNotificationsLayout;

    private ProgressBar loading, progressBar;
    private Switch newCommentsSwitch, commentsApprovalSwitch, forumSubscriptionsSwitch, newFriendSwitch;
    private Switch friendRequestSwitch, newGiftSwitch, notificationLikesSwitch, newMessagesSwitch;
    private Switch receiveNewsletterSwitch, subscribeMailsSwitch;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private String URL_POST_USER_SETTING;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.privacy_settings_notifications);
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
                    PrivacySettingsNotificationsUpdateTask notificationsUpdateTask = new PrivacySettingsNotificationsUpdateTask();
                    String newComments, commentsApproval, forumSubscription, newFriend, friendRequest, newGift, notificationLike, newMessages, receiveNewsletter, subscribeMails;

                    if (newCommentsSwitch.isChecked())
                        newComments = "1";
                    else
                        newComments = "0";

                    if (commentsApprovalSwitch.isChecked())
                        commentsApproval = "1";
                    else
                        commentsApproval = "0";

                    if (forumSubscriptionsSwitch.isChecked())
                        forumSubscription = "1";
                    else
                        forumSubscription = "0";

                    if (newFriendSwitch.isChecked())
                        newFriend = "1";
                    else
                        newFriend = "0";

                    if (friendRequestSwitch.isChecked())
                        friendRequest = "1";
                    else
                        friendRequest = "0";

                    if (newGiftSwitch.isChecked())
                        newGift = "1";
                    else
                        newGift = "0";

                    if (notificationLikesSwitch.isChecked())
                        notificationLike = "1";
                    else
                        notificationLike = "0";

                    if (newMessagesSwitch.isChecked())
                        newMessages = "1";
                    else
                        newMessages = "0";

                    if (receiveNewsletterSwitch.isChecked())
                        receiveNewsletter = "1";
                    else
                        receiveNewsletter = "0";

                    if (subscribeMailsSwitch.isChecked())
                        subscribeMails = "1";
                    else
                        subscribeMails = "0";

                    notificationsUpdateTask.execute(newComments, commentsApproval, forumSubscription, newFriend, friendRequest,
                            newGift, notificationLike,newMessages, receiveNewsletter, subscribeMails);

                } else {
                    Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void initView() {
        getSupportActionBar().setTitle("Notifications");
        privacySettingsNotificationsLayout = (ScrollView) this.findViewById(R.id.privacy_settings_layout);

        newCommentsSwitch = (Switch) this.findViewById(R.id.newCommentsSwitch);
        commentsApprovalSwitch = (Switch) this.findViewById(R.id.commentsApprovalSwitch);
        forumSubscriptionsSwitch = (Switch) this.findViewById(R.id.forumSubscriptionsSwitch);
        newFriendSwitch = (Switch) this.findViewById(R.id.newFriendSwitch);
        friendRequestSwitch = (Switch) this.findViewById(R.id.friendRequestSwitch);
        newGiftSwitch = (Switch) this.findViewById(R.id.newGiftSwitch);
        notificationLikesSwitch = (Switch) this.findViewById(R.id.notificationLikesSwitch);
        newMessagesSwitch = (Switch) this.findViewById(R.id.newMessagesSwitch);
        receiveNewsletterSwitch = (Switch) this.findViewById(R.id.receiveNewsletterSwitch);
        subscribeMailsSwitch = (Switch) this.findViewById(R.id.subscribeMailsSwitch);

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
                        loadPrivacySettingsNotifications();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        PrivacySettingsNotificationsTask mt = new PrivacySettingsNotificationsTask();
        mt.execute();
        loadPrivacySettingsNotifications();
    }

    private void loadPrivacySettingsNotifications() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                privacySettingsNotificationsLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                //fetch data
                new PrivacySettingsNotificationsTask().execute();
            } else {
                // display error
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                privacySettingsNotificationsLayout.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            progressBar.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            privacySettingsNotificationsLayout.setVisibility(View.GONE);
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
            pairs.add(new BasicNameValuePair("method", "accountapi.getPrivacySettingsNotifications"));

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

    public class PrivacySettingsNotificationsTask extends AsyncTask<Integer, Void, String> {

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
                    viewNotifications(result);
                    loading.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * show Notifications
     */
    private void viewNotifications(String result) {
        try {
            JSONObject mainJSON = new JSONObject(result);
            Object intervention = mainJSON.get("output");

            if (intervention instanceof JSONObject) {
                JSONObject outputJSON = mainJSON.getJSONObject("output");
                String comment_add_new_comment = outputJSON.getString("comment_add_new_comment");
                String comment_approve_new_comment = outputJSON.getString("comment_approve_new_comment");
                String forum_subscribe_new_post = outputJSON.getString("forum_subscribe_new_post");
                String friend_new_friend_accepted = outputJSON.getString("friend_new_friend_accepted");
                String friend_new_friend_request = outputJSON.getString("friend_new_friend_request");
                String gift_new_gift = outputJSON.getString("gift_new_gift");
                String like_new_like = outputJSON.getString("like_new_like");
                String mail_new_message = outputJSON.getString("mail_new_message");
                String newsletter_can_receive_notification = outputJSON.getString("newsletter_can_receive_notification");
                String newsletter_unsubscribe_all_mails = outputJSON.getString("newsletter_unsubscribe_all_mails");

                if (comment_add_new_comment.equals("1"))
                    newCommentsSwitch.setChecked(false);
                else
                    newCommentsSwitch.setChecked(true);

                if (comment_approve_new_comment.equals("1"))
                    commentsApprovalSwitch.setChecked(false);
                else
                    commentsApprovalSwitch.setChecked(true);

                if (forum_subscribe_new_post.equals("1"))
                    forumSubscriptionsSwitch.setChecked(false);
                else
                    forumSubscriptionsSwitch.setChecked(true);

                if (friend_new_friend_accepted.equals("1"))
                    newFriendSwitch.setChecked(false);
                else
                    newFriendSwitch.setChecked(true);

                if (friend_new_friend_request.equals("1"))
                    friendRequestSwitch.setChecked(false);
                else
                    friendRequestSwitch.setChecked(true);

                if (gift_new_gift.equals("1"))
                    newGiftSwitch.setChecked(false);
                else
                    newGiftSwitch.setChecked(true);

                if (like_new_like.equals("1"))
                    notificationLikesSwitch.setChecked(false);
                else
                    notificationLikesSwitch.setChecked(true);

                if (mail_new_message.equals("1"))
                    newMessagesSwitch.setChecked(false);
                else
                    newMessagesSwitch.setChecked(true);

                if (newsletter_can_receive_notification.equals("1"))
                    receiveNewsletterSwitch.setChecked(false);
                else
                    receiveNewsletterSwitch.setChecked(true);

                if (newsletter_unsubscribe_all_mails.equals("1"))
                    subscribeMailsSwitch.setChecked(false);
                else
                    subscribeMailsSwitch.setChecked(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();;
        }
    }


    public class PrivacySettingsNotificationsUpdateTask extends AsyncTask<String, Void, String> {
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
            pairs.add(new BasicNameValuePair("val[notification][comment.add_new_comment]", params[0]));
            pairs.add(new BasicNameValuePair("val[notification][comment.approve_new_comment]", params[1]));
            pairs.add(new BasicNameValuePair("val[notification][forum.subscribe_new_post]", params[2]));
            pairs.add(new BasicNameValuePair("val[notification][friend.new_friend_accepted]", params[3]));
            pairs.add(new BasicNameValuePair("val[notification][friend.new_friend_request]", params[4]));
            pairs.add(new BasicNameValuePair("val[notification][gift.new_gift]", params[5]));
            pairs.add(new BasicNameValuePair("val[notification][like.new_like]", params[6]));
            pairs.add(new BasicNameValuePair("val[notification][mail.new_message]", params[7]));
            pairs.add(new BasicNameValuePair("val[notification][newsletter.can_receive_notification]", params[8]));
            pairs.add(new BasicNameValuePair("val[notification][newsletter.unsubscribe_all_mails]", params[9]));

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
