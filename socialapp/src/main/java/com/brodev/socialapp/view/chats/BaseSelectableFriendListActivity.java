package com.brodev.socialapp.view.chats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.asyncTask.FriendsAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.fragment.members.FriendsListFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.utils.Consts;
import com.brodev.socialapp.view.ChatActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mypinkpal.app.R;
import com.brodev.socialapp.view.base.BaseLogeableActivity;
import com.brodev.socialapp.view.uihelper.SimpleActionModeCallback;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ConstsCore;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseSelectableFriendListActivity extends BaseLogeableActivity implements NewDialogCounterFriendsListener {

    protected DialogsSelectableFriendsAdapter friendsAdapter;
    protected ListView friendsListView;
    private ActionMode actionMode;
    private boolean isNeedToCloseWithoutRedirect;

    private PullToRefreshListView mPullRefreshListView;
    private FrameLayout searchLayout;
    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private EditText searchEdt;
    private EditText groupName;
    private PhraseManager phraseManager;
    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private FriendAdapter fa = null;
    private FriendAdapter faSearch;
    private int page, total, currentPos;
    private Timer tTimer = new Timer();
    private String sexuality;
    private com.brodev.socialapp.entity.User user;
    private User qUser;
    private NetworkUntil networkUntil = new NetworkUntil();
    private SparseBooleanArray sparseArrayCheckBoxes;
    private List<User> selectedFriends;
    private Switch typeSwitch;
    private NewDialogCounterFriendsListener counterChangedListener;
    private int counterFriends;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_group_friendlist);

        user = (com.brodev.socialapp.entity.User)getApplicationContext();
        phraseManager = new PhraseManager(getApplicationContext());
        page = 1;
        total = 1;

        initBase();
//        initListView();
        initUI();
        counterChangedListener = this;
    }

    @Override
    public void onBackPressed() {
        isNeedToCloseWithoutRedirect = true;
        super.onBackPressed();
    }

    private void initUI() {
//        friendsListView = findViewById(R.id.chat_friends_listview);
        searchLayout = (FrameLayout) findViewById(R.id.frame_search);
        searchEdt = (EditText) findViewById(R.id.searchEdit);

        groupName = (EditText) findViewById(R.id.groupname_edit);

        typeSwitch = (Switch) findViewById(R.id.createTypeSwitch);
        typeSwitch.setChecked(false);
        typeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    typeSwitch.setChecked(true);
                    startAction();
                } else {
                    typeSwitch.setChecked(false);
                }
            }
        });

        String[] data = getResources().getStringArray(R.array.sexuality);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        selectedFriends = new ArrayList<User>();

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.friend_fragment_list);

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                fa = new FriendAdapter(getApplicationContext());
                new FriendTask().execute(page);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ++page;
                new FriendTask().execute(page);
            }

        });

        //no internet connection
        noInternetLayout = (RelativeLayout) findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) findViewById(R.id.no_internet_image);

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
                        loadFriend();
                    }
                }, 2000);
            }
        });

        //set phrase
        searchEdt.setHint(phraseManager.getPhrase(getApplicationContext(), "friend.search"));

        registerReceiver(mHandleRequestFriendReceiver, new IntentFilter(Config.REQUEST_GET_FRIEND_ONLINE_ACTION));

        friendsListView = mPullRefreshListView.getRefreshableView();
        friendsListView.setSelector(R.drawable.list_item_background_selector);

        TextView view = new TextView(getApplicationContext());
        view.setLines(1);
        friendsListView.addFooterView(view, null, true);

        try {
            loadFriend();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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

    private void loadFriend() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void asyncSearch(final String s) {
        if (fa != null && fa.getCount() > -1) {
            tTimer.cancel();
            tTimer = new Timer();
            tTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //request search
                    if (s.toString().trim().length() > -1) {
                        faSearch = new FriendAdapter(getApplicationContext());
                        faSearch = searchFriend(user.getUserId(), s.toString().trim(), faSearch);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                friendsListView.setAdapter(faSearch);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                friendsListView.setAdapter(fa);
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
            FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask(getApplicationContext());
            friendsAsyncTask.execute(user.getTokenkey(), userId, sFind, sexuality);
            String result = friendsAsyncTask.get();
            if (result != null) {

                getFriendAdapter(fa, result);
            }
        } catch (Exception ex) {
            Friend f = new Friend();
            f.setNotice("No search results found");
            fa.add(f);
            ex.printStackTrace();
        }

        return fa;
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
        pairs.add(new BasicNameValuePair("mode", "getUserFriends"));
        pairs.add(new BasicNameValuePair("userId", user.getUserId()));
        pairs.add(new BasicNameValuePair("page", String.valueOf(page)));
        pairs.add(new BasicNameValuePair("sexuality", sexuality));

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
     * Function create Friend adapter
     *
     * @return Friend Adapter
     */
    public FriendAdapter getFriendAdapter(FriendAdapter madapter, String resString) {
        if (resString != null) {
            try {
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
                        //set full name
                        if (outputJson.has("full_name") && !outputJson.isNull("full_name"))
                            friend.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
                        //set image
                        if (outputJson.has("user_image") && !outputJson.isNull("user_image")) {
                            String imgUrl = "http://mypinkpal.com/file/pic/user/" + outputJson.getString("user_image");
                            imgUrl = imgUrl.replace("%s", "_100_square");
                            friend.setIcon(imgUrl);
                        }
                        // Quickblox id and password
                        //bronislaw
                        if (outputJson.has("quickbloxid") && !outputJson.isNull("quickbloxid"))
                            friend.setQuickbloxID(outputJson.getString("quickbloxid"));
                        if (outputJson.has("quickbloxpswd") && !outputJson.isNull("quickbloxpswd"))
                            friend.setQuickbloxpswd(outputJson.getString("quickbloxpswd"));

                        if (outputJson.has("mutual_friends") && !outputJson.isNull("mutual_friends"))
                            friend.setMutualFriends(Integer.parseInt(outputJson.getString("mutual_friends")));
                        if (outputJson.has("location_phrase") && !outputJson.isNull("location_phrase"))
                            friend.setLocation(outputJson.getString("location_phrase"));
                        if (outputJson.has("age") && !outputJson.isNull("age"))
                            friend.setAge(outputJson.getString("age"));
                        if (outputJson.has("sexuality") && !outputJson.isNull("sexuality"))
                            friend.setSexuality(outputJson.getString("sexuality"));
                        //set full name
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
                        //set gender
                        if (outputJson.has("gender_phrase") && !outputJson.isNull("gender_phrase"))
                            friend.setGender(outputJson.getString("gender_phrase"));

                        if (outputJson.has("isOnline"))
                            friend.setOnline(outputJson.getBoolean("isOnline"));

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
                        fa = new FriendAdapter(getApplicationContext());
                    }

                    fa = getFriendAdapter(fa, result);

                    if (fa != null) {
                        sparseArrayCheckBoxes = new SparseBooleanArray(fa.getCount());
                        if (page == 1) {
                            friendsListView.setAdapter(fa);
                        } else {
                            currentPos = friendsListView.getFirstVisiblePosition();

                            friendsListView.setAdapter(fa);
                            friendsListView.setSelectionFromTop(currentPos + 1, 0);

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

    public User convertFriendToUser(Friend friend) {
        qUser = new User(Integer.parseInt(friend.getQuickbloxid()), friend.getFullname(), friend.getEmail(), null, friend.getIcon(), friend.getUser_id());
        return qUser;
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
            Friend item = getItem(position);
            FriendViewHolder holder = null;

            if (view == null) {
                int layout = R.layout.list_item_chat_friend_selectable;

                view = LayoutInflater.from(getContext()).inflate(layout, null);
                RoundedImageView icon = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
                TextView title = (TextView) view.findViewById(R.id.name_textview);
                ImageView onlineImg = (ImageView) view.findViewById(R.id.online_imageview);
                CheckBox friendCheckbox = (CheckBox) view.findViewById(R.id.selected_friend_checkbox);

                final Friend inItem = getItem(position);
                final FriendViewHolder inHolder = holder;

                friendCheckbox.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        CheckBox checkBox = (CheckBox) view;
                        sparseArrayCheckBoxes.put(position, checkBox.isChecked());
                        notifyCounterChanged(checkBox.isChecked());
                        if (checkBox.isChecked()) {
                            selectedFriends.add(convertFriendToUser(inItem));
                        } else if (selectedFriends.contains(inItem)) {
                            selectedFriends.remove(convertFriendToUser(inItem));
                        }
//                        inHolder.contentRelativeLayout.setBackgroundColor(getBackgroundColorItem(
//                                inHolder.selectFriendCheckBox.isChecked()));
                    }
                });

                boolean checked = sparseArrayCheckBoxes.get(position);
                friendCheckbox.setChecked(checked);

                view.setTag(new FriendViewHolder(icon, title, onlineImg, friendCheckbox));

            }

            if (holder == null && view != null) {
                Object tag = view.getTag();
                if (tag instanceof FriendViewHolder) {
                    holder = (FriendViewHolder) tag;
                }
            }

            if (item != null && holder != null) {
                //set image friend;
                if (holder.avatarImageView != null) {
                    if (!"".equals(item.getIcon())) {
                        networkUntil.drawImageUrl(holder.avatarImageView, item.getIcon(), R.drawable.loading);
                    }
                }
                //set full name;
                if (holder.nameTextView != null) {
                    holder.nameTextView.setText(item.getFullname());
                }

                //set online
                /* Get from Friend's quickblox id to User and Check online */
                if (holder.onlineImageView != null) {
                    long itemId = (long)Integer.parseInt(item.getQuickbloxid());
                    User user = DatabaseManager.getUserById(getApplicationContext(), itemId);
                    if (user != null && user.isOnline())
                        holder.onlineImageView.setImageResource(R.drawable.user_online);
                    else
                        holder.onlineImageView.setImageResource(R.drawable.user_offline);
                }
            }

            return view;
        }
    }

    private int getBackgroundColorItem(boolean isSelect) {
        Resources resources = getApplicationContext().getResources();;
        return isSelect ? resources.getColor(R.color.list_item_background_pressed_color) : resources.getColor(
                R.color.white);
    }

    /**
     * Class friend view holder
     *
     * @author ducpham
     */
    public class FriendViewHolder {
        //        RelativeLayout contentRelativeLayout;
        public final RoundedImageView avatarImageView;
        public final TextView nameTextView;
        public final ImageView onlineImageView;
        //        TextView statusMessageTextView;
        public final CheckBox selectFriendCheckBox;

        public FriendViewHolder(RoundedImageView icon, TextView title,
                                ImageView onlineImage, CheckBox friendCheckbox) {
            this.avatarImageView = icon;
            this.nameTextView = title;
            this.onlineImageView = onlineImage;
            this.selectFriendCheckBox = friendCheckbox;
        }
    }

    @Override
    public void onDestroy() {
        fa = null;
        try {
            //getActivity().unregisterReceiver(mHandleMessageReceiver);
            unregisterReceiver(mHandleRequestFriendReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }

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
                    fa = new FriendAdapter(getApplicationContext());

                    new FriendTask().execute(page);
                }
            } else if (fa == null) {
                new FriendTask().execute(page);
            }
        }
    };



    protected abstract Cursor getFriends();

    private void initListView() {
        friendsAdapter = new DialogsSelectableFriendsAdapter(this, getFriends());
        friendsAdapter.setCounterChangedListener(this);
        friendsListView.setAdapter(friendsAdapter);
        friendsListView.setSelector(R.drawable.list_item_background_selector);
    }

    @Override
    public void onCounterFriendsChanged(int valueCounter) {
        if (actionMode != null) {
            if (valueCounter == ConstsCore.ZERO_INT_VALUE) {
                closeActionModeWithRedirect(true);
                return;
            }
        } else {
            startAction();
        }
        actionMode.setTitle(getResources().getString(R.string.ndl_ac_mode_title) + ConstsCore.SPACE + valueCounter);
    }

    private void startAction() {
        actionMode = startActionMode(new ActionModeCallback());
    }

    private void closeActionModeWithRedirect(boolean isNeedToCloseWithoutRedirect) {
        this.isNeedToCloseWithoutRedirect = isNeedToCloseWithoutRedirect;
        actionMode.finish();
    }

    private void initBase() {
        canPerformLogout.set(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            closeActionModeWithRedirect(true);
            return true;
        } else {
            isNeedToCloseWithoutRedirect = false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                isNeedToCloseWithoutRedirect = true;
                navigateToParent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract void onFriendsSelected(ArrayList<User> selectedFriends, QBDialogType type, String groupName);

    public static class SimpleComparator implements Comparator<User> {

        public int compare(User friend1, User friend2) {
            return (new Integer(friend1.getUserId())).compareTo(friend2.getUserId());
        }
    }

    private class ActionModeCallback extends SimpleActionModeCallback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (!isNeedToCloseWithoutRedirect) {
                if (groupName.getText().toString() == null || groupName.getText().toString().length() == 0) {
                    actionMode = null;
                    Toast.makeText(getApplicationContext(), "Please input group name.", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayList<User> sFriends = new ArrayList<User>(selectedFriends);
                Collections.sort(sFriends, new SimpleComparator());
                if (typeSwitch.isChecked() == false)
                    onFriendsSelected(sFriends, QBDialogType.GROUP, groupName.getText().toString());
                else
                    onFriendsSelected(sFriends, QBDialogType.PUBLIC_GROUP, groupName.getText().toString());
            }
            actionMode = null;
        }
    }

    private void notifyCounterChanged(boolean isIncrease) {
        changeCounter(isIncrease);
        counterChangedListener.onCounterFriendsChanged(counterFriends);
    }

    private void changeCounter(boolean isIncrease) {
        if (isIncrease) {
            counterFriends++;
        } else {
            counterFriends--;
        }
    }
}