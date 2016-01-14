package com.brodev.socialapp.view;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.chat.Chat;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.RoomUserFragment;
import com.brodev.socialapp.fragment.rightfriendsbar.FriendChatListFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mypinkpal.app.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.model.QBUser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bebel on 1/15/15.
 */
public class CreateRoomActivity extends SherlockFragmentActivity {

    private User user;
    private int page, total, currentPos;
    private NetworkUntil networkUntil = new NetworkUntil();
    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private FriendAdapter fa = null;
    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;
    private String event = null;
    private Chat chat;
    private ProgressBar progressBar;

    private Switch typeSwitch;
    private Button createBtn;
    private List<Friend> selected = new ArrayList<Friend>();
    private Mode mode = Mode.PRIVATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = (User) getApplicationContext();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_createroom);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New Group Chat");

        page = 1;
        total = 1;

        progressBar = (ProgressBar) findViewById(R.id.progressbar_room);
        progressBar.setVisibility(View.VISIBLE);

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.friend_fragment_list_room);

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

        typeSwitch = (Switch) findViewById(R.id.createTypeSwitch);
        typeSwitch.setChecked(false);
        typeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    typeSwitch.setChecked(true);
                } else {
                    typeSwitch.setChecked(false);
                }
            }
        });

        createBtn = (Button) findViewById(R.id.createChatButton);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                QBDialog dialogToCreate = new QBDialog();
                dialogToCreate.setName(usersListToChatName());
                if(selected.size() == 1){
                    dialogToCreate.setType(QBDialogType.PRIVATE);
                }else {
                    if (typeSwitch.isChecked() == false) {
                        dialogToCreate.setType(QBDialogType.GROUP);
//                        mode = Mode.GROUP;
                    }
                    else if (typeSwitch.isChecked() == true) {
                        dialogToCreate.setType(QBDialogType.PUBLIC_GROUP);
//                        mode = Mode.PUBLIC_GROUP;
                    }
                }

                dialogToCreate.setOccupantsIds(getUserIds(selected));

                /*
                QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallbackImpl<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        if(selected.size() == 1){
                            startSingleChat(dialog, mode);
                        } else {
                            startGroupChat(dialog, mode);
                        }
                    }

                    @Override
                    public void onError(List<String> errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(CreateRoomActivity.this);
                        dialog.setMessage("dialog creation errors: " + errors).create().show();
                    }
                }); */
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_CHAT_ACTION));

        registerReceiver(mHandleRequestFriendReceiver, new IntentFilter(Config.REQUEST_GET_FRIEND_ONLINE_ACTION));

        actualListView = mPullRefreshListView.getRefreshableView();

        TextView view = new TextView(getApplicationContext());
        view.setLines(1);
        actualListView.addFooterView(view, null, true);

        try {
            loadFriend();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void startSingleChat(QBDialog dialog, Mode mode) {
        Bundle bundle = new Bundle();
//        bundle.putSerializable(ChatActivity.EXTRA_MODE, mode);
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);

        ChatActivity.start(CreateRoomActivity.this, bundle);
    }

    private void startGroupChat(QBDialog dialog, Mode mode){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);
//        bundle.putSerializable(ChatActivity.EXTRA_MODE, mode);

        ChatActivity.start(CreateRoomActivity.this, bundle);
    }

    private void loadFriend() {
        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                mPullRefreshListView.setVisibility(View.VISIBLE);

                //fetch data
                new FriendTask().execute(page);
            } else {
                // display error
                mPullRefreshListView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mPullRefreshListView.setVisibility(View.GONE);
        }
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
        pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
        pairs.add(new BasicNameValuePair("method", "accountapi.getFriends"));
        pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
        pairs.add(new BasicNameValuePair("page", "" + page));

        // url request
        String URL = null;
        if (Config.CORE_URL == null) {
            URL = Config.makeUrl(user.getCoreUrl(), null, false);
        } else {
            URL = Config.makeUrl(Config.CORE_URL, null, false);
        }

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
                JSONObject mainJSON = new JSONObject(resString);

                Object intervention = mainJSON.get("output");

                //get api
                total = mainJSON.getJSONObject("api").getInt("total");

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
                        if (outputJson.has("user_image_path") && !outputJson.isNull("user_image_path"))
                            friend.setIcon(outputJson.getString("user_image_path"));
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

                        madapter.add(friend);
                    }
                } else if (intervention instanceof JSONObject) {
                    JSONObject output = (JSONObject) intervention;
                    Friend f = new Friend();
                    f.setNotice(Html.fromHtml(output.getString("notice")).toString());
                    madapter.add(f);
                }
            } catch (Exception ex) {
                mPullRefreshListView.setVisibility(View.GONE);
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
                progressBar.setVisibility(View.GONE);

                try {
                    //init friend adapter
                    if (page == 1 && fa == null) {
                        fa = new FriendAdapter(getApplicationContext());
                    }

                    fa = getFriendAdapter(fa, result);

                    if (fa != null) {
                        if (page == 1) {
                            actualListView.setAdapter(fa);
                        } else {
                            currentPos = actualListView.getFirstVisiblePosition();

                            actualListView.setAdapter(fa);
                            actualListView.setSelectionFromTop(currentPos + 1, 0);

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

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final Friend item = getItem(position);
            FriendViewHolder holder = null;

            if (view == null) {
                int layout = R.layout.fragment_roomuser;

                view = LayoutInflater.from(getContext()).inflate(layout, null);

                //call element from xml
                ImageView icon = (ImageView) view.findViewById(R.id.friend_roomuser);
                TextView title = (TextView) view.findViewById(R.id.friend_roomuser_text);
                CheckBox addCheck = (CheckBox) view.findViewById(R.id.user_checkbox);
                addCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.getId() == R.id.user_checkbox) {
                            if (isChecked) {
                                selected.add(item);
                            } else {
                                selected.remove(item);
                            }
                        }
                    }
                });

                view.setTag(new FriendViewHolder(icon, title, addCheck));
            }

            if (holder == null && view != null) {
                Object tag = view.getTag();
                if (tag instanceof FriendViewHolder) {
                    holder = (FriendViewHolder) tag;
                }
            }

            if (item != null && holder != null) {

                //set image friend;
                if (holder.imageHolder != null) {
                    if (!"".equals(item.getIcon())) {
                        networkUntil.drawImageUrl(holder.imageHolder, item.getIcon(), R.drawable.loading);
                    }
                }
                //set full name;
                if (holder.title != null) {
                    holder.title.setText(item.getFullname());
                }
            }

            return view;
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
        public final CheckBox addCheck;

        public FriendViewHolder(ImageView icon, TextView title, CheckBox onlineImage) {
            this.imageHolder = icon;
            this.title = title;
            this.addCheck = onlineImage;
        }
    }

    @Override
    public void onDestroy() {
        fa = null;
        try {
            unregisterReceiver(mHandleMessageReceiver);
            unregisterReceiver(mHandleRequestFriendReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }

    private String usersListToChatName(){
        String chatName = "";
        for(Friend user : selected){
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getUsername();
        }
        return chatName;
    }

    public static ArrayList<Integer> getUserIds(List<Friend> users){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(Friend user : users){
            ids.add(Integer.parseInt(user.getQuickbloxid()));
        }
        return ids;
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

    public static enum Mode {PUBLIC_GROUP, GROUP, PRIVATE}
}
