package com.brodev.socialapp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.AlbumStorageDirFactory;
import com.brodev.socialapp.android.BaseAlbumDirFactory;
import com.brodev.socialapp.android.FroyoAlbumDirFactory;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.asyncTask.UnFriendsAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.manager.NextActivity;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Feed;
import com.brodev.socialapp.entity.FeedMini;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.UserProfile;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.AlbumSelectedActivity;
import com.brodev.socialapp.view.ChatActivity;
import com.brodev.socialapp.view.CheckInActivity;
import com.brodev.socialapp.view.ComposeActivity;
import com.brodev.socialapp.view.EventDetailActivity;
import com.brodev.socialapp.view.FriendActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.brodev.socialapp.view.ImagePagerActivity;
import com.brodev.socialapp.view.ImageUpload;
import com.brodev.socialapp.view.ProfilePicActivity;
import com.brodev.socialapp.view.ProfilePicUtil;
import com.brodev.socialapp.view.ShareActivity;
import com.brodev.socialapp.view.VideoActivity;
import com.brodev.socialapp.view.VoiceActivity;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserFragment extends SherlockListFragment {

    private NetworkUntil networkUntil = new NetworkUntil();
    private FeedAdapter fa = null;
    private int page;
    private String sUserId;
    private int currentpos;
    private User user;
    private UserProfile objProfile;
    private String sPagesId;
    private JSONObject callback;
    private final int SELECT_FILE = 1;
    private final int REQUEST_CAMERA = 0;
    private Button actionFriend, MessageBtn;
    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;
    private static final String TIME = "time_phrase";
    private static final String FULLNAME = "full_name";
    private static final String ICON = "feed_icon";
    private static final String TITLE = "title_phrase_html";
    private static final String IMAGE = "feed_image";
    private static final String USERIMAGE = "user_image";
    private RelativeLayout share_button, photo_button, checkInButton, noInternetLayout;
    private TextView statusTxt, photoTxt, checkInTxt, noInternetTitle, noInternetContent;
    private ImageView shareImage, photoImage, noInternetImg;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private String mCurrentPhotoPath;
    private boolean bNotice;
    private String colorCode;
    private Button noInternetBtn;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    ProfilePicUtil profilePicUtil = new ProfilePicUtil();
    Bitmap bitmap;

    // phrase manager
    private PhraseManager phraseManager;
    private ColorView colorView;
    private ImageGetter imageGetter;

    private File setUpPhotoFile() throws IOException {
        File f = profilePicUtil.createImageFile(getActivity().getApplicationContext(), mAlbumStorageDirFactory);
        mCurrentPhotoPath = f.getAbsolutePath();
        return f;
    }

    private void handleBigCameraPhoto() {
        if (mCurrentPhotoPath != null) {
            bitmap = profilePicUtil.setPic(getActivity().getApplicationContext(), bitmap, null, mCurrentPhotoPath);
            profilePicUtil.galleryAddPic(getActivity().getApplicationContext(), mCurrentPhotoPath);
            mCurrentPhotoPath = null;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init value
        sPagesId = null;
        callback = new JSONObject();
        sUserId = null;
        objProfile = new UserProfile();
        bitmap = null;
        currentpos = 0;
        page = 1;
        bNotice = false;
        user = (User) getActivity().getApplicationContext();
        Bundle extras = getActivity().getIntent().getExtras();
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());
        imageGetter = new ImageGetter(getActivity().getApplicationContext());

        colorCode = colorView.getColorCode(getActivity(), user);

        if (extras != null) {
            if (getActivity().getIntent().hasExtra("user_id")) {
                sUserId = extras.getString("user_id");
            } else if (getActivity().getIntent().hasExtra("page_id")) {
                sPagesId = extras.getString("page_id");
            }
        }
    }

    private void loadUserInfo() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                mPullRefreshListView.setVisibility(View.VISIBLE);

                //fetch data
                if (sUserId != null) {
                    new ProfileTask().execute(sUserId);
                } else if (sPagesId != null) {
                    new ProfileTask().execute(sPagesId);
                } else {
                    new FeedLoadMoreTask().execute(page);
                }
            } else {
                // display error
                mPullRefreshListView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mPullRefreshListView.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        actualListView = mPullRefreshListView.getRefreshableView();
        try {
            loadUserInfo();
        } catch (Exception ex) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // create view from feed_fragment xml
        View view = inflater.inflate(R.layout.list_user_view, container, false);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.user_fragment_list);

        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                // call feed refresh task to execute
                new FeedLoadMoreTask().execute(page);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do the work to load more items at the end of list
                // here
                ++page;
                new FeedLoadMoreTask().execute(page);
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
                        //fetch data
                        loadUserInfo();
                    }
                }, 1000);
            }
        });

        return view;
    }

    public class ProfileTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = null;

            if (isCancelled()) {
                return null;
            }

            try {
                // String url
                String URL = null;
                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), null, false);
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, null, false);
                }
                Log.i("like URL: ", URL);
                // Use BasicNameValuePair to store POST data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));

                if (sUserId != null) {
                    pairs.add(new BasicNameValuePair("method", "accountapi.getUserInfo"));
                    pairs.add(new BasicNameValuePair("user_id", "" + params[0]));
                } else if (sPagesId != null) {
                    pairs.add(new BasicNameValuePair("method", "accountapi.finitem"));
                    pairs.add(new BasicNameValuePair("module", "pages"));
                    pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
                }

                result = networkUntil.makeHttpRequest(URL, "GET", pairs);

            } catch (Exception ex) {
                return null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject mainJSON = new JSONObject(result);

                    JSONObject outputJson = mainJSON.getJSONObject("output");

                    objProfile = new UserProfile();

                    if (outputJson.has("full_name")) {
                        objProfile.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
                    }

                    // bronislaw
                    if (outputJson.has("user_name")) {
                        objProfile.setUsername(outputJson.getString("user_name"));
                    }

                    if (outputJson.has("quickbloxid")) {
                        objProfile.setQuickbloxID(outputJson.getString("quickbloxid"));
                    }

                    if (outputJson.has("profile_page_id")) {
                        objProfile.setProfile_page_id(Html.fromHtml(outputJson.getString("profile_page_id")).toString());
                    }

                    if (outputJson.has("user_id")) {
                        objProfile.setUser_id(Html.fromHtml(outputJson.getString("user_id")).toString());
                    }

                    if (outputJson.has("dob_setting")) {
                        objProfile.setDob(Html.fromHtml(outputJson.getString("dob_setting")).toString());
                    }

                    if (outputJson.has("location_phrase")) {
                        objProfile.setLocation(Html.fromHtml(outputJson.getString("location_phrase")).toString());
                    } else {
                        objProfile.setLocation(null);
                    }

                    if (outputJson.has("birthday_phrase") && !"false".equals(outputJson.getString("birthday_phrase"))) {
                        objProfile.setBirthday(Html.fromHtml(outputJson.getString("birthday_phrase")).toString());
                    } else {
                        objProfile.setBirthday(null);
                    }

                    if (outputJson.has("photo_120px_square")) {
                        objProfile.setUserImage(outputJson.getString("photo_120px_square"));
                    }

                    if (outputJson.has("photo_200px_square")) {
                        objProfile.setUserImage(outputJson.getString("photo_200px_square"));
                    }

                    if (outputJson.has("Relationship Status")) {
                        objProfile.setRelationship_status(outputJson.getString("Relationship Status"));
                    }

                    if (outputJson.has("sexuality")) {
                        objProfile.setSexuality(outputJson.getString("sexuality"));
                    }

                    if (outputJson.has("cover_photo")) {
                        Object intervention = outputJson.get("cover_photo");

                        if (intervention instanceof JSONObject) {
                            JSONObject coverJson = outputJson.getJSONObject("cover_photo");
                            objProfile.setCoverPhoto(coverJson.getString("500"));
                        } else {
                            objProfile.setCoverPhoto(null);
                        }
                    }

                    // get data pages

                    if (outputJson.has("title")) {
                        objProfile.setTitle(Html.fromHtml(outputJson.getString("title")).toString());
                    }

                    if (outputJson.has("category_name")) {
                        objProfile.setCategory(Html.fromHtml(outputJson.getString("category_name")).toString());
                    }

                    if (outputJson.has("photo_sizes")) {
                        Object intervention = outputJson.get("photo_sizes");

                        if (intervention instanceof JSONObject) {
                            JSONObject coverJson = outputJson.getJSONObject("photo_sizes");
                            objProfile.setPagesImage(coverJson.getString("120"));
                        } else {
                            objProfile.setPagesImage(null);
                        }
                    }

                    if (outputJson.has("feed_callback")) {
                        Object intervention = outputJson.get("feed_callback");

                        if (intervention instanceof JSONObject) {
                            callback = outputJson.getJSONObject("feed_callback");
                        }
                    }
                    if (outputJson.has("is_friend")) {
                        objProfile.setIs_friend(outputJson.get("is_friend").toString());

                    }

                    if (outputJson.has("is_friend_request") && !outputJson.isNull("is_friend_request")) {
                        Log.d("psyh", "is_friend_request" + outputJson.getString("is_friend_request"));
                        if (outputJson.getString("is_friend_request").equals("2")) {
                            objProfile.setIs_friend("2");
                            Log.d("psyh", "objProfileInfo.getIs_friend" + objProfile.getIs_friend());
                        }
                    }

                    if (outputJson.has("total_friend")) {
                        objProfile.setTotal_friend(outputJson.getString("total_friend"));
                    }
                    if (outputJson.has("total_like")) {
                        objProfile.setTotal_like(outputJson.getString("total_like"));
                    }

                    if (outputJson.has("is_liked") && !outputJson.isNull("is_liked")) {
                        objProfile.setIs_liked(outputJson.getString("is_liked"));
                    }
                    // get feed item
                    new FeedLoadMoreTask().execute(page);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    /**
     * get feed list of logged user
     *
     * @author datnguyen
     */
    public String getResultFeedGet(int page) {

        String resultstring;
        String URL = null;

        if (sPagesId != null) {

            if (Config.CORE_URL == null) {
                URL = Config.makeUrl(user.getCoreUrl(), "getFeed", true) + "&token=" + user.getTokenkey() + "&page=" + page;
            } else {
                URL = Config.makeUrl(Config.CORE_URL, "getFeed", true) + "&token=" + user.getTokenkey() + "&page=" + page;
            }

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("callback", "" + callback));

            resultstring = networkUntil.makeHttpRequest(URL, "POST", pairs);
        } else {

            // Use BasicNameValuePair to create GET data
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
            pairs.add(new BasicNameValuePair("method", "accountapi.getFeed"));

            if (sUserId != null) {
                pairs.add(new BasicNameValuePair("user_id", "" + sUserId));
            } else {
                pairs.add(new BasicNameValuePair("user_id", "" + user.getUserId()));

            }
            pairs.add(new BasicNameValuePair("page", "" + page));

            // url request
            if (Config.CORE_URL == null) {
                URL = Config.makeUrl(user.getCoreUrl(), null, false);
            } else {
                URL = Config.makeUrl(Config.CORE_URL, null, false);
            }

            // request GET method to server

            resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
        }

        Log.i("pages FEED", resultstring);

        return resultstring;
    }

    /**
     * Function create feed adapter
     *
     * @return feed Adapter
     */
    public FeedAdapter getFeedAdapter(FeedAdapter fadapter, String resString) {

        if (resString != null) {
            try {
                // init feed adapter

                JSONObject mainJSON = new JSONObject(resString);
                JSONArray outJson = mainJSON.getJSONArray("output");

                // if output json is null
                if (outJson.length() == 0 && page == 1) {
                    Feed _feed = new Feed();
                    _feed.setNo_share(true);
                    _feed.setNotice(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.there_are_no_new_feeds_to_view_at_this_time"));
                    fadapter.add(_feed);
                }

                JSONObject pagesObj = null;
                JSONObject userObj = null;
                for (int i = 0; i < outJson.length(); i++) {
                    pagesObj = outJson.getJSONObject(i);

                    Feed objFeed = new Feed();
                    objFeed.setFeedId(pagesObj.getString("feed_id"));
                    if (pagesObj.has("item_id")) {
                        objFeed.setItemId(pagesObj.getString("item_id"));
                    }

                    objFeed.setFullName(pagesObj.getString(FULLNAME));
                    objFeed.setUserId(pagesObj.getString("user_id"));
                    objFeed.setTime(pagesObj.getString(TIME));

                    if (pagesObj.has(ICON)) {
                        objFeed.setIcon(pagesObj.getString(ICON));
                    }

                    if (pagesObj.has(TITLE) && !pagesObj.isNull(TITLE)) {
                        objFeed.setTitle(pagesObj.getString(TITLE));
                    } else {
                        objFeed.setTitle(pagesObj.getString("title_phrase"));
                    }

                    if (pagesObj.has("parent_user") && !pagesObj.isNull("parent_user")) {
                        userObj = pagesObj.getJSONObject("parent_user");
                        if (userObj.has("parent_full_name") && !userObj.isNull("parent_full_name")) {
                            objFeed.setTitle(pagesObj.getString(TITLE) + " &raquo; " + "<b><font color=\"" + colorCode + "\">" + userObj.getString("parent_full_name") + "</font></b>");
                        }
                    }

                    objFeed.setUserImage(pagesObj.getString(USERIMAGE));

                    if (pagesObj.has("no_share")) {
                        objFeed.setNo_share(pagesObj.getBoolean("no_share"));
                    } else {
                        objFeed.setNo_share(false);
                    }

                    if (pagesObj.has("feed_title") && !"false".equals(pagesObj.getString("feed_title"))) {
                        objFeed.setTitleFeed(Html.fromHtml(pagesObj.getString("feed_title")).toString());
                    }

                    objFeed.setFeedLink(pagesObj.getString("feed_link"));
                    if (pagesObj.has("parent_module_id")
                            && !pagesObj.isNull("parent_module_id")) {
                        objFeed.setModule(pagesObj.getString("parent_module_id"));
                    }

                    if (pagesObj.has("enable_like")) {
                        if (!pagesObj.isNull("feed_is_liked") && !"false".equals(pagesObj.getString("feed_is_liked"))) {
                            objFeed.setFeedIsLiked("feed_is_liked");
                        }
                        objFeed.setEnableLike(pagesObj.getBoolean("enable_like"));
                    } else {
                        objFeed.setEnableLike(false);
                    }

                    if (pagesObj.has("can_post_comment")) {
                        objFeed.setCanPostComment(pagesObj.getBoolean("can_post_comment"));
                    } else {
                        objFeed.setCanPostComment(false);
                    }

                    if (pagesObj.has("comment_type_id")) {
                        objFeed.setComment_type_id(pagesObj.getString("comment_type_id"));
                    }

                    if (pagesObj.has("total_comment")) {
                        objFeed.setTotalComment(pagesObj.getString("total_comment"));
                    }

                    if (pagesObj.has("profile_page_id")) {
                        objFeed.setProfile_page_id(pagesObj.getString("profile_page_id"));
                    }

                    if (pagesObj.has("feed_total_like") && !"null".equals(pagesObj.getString("feed_total_like"))) {
                        objFeed.setHasLike(pagesObj.getString("feed_total_like"));
                        objFeed.setTotalLike(Integer.parseInt(pagesObj.getString("feed_total_like")));
                    }

                    if (pagesObj.has("feed_status")) {
                        objFeed.setStatus(pagesObj.getString("feed_status"));
                    }

                    if (pagesObj.has("feed_status_html")) {
                        objFeed.setStatus(pagesObj.getString("feed_status_html"));
                    }

                    // get more info for link...

                    if (pagesObj.has("feed_title_extra")) {
                        objFeed.setFeedTitleExtra(Html.fromHtml(pagesObj.getString("feed_title_extra")).toString());
                    }

                    if (pagesObj.has("feed_content")) {
                        objFeed.setFeedContent(pagesObj.getString("feed_content"));
                    }

                    if (pagesObj.has("feed_content_html")) {
                        objFeed.setFeedContent(pagesObj.getString("feed_content_html"));
                    }

                    objFeed.setType(pagesObj.getJSONObject("social_app").getString("type_id"));

                    if (pagesObj.has("can_share_item_on_feed")) {
                        objFeed.setCan_share_item_on_feed(pagesObj.getBoolean("can_share_item_on_feed"));
                    }

                    if (pagesObj.has("like_type_id")) {
                        objFeed.setLikeTypeId(pagesObj.getString("like_type_id"));
                    }

                    if (pagesObj.has("like_item_id")) {
                        objFeed.setLikeItemId(pagesObj.getString("like_item_id"));
                    }

                    if (pagesObj.has("feed_link_share") && !pagesObj.isNull("feed_link_share"))
                        objFeed.setShareFeedLink(Html.fromHtml(pagesObj.getString("feed_link_share")).toString());

                    if (pagesObj.has("feed_link_share_url") && !pagesObj.isNull("feed_link_share_url"))
                        objFeed.setShareFeedLinkUrl(Html.fromHtml(pagesObj.getString("feed_link_share_url")).toString());

                    if (pagesObj.has("custom_data_cache")) {
                        if (pagesObj.getJSONObject("custom_data_cache").has("thread_id")
                                && !pagesObj.getJSONObject("custom_data_cache").isNull("thread_id"))
                            objFeed.setDataCacheId(pagesObj.getJSONObject("custom_data_cache").getString("thread_id"));
                    }

                    if (pagesObj.has("social_app")) {
                        JSONObject socialObj = pagesObj.getJSONObject("social_app");
                        Object intervention = socialObj.get("link");

                        if (intervention instanceof JSONObject) {
                            JSONObject requestObj = socialObj.getJSONObject("link").getJSONObject("request");

                            if (requestObj.has("page_id")) {
                                objFeed.setPage_id_request(requestObj.getString("page_id"));
                            } else if (requestObj.has("user_id")) {
                                objFeed.setUser_id_request(requestObj.getString("user_id"));
                            } else if (requestObj.has("photo_id")) {
                                objFeed.setPhoto_id_request(requestObj.getString("photo_id"));
                            }

                        }

                    }

                    if (!pagesObj.isNull(IMAGE)) {

                        Object feedImageObj = pagesObj.get(IMAGE);
                        if (feedImageObj instanceof JSONArray) {
                            ArrayList<String> Images_feed = new ArrayList<String>();

                            for (int m = 0; m < pagesObj.getJSONArray(IMAGE).length(); m++) {
                                Images_feed.add(pagesObj.getJSONArray(IMAGE).getString(m));
                                if (m == 0) {
                                    objFeed.setImage1(pagesObj.getJSONArray(IMAGE).getString(m));
                                } else if (m == 1) {
                                    objFeed.setImage2(pagesObj.getJSONArray(IMAGE).getString(m));
                                } else if (m == 2) {
                                    objFeed.setImage3(pagesObj.getJSONArray(IMAGE).getString(m));
                                } else if (m == 3) {
                                    objFeed.setImage4(pagesObj.getJSONArray(IMAGE).getString(m));
                                }
                                objFeed.setFeed_Image(Images_feed);
                            }
                        }
                    }

                    if (pagesObj.has("photos_id") && !pagesObj.isNull("photos_id")) {

                        Object photoIdsObj = pagesObj.get("photos_id");
                        if (photoIdsObj instanceof JSONArray) {
                            ArrayList<String> Images_id = new ArrayList<String>();
                            for (int s = 0; s < pagesObj.getJSONArray("photos_id").length(); s++) {
                                Images_id.add(pagesObj.getJSONArray("photos_id").getString(s));
                                if (s == 0) {
                                    objFeed.setImage_id_1(pagesObj.getJSONArray("photos_id").getString(s));
                                } else if (s == 1) {
                                    objFeed.setImage_id_2(pagesObj.getJSONArray("photos_id").getString(s));
                                } else if (s == 2) {
                                    objFeed.setImage_id_3(pagesObj.getJSONArray("photos_id").getString(s));
                                } else if (s == 3) {
                                    objFeed.setImage_id_4(pagesObj.getJSONArray("photos_id").getString(s));
                                }
                            }
                            objFeed.setImagesId(Images_id);
                        }
                    }

                    // if have share feed
                    if (pagesObj.has("share_feed")) {
                        Object intervention = pagesObj.get("share_feed");

                        if (intervention instanceof JSONObject) {
                            JSONObject shareObj = (JSONObject) intervention;
                            FeedMini feedMini = new FeedMini();

                            if (shareObj.has("full_name")) {
                                feedMini.setFullname(Html.fromHtml(shareObj.getString("full_name")).toString());
                            }

                            if (shareObj.has("feed_info")) {
                                feedMini.setFeedInfo(Html.fromHtml(shareObj.getString("feed_info")).toString());
                            }

                            if (shareObj.has("feed_status") && !shareObj.isNull("feed_status") && !"".equals(shareObj.getString("feed_status"))) {
                                feedMini.setFeedStatus(Html.fromHtml(shareObj.getString("feed_status")).toString());
                            }

                            if (shareObj.has("feed_title") && !shareObj.isNull("feed_title") && !"".equals(shareObj.getString("feed_title"))) {
                                feedMini.setFeedTitle(Html.fromHtml(shareObj.getString("feed_title")).toString());
                            }

                            if (shareObj.has("feed_image") && !shareObj.isNull("feed_image") && !"".equals(shareObj.getString("feed_image"))) {
                                feedMini.setFeedImage(shareObj.getString("feed_image"));
                            }
                            feedMini.setModule(pagesObj.getString("parent_module_id"));
                            objFeed.setFeedMini(feedMini);

                        }
                    }
                    if (pagesObj.has("location_img") && !pagesObj.isNull("location_img")) {
                        objFeed.setLocationImg(pagesObj.getString("location_img"));
                    }

                    if (pagesObj.has("location_link") && !pagesObj.isNull("location_link")) {
                        objFeed.setLocationLink(pagesObj.getString("location_link"));
                    }

                    fadapter.add(objFeed);

                }
            } catch (Exception ex) {
                ex.printStackTrace();
                mPullRefreshListView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                return null;
            }
        }
        return fadapter;

    }

    /**
     * Load more feed list of logged user
     *
     * @author datnguyen
     */
    public class FeedLoadMoreTask extends AsyncTask<Integer, Void, String> {
        String result = null;

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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            try {
                // get result from get method
                result = getResultFeedGet(params[0]);
            } catch (Exception ex) {
                return null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result != null) {
                    if (fa == null) {

                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.user_profile, getListView(), false);

                        statusTxt = (TextView) header.findViewById(R.id.statusTxt);
                        statusTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.status"));

                        photoTxt = (TextView) header.findViewById(R.id.photoTxt);
                        photoTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.photo"));

                        checkInTxt = (TextView) header.findViewById(R.id.checkinTxt);
                        checkInTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.check_in"));

                        final ImageView profile_image = (ImageView) header.findViewById(R.id.profile_image);

                        TextView profile_fullname = (TextView) header.findViewById(R.id.profile_fullname);
                        TextView profile_sexuality = (TextView) header.findViewById(R.id.profile_sexuality);
                        TextView profile_location = (TextView) header.findViewById(R.id.profile_location);
                        TextView profile_birthday = (TextView) header.findViewById(R.id.profile_birthday);
                        ImageView profile_cover = (ImageView) header.findViewById(R.id.item_img_cover);
                        share_button = (RelativeLayout) header.findViewById(R.id.share_button);
                        photo_button = (RelativeLayout) header.findViewById(R.id.photo_button);
                        checkInButton = (RelativeLayout) header.findViewById(R.id.checkin_button);

                        if (sPagesId == null && Boolean.parseBoolean(user.getCheckin()) && sUserId.equals(user.getUserId())) {
                            checkInButton.setVisibility(View.VISIBLE);
                        } else {
                            checkInButton.setVisibility(View.GONE);
                        }

                        actionFriend = (Button) header.findViewById(R.id.profile_actionFriend);
                        MessageBtn = (Button) header.findViewById(R.id.profile_MessageBtn);

                        if (!"".equals(objProfile.getIs_friend())) {
                            if ("false".equals(objProfile.getIs_friend())) {
                                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.add_to_friends"));
                            } else if ("3".equals(objProfile.getIs_friend())) {
                                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.confirm_friend_request"));
                            } else if ("2".equals(objProfile.getIs_friend())) {
                                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.pending_friend_confirmation"));
                            } else {
                                actionFriend.setText("Unfriend");
                                /*
                                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.friends") + " ("
                                        + objProfile.getTotal_friend() + ")");
                                */
                            }

                            actionFriend.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    if (!"".equals(objProfile.getIs_friend())) {
                                        if ("false".equals(objProfile.getIs_friend())) {
                                            new addFriend().execute();
                                            actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.pending_friend_confirmation"));
                                            objProfile.setIs_friend("2");
                                        } else if ("3".equals(objProfile.getIs_friend())) {

                                        } else if ("2".equals(objProfile.getIs_friend())) {

                                        } else {
                                            Log.d("psyh", "sUserId:" + sUserId);
                                            Log.d("psyh", "objProfile.getUser_id():" + objProfile.getUser_id());
                                            Log.d("psyh", "user.getUserId()):" + user.getUserId());

                                            new UnFriendsAsyncTask(getActivity()).execute(user.getUserId(), sUserId);
                                            actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.add_to_friends"));
                                            objProfile.setIs_friend("false");
                                            /*
                                            Intent intent = new Intent(getActivity(), FriendActivity.class);
                                            intent.putExtra("user_id", objProfile.getUser_id());
                                            intent.putExtra("is_profile", true);
                                            getActivity().startActivity(intent);
                                            */
                                        }

                                    }

                                }

                            });
                        }
                        MessageBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "friend.message"));

                        MessageBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (objProfile.getIs_friend() != null && "true".equals(objProfile.getIs_friend())) {
                                    Intent intent = null;
                                    if (user.getChatKey() != null) {
                                        intent = new Intent(getActivity(), ChatActivity.class);
                                        intent.putExtra("image", objProfile.getUserImage());
                                    } else {
                                        intent = new Intent(getActivity(), ComposeActivity.class);
                                    }

                                    intent.putExtra("fullname", objProfile.getFullname());
                                    intent.putExtra("user_id", objProfile.getUser_id());

                                    if (intent != null) {
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getActivity().startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            phraseManager.getPhrase(getActivity().getApplicationContext(), "mail.unable_to_send_a_private_message_to_this_user_at_the_moment"),
                                            Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                        //bronislaw
                        header.findViewById(R.id.chatImg).setOnClickListener(new ChatListener());
                        header.findViewById(R.id.videochatImg).setOnClickListener(new ChatListener());
                        header.findViewById(R.id.audiochatImg).setOnClickListener(new ChatListener());

                        if ((sUserId == null && sPagesId == null) || (sUserId != null && sUserId.equals(user.getUserId()))) {
                            actionFriend.setVisibility(View.GONE);
                            MessageBtn.setVisibility(View.GONE);
                        }

                        shareImage = (ImageView) header.findViewById(R.id.image_icon_status);
                        photoImage = (ImageView) header.findViewById(R.id.image_icon_photo);
                        share_button.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                statusTxt.setTextColor(Color.parseColor("#ffffff"));
                                colorView.changeColorAction(share_button, user.getColor());
                                shareImage.setImageResource(R.drawable.status_white_icon);
                                Intent intent = new Intent(getActivity(), ImageUpload.class);
                                if (sUserId != null || sPagesId != null) {
                                    if (sPagesId != null) {
                                        intent.putExtra("page_id", sPagesId);
                                        intent.putExtra("owner_user_id", objProfile.getUser_id());
                                        intent.putExtra("page_title", objProfile.getTitle());
                                        intent.putExtra("fullname", objProfile.getFullname());
                                        intent.putExtra("profile_page_id", objProfile.getProfile_page_id());
                                        intent.putExtra("pages_image", objProfile.getPagesImage());
                                    } else {
                                        intent.putExtra("user_id", objProfile.getUser_id());
                                    }
                                } else {
                                    intent.putExtra("owner_user_id", user.getUserId());
                                }
                                getActivity().startActivity(intent);
                            }
                        });
                        photo_button.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), AlbumSelectedActivity.class);
                                photoTxt.setTextColor(Color.parseColor("#ffffff"));
                                colorView.changeColorAction(photo_button, user.getColor());
                                photoImage.setImageResource(R.drawable.photo_white_icon);
                                if (sUserId != null || sPagesId != null) {
                                    if (sPagesId != null) {
                                        intent.putExtra("page_id", sPagesId);
                                        intent.putExtra("owner_user_id", objProfile.getUser_id());
                                        intent.putExtra("page_title", objProfile.getTitle());
                                        intent.putExtra("fullname", objProfile.getFullname());
                                        intent.putExtra("profile_page_id", objProfile.getProfile_page_id());
                                        intent.putExtra("pages_image", objProfile.getPagesImage());
                                    } else {
                                        intent.putExtra("user_id", objProfile.getUser_id());
                                    }
                                } else {
                                    intent.putExtra("owner_user_id", user.getUserId());
                                }
                                getActivity().startActivity(intent);
                            }
                        });

                        checkInButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkInButton.setBackgroundColor(Color.parseColor("#f3f3f3"));
                                Intent intent = new Intent(getActivity(), CheckInActivity.class);
                                startActivity(intent);
                            }
                        });

                        if (sUserId != null || sPagesId != null) {
                            if (!"".equals(objProfile.getCoverPhoto()) && objProfile.getCoverPhoto() != null) {
                                profile_cover.setVisibility(View.VISIBLE);
                                networkUntil.drawImageUrl(profile_cover, objProfile.getCoverPhoto(), R.drawable.loading);
                                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) profile_image.getLayoutParams();
                                layoutParams.setMargins(
                                        layoutParams.rightMargin + 8,
                                        layoutParams.topMargin - 65,
                                        layoutParams.leftMargin,
                                        layoutParams.bottomMargin);
                                profile_image.setLayoutParams(layoutParams);
                            } else {
                                profile_cover.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 40));
                            }

                            if (sPagesId != null) {
                                if (objProfile.getIs_liked() == null) {
                                    actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
                                } else {
                                    actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
                                }

                                actionFriend.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        onActionLikeClick();
                                    }
                                });

                                MessageBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "search.members") + "("
                                        + objProfile.getTotal_like() + ")");

                                MessageBtn.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), FriendActivity.class);
                                        intent.putExtra("type", "pages");
                                        intent.putExtra("item_id", sPagesId);
                                        getActivity().startActivity(intent);
                                    }
                                });
                                profile_image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                networkUntil.drawImageUrl(profile_image, objProfile.getPagesImage(), R.drawable.loading);
                                profile_fullname.setText(Html.fromHtml(objProfile.getTitle()).toString());
                                profile_sexuality.setText(Html.fromHtml(objProfile.getSexuality()).toString());
                                profile_location.setText(Html.fromHtml(objProfile.getCategory()).toString());
                            } else {
                                networkUntil.drawImageUrl(profile_image, objProfile.getUserImage(), R.drawable.loading);
                                profile_fullname.setText(objProfile.getFullname());
                                profile_sexuality.setText(objProfile.getSexuality());
                                profile_location.setText(objProfile.getLocation());
                                profile_birthday.setText(objProfile.getBirthday());

                            }

                        } else {

                            if (!"".equals(user.getCoverPhoto()) && user.getCoverPhoto() != null) {
                                profile_cover.setVisibility(View.VISIBLE);
                                networkUntil.drawImageUrl(profile_cover, user.getCoverPhoto(), R.drawable.loading);
                                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) profile_image.getLayoutParams();
                                layoutParams.setMargins(
                                        layoutParams.rightMargin + 8,
                                        layoutParams.topMargin - 65,
                                        layoutParams.leftMargin,
                                        layoutParams.bottomMargin);
                                profile_image.setLayoutParams(layoutParams);
                            } else {
                                profile_cover.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 40));
                                profile_image.setVisibility(View.VISIBLE);
                            }

                            networkUntil.drawImageUrl(profile_image, user.getUserImage(), R.drawable.loading);
                            profile_fullname.setText(user.getFullname());
                            profile_location.setText(user.getLocation());
                            profile_birthday.setText(user.getBirthday());
                        }

                        // action change profile picture
                        if (sPagesId == null && (sUserId == null || (sUserId != null && sUserId.equals(user.getUserId())))) {
                            profile_image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showDialogEditPic();
                                }
                            });
                        }

                        getListView().addHeaderView(header, null, false);
                    }

                    if (page == 1 || fa == null) {
                        fa = new FeedAdapter(getActivity());
                    }

                    fa = getFeedAdapter(fa, result);

                    if (fa != null) {
                        if (page == 1) {
                            actualListView.setAdapter(fa);
                        } else {
                            currentpos = getListView().getFirstVisiblePosition();
                            actualListView.setAdapter(fa);
                            getListView().setSelectionFromTop(currentpos + 1, 0);
                        }
                    }

                    fa.notifyDataSetChanged();
                }
                mPullRefreshListView.onRefreshComplete();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.onPostExecute(result);
        }

    }

    /**
     * Quickblox messenger and video/audio
     */
    //bronislaw
    class ChatListener implements View.OnClickListener {

        ChatListener() {}

        @Override
        public void onClick(View v) {
                Intent intent = null;

                switch (v.getId()) {
                    case R.id.chatImg:
                        intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("fullname", objProfile.getFullname());
                        intent.putExtra("user_id", objProfile.getUser_id());
                        intent.putExtra("image", objProfile.getUserImage());
                        intent.putExtra("username", objProfile.getUsername());
                        intent.putExtra("quickbloxid", objProfile.getQuickbloxID());
                        break;
                    case R.id.videochatImg:
                        intent = new Intent(getActivity(), VideoActivity.class);
                        intent.putExtra("fullname", objProfile.getFullname());
                        intent.putExtra("quickbloxid", objProfile.getQuickbloxID());
                        intent.putExtra("calltype", "video_type");
                        break;
                    case R.id.audiochatImg:
                        intent = new Intent(getActivity(), VoiceActivity.class);
                        intent.putExtra("fullname", objProfile.getFullname());
                        intent.putExtra("quickbloxid", objProfile.getQuickbloxID());
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

    protected void onActionLikeClick() {
        if (objProfile.getIs_liked() == null) {
            actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
            new ActionLike().execute(sPagesId, "pages", null, "like");
            objProfile.setIs_liked("1");
        } else {
            actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
            new ActionLike().execute(sPagesId, "pages", null, "unlike");
            objProfile.setIs_liked(null);
        }
        fa.notifyDataSetChanged();
    }

    public class ActionLike extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String likerequest = null;

            if (isCancelled()) {
                return null;
            }

            try {
                // Use BasicNameValuePair to create GET data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));

                if (("like").equals(params[3])) {
                    pairs.add(new BasicNameValuePair("method", "accountapi.like"));
                } else {
                    pairs.add(new BasicNameValuePair("method", "accountapi.unlike"));
                }

                if (params[1] != null) {
                    pairs.add(new BasicNameValuePair("type", "" + params[1]));
                    pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
                } else {
                    pairs.add(new BasicNameValuePair("feed_id", "" + params[2]));
                }

                // url request
                String URL = null;
                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), null, false);
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, null, false);
                }

                // request GET method to server
                likerequest = networkUntil.makeHttpRequest(URL, "GET", pairs);
                Log.i("like pages", likerequest);
            } catch (Exception ex) {
                //Log.i(DEBUG_TAG, ex.getMessage());
            }
            return likerequest;
        }


    }

    /**
     * Show dialog change profile picture
     */
    public void showDialogEditPic() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.change_picture, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "please wait ...", Toast.LENGTH_LONG).show();

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = null;
                    try {
                        f = setUpPhotoFile();
                        if (!f.exists()) {
                            Toast.makeText(getActivity().getApplicationContext(), "Error while capturing image", Toast.LENGTH_LONG).show();
                            return;
                        }
                        mCurrentPhotoPath = f.getAbsolutePath();
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    } catch (Exception ex) {
                        f = null;
                        mCurrentPhotoPath = null;
                    }
                    startActivityForResult(cameraIntent, REQUEST_CAMERA);
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_FILE);
                }
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent = new Intent(this.getActivity(), ProfilePicActivity.class);

        if (requestCode == SELECT_FILE) {
            Uri selectedImageUri = null;
            try {
                selectedImageUri = data.getData();
                intent.putExtra("uri_image", selectedImageUri.toString());
                //if (sUserId != null) intent.putExtra("user_id", sUserId);
                //  else if (sPagesId != null) intent.putExtra("page_id", sPagesId);

                getActivity().startActivity(intent);
                getActivity().finish();

            } catch (Exception ex) {
                selectedImageUri = null;
            }
        } else if (requestCode == REQUEST_CAMERA) {
            try {
                handleBigCameraPhoto();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (bitmap != null) {

                    bitmap.compress(CompressFormat.PNG, 75, stream);
                    byte[] byteArray = stream.toByteArray();

                    intent.putExtra("take_photo", byteArray);
                    //   if (sUserId != null) intent.putExtra("user_id", sUserId);
                    //   else if (sPagesId != null) intent.putExtra("page_id", sPagesId);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "error capture picture", Toast.LENGTH_LONG).show();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                bitmap = null;
            }
        }
    }

    public class addFriend extends AsyncTask<String, Void, String> {
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

                pairs.add(new BasicNameValuePair("user_id", objProfile.getUser_id()));

                likerequest = networkUntil.makeHttpRequest(URL, "POST", pairs);
                Log.i("psyh", likerequest);
                Log.i("add Friend", likerequest);
            } catch (Exception ex) {
                // Log.i(DEBUG_TAG, ex.getMessage());
            }
            return null;
        }

    }

    /**
     * Create feed browse adapter
     */
    public class FeedAdapter extends ArrayAdapter<Feed> {

        FeedViewHolder holder;

        public FeedAdapter(Context context) {
            super(context, 0);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final Feed item = getItem(position);
            holder = null;

            if (view == null) {
                int layout = R.layout.feed_item;

                view = LayoutInflater.from(getContext()).inflate(layout, null);
                // call element from xml

                ImageView iv = (ImageView) view.findViewById(R.id.grid_item_img);
                TextView Title = (TextView) view.findViewById(R.id.grid_item_fullname);
                TextView title_feed = (TextView) view.findViewById(R.id.grid_item_title);
                TextView feedStatus = (TextView) view.findViewById(R.id.grid_item_status);
                TextView Time = (TextView) view.findViewById(R.id.grid_item_time);
                ImageView icon = (ImageView) view.findViewById(R.id.grid_item_icon);
                ImageView feedimg_small = (ImageView) view.findViewById(R.id.grid_feedimg_small);
                ImageView feedimg = (ImageView) view.findViewById(R.id.grid_feedimg);
                ImageView feedimg1 = (ImageView) view.findViewById(R.id.grid_feedimg1);
                ImageView feedimg2 = (ImageView) view.findViewById(R.id.grid_feedimg2);
                ImageView feedimg3 = (ImageView) view.findViewById(R.id.grid_feedimg3);
                ImageView feedimg4 = (ImageView) view.findViewById(R.id.grid_feedimg4);
                TextView total_like = (TextView) view.findViewById(R.id.total_like);
                TextView total_comment = (TextView) view.findViewById(R.id.total_comment);
                ImageView like_icon = (ImageView) view.findViewById(R.id.grid_item_like_icon);
                ImageView comment_icon = (ImageView) view.findViewById(R.id.grid_item_comment_icon);
                LinearLayout like_view = (LinearLayout) view.findViewById(R.id.like_view_view);
                TextView like = (TextView) view.findViewById(R.id.like);
                TextView comment = (TextView) view.findViewById(R.id.comment);
                RelativeLayout like_view_like = (RelativeLayout) view.findViewById(R.id.like_view_like);
                RelativeLayout like_view_comment = (RelativeLayout) view.findViewById(R.id.like_view_comment);
                RelativeLayout like_view_share = (RelativeLayout) view.findViewById(R.id.like_view_share);

                TextView shareBtn = (TextView) view.findViewById(R.id.share);

                TextView notice = (TextView) view.findViewById(R.id.notice);

                // share feed item view
                RelativeLayout shareFeedView = (RelativeLayout) view.findViewById(R.id.share_feed_item_view);
                TextView titleFeedShare = (TextView) view.findViewById(R.id.share_feed_title_txt);
                TextView titleOfFeedShare = (TextView) view.findViewById(R.id.share_feed_title_of_feed_txt);
                TextView feedShareStatus = (TextView) view.findViewById(R.id.share_feed_status_txt);
                ImageView feedShareImage = (ImageView) view.findViewById(R.id.share_feed_image_view);

                // more info link feed
                TextView feedTitleExtra = (TextView) view.findViewById(R.id.grid_item_link_title_extra);
                TextView feedContent = (TextView) view.findViewById(R.id.grid_item_link_feed_content);

                // set phrase
                comment.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.comment"));
                shareBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.share"));
                RelativeLayout feedSmallLayout = (RelativeLayout) view.findViewById(R.id.grid_feed_item_small_layout);

                View feedLikeView = (View) view.findViewById(R.id.feed_like_view);
                LinearLayout feedLikeCommentLayout = (LinearLayout) view.findViewById(R.id.feed_like_comment_layout);

                ImageView commentImageView = (ImageView) view.findViewById(R.id.total_comment_image);

                ImageView likeImg = (ImageView) view.findViewById(R.id.like_icon_img);
                ImageView commentImg = (ImageView) view.findViewById(R.id.total_comment_image);

                ImageView feedLocationView = (ImageView) view.findViewById(R.id.grid_item_location);

                view.setTag(new FeedViewHolder(iv, Title, title_feed,
                        feedStatus, Time, total_like, total_comment, like_icon,
                        comment_icon, like_view, like, comment, icon,
                        feedimg_small, feedimg, feedimg1, feedimg2, feedimg3,
                        feedimg4, like_view_like, like_view_comment,
                        like_view_share, notice, shareBtn, shareFeedView,
                        titleFeedShare, titleOfFeedShare, feedShareStatus,
                        feedShareImage, feedTitleExtra, feedContent,
                        feedSmallLayout, feedLikeView, feedLikeCommentLayout,
                        commentImageView, likeImg, commentImg, feedLocationView));
            }

            if (holder == null && view != null) {
                Object tag = view.getTag();
                if (tag instanceof FeedViewHolder) {
                    holder = (FeedViewHolder) tag;
                }
            }

            if (item != null && holder != null) {
                //change color
                colorView.changeColorLikeCommnent(holder.likeImg, holder.commentImg, user.getColor());

                if (item.getNotice() != null) {
                    bNotice = true;
                    // invisible
                    view.findViewById(R.id.feed_item_layout_all).setVisibility(View.GONE);
                    // show notice
                    view.findViewById(R.id.notice_layout).setVisibility(View.VISIBLE);
                    holder.notice.setText(item.getNotice());
                    colorView.changeColorText(holder.notice, user.getColor());
                } else {
                    view.findViewById(R.id.feed_item_layout_all).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.notice_layout).setVisibility(View.GONE);
                }

                // set share feed item if have
                if (item.getFeedMini() != null) {
                    // visible share feed view
                    holder.shareFeedItemView.setVisibility(View.VISIBLE);
                    // set value to view
                    holder.titleFeedShare.setText(item.getFeedMini().getFullname() + " " + item.getFeedMini().getFeedInfo());
                    // set title share feed if have
                    if (item.getFeedMini().getFeedTitle() != null) {
                        holder.titleOfFeedShare.setVisibility(View.VISIBLE);
                        holder.titleOfFeedShare.setText(item.getFeedMini().getFeedTitle());
                    } else {
                        holder.titleOfFeedShare.setVisibility(View.GONE);
                    }
                    // set status of share feed if have
                    if (item.getFeedMini().getFeedStatus() != null) {
                        holder.feedShareStatus.setVisibility(View.VISIBLE);
                        holder.feedShareStatus.setText(item.getFeedMini().getFeedStatus());
                    } else {
                        holder.feedShareStatus.setVisibility(View.GONE);
                    }
                    // set share feed image if have
                    if (item.getFeedMini().getFeedImage() != null && item.getFeedMini().getModule().equals("photo")) {
                        holder.shareFeedImage.setVisibility(View.VISIBLE);
                        networkUntil.drawImageUrl(holder.shareFeedImage, item.getFeedMini().getFeedImage(), R.drawable.loading);
                    } else {
                        holder.shareFeedImage.setVisibility(View.GONE);
                    }
                } else {
                    holder.shareFeedItemView.setVisibility(View.GONE);
                }

                // set image user;
                if (holder.iv != null) {
                    if (!"".equals(item.getUserImage())) {
                        networkUntil.drawImageUrl(holder.iv, item.getUserImage(), R.drawable.loading);
                        holder.iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (item.getProfile_page_id() != null && !("0").equals(item.getProfile_page_id())) {
                                    Intent intent = new Intent(getActivity(), FriendTabsPager.class);
                                    intent.putExtra("page_id", item.getProfile_page_id());
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(getActivity(), FriendTabsPager.class);
                                    intent.putExtra("user_id", item.getUserId());
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }

                // set share
                if (holder.like_view_share != null && item.getNo_share() != true) {
                    holder.like_view_share.setVisibility(View.VISIBLE);
                    holder.shareBtn.setVisibility(View.VISIBLE);
                    holder.like_view_share.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ShareActivity.class);
                            intent.putExtra("parent_feed_id", item.getItemId());
                            intent.putExtra("parent_module_id", item.getType());
                            intent.putExtra("can_share_item_on_feed", item.getCan_share_item_on_feed());
                            intent.putExtra("user_id", item.getUserId());
                            intent.putExtra("feed_link", item.getShareFeedLink());
                            intent.putExtra("feed_link_url", item.getShareFeedLinkUrl());
                            startActivity(intent);
                        }
                    });
                } else {
                    holder.shareBtn.setVisibility(View.GONE);
                    holder.like_view_share.setVisibility(View.GONE);
                }

                // set title
                if (holder.Title != null && item.getTitle() != null) {
                    holder.Title.setText(Html.fromHtml(item.getTitle()));
                    if (item.getFeedMini() != null) {
                        holder.Title.setText(Html.fromHtml(item.getTitle() + " " + phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.shared")));
                    }
                }

                // set feed time
                if (holder.Time != null) {
                    holder.Time.setText(item.getTime());
                }
                // set feed icon
                if (holder.icon != null) {
                    if (!"".equals(item.getIcon())) {
                        networkUntil.drawImageUrl(holder.icon, item.getIcon(), R.drawable.loading);
                    }
                }
                // set title_feed;

                if (holder.feedStatus != null && item.getStatus() != null && !("null").equals(item.getStatus()) && !item.getType().equals("blog")) {
                    holder.feedStatus.setVisibility(View.VISIBLE);
                    // interesting part starts from here here:
                    Html.ImageGetter ig = imageGetter.create(position, item.getStatus(), holder.feedStatus);
                    holder.feedStatus.setTag(position);
                    holder.feedStatus.setText(Html.fromHtml(item.getStatus(), ig, null));
                } else {
                    holder.feedStatus.setVisibility(View.GONE);
                }

                // set_total_like

                if (holder.total_like != null && item.getHasLike() != null
                        && item.getEnableLike() != null
                        && item.getEnableLike() != false) {
                    holder.like.setVisibility(View.VISIBLE);
                    if (item.getFeedIsLiked() == null) {
                        holder.like.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
                    } else {
                        holder.like.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
                    }

                    holder.like_view_like.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            onListItemClick(position, item.getFeedIsLiked());
                        }
                    });
                    holder.like_view_like.setVisibility(View.VISIBLE);
                    holder.total_like.setVisibility(View.VISIBLE);
                    holder.like_icon.setVisibility(View.VISIBLE);
                    holder.total_like.setText(Integer.toString(item.getTotalLike()));
                    colorView.changeColorText(holder.total_like, user.getColor());
                } else {
                    holder.like_view_like.setVisibility(View.GONE);
                    holder.total_like.setVisibility(View.GONE);
                    holder.like_icon.setVisibility(View.GONE);
                    holder.like.setVisibility(View.GONE);
                }

                // set_total_comment

                if (holder.total_comment != null
                        && item.getTotalComment() != null
                        && item.getCanPostComment() == true) {
                    holder.comment.setVisibility(View.VISIBLE);
                    holder.total_comment.setVisibility(View.VISIBLE);
                    holder.comment_icon.setVisibility(View.VISIBLE);
                    holder.like_view_comment.setVisibility(View.VISIBLE);
                    holder.commentImageView.setVisibility(View.VISIBLE);
                    holder.total_comment.setText(item.getTotalComment());
                    colorView.changeColorText(holder.total_comment, user.getColor());

                    holder.like_view_comment.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if ("event".equals(item.getType())) {
                                Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                                intent.putExtra("event_id", item.getItemId());
                                getActivity().startActivity(intent);
                            } else {
                                onCommentClick(position);
                            }
                        }
                    });
                } else {
                    holder.commentImageView.setVisibility(View.GONE);
                    holder.like_view_comment.setVisibility(View.GONE);
                    holder.comment.setVisibility(View.GONE);
                    holder.total_comment.setVisibility(View.GONE);
                    holder.comment_icon.setVisibility(View.GONE);
                }

                if (item.getEnableLike() != null && item.getCanPostComment() != null && !item.getEnableLike() && !item.getCanPostComment()) {
                    holder.feedLikeView.setVisibility(View.GONE);
                    holder.feedLikeCommentView.setVisibility(View.GONE);
                } else {
                    holder.feedLikeView.setVisibility(View.VISIBLE);
                    holder.feedLikeCommentView.setVisibility(View.VISIBLE);
                    holder.feedLikeCommentView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), FriendActivity.class);
                            intent.putExtra("type", item.getType());
                            intent.putExtra("item_id", item.getItemId());
                            intent.putExtra("total_like", item.getTotalLike());
                            getActivity().startActivity(intent);
                        }
                    });
                }

                if (("photo").equals(item.getType()) || ("photo_comment").equals(item.getType())) {
                    holder.title_feed.setVisibility(View.GONE);
                    holder.feedimg_small.setVisibility(View.GONE);
                    holder.feedContent.setVisibility(View.GONE);
                    holder.feedTitleExtra.setVisibility(View.GONE);
                    holder.feedimg_small.setImageBitmap(null);
                    if (item.getImage1() != null) {

                        if (item.getImage2() != null) {

                            if (item.getImage3() != null
                                    && item.getImage4() == null) {
                                holder.feedimg1.setVisibility(View.VISIBLE);
                                holder.feedimg2.setVisibility(View.VISIBLE);
                                holder.feedimg3.setVisibility(View.VISIBLE);
                                holder.feedimg4.setImageBitmap(null);
                                holder.feedimg4.setVisibility(View.GONE);
                                holder.feedimg.setImageBitmap(null);
                                holder.feedimg.setVisibility(View.GONE);
                                networkUntil.drawImageUrl(holder.feedimg1,
                                        item.getImage1(), R.drawable.loading);
                                networkUntil.drawImageUrl(holder.feedimg2,
                                        item.getImage2(), R.drawable.loading);
                                networkUntil.drawImageUrl(holder.feedimg3,
                                        item.getImage3(), R.drawable.loading);

                                holder.feedimg1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 0);
                                    }
                                });

                                holder.feedimg2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 1);
                                    }
                                });

                                holder.feedimg3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 2);
                                    }
                                });

                            } else if (item.getImage4() != null) {
                                holder.feedimg1.setVisibility(View.VISIBLE);
                                holder.feedimg2.setVisibility(View.VISIBLE);
                                holder.feedimg3.setVisibility(View.VISIBLE);
                                holder.feedimg4.setVisibility(View.VISIBLE);
                                holder.feedimg.setImageBitmap(null);
                                holder.feedimg.setVisibility(View.GONE);

                                networkUntil.drawImageUrl(holder.feedimg1,
                                        item.getImage1(), R.drawable.loading);
                                networkUntil.drawImageUrl(holder.feedimg2,
                                        item.getImage2(), R.drawable.loading);
                                networkUntil.drawImageUrl(holder.feedimg3,
                                        item.getImage3(), R.drawable.loading);
                                networkUntil.drawImageUrl(holder.feedimg4,
                                        item.getImage4(), R.drawable.loading);

                                holder.feedimg1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 0);
                                    }
                                });

                                holder.feedimg2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 1);
                                    }
                                });

                                holder.feedimg3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 2);
                                    }
                                });

                                holder.feedimg4.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 3);
                                    }
                                });

                            } else {
                                holder.feedimg1.setVisibility(View.VISIBLE);
                                holder.feedimg2.setVisibility(View.VISIBLE);
                                holder.feedimg3.setImageBitmap(null);
                                holder.feedimg4.setImageBitmap(null);
                                holder.feedimg3.setVisibility(View.GONE);
                                holder.feedimg4.setVisibility(View.GONE);
                                holder.feedimg.setImageBitmap(null);
                                holder.feedimg.setVisibility(View.GONE);
                                networkUntil.drawImageUrl(holder.feedimg1,
                                        item.getImage1(), R.drawable.loading);
                                networkUntil.drawImageUrl(holder.feedimg2,
                                        item.getImage2(), R.drawable.loading);

                                holder.feedimg1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 0);
                                    }
                                });

                                holder.feedimg2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onPhotoClick(position, 1);
                                    }
                                });

                            }

                        } else {
                            holder.feedimg.setVisibility(View.VISIBLE);
                            holder.feedimg1.setVisibility(View.GONE);
                            holder.feedimg2.setVisibility(View.GONE);
                            holder.feedimg3.setVisibility(View.GONE);
                            holder.feedimg4.setVisibility(View.GONE);
                            holder.feedimg1.setImageBitmap(null);
                            holder.feedimg2.setImageBitmap(null);
                            holder.feedimg3.setImageBitmap(null);
                            holder.feedimg4.setImageBitmap(null);

                            networkUntil.drawImageUrl(holder.feedimg,
                                    item.getImage1(), R.drawable.loading);

                            holder.feedimg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onPhotoClick(position, 4);
                                }
                            });

                        }

                    } else {
                        holder.feedimg.setImageBitmap(null);
                        holder.feedimg.setVisibility(View.GONE);
                    }
                } else {
                    holder.feedimg.setVisibility(View.GONE);
                    holder.feedimg1.setVisibility(View.GONE);
                    holder.feedimg2.setVisibility(View.GONE);
                    holder.feedimg3.setVisibility(View.GONE);
                    holder.feedimg4.setVisibility(View.GONE);
                    holder.feedimg1.setImageBitmap(null);
                    holder.feedimg2.setImageBitmap(null);
                    holder.feedimg3.setImageBitmap(null);
                    holder.feedimg4.setImageBitmap(null);
                    holder.feedimg.setImageBitmap(null);

                    if (holder.title_feed != null
                            && item.getTitleFeed() != null
                            && !("").equals(item.getTitleFeed())) {
                        holder.title_feed.setVisibility(View.VISIBLE);
                        holder.title_feed.setText(item.getTitleFeed());
                        holder.feedSmallLayout.setVisibility(View.VISIBLE);
                    } else {
                        holder.title_feed.setVisibility(View.GONE);
                    }

                    // add text info
                    if (holder.feedTitleExtra != null
                            && item.getFeedTitleExtra() != null
                            && !("").equals(item.getFeedTitleExtra())) {
                        holder.feedTitleExtra.setVisibility(View.VISIBLE);
                        holder.feedTitleExtra.setText(item.getFeedTitleExtra());
                        holder.feedSmallLayout.setVisibility(View.VISIBLE);
                    } else {
                        holder.feedTitleExtra.setVisibility(View.GONE);
                    }

                    // add text info
                    if (holder.feedContent != null && item.getFeedContent() != null && !("").equals(item.getFeedContent())) {
                        holder.feedContent.setVisibility(View.VISIBLE);
                        // interesting part starts from here here:
                        Html.ImageGetter ig = imageGetter.create(position, item.getFeedContent(), holder.feedContent);

                        holder.feedContent.setTag(position);
                        holder.feedContent.setText(Html.fromHtml(item.getFeedContent(), ig, null));
                        holder.feedSmallLayout.setVisibility(View.VISIBLE);
                    } else {
                        holder.feedContent.setVisibility(View.GONE);
                    }

                    if (holder.feedimg_small != null) {
                        if (item.getImage1() != null
                                && !"".equals(item.getUserImage())) {
                            holder.feedimg_small.setVisibility(View.VISIBLE);
                            networkUntil.drawImageUrl(holder.feedimg_small,
                                    item.getImage1(), R.drawable.loading);
                            holder.feedSmallLayout.setVisibility(View.VISIBLE);
                        } else {
                            holder.feedimg_small.setVisibility(View.GONE);
                            holder.feedimg_small.setImageBitmap(null);
                        }
                    }

                    if ((item.getTitleFeed() == null || "".equals(item.getTitleFeed()))
                            && (item.getFeedContent() == null || ("").equals(item.getFeedContent()))
                            && (item.getImage1() == null || ("").equals(item.getUserImage()))) {
                        holder.feedSmallLayout.setVisibility(View.GONE);
                    }

                    holder.feedSmallLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (item.getPage_id_request() != null) {
                                Intent intent = new Intent(getActivity(), FriendTabsPager.class);
                                intent.putExtra("page_id", item.getPage_id_request());
                                startActivity(intent);
                            } else if (item.getUser_id_request() != null) {
                                Intent intent = new Intent(getActivity(), FriendTabsPager.class);
                                intent.putExtra("user_id", item.getUser_id_request());
                                startActivity(intent);
                            } else {
                                if ("link".equals(item.getType())) {
                                    String url = item.getFeedLink();
                                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                                        url = "http://" + url;
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    startActivity(browserIntent);
                                } else
                                    new NextActivity(getActivity()).linkActivity(item.getType(), item.getItemId(), item.getDataCacheId(), item.getTitleFeed(), item.getFeedLink());
                            }

                        }
                    });
                }
                if (holder.feedLocationView != null) {
                    if (item.getLocationImg() != null) {
                        holder.feedLocationView.setVisibility(View.VISIBLE);
                        networkUntil.drawImageUrl(holder.feedLocationView, Html.fromHtml(item.getLocationImg()).toString(), R.drawable.loading);

                        holder.feedLocationView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Html.fromHtml(item.getLocationLink()).toString()));
                                getActivity().startActivity(browserIntent);
                            }
                        });
                    } else {
                        holder.feedLocationView.setVisibility(View.GONE);
                    }
                }
            }

            return view;
        }

        protected void onListItemClick(int position, String like) {
            Feed item1 = getItem(position);

            if (like == null) {

                item1.setFeedIsLiked("1");
                item1.setTotalLike(item1.getTotalLike() + 1);

                new test().execute(item1.getItemId(), item1.getType(), item1.getFeedId(), "like");
            } else {
                item1.setFeedIsLiked(null);
                item1.setTotalLike(item1.getTotalLike() - 1);
                new test().execute(item1.getItemId(), item1.getType(), item1.getFeedId(), "unlike");

            }

            notifyDataSetChanged();
        }

        protected void onCommentClick(int position) {
            Feed item2 = getItem(position);
            if (getActivity() == null)
                return;
            FriendTabsPager activity = (FriendTabsPager) getActivity();
            activity.doShowCommentDetail(position, item2.getType(),
                    item2.getItemId(), item2.getModule());

        }

        protected void onPhotoClick(int position, int photo_position) {
            String[] imagesId;
            String[] itemid = null;
            String[] imageHasLike = null;
            String[] imageFeedisLike = null;
            String[] imageTotal_like = null;
            String[] imageTotal_comment = null;
            String[] imageType = null;
            Feed item3 = getItem(position);

            Intent intent = new Intent(getActivity(), ImagePagerActivity.class);
            String[] imageUrls = item3.getFeed_Image().toArray(
                    new String[item3.getFeed_Image().size()]);
            if (photo_position < 4) {
                imagesId = item3.getImagesId().toArray(
                        new String[item3.getImagesId().size()]);
                intent.putExtra("position", photo_position);

            } else {
                ArrayList<String> Images_id = new ArrayList<String>();
                ArrayList<String> item_id = new ArrayList<String>();
                ArrayList<String> Has_Like = new ArrayList<String>();
                ArrayList<String> FeedisLiked = new ArrayList<String>();
                ArrayList<String> totallike = new ArrayList<String>();
                ArrayList<String> totalcomment = new ArrayList<String>();
                ArrayList<String> typeid = new ArrayList<String>();

                Images_id.add(item3.getPhoto_id_request());
                item_id.add(item3.getItemId());
                Has_Like.add(item3.getHasLike());
                if (item3.getHasLike() != null && item3.getEnableLike() != null
                        && item3.getEnableLike() != false) {
                    if (item3.getFeedIsLiked() == null) {
                        FeedisLiked.add("null");
                    } else {
                        FeedisLiked.add(item3.getFeedIsLiked());
                    }
                }
                totallike.add(Integer.toString(item3.getTotalLike()));

                if (item3.getTotalComment() != null) {
                    totalcomment.add(item3.getTotalComment());
                }

                typeid.add(item3.getType());

                imagesId = Images_id.toArray(new String[Images_id.size()]);
                itemid = item_id.toArray(new String[item_id.size()]);
                imageHasLike = Has_Like.toArray(new String[Has_Like.size()]);
                imageFeedisLike = FeedisLiked.toArray(new String[FeedisLiked
                        .size()]);
                imageTotal_like = totallike
                        .toArray(new String[totallike.size()]);
                imageTotal_comment = totalcomment
                        .toArray(new String[totalcomment.size()]);
                imageType = typeid.toArray(new String[typeid.size()]);

                System.out.println("imagesId" + " = "
                        + item3.getPhoto_id_request());
                System.out.println("itemid" + " = " + item3.getItemId());
                System.out.println("imageHasLike" + " = " + item3.getHasLike());
                System.out.println("imageFeedisLike" + " = "
                        + item3.getFeedIsLiked());
                System.out.println("imageTotal_comment" + " = "
                        + item3.getTotalLike());
                System.out.println("imageType" + " = "
                        + item3.getTotalComment());
            }

            intent.putExtra("photo_id", imagesId);
            intent.putExtra("image", imageUrls);
            intent.putExtra("Itemid", itemid);
            intent.putExtra("HasLike", imageHasLike);
            intent.putExtra("FeedisLike", imageFeedisLike);
            intent.putExtra("Total_like", imageTotal_like);
            intent.putExtra("Total_comment", imageTotal_comment);
            intent.putExtra("Type", imageType);
            startActivity(intent);
        }

        public class test extends AsyncTask<String, Void, String> {
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
                    // Use BasicNameValuePair to create GET data
                    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                    pairs.add(new BasicNameValuePair("token", user.getTokenkey()));

                    if (("like").equals(params[3])) {
                        pairs.add(new BasicNameValuePair("method", "accountapi.like"));
                    } else {
                        pairs.add(new BasicNameValuePair("method", "accountapi.unlike"));
                    }

                    if (params[1] != null) {
                        pairs.add(new BasicNameValuePair("type", "" + params[1]));
                        pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
                    } else {
                        pairs.add(new BasicNameValuePair("feed_id", "" + params[2]));
                    }

                    // url request
                    String URL = null;
                    if (Config.CORE_URL == null) {
                        URL = Config.makeUrl(user.getCoreUrl(), null, false);
                    } else {
                        URL = Config.makeUrl(Config.CORE_URL, null, false);
                    }

                    // request GET method to server
                    likerequest = networkUntil.makeHttpRequest(URL, "GET", pairs);
                    Log.i("like request", likerequest);
                } catch (Exception ex) {
                    // Log.i(DEBUG_TAG, ex.getMessage());
                }
                return null;
            }

        }

    }

    /**
     * Class feed view holder
     *
     * @author datnguyen
     */
    public class FeedViewHolder {
        public final ImageView iv;
        public final TextView Title;
        public final TextView title_feed;
        public final TextView feedStatus;
        public final TextView Time;

        // like view
        public final TextView total_like;
        public final TextView total_comment;
        public final ImageView like_icon;
        public final ImageView comment_icon;
        public final ImageView likeImg;
        public final ImageView commentImg;
        public final LinearLayout like_view;
        public final TextView like;
        public final TextView comment;

        // icon feed
        public final ImageView icon;
        // feed_small
        public final ImageView feedimg_small;
        // feed photo
        public final ImageView feedimg;
        public final ImageView feedimg1;
        public final ImageView feedimg2;
        public final ImageView feedimg3;
        public final ImageView feedimg4;

        // share
        public final RelativeLayout like_view_like;
        public final RelativeLayout like_view_comment;
        public final RelativeLayout like_view_share;
        public final RelativeLayout feedSmallLayout;
        public final TextView shareBtn;

        // notice
        public final TextView notice;
        // add info for link
        public final TextView feedTitleExtra;
        public final TextView feedContent;

        // share feed item view
        public final RelativeLayout shareFeedItemView;
        public final TextView titleFeedShare;
        public final TextView titleOfFeedShare;
        public final TextView feedShareStatus;
        public final ImageView shareFeedImage;
        public final View feedLikeView;
        public final ImageView commentImageView;
        public final LinearLayout feedLikeCommentView;
        public final ImageView feedLocationView;

        public FeedViewHolder(ImageView iv, TextView title,
                              TextView title_feed, TextView feedStatus, TextView time,
                              TextView total_like, TextView total_comment,
                              ImageView like_icon, ImageView comment_icon,
                              LinearLayout like_view, TextView like, TextView comment,
                              ImageView icon, ImageView feedimg_small, ImageView feedimg,
                              ImageView feedimg1, ImageView feedimg2, ImageView feedimg3,
                              ImageView feedimg4, RelativeLayout like_view_like,
                              RelativeLayout like_view_comment,
                              RelativeLayout like_view_share, TextView notice,
                              TextView shareBtn, RelativeLayout shareFeedItemView,
                              TextView titleFeedShare, TextView titleOfFeedShare,
                              TextView feedShareStatus, ImageView shareFeedImage,
                              TextView feedTitleExtra, TextView feedContent,
                              RelativeLayout feedSmallLayout, View feedLikeView,
                              LinearLayout feedLikeCommentView, ImageView commentImageView,
                              ImageView likeImg, ImageView commentImg, ImageView feedLocationView) {
            super();
            this.iv = iv;
            this.Title = title;
            this.title_feed = title_feed;
            this.feedStatus = feedStatus;
            this.Time = time;
            this.total_like = total_like;
            this.total_comment = total_comment;
            this.like_icon = like_icon;
            this.comment_icon = comment_icon;
            this.like_view = like_view;
            this.like = like;
            this.comment = comment;
            this.icon = icon;
            this.feedimg_small = feedimg_small;
            this.feedimg = feedimg;
            this.feedimg1 = feedimg1;
            this.feedimg2 = feedimg2;
            this.feedimg3 = feedimg3;
            this.feedimg4 = feedimg4;
            this.like_view_like = like_view_like;
            this.like_view_comment = like_view_comment;
            this.like_view_share = like_view_share;
            this.notice = notice;
            this.shareBtn = shareBtn;
            this.shareFeedItemView = shareFeedItemView;
            this.titleFeedShare = titleFeedShare;
            this.titleOfFeedShare = titleOfFeedShare;
            this.feedShareStatus = feedShareStatus;
            this.shareFeedImage = shareFeedImage;
            this.feedTitleExtra = feedTitleExtra;
            this.feedContent = feedContent;
            this.feedSmallLayout = feedSmallLayout;
            this.feedLikeView = feedLikeView;
            this.feedLikeCommentView = feedLikeCommentView;
            this.commentImageView = commentImageView;
            this.likeImg = likeImg;
            this.commentImg = commentImg;
            this.feedLocationView = feedLocationView;
        }
    }

    @Override
    public void onResume() {
        try {
            if (fa != null && Config.feed.isContinueFeed() == true) {
                if (bNotice) {
                    fa.clear();
                }
                fa.insert(Config.feed, 0);
                fa.notifyDataSetChanged();
                bNotice = false;
                Config.feed.setContinueFeed(false);
            }

            if (photoTxt != null) {
                photoTxt.setTextColor(Color.parseColor("#797979"));
                photo_button.setBackgroundColor(Color.parseColor("#f3f3f3"));
                photoImage.setImageResource(R.drawable.photo_black_icon);

                statusTxt.setTextColor(Color.parseColor("#797979"));
                share_button.setBackgroundColor(Color.parseColor("#f3f3f3"));
                shareImage.setImageResource(R.drawable.status_black_icon);
            }
        } catch (Exception e) {
        }
        super.onResume();
    }
}
