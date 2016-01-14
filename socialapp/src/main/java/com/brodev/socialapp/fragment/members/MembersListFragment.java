package com.brodev.socialapp.fragment.members;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.chat.Chat;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.asyncTask.MembersAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.FriendTabsPager;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ducpham
 * @company Brodev.com
 */
public class MembersListFragment extends SherlockListFragment {

    private User user;
    private int page, total, currentPos;
    private NetworkUntil networkUntil = new NetworkUntil();
    private FriendAdapter fa = null;
    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;
    private Chat chat;
    private EditText searchEdt;
    // private Spinner sexSpin;
    private String sexuality;
    private Timer tTimer = new Timer();
    private FriendAdapter faSearch;
    private PhraseManager phraseManager;
    private String event = null;
    //   private ChatNotification chatNotification;
    private String notiMess, sAvatarUrl;
    private ColorView colorView;
    private Pattern pattern;
    private Matcher matcher = null;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private FrameLayout searchLayout;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    public void startChat() {
        // chat = new Chat(this, getActivity().getApplicationContext());
        // chat.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = (User) getActivity().getApplicationContext();
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());
        page = 1;
        total = 1;
        notiMess = null;
        sAvatarUrl = null;
        //  chatNotification = new ChatNotification();
        pattern = Pattern.compile("src=\"(.*?)\"");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //create view from friend_fragment xml
        View view = inflater.inflate(R.layout.friend_fragment, container, false);

        searchLayout = (FrameLayout) view.findViewById(R.id.frame_search);

        searchEdt = (EditText) view.findViewById(R.id.searchEdit);

        // sexSpin = (Spinner) view.findViewById(R.id.sexSpin);
        String[] data = getResources().getStringArray(R.array.sexuality);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     /*   sexSpin.setAdapter(adapter);
        sexSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sexuality = getResources().getStringArray(R.array.sexuality)[position];
                asyncSearch(searchEdt.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sexuality = "";
            }
        });*/

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

        return view;
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


        //  getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_CHAT_ACTION));

        getActivity().registerReceiver(mHandleRequestFriendReceiver, new IntentFilter(Config.REQUEST_GET_FRIEND_ONLINE_ACTION));

        actualListView = mPullRefreshListView.getRefreshableView();

        TextView view = new TextView(getActivity().getApplicationContext());
        view.setLines(1);
        actualListView.addFooterView(view, null, true);

        try {
            loadFriend();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (user.getChatSecretKey() != null) {
            startChat();
        }

        searchEdt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable s) {
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
                    if (s.trim().length() > -1) {
                        faSearch = new FriendAdapter(getActivity());
                        faSearch = searchFriend(String.valueOf(page), s.trim(), faSearch);

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
     * @param sFind
     * @param fa
     * @return
     */
    public FriendAdapter searchFriend(String page, String sFind, final FriendAdapter fa) {
        try {
            MembersAsyncTask membersAsyncTask = new MembersAsyncTask(getActivity().getApplicationContext());
            membersAsyncTask.execute(user.getUserId(), sFind, sexuality);
            String result = membersAsyncTask.get();
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

                            fa.add(friend);
                        }

                    }
                } else {
                    Friend f = new Friend();
                    f.setNotice(Html.fromHtml(invt.toString()).toString());
                    fa.add(f);
                }*/
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

        /*
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
        }*/

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
        pairs.add(new BasicNameValuePair("mode", "searchUsers"));
        pairs.add(new BasicNameValuePair("userId", user.getUserId()));
        pairs.add(new BasicNameValuePair("page", String.valueOf(page)));
        pairs.add(new BasicNameValuePair("sexuality", sexuality));

        //http://mypinkpal.com/mypinkpalapi.php?mode=searchUsers&srch=
        // url request
        String URL = null;
    /*
        if (Config.CORE_URL == null) {
            URL = Config.makeUrl(user.getCoreUrl(), null, false);
        } else {
            URL = Config.makeUrl(Config.CORE_URL, null, false);
        }
    */
//        //http://mypinkpal.com/mypinkpalapi.php?mode=getUserFriends&userId=1&page=1

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
                Object intervention = mainJSON.get("users");
                total = mainJSON.getInt("total");
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
                        if (outputJson.has("sexuality") && !outputJson.isNull("sexuality"))
                            friend.setSexuality(outputJson.getString("sexuality"));
                        if (outputJson.has("mutual_friends") && !outputJson.isNull("mutual_friends"))
                            friend.setMutualFriends(Integer.parseInt(outputJson.getString("mutual_friends")));
                        if (outputJson.has("location_phrase") && !outputJson.isNull("location_phrase"))
                            friend.setLocation(outputJson.getString("location_phrase"));
                        if (outputJson.has("age") && !outputJson.isNull("age"))
                            friend.setAge(outputJson.getString("age"));
                        if (outputJson.has("is_friend") && !outputJson.isNull("is_friend"))
                            friend.setIs_friend(outputJson.getString("is_friend"));
                        if (outputJson.has("is_friend_request") && !outputJson.isNull("is_friend_request"))
                            if (outputJson.getString("is_friend_request").equals("2"))
                                friend.setIs_friend(outputJson.getString("is_friend_request"));
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
                            friend.setOnline((outputJson.getInt("isOnline") == 1) ? true : false);
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
                ex.printStackTrace();
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
                int layout = R.layout.friends_new_list_row;

                view = LayoutInflater.from(getContext()).inflate(layout, null);
                if (user.getChatKey() != null) {
                    view.findViewById(R.id.user_online).setVisibility(View.VISIBLE);
                }

                //call element from xml
                ImageView icon = (ImageView) view.findViewById(R.id.friend_image_friend);
                TextView title = (TextView) view.findViewById(R.id.title);
                ImageView onlineImg = (ImageView) view.findViewById(R.id.user_online);

                String sex = item.getSexuality();
                if (null != sex && !"".equals(sex)) {
                    ((TextView) view.findViewById(R.id.sexualityTv)).setText("(" + sex + ")");
                }
                String age = item.getAge();
                if (null != age && !"".equals(age)) {
                    ((TextView) view.findViewById(R.id.ageTv)).setText("" + age + "");
                }
                TextView mutualFriends = (TextView) view.findViewById(R.id.mutualFriends);
                int friendsCount = item.getMutualFriends();
                if (friendsCount > 0) {
                    mutualFriends.setVisibility(View.VISIBLE);
                    mutualFriends.setText(friendsCount + " " + getResources().getString(
                            (friendsCount == 1) ? R.string.mutual_friend_text : R.string.mutual_friends_text));
                } else {
                    mutualFriends.setVisibility(View.INVISIBLE);
                }

                ((TextView) view.findViewById(R.id.location)).setText(item.getLocation());

                icon.setOnClickListener(new View.OnClickListener() {
                    Friend friend = (Friend) getItem(position);

                    @Override
                    public void onClick(View v) {
                        //    contentLoading.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(getActivity(), FriendTabsPager.class);
                        intent.putExtra("user_id", friend.getUser_id());
                        startActivity(intent);

                    }
                });


                final Button addFriend = (Button) view.findViewById(R.id.sendMessBtn);
                addFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.add_to_friends"));
                if (!"".equals(item.getIs_friend())) {
                    if ("0".equals(item.getIs_friend())) {
                        addFriend.setOnClickListener(new View.OnClickListener() {
                            Friend friend = (Friend) getItem(position);

                            @Override
                            public void onClick(View v) {
                                if (!"".equals(item.getIs_friend())) {
                                    if ("0".equals(item.getIs_friend())) {
                                        new AddFriendTask().execute(String.valueOf(item.getUser_id()));
                                        addFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.pending_friend_confirmation"));
                                        item.setIs_friend("2");
                                    } else if ("3".equals(item.getIs_friend())) {

                                    } else if ("2".equals(item.getIs_friend())) {

                                    } else {
                                    }

                                }
                            }
                        });
                    } else if ("3".equals(item.getIs_friend())) {
                    } else if ("2".equals(item.getIs_friend())) {
                        addFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.pending_friend_confirmation"));
                        addFriend.setClickable(false);
                    } else {
                        addFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.friends"));
                    }

                }


                //notice
                TextView notice = (TextView) view.findViewById(R.id.notice);

                view.setTag(new FriendViewHolder(icon, title, notice, onlineImg));
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
                if (holder.onlineImg != null) {
                    if (item.isOnline() == true)
                        holder.onlineImg.setImageResource(R.drawable.user_online);
                    else
                        holder.onlineImg.setImageResource(R.drawable.user_offline);
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
        public final TextView notice;
        public final ImageView onlineImg;

        public FriendViewHolder(ImageView icon, TextView title,
                                TextView notice, ImageView onlineImage) {
            this.imageHolder = icon;
            this.title = title;
            this.notice = notice;
            this.onlineImg = onlineImage;
        }
    }

    @Override
    public void onDestroy() {
        fa = null;
        try {
            //getActivity().unregisterReceiver(mHandleMessageReceiver);
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

    }*/
/*
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
                    sAvatarUrl = sAvatarUrl.substring(0, sAvatarUrl.length()-1);
                }

                /*
                chatNotification.generateNotification(getActivity().getApplicationContext(), notiMess,
                        data.getString("full_name"), sAvatarUrl, data.getString("userId"));

                chatNotification.displayMessage(getActivity().getApplicationContext());*/
        /*    }
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
    /*
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver()
    {
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

    public class AddFriendTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }
            try {
                String likerequest;
                String URL = null;

                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), "addFriendRequest", true) + "&token=" + user.getTokenkey();
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, "addFriendRequest", true) + "&token=" + user.getTokenkey();
                }

                // Use BasicNameValuePair to create GET data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();

                pairs.add(new BasicNameValuePair("user_id", params[0]));

                likerequest = networkUntil.makeHttpRequest(URL, "POST", pairs);
                Log.d("psyh", likerequest);
            } catch (Exception ex) {
                // Log.i(DEBUG_TAG, ex.getMessage());
            }
            return null;
        }

    }
}
