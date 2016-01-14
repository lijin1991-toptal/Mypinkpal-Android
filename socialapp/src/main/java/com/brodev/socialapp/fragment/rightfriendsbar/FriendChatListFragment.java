package com.brodev.socialapp.fragment.rightfriendsbar;

//import android.app.LoaderManager;
//import android.content.Loader;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.chat.Chat;
import com.brodev.chat.ChatNotification;
import com.brodev.chat.PrivateChatManagerImpl;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.SessionManager;
import com.brodev.socialapp.android.asyncTask.FriendsAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.BaseActivity;
import com.brodev.socialapp.view.ChatActivity;
import com.brodev.socialapp.view.DashboardActivity;
import com.brodev.socialapp.view.SkipAddFriend;
import com.brodev.socialapp.view.VideoActivity;
import com.brodev.socialapp.view.VoiceActivity;
import com.brodev.socialapp.view.chats.DialogsAdapter;
import com.brodev.socialapp.view.chats.PrivateDialogActivity;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.commands.QBAcceptFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadUserCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.FriendUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.keyboardsurfer.android.widget.crouton.Crouton;


/**
 * @author ducpham
 * @company Brodev.com
 */
//public class FriendChatListFragment extends SherlockListFragment implements ChatCallbackAdapter {
public class FriendChatListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private User user;
    private int page, total, currentPos;
    private NetworkUntil networkUntil = new NetworkUntil();
    private FriendAdapter fa = null;
    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;
    private Chat chat;
    private EditText searchEdt;
    private Timer tTimer = new Timer();
    private FriendAdapter faSearch;
    private PhraseManager phraseManager;
    private String event = null;
    private ChatNotification chatNotification;
    private String notiMess, sAvatarUrl;
    private ColorView colorView;
    private Pattern pattern;
    private Matcher matcher = null;
    private ProgressBar friendsProgressbar;

    //bronislaw
    private static final int DIALOGS_LOADER_ID = 0;
    private DialogsAdapter dialogsAdapter;
    private QBDialog getDialog;
    private BaseActivity baseActivity;
    private BaseActivity.FailAction failAction;
    private TextView emptyListTextView;
    private Friend myFriend;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private FrameLayout searchLayout;
    private boolean isLoadDialogs;
    private int friend_id;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    /*
    public void startChat() {
        chat = new Chat(this, getActivity().getApplicationContext());
        chat.start();
    }
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = (User) getActivity().getApplicationContext();
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());
        page = 1;
        total = 1;
        notiMess = null;
        sAvatarUrl = null;
        chatNotification = new ChatNotification();
        pattern = Pattern.compile("src=\"(.*?)\"");
        isLoadDialogs = false;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //create view from friend_fragment xml
        View view = inflater.inflate(R.layout.friend_fragment, container, false);

        emptyListTextView = (TextView) view.findViewById(R.id.empty_friendlist_textview);

        searchLayout = (FrameLayout) view.findViewById(R.id.frame_search);

        searchEdt = (EditText) view.findViewById(R.id.searchEdit);

        friendsProgressbar = (ProgressBar) view.findViewById(R.id.friends_progressbar);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.friend_fragment_list);

        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                fa = new FriendAdapter(getActivity().getApplicationContext());
                new FriendTask().execute(page);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ++page;
                new FriendTask().execute(page);
            }

        });

        //no internet connection
        noInternetLayout = (RelativeLayout) view.findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) view.findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) view.findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) view.findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) view.findViewById(R.id.no_internet_image);

        //change color for no internet
        colorView.changeImageForNoInternet(noInternetImg, noInternetBtn, user.getColor());

        //set text for no internet element
        noInternetBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.try_again"));
        noInternetTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_title"));
        noInternetContent.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_content"));

        //action click load try again
        noInternetBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadFriend();
                    }
                }, 2000);
            }
        });

        //bronislaw
        baseActivity = (BaseActivity) getActivity();
        failAction = baseActivity.getFailAction();

        addActions();
        initCursorLoaders();

        return view;
    }

    @Override
    public void onResume() {
//        Crouton.cancelAllCroutons();
        /*
        if (dialogsAdapter != null) {
            checkVisibilityEmptyLabel();
        }
        */
        SessionManager session;
        session = new SessionManager(getActivity());
        HashMap<String, String> _user = session.getUserDetails();
        QBDialog checkDialog;
        checkDialog = ChatUtils.getExistPrivateDialog(baseActivity, Integer.parseInt(_user.get(SessionManager.KEY_QBID)));
        if (checkDialog != null)
            isLoadDialogs = true;
        else
            isLoadDialogs = false;

        super.onResume();
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION, new CreateChatSuccessAction());
        baseActivity.addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION, new OnFailDialogAction());
//        baseActivity.addAction(QBServiceConsts.LOAD_USER_SUCCESS_ACTION, new LoadUserSuccessAction());
//        baseActivity.addAction(QBServiceConsts.LOAD_USER_FAIL_ACTION, new OnFailUserAction());
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION,
                new LoadChatsDialogsSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
    }

    private class LoadChatsDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            isLoadDialogs = true;
            emptyListTextView.setVisibility(View.GONE);
            friendsProgressbar.setVisibility(View.GONE);
            mPullRefreshListView.setVisibility(View.VISIBLE);

            ArrayList<ParcelableQBDialog> parcelableDialogsList = bundle.getParcelableArrayList(
                    QBServiceConsts.EXTRA_CHATS_DIALOGS);
            if (parcelableDialogsList == null) {
                emptyListTextView.setVisibility(View.VISIBLE);
                friendsProgressbar.setVisibility(View.VISIBLE);
                mPullRefreshListView.setVisibility(View.GONE);
            }
        }
    }

    private void initCursorLoaders() {
        getLoaderManager().initLoader(DIALOGS_LOADER_ID, null, this);
    }

    private void initChatsDialogs(Cursor dialogsCursor) {
        dialogsAdapter = new DialogsAdapter(baseActivity, dialogsCursor);
        dialogsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (isLoadDialogs)
                    checkVisibilityEmptyLabel();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return DatabaseManager.getAllDialogsCursorLoader(baseActivity);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dialogsCursor) {
        initChatsDialogs(dialogsCursor);
        checkVisibilityEmptyLabel();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void checkVisibilityEmptyLabel() {
        if (dialogsAdapter.isEmpty()) {
            emptyListTextView.setVisibility(View.VISIBLE);
            friendsProgressbar.setVisibility(View.VISIBLE);
            mPullRefreshListView.setVisibility(View.GONE);
        } else if (!dialogsAdapter.isEmpty() && isLoadDialogs){
            emptyListTextView.setVisibility(View.GONE);
            friendsProgressbar.setVisibility(View.GONE);
            mPullRefreshListView.setVisibility(View.VISIBLE);
        }
    }

    private void loadFriend() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                mPullRefreshListView.setVisibility(View.VISIBLE);
                searchLayout.setVisibility(View.VISIBLE);

                //fetch data
                new FriendTask().execute(page);
            } else {
                // display error
                searchLayout.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                mPullRefreshListView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            searchLayout.setVisibility(View.GONE);
            mPullRefreshListView.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //set phrase
        searchEdt.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "friend.search"));

        getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_CHAT_ACTION));

        getActivity().registerReceiver(mHandleRequestFriendReceiver, new IntentFilter(Config.REQUEST_GET_FRIEND_ONLINE_ACTION));

        actualListView = mPullRefreshListView.getRefreshableView();

        TextView view = new TextView(getActivity().getApplicationContext());
        view.setLines(1);
        actualListView.addFooterView(view, null, true);

        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                Cursor selectedChatCursor = (Cursor) dialogsAdapter.getItem(position);;
                QBDialog dialog = DatabaseManager.getDialogFromCursor(selectedChatCursor);
                int occupantId = ChatUtils.getOccupantIdFromList(dialog.getOccupants());
                com.quickblox.q_municate_core.models.User user = dialogsAdapter.getOccupantById(occupantId);
                if (!TextUtils.isEmpty(dialog.getDialogId())) {
                    PrivateDialogActivity.start(baseActivity, user, dialog);
                }
                */
                Friend friend = (Friend) actualListView.getAdapter().getItem(position);
                getQBdialogfromFriend(friend);
            }
        });

        try {
            loadFriend();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /*
        if (user.getChatSecretKey() != null) {
            startChat();
        }
        */

        searchEdt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                asyncSearch(s.toString());
            }
        });
    }

    private void asyncSearch(final String s) {
        if (fa != null && fa.getCount() > -1) {
            tTimer.cancel();
            tTimer = new Timer();
            tTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //request search
                    if (s.toString().trim().length() > -1) {
                        faSearch = new FriendAdapter(getActivity());
                        faSearch = searchFriend(user.getUserId(), s.toString().trim(), faSearch);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actualListView.setAdapter(faSearch);
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actualListView.setAdapter(fa);
                            }
                        });
                    }
                }
            }, 300);
        }
    }

    /**
     * Search friend
     *
     * @param userId
     * @param sFind
     * @param fa
     * @return
     */
    public FriendAdapter searchFriend(String userId, String sFind, final FriendAdapter fa) {
        try {
            FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask(getActivity().getApplicationContext());
            friendsAsyncTask.execute(user.getTokenkey(), userId, sFind);
            String result = friendsAsyncTask.get();
            if (result != null) {
                getFriendAdapter(fa, result);
                /*
                JSONObject mainJSON = new JSONObject(result);
                JSONObject outputJson = mainJSON.getJSONObject("output");
                Object invt = outputJson.get("aSearchResults");

                JSONObject json = null;
                Friend friend = null;
                if (invt instanceof JSONArray) {
                    JSONArray output = (JSONArray) invt;

                    for (int i = 0; i < output.length(); i++) {
                        json = output.getJSONObject(i);
                        if (!json.getString("user_id").equals(user.getUserId())) {
                            friend = new Friend();
                            //set user id
                            if (json.has("user_id") && !json.isNull("user_id"))
                                friend.setUser_id(json.getString("user_id"));
                            //set full name
                            if (json.has("full_name") && !json.isNull("full_name"))
                                friend.setFullname(Html.fromHtml(json.getString("full_name")).toString());
                            //set user image
                            if (json.has("avatar_url") && !json.isNull("avatar_url"))
                                friend.setIcon(json.getString("avatar_url"));
                            //set is online
                            if (json.has("isOnline") && !json.isNull("isOnline"))
                                friend.setOnline(json.getBoolean("isOnline"));
                            //set Age
                            if (json.has("age") && !json.isNull("age"))
                                friend.setAge(json.getString("age"));
                            //set sexuality
                            if (json.has("sexuality") && !json.isNull("sexuality"))
                                friend.setSexuality(json.getString("sexuality"));
                            //set location
                            if (json.has("location_phrase") && !json.isNull("location_phrase"))
                                friend.setLocation(json.getString("location_phrase"));

                            fa.add(friend);
                        }
                    }
                } else {
                    Friend f = new Friend();
                    f.setNotice(Html.fromHtml(invt.toString()).toString());
                    fa.add(f);
                }
                */
            }
        } catch (Exception ex) {
            Friend f = new Friend();
            f.setNotice("No search results found");
            fa.add(f);
            ex.printStackTrace();
        }

        return fa;
    }

    @Override
    public void onListItemClick(ListView listview, View view, int position, long id) {
        Friend friend = (Friend) actualListView.getAdapter().getItem(position);
        getQBdialogfromFriend(friend);
        /*
        Friend friend = (Friend) actualListView.getAdapter().getItem(position);
        if (friend.getNotice() == null) {
            Intent intent = null;
            //init intent
            if (user.getChatSecretKey() == null) {
                intent = new Intent(this.getActivity(), FriendTabsPager.class);
                intent.putExtra("user_id", friend.getUser_id());
            } else {
                intent = new Intent(this.getActivity(), ChatActivity.class);
                intent.putExtra("fullname", friend.getFullname());
                intent.putExtra("user_id", friend.getUser_id());
                intent.putExtra("image", friend.getIcon());
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }
        }
        Log.d("psyh3", "onListItemClick");*/
    }

    // bronislaw
    /* Save friend lists */
    private void savePreferences(String id, String aUrl) {
        SharedPreferences pref = getActivity().getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(id, aUrl);
        editor.commit();
    }

    /* Clear all friend lists */
    private void removeAllPreferences() {
        SharedPreferences pref = getActivity().getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * function get result from get method
     *
     * @param page
     * @return string result
     */
    public String getResultFromGET(int page) {
        String resultstring = null;

        if (fa != null && fa.getCount() == total && user.getChatKey() == null) {
            return null;
        }

        // Use BasicNameValuePair to create GET data
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        /*
        pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
        pairs.add(new BasicNameValuePair("method", "accountapi.getFriends"));
        pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
        pairs.add(new BasicNameValuePair("page", "" + page));
        */
        pairs.add(new BasicNameValuePair("mode", "getUserFriends"));
        pairs.add(new BasicNameValuePair("userId", user.getUserId()));
        pairs.add(new BasicNameValuePair("page", String.valueOf(page)));
//        pairs.add(new BasicNameValuePair("sexuality", ""));

        // url request
        String URL = null;
        /*
        if (Config.CORE_URL == null) {
            URL = Config.makeUrl(user.getCoreUrl(), null, false);
        } else {
            URL = Config.makeUrl(Config.CORE_URL, null, false);
        }
        */

        URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);
        // request GET method to server
        resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
        Log.d("quickblox", "resultstring FRIENDS: " + resultstring);
        Log.d("psyh", "resultstring FRIENDS: " + resultstring);
        return resultstring;
    }

    /**
     * Function create Friend adapter
     *
     * @return Friend Adapter
     */
    public FriendAdapter getFriendAdapter(FriendAdapter madapter, String resString) {
        if (resString != null) {
            try {
                /* bronislaw */
                removeAllPreferences();

                JSONObject mainJSON = new JSONObject(resString);

                Object intervention = mainJSON.get("output");

                //get api
                total = mainJSON.getJSONObject("api").getInt("total_friend");

                if (intervention instanceof JSONArray) {

                    JSONArray outJson = (JSONArray) intervention;

                    JSONObject outputJson = null;
                    Friend friend = null;

                    for (int i = 0; i < outJson.length(); i++) {
                        outputJson = outJson.getJSONObject(i);
                        friend = new Friend();
                        //set user id
                        if (outputJson.has("user_id") && !outputJson.isNull("user_id"))
                            friend.setUser_id(outputJson.getString("user_id"));
                        //set user email
                        if (outputJson.has("email") && !outputJson.isNull("email"))
                            friend.setEmail(outputJson.getString("email"));
                        //set user name
                        if (outputJson.has("user_name") && !outputJson.isNull("user_name"))
                            friend.setUsername(outputJson.getString("user_name"));
                        //set full name
                        if (outputJson.has("full_name") && !outputJson.isNull("full_name"))
                            friend.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
                        //set image
                        if (outputJson.has("user_image") && !outputJson.isNull("user_image")) {
                            String imgUrl = "http://mypinkpal.com/file/pic/user/" + outputJson.getString("user_image");
                            imgUrl = imgUrl.replace("%s", "_100_square");
                            friend.setIcon(imgUrl);

                            //bronislaw
                            savePreferences(friend.getFullname(), friend.getIcon());
                        }
                        /*
                        //set image
                        if (outputJson.has("user_image_path") && !outputJson.isNull("user_image_path"))
                            friend.setIcon(outputJson.getString("user_image_path"));
                            */
                        //set birthday phrase
                        if (!outputJson.isNull("dob_setting")) {
                            int dobSetting = Integer.parseInt(outputJson.getString("dob_setting"));
                            if (dobSetting != 3 && dobSetting != 1) {
                                if (dobSetting == 2) {
                                    friend.setBirthday(outputJson.getString("month") + " " + outputJson.getString("day"));
                                } else {
                                    friend.setBirthday(outputJson.getString("month") + " " + outputJson.getString("day") + ", " + outputJson.getString("year"));
                                }
                            }
                        }

                        // Quickblox id and password
                        //bronislaw
                        if (outputJson.has("quickbloxid") && !outputJson.isNull("quickbloxid"))
                            friend.setQuickbloxID(outputJson.getString("quickbloxid"));
                        if (outputJson.has("quickbloxpswd") && !outputJson.isNull("quickbloxpswd"))
                            friend.setQuickbloxpswd(outputJson.getString("quickbloxpswd"));

                        //set gender
                        if (outputJson.has("gender_phrase") && !outputJson.isNull("gender_phrase"))
                            friend.setGender(outputJson.getString("gender_phrase"));

                        if (outputJson.has("isOnline"))
                            friend.setOnline(outputJson.getBoolean("isOnline"));

                        //set Age
                        if (outputJson.has("age") && !outputJson.isNull("age"))
                            friend.setAge(outputJson.getString("age"));

                        //set sexuality
                        if (outputJson.has("sexuality") && !outputJson.isNull("sexuality"))
                            friend.setSexuality(outputJson.getString("sexuality"));

                        //set location
                        if (outputJson.has("location_phrase") && !outputJson.isNull("location_phrase"))
                            friend.setLocation(outputJson.getString("location_phrase"));

                        madapter.add(friend);
                    }
                } else if (intervention instanceof JSONObject) {
                    JSONObject output = (JSONObject) intervention;
                    Friend f = new Friend();
                    f.setNotice(Html.fromHtml(output.getString("notice")).toString());
                    madapter.add(f);
                }
            } catch (Exception ex) {
                searchLayout.setVisibility(View.GONE);
                mPullRefreshListView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                return null;
            }
        }
        return madapter;
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
            if (result != null) {
                try {
                    //init friend adapter
                    if (page == 1 && fa == null) {
                        fa = new FriendAdapter(getActivity().getApplicationContext());
                    }

                    fa = getFriendAdapter(fa, result);

                    if (fa != null) {
                        if (page == 1) {
                            actualListView.setAdapter(fa);
                        } else {
                            currentPos = getListView().getFirstVisiblePosition();

                            actualListView.setAdapter(fa);
                            getListView().setSelectionFromTop(currentPos + 1, 0);

                            fa.notifyDataSetChanged();
                        }
                    }
                } catch (Exception ex) {
                    fa = null;
                    ex.printStackTrace();
                }
            }
            mPullRefreshListView.onRefreshComplete();
            Config.TIME_REQUEST = System.currentTimeMillis();
        }
    }

    /**
     * Create friend browse adapter
     *
     * @author ducpham
     */
    public class FriendAdapter extends ArrayAdapter<Friend> {
        public FriendAdapter(Context context) {
            super(context, 0);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final Friend item = getItem(position);
            FriendViewHolder holder = null;

            if (view == null) {
                int layout = R.layout.friend_list_row;

                view = LayoutInflater.from(getContext()).inflate(layout, null);
                if (user.getChatKey() != null) {
                    view.findViewById(R.id.user_online).setVisibility(View.VISIBLE);
                }

                //call element from xml
                ImageView icon = (ImageView) view.findViewById(R.id.friend_image_friend);
                TextView title = (TextView) view.findViewById(R.id.title);
                ImageView onlineImg = (ImageView) view.findViewById(R.id.user_online);
                TextView year_sex_txt = (TextView) view.findViewById(R.id.year_sex);
                TextView friend_position_txt = (TextView) view.findViewById(R.id.friend_position_txt);

                //notice
                TextView notice = (TextView) view.findViewById(R.id.notice);

                view.setTag(new FriendViewHolder(icon, title, notice, year_sex_txt, friend_position_txt, onlineImg));

            }

            if (holder == null && view != null) {
                Object tag = view.getTag();
                if (tag instanceof FriendViewHolder) {
                    holder = (FriendViewHolder) tag;
                }
            }

            if (item != null && holder != null) {
                //if has notice
                if (item.getNotice() != null) {
                    view.findViewById(R.id.friend_image_friend).setVisibility(View.GONE);
                    view.findViewById(R.id.friend_content_view).setVisibility(View.GONE);
                    view.findViewById(R.id.user_online_layout).setVisibility(View.GONE);
                    view.findViewById(R.id.year_sex).setVisibility(View.GONE);
                    view.findViewById(R.id.friend_position_txt).setVisibility(View.GONE);

                    //enable friend requests view
                    view.findViewById(R.id.friend_notice_layout).setVisibility(View.VISIBLE);
                    holder.notice.setText(item.getNotice());
                    colorView.changeColorText(holder.notice, user.getColor());
                }

                //set image friend;
                if (holder.imageHolder != null) {
                    if (!"".equals(item.getIcon())) {
                        networkUntil.drawImageUrl(holder.imageHolder, item.getIcon(), R.drawable.loading);
                    }
                }
                //set full name;
                if (holder.title != null) {
                    holder.title.setText(item.getFullname());
                    colorView.changeColorText(holder.title, user.getColor());
                }

                //set online
                /* Get from Friend's quickblox id to User and Check online */
                long itemId = (long)Integer.parseInt(item.getQuickbloxid());
                com.quickblox.q_municate_core.models.User user = DatabaseManager.getUserById(getActivity().getApplicationContext(), itemId);
                if (user != null && user.isOnline())
                    holder.onlineImg.setImageResource(R.drawable.user_online);
                else
                    holder.onlineImg.setImageResource(R.drawable.user_offline);

                /*
                //set online
                if (holder.onlineImg != null) {
                    if (item.isOnline() == true)
                        holder.onlineImg.setImageResource(R.drawable.user_online);
                    else
                        holder.onlineImg.setImageResource(R.drawable.user_offline);
                }
                */

                //set year and sex
                if (holder.year_sex_txt != null) {
                    String year_sex;
                    if (item.getAge() == null && item.getSexuality() == null)
                        year_sex = "";
                    else if (item.getAge() != null && item.getSexuality() == null)
                        year_sex = item.getAge();
                    else if (item.getAge() == null && item.getSexuality() != null)
                        year_sex = "(" + item.getSexuality() + ")";
                    else
                        year_sex = item.getAge() + " (" + item.getSexuality() + ")";

                    holder.year_sex_txt.setText(year_sex);
                }

                //set friend position
                if (holder.friend_postion_txt != null) {
                    if (item.getLocation() != null)
                        holder.friend_postion_txt.setText(item.getLocation());
                    else if (item.getLocation() == null)
                        holder.friend_postion_txt.setText("");
                }
            }

//            view.findViewById(R.id.chatImg).setOnClickListener(new ChatListener(position));
//            view.findViewById(R.id.videochatImg).setOnClickListener(new ChatListener(position));
//            view.findViewById(R.id.audiochatImg).setOnClickListener(new ChatListener(position));

            view.findViewById(R.id.friend_image_friend).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i("dkd", item.toString());
                    getQBdialogfromFriend(item);
                }
            });

            view.findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i("dkd", item.toString());
                    getQBdialogfromFriend(item);
                }
            });

            return view;
        }
    }

    private void startPrivateChatLocalActivity(QBDialog dialog, Friend friend) {
        int occupantId = ChatUtils.getOccupantIdFromList(dialog.getOccupants());
        com.quickblox.q_municate_core.models.User occupant;
        occupant = dialogsAdapter.getOccupantByIdAndFriend(occupantId, friend);
        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            PrivateDialogActivity.start(baseActivity, occupant, dialog);
        }
    }

    /**
     * Async Task login action
     */
    public class HttpUrlAsynctask extends AsyncTask<Friend, Void, Friend> {

        protected void onPostExecute(Friend result) {
            friendsProgressbar.setVisibility(View.GONE);

            //if result is null
            if (result == null) {
                Toast.makeText(baseActivity, "Can't find user", Toast.LENGTH_LONG).show();
            } else {
                connectChatActivity(result);
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            friendsProgressbar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Friend doInBackground(Friend... param) {
            User user = (User) getActivity().getApplicationContext();
            NetworkUntil network = new NetworkUntil();
            String URL = null;
            Friend friend = param[0];

            try {
                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), null, false);
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, null, false);
                }

                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("method", "accountapi.registerQbUser"));
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                pairs.add(new BasicNameValuePair("user_id", friend.getUser_id()));

                String resultStr = network.makeHttpRequest(URL, "GET", pairs);

                Log.d("QuickBlox", "RESULT: " +  resultStr);

                //convert to json
                JSONObject mainJson = new JSONObject(resultStr);
                JSONObject outputJson = mainJson.getJSONObject("output");
                if (!outputJson.isNull("quickbloxid") && outputJson.has("quickbloxid")) {
                    if (outputJson.getString("quickbloxid").toString().equals("0")) {
                        Log.d("quickblox", "fail register user");
                        return null;
                    }
                } else {
                    Log.d("quickblox", "There is no quickbloxid in JSON");
                    return null;
                }
            } catch (Exception ex) {
                Log.d("quickblox", ex.toString());
                return null;
            }

            return friend;
        }
    }

    public void connectChatActivity(Friend friend) {

        myFriend = friend;
        getDialog = null;
        getDialog = ChatUtils.getExistPrivateDialog(baseActivity, Integer.parseInt(friend.getQuickbloxid()));
        com.quickblox.q_municate_core.models.User occupant;

        if (getDialog != null) {
            startPrivateChatLocalActivity(getDialog, friend);
            /*
            if (occupant == null) {
                friendsProgressbar.setVisibility(View.VISIBLE);
                friend_id = 0;
                friend_id = Integer.parseInt(friend.getQuickbloxid());
                QBLoadUserCommand.start(baseActivity, Integer.parseInt(friend.getQuickbloxid()));
            } else {
                if (occupant.getFullName() == null) {
                    occupant.setFullName(occupant.getEmail());
                }
                PrivateDialogActivity.start(baseActivity, occupant, getDialog);
            }
            */
        } else {
            friendsProgressbar.setVisibility(View.VISIBLE);
            /*
            if (occupant == null) {
                friend_id = 0;
                friend_id = Integer.parseInt(friend.getQuickbloxid());
                QBLoadUserCommand.start(baseActivity, Integer.parseInt(friend.getQuickbloxid()));
            } else {
                if (occupant.getFullName() == null) {
                    occupant.setFullName(occupant.getEmail());
                }
                QBCreatePrivateChatCommand.start(baseActivity, occupant);
            }
            */
            occupant = dialogsAdapter.getOccupantByIdAndFriend(Integer.parseInt(friend.getQuickbloxid()), friend);
            QBCreatePrivateChatCommand.start(baseActivity, occupant);
        }
    }

    public void getQBdialogfromFriend(Friend friend) {

        if (Integer.parseInt(friend.getQuickbloxid()) == 0) {
            ConnectivityManager connMgr;
            NetworkInfo networkInfo;
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                HttpUrlAsynctask mtask = new HttpUrlAsynctask();
                mtask.execute(friend);
            } else {
                Toast.makeText(baseActivity, "Please check network", Toast.LENGTH_LONG).show();
            }
            return;
        }

        connectChatActivity(friend);
    }

    private class CreateChatSuccessAction implements Command {

        @Override
             public void execute(Bundle bundle) throws Exception {
        friendsProgressbar.setVisibility(View.GONE);
        QBDialog dialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        if (dialog != null) {
            startPrivateChatLocalActivity(dialog, myFriend);
        } else {
            com.quickblox.q_municate_core.utils.ErrorUtils.showError(baseActivity, getString(R.string.dlg_fail_create_chat));
        }
    }
}

    /*
    private class LoadUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            if (friend_id != 0) {
                QBDialog dialog = ChatUtils.getExistPrivateDialog(baseActivity, friend_id);
                occupant = null;
                occupant = DatabaseManager.getUserById(baseActivity, friend_id);
                if (occupant != null) {
                    if (occupant.getFullName() == null) {
                        occupant.setFullName(occupant.getEmail());
                    }

                    if (dialog != null) {
                        PrivateDialogActivity.start(baseActivity, occupant, dialog);
                    } else {
                        QBCreatePrivateChatCommand.start(baseActivity, occupant);
                    }
                }
            }
        }
    }

    private class OnFailUserAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            friendsProgressbar.setVisibility(View.GONE);
            Toast.makeText(baseActivity, "Load user fail", Toast.LENGTH_LONG).show();
        }
    }
    */

    private class OnFailDialogAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            friendsProgressbar.setVisibility(View.GONE);
            com.quickblox.q_municate_core.utils.ErrorUtils.showError(baseActivity, getString(R.string.dlg_fail_create_chat));
        }
    }

    class ChatListener implements View.OnClickListener {

        int position;

        ChatListener(int position) {
            this.position = position + 1;
        }

        @Override
        public void onClick(View v) {
            Friend friend = (Friend) actualListView.getAdapter().getItem(position);
            if (friend.getNotice() == null) {
                Intent intent = null;
                //init intent
                /* bronislaw
                if (user.getChatSecretKey() == null) {
                    intent = new Intent(getActivity(), FriendTabsPager.class);
                    intent.putExtra("user_id", friend.getUser_id());
                } else { */
                    switch (v.getId()) {
                        case R.id.chatImg:
                            intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("fullname", friend.getFullname());
                            intent.putExtra("user_id", friend.getUser_id());
                            intent.putExtra("image", friend.getIcon());
                            intent.putExtra("username", friend.getUsername());
                            intent.putExtra("quickbloxid", friend.getQuickbloxid());
                            break;
                        case R.id.videochatImg:
                            intent = new Intent(getActivity(), VideoActivity.class);
                            intent.putExtra("fullname", friend.getFullname());
                            intent.putExtra("quickbloxid", friend.getQuickbloxid());
                            intent.putExtra("calltype", "video_type");
                            break;
                        case R.id.audiochatImg:
                            intent = new Intent(getActivity(), VoiceActivity.class);
                            intent.putExtra("fullname", friend.getFullname());
                            intent.putExtra("quickbloxid", friend.getQuickbloxid());
                            intent.putExtra("calltype", "voice_type");
                            break;
                    }
//                }

                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                }
            }
        }
    }




    /**
     * Class friend view holder
     *
     * @author ducpham
     */
    public class FriendViewHolder {
        public final ImageView imageHolder;
        public final TextView title;
        public final TextView notice;
        public final TextView year_sex_txt;
        public final TextView friend_postion_txt;
        public final ImageView onlineImg;


        public FriendViewHolder(ImageView icon, TextView title,
                                TextView notice, TextView year_sex_txt, TextView friend_postion_txt, ImageView onlineImage) {
            this.imageHolder = icon;
            this.title = title;
            this.notice = notice;
            this.year_sex_txt = year_sex_txt;
            this.friend_postion_txt = friend_postion_txt;
            this.onlineImg = onlineImage;
        }
    }

    @Override
    public void onDestroy() {
        fa = null;
        try {
            getActivity().unregisterReceiver(mHandleMessageReceiver);
            getActivity().unregisterReceiver(mHandleRequestFriendReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }

    /*
    @Override
    public void callback(JSONArray data) throws JSONException {

        Object object = data.get(0);
        if (object instanceof String) {
            if (event != null && object.toString() != null) {
                JSONObject jo = new JSONObject();
                jo.put("status", object.toString());
                displayMessage(getActivity().getApplicationContext(), event, jo);
            }
        } else if (object instanceof JSONObject) {
            Log.i("format", "json object");
        }

    }

    @Override
    public void on(String event, JSONObject data) {
        try {
            if (event.equals("receive:message")) {
                //message
                displayMessage(getActivity().getApplicationContext(), "chat", data);
            } else if (event.equals("receive:composing")) {
                //composing
                displayMessage(getActivity().getApplicationContext(), "composing", data);
            } else if (event.equals("receive:read")) {
                //action read
                displayMessage(getActivity().getApplicationContext(), "read", data);
            } else if (event.equals("receive:sticker")) {
                //sticker
                displayMessage(getActivity().getApplicationContext(), "sticker", data);
            } else if (event.equals("receive:notification")) {
                if (data.getString("type").equals("chat")) {
                    notiMess = Html.fromHtml(data.getString("full_name")).toString() + ": " + Html.fromHtml(data.getString("message").toString());
                } else if (data.getString("type").equals("sticker")) {
                    notiMess = Html.fromHtml(data.getString("full_name")).toString() + ": " +
                            Html.fromHtml(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.you_sent_a_sticker")).toString();
                }
                //get avatar url from html
                sAvatarUrl = data.getString("avatar_url");
                matcher = pattern.matcher(sAvatarUrl);
                while (matcher.find()) { // find next match
                    sAvatarUrl = matcher.group().replace("src=\"", "");
                    sAvatarUrl = sAvatarUrl.substring(0, sAvatarUrl.length() - 1);
                }

                chatNotification.generateNotification(getActivity().getApplicationContext(), notiMess,
                        data.getString("full_name"), sAvatarUrl, data.getString("userId"));

                chatNotification.displayMessage(getActivity().getApplicationContext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    */

    /**
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context
     * @param type
     * @param json
     */
    public static void displayMessage(Context context, String type, JSONObject json) {
        Intent intent = new Intent(Config.DISPLAY_CHAT_ACTION);
        intent.putExtra("type", type);

        intent.putExtra("params", json.toString());

        context.sendBroadcast(intent);
    }

    /**
     * Request get friend online after 30s
     */
    private final BroadcastReceiver mHandleRequestFriendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean bRequest = intent.getExtras().getBoolean("request_friend");
            long time = intent.getExtras().getLong("request_time");

            if (bRequest == true && user.getChatKey() != null) {
                if ((time - Config.TIME_REQUEST) > Config.ONLINE_FRIEND_REQUEST_TIME) {
                    page = 1;
                    fa = new FriendAdapter(getActivity().getApplicationContext());

                    new FriendTask().execute(page);
                }
            } else if (fa == null) {
                new FriendTask().execute(page);
            }
        }
    };

    /**
     * Receiving message
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getExtras().getString("type");
            String toUserId = intent.getExtras().getString("userId");
            String message = intent.getExtras().getString("message");
            String image = intent.getExtras().getString("image");
            String fullname = intent.getExtras().getString("fullname");

            try {
                //send message chat
                if (type.equals("send")) {
                    chat.sendMessage(toUserId, message, image, fullname, Config.MESSENGER_AGENT_INFO);
                }
                //send composing
                else if (type.equals("composing")) {
                    chat.composingChat(toUserId);
                }
                //send read
                else if (type.equals("read")) {
                    chat.read(toUserId);
                }
                //block contact
                else if (type.equals("block")) {
                    event = chat.block(toUserId);
                }
                //unblock contact
                else if (type.equals("unblock")) {
                    event = chat.unblock(toUserId);
                }
                //delete conversation
                else if (type.equals("delete")) {
                    event = chat.deleteChat(toUserId);
                }
                //send sticker
                else if (type.equals("send_sticker")) {
                    event = chat.sendSticker(toUserId, message, image, fullname, Config.MESSENGER_AGENT_INFO);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    /*
    @Override
    public void onMessage(String message) {
        Log.i("message String >>>> ", message);

    }

    @Override
    public void onMessage(JSONObject json) {
        Log.i("message json >>>> ", json.toString());
    }

    @Override
    public void onConnect() {
        //chat.joinChat();
    }

    @Override
    public void onDisconnect() {
    }

    @Override
    public void onConnectFailure() {
    }
    */

}
