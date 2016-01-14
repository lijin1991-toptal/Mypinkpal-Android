package com.brodev.socialapp.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.brodev.socialapp.android.SessionManager;
import com.brodev.socialapp.android.WakeLocker;
import com.brodev.socialapp.android.asyncTask.GetListUnreadMessageTask;
import com.brodev.socialapp.android.asyncTask.SetMyLocationTask;
import com.brodev.socialapp.badgeview.BadgeView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.CommentDetailFragment;
import com.brodev.socialapp.fragment.ConfirmFriendFragment;
import com.brodev.socialapp.fragment.DashboardFragment;
import com.brodev.socialapp.fragment.DialogConversationFragment;
import com.brodev.socialapp.fragment.MessageFragment;
import com.brodev.socialapp.fragment.NotificationFragment;
import com.brodev.socialapp.fragment.RsvpEventDialogFragment;
import com.brodev.socialapp.fragment.rightfriendsbar.FriendChatListFragment;
import com.brodev.socialapp.gcm.GSMHelper;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.base.BaseLogeableActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mypinkpal.app.R;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadFriendListCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginAndJoinDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatDialogUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.FindUnknownFriendsTask;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DashboardActivity extends BaseLogeableActivity {

    private static final String TAG = DashboardActivity.class.getSimpleName();
    private Fragment mContent;
    private User userApp;
    private ImageView friendImg, mailImg, notifyImg;
    private AdView adView;
    public static String email, userId, token;
    private GSMHelper gsmHelper;
    private NetworkUntil networkUntil = new NetworkUntil();

    public DashboardActivity() {
//        super(R.string.app_name);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gsmHelper = new GSMHelper(this);

        userApp = (User) getApplication().getApplicationContext();
        new SetMyLocationTask(userApp).execute();
        getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        getSlidingMenu().setSecondaryMenu(R.layout.menu_layout);
        getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);

        //right side bar
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_layout, new FriendChatListFragment())
                .commit();

        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");

        if (mContent == null) {
            mContent = new DashboardFragment();
            getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
            getSlidingMenu().setShadowDrawable(R.drawable.shadow);
        }

        /* Init QuickBlox Chat */
        if (!TextUtils.isEmpty(userApp.getEmail()) && !TextUtils.isEmpty(userApp.getQuickbloxpswd())) {
            checkStartExistSession(userApp.getEmail(), userApp.getQuickbloxpswd());
        } else {
            SessionManager session;
            session = new SessionManager(getApplicationContext());
            HashMap<String, String> _user = session.getUserDetails();
            // email
            String _email = _user.get(SessionManager.KEY_EMAIL);
            String _passwd = _user.get(SessionManager.KEY_QBPASS);
            if (!TextUtils.isEmpty(_email) && !TextUtils.isEmpty(_passwd)) {
                checkStartExistSession(_email, _passwd);
            } else {
                Toast.makeText(getApplicationContext(), "Please check email and password", Toast.LENGTH_LONG).show();
            }
        }

        //Content dashboard
        setContentView(R.layout.content_frame);

        /* bronislaw */
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, mContent)
                .commit();


        //get ad
        if (userApp.getKey_admob() != null) {
            // Start loading the ad in the background.
            adView = new AdView(this, AdSize.BANNER, userApp.getKey_admob());

            //call ad view
            LinearLayout layout = (LinearLayout) findViewById(R.id.adView);
            layout.addView(adView);

            // Create an ad request.
            final AdRequest adRequest = new AdRequest();

            adView.loadAd(adRequest);
        }

        //get param
        userId = userApp.getUserId();
        token = userApp.getTokenkey();
        email = getIntent().getStringExtra("email");

        //CALL FUNCTION PUSH NOTIFICATION
        if (userApp.isRegisterGCM() == false) {
            pushnotification();
        }

        registerReceiver(mHandleMessageReceiverRead, new IntentFilter(Config.DISPLAY_CHAT_ACTION_READ));

        //call view by id
        friendImg = (ImageView) findViewById(R.id.friend_top_bar);
        mailImg = (ImageView) findViewById(R.id.mail_top_bar);
        notifyImg = (ImageView) findViewById(R.id.notification_top_bar);
        //action click friend top bar
        friendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //hide badge after clicking
                Config.notifyFriendCount = 0;
                if (Config.notifyFriendCount == 0) {
                    Config.friendBadge.hide();
                }
                mailImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_mail));
                notifyImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
                friendImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_user_new));
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, new ConfirmFriendFragment()).commit();
            }
        });

        //action click message top bar
        mailImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //hide badge after clicking
                Config.notifyMailCount = 0;
                if (Config.notifyMailCount == 0) {
                    Config.mailBadge.hide();
                }
                mailImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_mail_new));
                notifyImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
                friendImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_user));
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, new MessageFragment()).commit();

            }
        });

        //action click notification top bar
        notifyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //hide badge after clicking
                Config.notifyCount = 0;
                if (Config.notifyCount == 0) {
                    Config.notifyBadge.hide();
                }
                mailImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_mail));
                notifyImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification_new));
                friendImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_user));
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, new NotificationFragment()).commit();
            }
        });

        /**
         * Badge view for top bar (friend, mail, notification)
         */

        //for friend badge
        Config.friendBadge = new BadgeView(getApplicationContext(), friendImg);
        Config.friendBadge.setTextSize(12);
        Config.friendBadge.setBadgePosition(BadgeView.POSITION_TOP_LEFT);

        //for mail badge
        Config.mailBadge = new BadgeView(getApplicationContext(), mailImg);
        Config.mailBadge.setTextSize(12);
        Config.mailBadge.setBadgePosition(BadgeView.POSITION_TOP_LEFT);

        //for notification
        Config.notifyBadge = new BadgeView(getApplicationContext(), notifyImg);
        Config.notifyBadge.setTextSize(12);
        Config.notifyBadge.setBadgePosition(BadgeView.POSITION_TOP_LEFT);

        //set background color for badgeView
        changeBackgroundColor(Config.friendBadge, "#272727", userApp);
        changeBackgroundColor(Config.mailBadge, "#272727", userApp);
        changeBackgroundColor(Config.notifyBadge, "#272727", userApp);

        getProportion();
        getDeviceNameForMessenger();

        //Quickblox
        addActions();
//        checkGCMRegistration();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void changeBackgroundColor(BadgeView badgeView, String colorCode, User user) {
        if ("Brown".equals(user.getColor()) || "Pink".equals(user.getColor()) || "Red".equals(user.getColor())) {
            badgeView.setBadgeBackgroundColor(Color.parseColor(colorCode));
        }
    }

    /**
     * switch content
     *
     * @param fragment
     */
    public void switchContent(Fragment fragment) {
        mContent = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        getSlidingMenu().showContent();
    }

    //show web view
    public void onWebHtml(String pos) {
        Intent intent = WebviewActivity.newInstance(this, pos);
        startActivity(intent);
    }

    //show comment detail
    public void doShowCommentDetail(int position, String type_id, String item_id, String module_id) {
        CommentDetailFragment.newInstance(position, type_id, item_id, module_id, "").show(this);
    }

    //show conversation dialog
    public void showConversationDialog(String userId, boolean isBlock) {
        DialogConversationFragment.newInstance(userId, isBlock).show(getSupportFragmentManager(), "dialog");
    }

    //show rsvp event dialog
    public void showRSVPDialog(String module, int item) {
        RsvpEventDialogFragment.newInstance(module, item).show(getSupportFragmentManager(), "dialog");
    }

    public void doShowIcon(int position) {
        mailImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_mail));
        notifyImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        friendImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_user));
    }

    /**
     * PUSH NOTIFICATION FUNCTION
     */
    public void pushnotification() {
        Log.d("push","push");
       /* if (Config.CORE_URL == null || Config.SENDER_ID == null) {
            final AssetManager assetManager = getBaseContext().getAssets();
            new NetworkUntil(getApplicationContext(), assetManager);
        }

        System.out.println(Config.CORE_URL);
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);

        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);

        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(this);

        registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_MESSAGE_ACTION));

        if (regId.equals("")) {
            // Registration is not present, register now with GCM
            GCMRegistrar.register(this, Config.SENDER_ID);
        } else {

        }
        userApp.setRegisterGCM(true);*/
    }

    /**
     * Receiving push messages
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String notify = intent.getExtras().getString("message");

            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());

            Log.i("NOTIFICATION COUNT", Config.notifyCount + " " + Config.notifyFriendCount + " " + Config.notifyMailCount);

            //update badge view in top bar
            badgeUpdate(notify);

            Log.i("NOTIFICATION", notify);

            // Releasing wake lock
            WakeLocker.release();
        }
    };

    /**
     * Receiving message
     */
    private final BroadcastReceiver mHandleMessageReceiverRead = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isRead = intent.getExtras().getBoolean("read");
            if (isRead) {
                try {
                    if (Config.notifyMailCount > 0) {
                        Config.notifyMailCount = Config.notifyMailCount - 1;
                        if (Config.notifyMailCount > 0) {
                            Config.mailBadge.setText(String.valueOf(Config.notifyMailCount));
                        } else {
                            Config.mailBadge.hide();
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
    };

    /**
     * update badge view
     *
     * @param notify
     */
    public void badgeUpdate(String notify) {
        if (notify.equals("notification")) {
            ++Config.notifyCount;

            Config.notifyBadge.setText(String.valueOf(Config.notifyCount));
            Config.notifyBadge.show();

        } else if (notify.equals("mail")) {
            ++Config.notifyMailCount;

            Config.mailBadge.setText(String.valueOf(Config.notifyMailCount));
            Config.mailBadge.show();

        } else if (notify.equals("add_friend")) {
            ++Config.notifyFriendCount;

            Config.friendBadge.setText(String.valueOf(Config.notifyFriendCount));
            Config.friendBadge.show();
        }
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mHandleMessageReceiver);
            unregisterReceiver(mHandleMessageReceiverRead);
            GCMRegistrar.onDestroy(this);
            if (adView != null) {
                adView.destroy();
            }
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent backtoHome = new Intent(Intent.ACTION_MAIN);
        backtoHome.addCategory(Intent.CATEGORY_HOME);
        backtoHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(backtoHome);
    }

    /**
     * switch content
     *
     * @param fragment
     */
    public void switchContentForRight(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_layout, fragment)
                .commit();
        getSlidingMenu().showContent();
    }

    /**
     * set mode for sliding menu
     *
     * @param mode
     */
    public void setModeSliding(int mode) {
        SlidingMenu sm = getSlidingMenu();
        switch (mode) {
            case Config.LEFT_SLIDING:
                sm.setMode(SlidingMenu.LEFT);
                sm.setShadowDrawable(R.drawable.shadow);
                break;
            case Config.RIGHT_SLIDEING:
                sm.setMode(SlidingMenu.RIGHT);
                sm.setShadowDrawable(R.drawable.shadowright);
                break;
            case Config.LEFT_RIGHT_SLIDING:
                sm.setMode(SlidingMenu.LEFT_RIGHT);
                break;

            default:
                break;
        }
    }

    public int getUnReadChat(User user) {
        int unReadCnt = 0;

        try {
            GetListUnreadMessageTask getListUnreadMessageTask = new GetListUnreadMessageTask();
            getListUnreadMessageTask.execute(user.getChatServerUrl(), user.getChatKey(), user.getUserId());
            String unReadList = getListUnreadMessageTask.get();
            if (unReadList != null) {
                JSONArray jsonArray = new JSONArray(unReadList);
                unReadCnt = jsonArray.length();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            unReadCnt = 0;
        }
        return unReadCnt;
    }

    /**
     * Show right bar
     */
    public void showRightBar() {
        getSlidingMenu().showSecondaryMenu();
    }

    public void getProportion() {
        Display display = getWindowManager().getDefaultDisplay();

        @SuppressWarnings("deprecation")
        int width = display.getWidth();
        double size = (width / getResources().getInteger(R.integer.sticker_width));

        Config.CHAT_STICKER = (int) Math.ceil(size);
    }

    /**
     * Get device name/agentInfo for messenger
     */
    public void getDeviceNameForMessenger() {
        if (Config.MESSENGER_AGENT_INFO == null) {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                Config.MESSENGER_AGENT_INFO = capitalize(model);
            } else {
                Config.MESSENGER_AGENT_INFO = capitalize(manufacturer) + " " + model;
            }
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
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

    @Override
    protected void onResume() {
        super.onResume();
        gsmHelper.checkPlayServices();
    }

    private void initBroadcastActionList() {
        addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION, new LoadDialogsSuccessAction());
        addAction(QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION, new LoadFriendsSuccessAction());
        addAction(QBServiceConsts.LOAD_FRIENDS_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, new ImportFriendsSuccessAction());
        addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, new ImportFriendsFailAction());
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, new LoginSuccessAction());
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LOGIN_AND_JOIN_CHATS_SUCCESS_ACTION, new LoginAndJoinChatsSuccessAction());
        addAction(QBServiceConsts.LOGIN_AND_JOIN_CHATS_FAIL_ACTION, failAction);
        initBroadcastActionList();
    }

    @Override
    protected void onFailAction(String action) {
        super.onFailAction(action);
        Toast.makeText(getApplicationContext(), action, Toast.LENGTH_LONG).show();
    }

    private void importFriendsFinished() {
        PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
    }

    private class ImportFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            importFriendsFinished();
        }
    }

    private class ImportFriendsFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            importFriendsFinished();
        }
    }

    private class LoginAndJoinChatsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBLoadDialogsCommand.start(DashboardActivity.this);

            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
            loadFriendsList();
            Toast.makeText(getApplicationContext(), "Quickblox login and join success", Toast.LENGTH_LONG).show();
        }
    }

    private class LoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
            loadFriendsList();
            Toast.makeText(getApplicationContext(), "Quickblox login success", Toast.LENGTH_LONG).show();

            /*
            // Get Friend list and Store in UserTable and FriendTable
            if (userApp.getQuickbloxid() != null)
                new FriendTask().execute(0);
                */
        }
    }

    private class LoadDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideActionBarProgress();
            hideProgress();
            Toast.makeText(getApplicationContext(), "Load dialogs success", Toast.LENGTH_LONG).show();

            ArrayList<ParcelableQBDialog> parcelableDialogsList = bundle.getParcelableArrayList(
                    QBServiceConsts.EXTRA_CHATS_DIALOGS);
            if (parcelableDialogsList != null && !parcelableDialogsList.isEmpty()) {
                ArrayList<QBDialog> dialogsList = ChatDialogUtils.parcelableDialogsToDialogs(
                        parcelableDialogsList);
                new FindUnknownFriendsTask(DashboardActivity.this).execute(dialogsList, null);
            }
        }
    }

    private class LoadFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            Toast.makeText(getApplicationContext(), "Load friends success", Toast.LENGTH_LONG).show();
            loadChatsDialogs();
        }
    }

    private void loadChatsDialogs() {
        QBLoadDialogsCommand.start(this);
    }

    private void loadFriendsList() {
        QBLoadFriendListCommand.start(this);
    }


    private void checkGCMRegistration() {
        if (gsmHelper.checkPlayServices()) {
            if (!gsmHelper.isDeviceRegisteredWithUser(userApp)) {
                gsmHelper.registerInBackground();
                return;
            }
            boolean subscribed = gsmHelper.isSubscribed();
            if (!subscribed) {
                gsmHelper.subscribeToPushNotifications();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    //bronislaw
    private void checkStartExistSession(String userEmail, String userPassword){
        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);
        if ( ( isEmailEntered && isPasswordEntered ) /* || (isLoggedViaFB(isPasswordEntered))*/ ) {
            runExistSession(userEmail, userPassword);
        }
    }

    private void runExistSession(String userEmail, String userPassword) {
        //check is token valid for about 1 minute
        if (AppSession.isSessionExistOrNotExpired(TimeUnit.MINUTES.toMillis(ConstsCore.TOKEN_VALID_TIME_IN_MINUTES))){
            QBLoginAndJoinDialogsCommand.start(this);
        } else {
            doAutoLogin(userEmail, userPassword);
        }
    }

    private void doAutoLogin(String userEmail, String userPassword){
        if (LoginType.EMAIL.equals(getCurrentLoginType())) {
            login(userEmail, userPassword);
        } /* else {
            facebookHelper.loginWithFacebook();
        } */
    }

    private void login(String userEmail, String userPassword) {
        QBUser user = new QBUser(null, userPassword, userEmail);
        QBLoginCommand.start(this, user);
    }

    private LoginType getCurrentLoginType() {
        return AppSession.getSession().getLoginType();
    }


    /**
     * function get result from get method
     *
     * @param page
     * @return string result
     */
    public String getResultFromGET(int page) {
        String resultstring = null;

        // Use BasicNameValuePair to create GET data
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("mode", "getUserFriends"));
        pairs.add(new BasicNameValuePair("userId", userApp.getUserId()));
        pairs.add(new BasicNameValuePair("page", String.valueOf(page)));
        pairs.add(new BasicNameValuePair("sexuality", ""));

        // url request
        String URL = null;

        URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);
        // request GET method to server
        Log.d("psyh", "URL: " + URL);
        resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
        Log.d("psyh", "resultstring: " + resultstring);
        return resultstring;
    }

    /**
     * get friend list of logged user
     *
     * @author ducpham
     */
    public class FriendTask extends AsyncTask<Integer, Void, String> {

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
            // Simulates a background task
            try {
                //get result from get method
                resultstring = getResultFromGET(params[0]);
            } catch (Exception e) {
            }

            return resultstring;
        }

        @Override
        protected void onPostExecute(String result) {
            if (resultstring != null) {
                getUserIdfromFriendList(result);
            } else {
                Toast.makeText(getApplicationContext(), "You have not any friend", Toast.LENGTH_LONG).show();
                Log.e(TAG, "You have not any friend");
            }
        }
    }

    public void getUserIdfromFriendList(String resString) {

        if (resString != null) {
            try {
                JSONObject mainJSON = new JSONObject(resString);
                Object intervention = mainJSON.get("output");

                if (intervention instanceof JSONArray) {
                    JSONArray outJson = (JSONArray) intervention;
                    JSONObject outputJson = null;
                    int id;

                    for (int i = 0; i < outJson.length(); i++) {
                        outputJson = outJson.getJSONObject(i);
                        if (outputJson.has("quickbloxid") && !outputJson.isNull("quickbloxid")) {
                            id = Integer.parseInt(outputJson.getString("quickbloxid"));
                            if (id != 0)
                                QBAddFriendCommand.start(this, id);
                        }
                    }
                }
            } catch (Exception ex) {
                return;
            }
        }

        return;
    }
}
