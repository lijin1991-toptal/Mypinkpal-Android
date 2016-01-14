package com.brodev.socialapp.fragment;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Html;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brodev.socialapp.android.Logout;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.SessionManager;
import com.brodev.socialapp.android.SettingFacebook;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Menu;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.facebook.FacebookPreference;
import com.brodev.socialapp.fragment.GroupChat.GroupChatFragment;
import com.brodev.socialapp.fragment.invites.InviteFriendsFragment;
import com.brodev.socialapp.fragment.members.FriendsListFragment;
import com.brodev.socialapp.fragment.members.MembersListFragment;
import com.brodev.socialapp.fragment.membership.BalanceFragment;
import com.brodev.socialapp.fragment.membership.MembershipFragment;
import com.brodev.socialapp.fragment.nearme.NearMeListFragment;
import com.brodev.socialapp.fragment.rightfriendsbar.FriendChatListFragment;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.DashboardActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.google.gson.JsonObject;
import com.mypinkpal.app.R;
import com.mypinkpal.app.Rightsidebar.BlogRightFragment;
import com.mypinkpal.app.Rightsidebar.EventRightFragment;
import com.mypinkpal.app.Rightsidebar.MarketPlaceRightFragment;
import com.mypinkpal.app.Rightsidebar.MusicRightFragment;
import com.mypinkpal.app.Rightsidebar.VideoRightFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class Side bar fragment
 */
public class SideBarFragment extends ListFragment {

    // init Network Until
    private NetworkUntil networkUntil = new NetworkUntil();
    public SidebarAdapter sa = null;
    private User user;
    // Session manager
    private SessionManager session;
    private FacebookPreference fbSession;
    // phrase manager
    private PhraseManager phraseManager;
    private ColorView colorView;

    private EditText searchTxt;
    private RelativeLayout searchView, noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ProgressBar progressSidebar;
    private ImageView noInternetImg;
    private Button noInternetBtn;
    private FrameLayout searchLayout;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public static SideBarFragment newInstance() {
        return new SideBarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        user = (User) getActivity().getApplicationContext();

        session = new SessionManager(getActivity().getApplicationContext());
        fbSession = new FacebookPreference(getActivity().getApplicationContext());
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.side_bar, container, false);

        searchTxt = (EditText) view.findViewById(R.id.searchEdit);
        searchView = (RelativeLayout) view.findViewById(R.id.search_view);
        progressSidebar = (ProgressBar) view.findViewById(R.id.sidebar_loading);

        searchLayout = (FrameLayout) view.findViewById(R.id.frame_search);
        searchLayout.setVisibility(View.VISIBLE);
        progressSidebar.setVisibility(View.VISIBLE);

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
                        noInternetLayout.setVisibility(View.GONE);
                        searchLayout.setVisibility(View.VISIBLE);
                        progressSidebar.setVisibility(View.VISIBLE);

                        loadSidebar();
                    }
                }, 1000);
            }
        });
        BROADCAST.sideBarFragment = this;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            loadSidebar();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        searchTxt.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "friend.search"));

        searchTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, final boolean hasFocus) {
                if (hasFocus) {

                    /*
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    startActivity(intent);
                    */
                    Fragment newContent = new MembersListFragment();
                    Fragment newContentRight = new FriendChatListFragment();
                    int mode = Config.LEFT_RIGHT_SLIDING;
                    switchFragment(newContent, newContentRight, mode);

                }
            }
        });

        getActivity().registerReceiver(mHandleRequestSidebarReceiver, new IntentFilter(Config.REQUEST_GET_SIDEBAR));
    }

    private void loadSidebar() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                //fetch data
                new SideBarTask().execute();
            } else {
                // display error
                searchLayout.setVisibility(View.GONE);
                progressSidebar.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                noInternetTitle.setTextColor(Color.WHITE);
            }
        } catch (Exception ex) {
            // display error
            searchLayout.setVisibility(View.GONE);
            progressSidebar.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            noInternetTitle.setTextColor(Color.WHITE);
            ex.printStackTrace();
        }
    }

    /**
     * Request get sidebar
     */
    private final BroadcastReceiver mHandleRequestSidebarReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (sa == null) {
                try {
                    new SideBarTask().execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // display error
                    noInternetLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        try {
            getActivity().unregisterReceiver(mHandleRequestSidebarReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (sa != null && sa.getCount() > 1) {
            try {
                // get profile menu
                Menu menu = sa.getItem(1);
                // set icon
                if (!menu.getIcon().equals(user.getUserImage())) {
                    menu.setIcon(user.getUserImage());
                    ((SidebarAdapter) getListAdapter()).notifyDataSetChanged();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        searchView.requestFocus();
        BROADCAST.sideBarFragment = this;
        super.onResume();
    }

    /**
     * Action click item on list view side bar
     */
    @Override
    public void onListItemClick(ListView listview, View view, int position, long id) {


        Fragment newContent = null;
        Fragment newContentRight = null;
        boolean checkRightMenu = true;

        Menu menu = (Menu) getListAdapter().getItem(position);
        int mode = Config.LEFT_RIGHT_SLIDING;
        if (getActivity() == null)
            return;

        Fragment f = (Fragment) getFragmentManager().findFragmentById(R.id.menu_layout);

        DashboardActivity activity = (DashboardActivity) getActivity();
        activity.doShowIcon(3);

        //check internet
        connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            if (!menu.getUrl().equals("dashboard") && !menu.getUrl().equals("myprofile") && !menu.getUrl().equals("pages") && !menu.getUrl().equals("GroupChat") && !menu.getUrl().equals("credit") && !menu.getUrl().equals("page_item") && !menu.getUrl().equals("account_setting")
                    && !menu.getUrl().equals("edit_profile")  && !menu.getUrl().equals("privacy_setting") && !menu.getUrl().equals("notification_setting") && !menu.getUrl().equals("message") && !menu.getUrl().equals("photo") && !menu.getUrl().equals("logout")) {
                newContent = new NoInternetFragment();
                checkRightMenu = true;
                if (!(f instanceof FriendChatListFragment) && checkRightMenu) {
                    newContentRight = new FriendChatListFragment();
                }
                switchFragment(newContent, newContentRight, mode);

                return;
            } else if (menu.getUrl().equals("logout")) {
                prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                editor = prefs.edit();
                user.setTokenkey(null);
                editor.putString("token_key", user.getTokenkey());
                editor.commit();

                session.logoutUser(getActivity());

                return;
            }

        }

        // link to fragment
        if (menu.getUrl().equals("dashboard")) {
            newContent = new DashboardFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("message")) {
            newContent = new MessageFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("friends")) {
            newContent = new FriendsListFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("NearMe")) {
            newContent = new NearMeListFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("GroupChat")) {
            newContent = new GroupChatFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("credit")) {
            newContent = new CreditFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("videochat")) {
            //  newContent = new VideoChatQFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("balance")) {
            newContent = new BalanceFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("invite")) {
            newContent = new InviteFriendsFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("user.browse")) {
            newContent = new MembersListFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("blog")) {
            newContent = new BlogFragment();
            newContentRight = new BlogRightFragment();
            checkRightMenu = false;
        } else if (menu.getUrl().equals("myprofile")) {
            Intent intent = new Intent(getActivity(), FriendTabsPager.class);
            intent.putExtra("user_id", user.getUserId());
            BROADCAST.saveAfterFirstClick(listview, view, position, id);
            startActivity(intent);
        } else if (menu.getUrl().equals("membership")) {
            newContent = new MembershipFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("forum")) {
            newContent = new ForumPagerFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("photo")) {
            newContent = new AlbumsFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("pages")) {
            newContent = new PagesFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("music")) {
            newContent = new MusicFragment();
            newContentRight = new MusicRightFragment();
            mode = Config.LEFT_RIGHT_SLIDING;
            checkRightMenu = false;
        } else if (menu.getUrl().equals("event")) {
            newContent = new EventFragment();
            newContentRight = new EventRightFragment();
            checkRightMenu = false;
        } else if (menu.getUrl().equals("marketplace")) {
            newContent = new MarketPlaceFragment();
            newContentRight = new MarketPlaceRightFragment();
            checkRightMenu = false;
        } else if (menu.getUrl().equals("video")) {
            newContent = new VideoFragment();
            newContentRight = new VideoRightFragment();
            checkRightMenu = false;
        } else if (menu.getUrl().equals("page_item")) {
            Intent intent = new Intent(getActivity(), FriendTabsPager.class);
            intent.putExtra("page_id", menu.getPage_id());
            startActivity(intent);
        } else if (menu.getUrl().equals("account_setting")) {
            newContent = new AccountSettingFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("edit_profile")) {
            newContent = new EditProfileFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("privacy_setting")) {
            newContent = new PrivacySettingFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("notification_setting")) {
            newContent = new NotificationSettingFragment();
            checkRightMenu = true;
        } else if (menu.getUrl().equals("logout")) {
            // call function logout
            logout(getActivity());
        } else if (menu.getUrl().equals("poll")) {
            newContent = new WebviewFragmentPoll(menu.getLink());
        } else {
            newContent = new WebviewFragment(menu.getLink());
        }

        // set right content
        if (!(f instanceof FriendChatListFragment) && checkRightMenu) {
            newContentRight = new FriendChatListFragment();
        }

        if (newContent != null) {
            switchFragment(newContent, newContentRight, mode);
        }


    }

    /**
     * function logout
     */
    public void logout(Activity activity) {
        // request logout to server

        Logout lg = new Logout(activity.getApplicationContext());
        lg.execute(user.getTokenkey());

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = prefs.edit();
        editor.clear();

        // clear facebook user
        fbSession.logoutFacebookUser(activity);

        // call get setting
        SettingFacebook settingFb = new SettingFacebook(activity);
        settingFb.execute();

        user.setTokenkey(null);
        user.setUserImage(null);
        user.setRegisterGCM(false);
        user.setKey_admob(null);

        editor.commit();

        new loadDefaultLanguage(activity.getApplicationContext()).execute();

        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        session.logoutUser(activity);

    }

    /**
     * Change fragment on dash board activity
     *
     * @param fragment
     */
    public void switchFragment(Fragment fragment, Fragment fragRight, int mode) {
        if (getActivity() == null)
            return;
        if (getActivity() instanceof DashboardActivity) {
            DashboardActivity fca = (DashboardActivity) getActivity();
            // set mode for sliding menu
            fca.setModeSliding(mode);
            fca.switchContent(fragment);
            if (fragRight != null) {
                fca.switchContentForRight(fragRight);
            }
        }
    }

    /**
     * Class side bar task
     *
     * @author ducpham
     */
    private class SideBarTask extends AsyncTask<String, Void, String> {
        String resultstring;
        JSONObject mainJson;
        JSONObject socialappJSON, notify;

        @Override
        protected String doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }
            try {
                // create side bar adapter
                sa = new SidebarAdapter(getActivity());

                // Use BasicNameValuePair to create GET data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                pairs.add(new BasicNameValuePair("method", "accountapi.getUserInfo"));
                pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
                pairs.add(new BasicNameValuePair("login", "1"));

                // url request
                String URL = null;
                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), null, false);
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, null, false);
                }

                // request GET method to server
                resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);

            } catch (Exception ex) {
            }
            return resultstring;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(String result) {
            try {
                mainJson = new JSONObject(result);
                JSONObject outputJson = mainJson.getJSONObject("output");
                // get app module json
                JSONObject appModJson = outputJson.getJSONObject("appMod");

                JSONObject menuObj = null;

                socialappJSON = mainJson.getJSONObject("social_app");
                notify = socialappJSON.getJSONObject("notify");

                // add user info to side bar
                sa.addHeader(phraseManager.getPhrase(getActivity().getApplicationContext(), "pages.account"));
                Menu m = new Menu();

                m.setIcon(Html.fromHtml(outputJson.getString("photo_120px_square")).toString());
                // set phrase
                m.setPhrase(Html.fromHtml(outputJson.getString("full_name")).toString());
                m.setHeader(false);
                m.setUser(true);
                m.setUrl("myprofile");
                sa.addItem(m);

/*TODO MEMBERSHIP
                // add membership status
                String membershipTitle = "Membership Title";
                String[] menuMembershipIcon = getResources().getStringArray(R.array.ns_menu_items_icon);
                int id_icon = getResources().getIdentifier(menuMembershipIcon[3], "drawable", getActivity().getPackageName());
                Menu accMembershipItem = new Menu(membershipTitle, id_icon, "member_type", false);
                sa.addItem(accMembershipItem);
                // add balance
                String balanceTitle = "Balance Value";
                String[] menuBalanceIcon = getResources().getStringArray(R.array.ns_menu_items_icon);
                id_icon = getResources().getIdentifier(menuBalanceIcon[3], "drawable", getActivity().getPackageName());
                Menu balanceItem = new Menu(balanceTitle, id_icon, "balance", false);
                sa.addItem(balanceItem);
                // add video chat Qblox
                String videoChatQTitle = "Quickblox Chat";
                String[] menuVideoChatQIcon = getResources().getStringArray(R.array.ns_menu_items_icon);
                id_icon = getResources().getIdentifier(menuVideoChatQIcon[3], "drawable", getActivity().getPackageName());
                Menu videoChatQItem = new Menu(videoChatQTitle, id_icon, "videochat", false);
                sa.addItem(videoChatQItem);
*///TODO MEMBERSHIP


                user.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
                user.setDob(outputJson.getString("dob_setting"));


                if (outputJson.has("location_phrase")) {
                    user.setLocation(Html.fromHtml(outputJson.getString("location_phrase")).toString());
                } else {
                    user.setLocation(null);
                }

                if (outputJson.has("birthday_phrase") && !"false".equals(outputJson.getString("birthday_phrase"))) {
                    user.setBirthday(Html.fromHtml(outputJson.getString("birthday_phrase")).toString());
                } else {
                    user.setBirthday(null);
                }

                /* Edit profile */
                if (outputJson.has("country_iso")) {
                    user.setCountry_iso(Html.fromHtml(outputJson.getString("country_iso")).toString());
                }

                if (outputJson.has("city_location")) {
                    String city = Html.fromHtml(outputJson.getString("city_location")).toString();
                    if (city == null || city.equals("null"))
                        user.setCity_location("");
                    else
                        user.setCity_location(city);
                }

                if (outputJson.has("postal_code")) {
                    String code = Html.fromHtml(outputJson.getString("postal_code")).toString();
                    if (code == null || code.equals("null"))
                        user.setPostal_code("");
                    else
                        user.setPostal_code(code);
                }

                if (outputJson.has("birthday_time_stamp")) {
                    user.setBirthday_time_stamp(Html.fromHtml(outputJson.getString("birthday_time_stamp")).toString());
                }

                if (outputJson.has("gender")) {
                    user.setUserGender(Html.fromHtml(outputJson.getString("gender")).toString());
                }

                if (outputJson.has("sexuality")) {
                    user.setSexuality(Html.fromHtml(outputJson.getString("sexuality")).toString());
                }

                if (outputJson.has("religion")) {
                    user.setReligion(Html.fromHtml(outputJson.getString("religion")).toString());
                }

                if (outputJson.has("relation_id")) {
                    user.setRelation_id(Html.fromHtml(outputJson.getString("relation_id")).toString());
                }

                if (outputJson.has("relation_with")) {
                    user.setRelation_with(Html.fromHtml(outputJson.getString("relation_with")).toString());
                }

                if (outputJson.has("relation")) {
                    user.setRelation(Html.fromHtml(outputJson.getString("relation")).toString());
                }

                if (outputJson.has("signature")) {
                    String signature = Html.fromHtml(outputJson.getString("signature")).toString();
                    if (signature == null || signature.equals("null"))
                        user.setSignature("");
                    else
                        user.setSignature(signature);
                }

                if (outputJson.has("use_timeline")) {
                    user.setUse_timeline(Html.fromHtml(outputJson.getString("use_timeline")).toString());
                }

                if (outputJson.has("total_credit")) {
                    user.setCredits(Integer.valueOf(outputJson.getString("total_credit")));
                }

                if (outputJson.has("custom")) {
                    JSONObject custom = outputJson.getJSONObject("custom");
                    if (custom.has("about_me")) {
                        String aboutme = Html.fromHtml(custom.getString("about_me")).toString();
                        if (aboutme == null || aboutme.equals("null"))
                            user.setCustom_aboutme("");
                        else
                            user.setCustom_aboutme(aboutme);
                    }
                    if (custom.has("who_i_d_like_to_meet")) {
                        String whoMeet = Html.fromHtml(custom.getString("who_i_d_like_to_meet")).toString();
                        if (whoMeet == null || whoMeet.equals("null"))
                            user.setCustom_whomeet("");
                        else
                            user.setCustom_whomeet(whoMeet);
                    }
                    if (custom.has("movies")) {
                        String movies = Html.fromHtml(custom.getString("movies")).toString();
                        if (movies == null || movies.equals("null"))
                            user.setCustom_movies("");
                        else
                            user.setCustom_movies(movies);
                    }
                    if (custom.has("interests")) {
                        String interest = Html.fromHtml(custom.getString("interests")).toString();
                        if (interest == null || interest.equals("null"))
                            user.setCustom_interests("");
                        else
                            user.setCustom_interests(interest);
                    }
                    if (custom.has("music")) {
                        String music = Html.fromHtml(custom.getString("music")).toString();
                        if (music == null || music.equals("null"))
                            user.setCustom_music("");
                        else
                            user.setCustom_music(music);
                    }
                    if (custom.has("smoker")) {
                        String smoker = Html.fromHtml(custom.getString("smoker")).toString();
                        if (smoker == null || smoker.equals("null"))
                            user.setCustom_smoker("");
                        else
                            user.setCustom_smoker(smoker);
                    }
                    if (custom.has("drinker")) {
                        String drinker = Html.fromHtml(custom.getString("drinker")).toString();
                        if (drinker == null || drinker.equals("null"))
                            user.setCustom_drinker("");
                        else
                            user.setCustom_drinker(drinker);
                    }
                }

                user.setUserImage(outputJson.getString("photo_120px_square"));

                if (outputJson.has("info")) {

                    JSONObject info = outputJson.getJSONObject("info");
                    if (info.has("Gender")) {
                        user.setGender(Html.fromHtml(info.getString("Gender")).toString());
                    }
                    if (info.has("Age")) {
                        user.setAge(Html.fromHtml(info.getString("Age")).toString());
                    }
                    if (info.has("Location")) {
                        user.setLocation_info(Html.fromHtml(info.getString("Location")).toString());
                    }
                    if (info.has("Last Login")) {
                        user.setLast_login(Html.fromHtml(info.getString("Last Login")).toString());
                    }
                    if (info.has("Member Since")) {
                        user.setMember_since(Html.fromHtml(info.getString("Member Since")).toString());
                    }
                    if (info.has("Membership")) {
                        user.setMembership(Html.fromHtml(info.getString("Membership")).toString());
                    }
                    if (info.has("Profile Views")) {
                        user.setProfile_views(Html.fromHtml(info.getString("Profile Views")).toString());
                    }
                    if (info.has("RSS Subscribers")) {
                        user.setRSS_Subscribers(Html.fromHtml(info.getString("RSS Subscribers")).toString());
                    }
                }

                // add membership item
                m = new Menu();
                m.setIdIcon(R.drawable.friend_icon);
                m.setPhrase("Membership");
                m.setHeader(false);
                m.setUser(false);
                m.setUrl("membership");
                sa.addItem(m);

                Object intervention = outputJson.get("cover_photo");
                if (intervention instanceof JSONObject) {
                    JSONObject coverJson = outputJson.getJSONObject("cover_photo");
                    user.setCoverPhoto(coverJson.getString("500"));
                } else {
                    user.setCoverPhoto(null);
                }
                // add dash board message friend to side bar
                sa.addHeader(phraseManager.getPhrase(getActivity()
                        .getApplicationContext(), "accountapi.favourite"));

                String[] menuItems = new String[]{
                        phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.news_feed"),
                        phraseManager.getPhrase(getActivity().getApplicationContext(), "mail.messages_title"),
                        phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.friends"),
                        "Invite Friends",
                        "Near me",
                        "Group Chat"
                };

                String[] menuItemsIcon = getResources().getStringArray(R.array.ns_menu_items_icon);
                int res = 0;

                // get id title icon
                int id_icon;
                for (String item : menuItems) {
                    id_icon = getResources().getIdentifier(menuItemsIcon[res], "drawable", getActivity().getPackageName());
                    Menu mItem = new Menu(item, id_icon, null, false);
                    if (res == 0) {
                        mItem.setUrl("dashboard");
                    } else if (res == 1) {
                        mItem.setCounter(notify.getInt("mail"));
                        mItem.setUrl("message");
                    } else if (res == 2) {
                        mItem.setUrl("friends");
                    } else if (res == 3) {
                        mItem.setUrl("invite");
                    } else if (res == 4) {
                        mItem.setUrl("NearMe");
                    } else if (res == 5) {
                        mItem.setUrl("GroupChat");
                    }

                    sa.addItem(mItem);
                    res++;
                }

                // invite friends
                /*
                String inviteTitle = "Invite Friends";
                String[] menuInviteIcon = getResources().getStringArray(R.array.ns_menu_items_icon);
                int id_icon = getResources().getIdentifier(menuInviteIcon[3], "drawable", getActivity().getPackageName());
                Menu inviteItem = new Menu(inviteTitle, id_icon, "invite", false);
                sa.addItem(inviteItem);
                */

                // set header for modules
                sa.addHeader(phraseManager.getPhrase(getActivity().getApplicationContext(), "admincp.modules"));

                for (int i = 0; i < appModJson.length(); i++) {
                    Menu menu = new Menu();
                    menuObj = appModJson.getJSONObject(appModJson.names().getString(i));
                    // set phrase
                    menu.setPhrase(Html.fromHtml(menuObj.getString("phrase")).toString());
                    // set icon url
                    id_icon = getResources().getIdentifier(menuObj.getString("module"), "drawable", getActivity().getPackageName());
                    if (id_icon != 0) {
                        menu.setIdIcon(id_icon);
                    } else {
                        menu.setIcon(menuObj.getString("icon"));
                    }
                    menu.setIsActive(menuObj.getString("module_is_active"));
                    menu.setUrl(menuObj.getString("url"));
                    menu.setLink(menuObj.getString("link"));
                    // add to side bar adapter
                    Log.d("psyh", "MENU getPhrase: " + menu.getPhrase());
                    if ("Music Sharing".equals(menu.getPhrase()) ||
                            "Messenger".equals(menu.getPhrase()) ||
                            "Businesses".equals(menu.getPhrase()) ||
                            "Apps".equals(menu.getPhrase()) ||
                            "Polls".equals(menu.getPhrase()) ||
                            "Quizzes".equals(menu.getPhrase())) {
                    } else {
                        sa.addItem(menu);
                    }
                }

                // if have pages --> add pages
                if (!outputJson.isNull("sidebar_item")) {
                    // set header for pages
                    JSONArray jsonPages = outputJson.getJSONArray("sidebar_item");
                    if (jsonPages.length() != 0) {
                        sa.addHeader(phraseManager.getPhrase(getActivity().getApplicationContext(), "pages.pages"));
                    }
                    for (int i = 0; i < jsonPages.length(); i++) {
                        Menu pageMenu = new Menu();
                        JSONObject pages = jsonPages.getJSONObject(i);
                        pageMenu.setPhrase(pages.getString("title"));
                        pageMenu.setIcon(pages.getString("icon_image"));
                        pageMenu.setPage_id(pages.getString("page_id"));
                        pageMenu.setUrl("page_item");
                        sa.addItem(pageMenu);
                    }
                }

                // add logout
                sa.addHeader("");

                //add notification settings
                id_icon = getResources().getIdentifier("notification_setting_icon", "drawable", getActivity().getPackageName());
                sa.addItem(new Menu(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.notification_settings"), id_icon, "notification_setting", false));

                // add account setting
                id_icon = getResources().getIdentifier("account_setting_icon", "drawable", getActivity().getPackageName());
                sa.addItem(new Menu(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.account_settings"), id_icon, "account_setting", false));

                // add edit profile
                id_icon = getResources().getIdentifier("account_setting_icon", "drawable", getActivity().getPackageName());
                sa.addItem(new Menu("Edit Profile", id_icon, "edit_profile", false));

                // add privacy settings
                id_icon = getResources().getIdentifier("account_setting_icon", "drawable", getActivity().getPackageName());
                sa.addItem(new Menu("Privacy Settings", id_icon, "privacy_setting", false));

                //add logout
                id_icon = getResources().getIdentifier("log_out_icon", "drawable", getActivity().getPackageName());
                sa.addItem(new Menu(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.logout"), id_icon, "logout", false));

                progressSidebar.setVisibility(View.GONE);

                if (sa != null) {
                    setListAdapter(sa);
                }
            } catch (Exception ex) {
                sa = null;
                // display error
                searchLayout.setVisibility(View.GONE);
                progressSidebar.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                noInternetTitle.setTextColor(Color.WHITE);
                ex.printStackTrace();
            }

            super.onPostExecute(result);
        }
    }

    /**
     * Create side bar adapter
     *
     * @author ducpham
     */
    public class SidebarAdapter extends ArrayAdapter<Menu> {

        public SidebarAdapter(Context context) {
            super(context, 0);
        }

        public void addHeader(String title) {
            add(new Menu(title, true));
        }

        public void addItem(String title, String url, String link, String icon,
                            String isActive, int counter, boolean isHeader) {
            add(new Menu(title, url, link, icon, isActive, counter, isHeader));
        }

        public void addItem(Menu menu) {
            add(menu);
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position).isUser())
                return 2;
            return getItem(position).isHeader() ? 0 : 1;
        }

        @Override
        public boolean isEnabled(int position) {
            return !getItem(position).isHeader();
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            Menu item = getItem(position);
            ViewHolder holder = null;
            View view = convertView;

            if (view == null) {
                int layout = R.layout.sidebar_row;
                // if is header
                if (item.isHeader()) {
                    layout = R.layout.sidebar_header;
                } else if (item.isUser()) {
                    layout = R.layout.sidebar_user_row;
                }

                view = LayoutInflater.from(getContext()).inflate(layout, null);

                TextView header = (TextView) view.findViewById(R.id.menurow_title);
                ImageView icon = (ImageView) view.findViewById(R.id.menurow_icon);
                TextView title = (TextView) view.findViewById(R.id.menurow_counter);
                view.setTag(new ViewHolder(header, icon, title));

            }

            if (holder == null && view != null) {
                Object tag = view.getTag();
                if (tag instanceof ViewHolder) {
                    holder = (ViewHolder) tag;
                }
            }

            if (item != null && holder != null) {
                if (holder.textHolder != null) {
                    if ((item.getPhrase() != null)) {
                        String phrase = item.getPhrase();
                        if ("Credits".equals(phrase))
                            phrase += "(" + String.valueOf(user.getCredits()) + ")";
                        else if ("Membership".equals(phrase))
                            phrase += ": " + user.getMembership();

                        holder.textHolder.setText(phrase);
                    } else {
                        holder.textHolder.setText(item.getIdTitle());
                    }
                }

                if (holder.textCounterHolder != null) {
                    if (item.getCounter() > 0) {
                        holder.textCounterHolder.setVisibility(View.VISIBLE);
                        holder.textCounterHolder.setText("" + item.getCounter());
                    } else {
                        holder.textCounterHolder.setVisibility(View.GONE);
                    }
                }

                if (holder.imageHolder != null) {
                    if (!"".equals(item.getIcon())) {
                        holder.imageHolder.setVisibility(View.VISIBLE);
                        if (item.getIcon() != null) {
                            networkUntil.drawImageUrl(holder.imageHolder, item.getIcon(), R.drawable.loading);
                        } else {
                            holder.imageHolder.setImageResource(item.getIdIcon());
                        }
                    } else {
                        holder.imageHolder.setVisibility(View.GONE);
                    }
                }
            }

            return view;

        }
    }

    /**
     * Class View holder
     *
     * @author ducpham
     */
    public static class ViewHolder {
        public final TextView textHolder;
        public final ImageView imageHolder;
        public final TextView textCounterHolder;

        public ViewHolder(TextView header, ImageView icon, TextView title) {
            this.textHolder = header;
            this.imageHolder = icon;
            this.textCounterHolder = title;
        }
    }

    public class loadDefaultLanguage extends AsyncTask<String, Void, String> {
        private Context context;

        public loadDefaultLanguage(Context context) {
            this.context = context;

        }

        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject settingJSON = new JSONObject(result);
                    if (settingJSON.has("phrases")) {
                        phraseManager.saveJSONObject(this.context, settingJSON.getJSONObject("phrases"));
                    }
                    //set color
                    user.setColor(settingJSON.getString("color_app"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(String... param) {
            if (isCancelled()) {
                return null;
            }
            try {
                // get check key url
                String url_checkKey = Config.CORE_URL + Config.URL_GET_SETTING;

                // Use BasicNameValuePair to store POST data

                String resultstring = networkUntil.makeHttpRequest(url_checkKey, "GET", null);
                return resultstring;

            } catch (Exception ex) {

            }

            return null;
        }
    }
}
