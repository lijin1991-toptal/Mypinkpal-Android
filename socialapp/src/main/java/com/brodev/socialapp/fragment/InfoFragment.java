package com.brodev.socialapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.UserProfile;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.AlbumSelectedActivity;
import com.brodev.socialapp.view.ChatActivity;
import com.brodev.socialapp.view.CheckInActivity;
import com.brodev.socialapp.view.ComposeActivity;
import com.brodev.socialapp.view.FriendActivity;
import com.brodev.socialapp.view.ImageUpload;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @company Brodev.com
 */
public class InfoFragment extends SherlockFragment {

    private User user;
    private NetworkUntil networkUntil = new NetworkUntil();
    private UserProfile objProfileInfo;
    private String sUserId;
    private String sPagesId;
    private JSONObject callback;

    private ImageView profile_image, profile_cover;
    private TextView profile_fullname, profile_location, profile_birthday,
            info_gender, info_age, info_location, info_relation, info_sexuality, info_last_login,
            info_member_since, info_membership, info_profile_view,
            info_rss_subscribers, info_profile, pages_info;

    private Button actionFriend, MessageBtn, noInternetBtn;
    private String URL_APPROVE_FRIEND;
    private PhraseManager phraseManager;
    private RelativeLayout profile_info, share_button, photo_button, checkInButton, noInternetLayout, noInternetLayoutInfo;
    private TextView statusTxt, photoTxt, checkInTxt, noInternetTitle, noInternetContent;
    private ImageView shareImage, photoImage, noInternetImg;
    private ScrollView scrollViewLayout;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    private ColorView colorView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //init value
        sUserId = null;
        sPagesId = null;
        callback = new JSONObject();
        user = (User) getActivity().getApplicationContext();
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());
        objProfileInfo = new UserProfile();

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            if (getActivity().getIntent().hasExtra("user_id")) {
                sUserId = extras.getString("user_id");
            } else if (getActivity().getIntent().hasExtra("page_id")) {
                sPagesId = extras.getString("page_id");
            }
        }
        super.onCreate(savedInstanceState);
    }

    private void loadUserInfo() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                scrollViewLayout.setVisibility(View.VISIBLE);
                noInternetLayoutInfo.setVisibility(View.GONE);

                //fetch data
                if (sUserId != null) {
                    new ProfileTask().execute(sUserId);
                } else if (sPagesId != null) {
                    new ProfileTask().execute(sPagesId);
                }
            } else {
                // display error
                noInternetLayoutInfo.setVisibility(View.VISIBLE);
                scrollViewLayout.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            noInternetLayoutInfo.setVisibility(View.VISIBLE);
            scrollViewLayout.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            loadUserInfo();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Change color action
     *
     * @param btnAction
     * @param colorCode
     */
    private void changeColorAction(RelativeLayout btnAction, String colorCode) {
        if ("Brown".equalsIgnoreCase(colorCode)) {
            btnAction.setBackgroundColor(Color.parseColor("#da6e00"));
        } else if ("Pink".equalsIgnoreCase(colorCode)) {
            btnAction.setBackgroundColor(Color.parseColor("#ef4964"));
        } else if ("Green".equalsIgnoreCase(colorCode)) {
            btnAction.setBackgroundColor(Color.parseColor("#348105"));
        } else if ("Violet".equalsIgnoreCase(colorCode)) {
            btnAction.setBackgroundColor(Color.parseColor("#8190db"));
        } else if ("Red".equalsIgnoreCase(colorCode)) {
            btnAction.setBackgroundColor(Color.parseColor("#ff0606"));
        } else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
            btnAction.setBackgroundColor(Color.parseColor("#4e529b"));
        } else {
            btnAction.setBackgroundColor(Color.parseColor("#0084c9"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //create view from friend_fragment xml
        View view = inflater.inflate(R.layout.info_fragment, container, false);

        scrollViewLayout = (ScrollView) view.findViewById(R.id.scrollview_user_info);

        profile_image = (ImageView) view.findViewById(R.id.profile_image);
        profile_fullname = (TextView) view.findViewById(R.id.profile_fullname);
        profile_location = (TextView) view.findViewById(R.id.profile_location);
        profile_birthday = (TextView) view.findViewById(R.id.profile_birthday);
        profile_cover = (ImageView) view.findViewById(R.id.item_img_cover);
        info_gender = (TextView) view.findViewById(R.id.profile_info_text_gender);
        info_age = (TextView) view.findViewById(R.id.profile_info_text_age);
        info_location = (TextView) view.findViewById(R.id.profile_info_text_location);
        info_sexuality = (TextView) view.findViewById(R.id.profile_info_text_sexuality);
        info_relation = (TextView) view.findViewById(R.id.profile_info_text_relation);
        info_last_login = (TextView) view.findViewById(R.id.profile_info_text_last_login);
        info_member_since = (TextView) view.findViewById(R.id.profile_info_text_member_since);
        info_membership = (TextView) view.findViewById(R.id.profile_info_text_membership);
        info_profile_view = (TextView) view.findViewById(R.id.profile_info_text_Profile_Views);
        info_rss_subscribers = (TextView) view.findViewById(R.id.profile_info_text_RSS_Subscribers);
        actionFriend = (Button) view.findViewById(R.id.profile_actionFriend);
        MessageBtn = (Button) view.findViewById(R.id.profile_MessageBtn);

        info_profile = (TextView) view.findViewById(R.id.profile_info);
        info_profile.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.basic_info"));

        statusTxt = (TextView) view.findViewById(R.id.statusTxt);
        statusTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.status"));

        checkInTxt = (TextView) view.findViewById(R.id.checkinTxt);
        checkInTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.check_in"));

        photoTxt = (TextView) view.findViewById(R.id.photoTxt);
        photoTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.photo"));
        shareImage = (ImageView) view.findViewById(R.id.image_icon_status);
        photoImage = (ImageView) view.findViewById(R.id.image_icon_photo);
        share_button = (RelativeLayout) view.findViewById(R.id.share_button);
        photo_button = (RelativeLayout) view.findViewById(R.id.photo_button);
        checkInButton = (RelativeLayout) view.findViewById(R.id.checkin_button);

        /*
        user.getCheckin();
        Boolean.parseBoolean(user.getCheckin());
        user.getUserId();
        sUserId.equals(user.getUserId());
*/

        if (sPagesId == null && Boolean.parseBoolean(user.getCheckin()) && sUserId != null && user != null && user.getUserId() != null && sUserId.equals(user.getUserId())) {
            checkInButton.setVisibility(View.VISIBLE);
        } else {
            checkInButton.setVisibility(View.GONE);
        }

        share_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                statusTxt.setTextColor(Color.parseColor("#ffffff"));
                changeColorAction(share_button, user.getColor());
                shareImage.setImageResource(R.drawable.status_white_icon);
                Intent intent = new Intent(getActivity(), ImageUpload.class);
                if (sUserId != null || sPagesId != null) {
                    if (sPagesId != null) {
                        intent.putExtra("page_id", sPagesId);
                        intent.putExtra("owner_user_id", objProfileInfo.getUser_id());
                        intent.putExtra("page_title", objProfileInfo.getTitle());
                        intent.putExtra("fullname", objProfileInfo.getFullname());
                        intent.putExtra("profile_page_id", objProfileInfo.getProfile_page_id());
                        intent.putExtra("pages_image", objProfileInfo.getPagesImage());
                    } else {
                        intent.putExtra("user_id", objProfileInfo.getUser_id());
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
                photoTxt.setTextColor(Color.parseColor("#ffffff"));
                changeColorAction(photo_button, user.getColor());
                photoImage.setImageResource(R.drawable.photo_white_icon);
                Intent intent = new Intent(getActivity(), AlbumSelectedActivity.class);
                if (sUserId != null || sPagesId != null) {
                    if (sPagesId != null) {
                        intent.putExtra("page_id", sPagesId);
                        intent.putExtra("owner_user_id", objProfileInfo.getUser_id());
                        intent.putExtra("page_title", objProfileInfo.getTitle());
                        intent.putExtra("fullname", objProfileInfo.getFullname());
                        intent.putExtra("profile_page_id", objProfileInfo.getProfile_page_id());
                        intent.putExtra("pages_image", objProfileInfo.getPagesImage());
                    } else {
                        intent.putExtra("user_id", objProfileInfo.getUser_id());
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

        //set gender phrase
        TextView genderTxt = (TextView) view.findViewById(R.id.gender_txt);
        genderTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.gender"));
        //set age phrase
        TextView ageTxt = (TextView) view.findViewById(R.id.age_txt);
        ageTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.age"));
        //set location phrase
        TextView localtionTxt = (TextView) view.findViewById(R.id.location_txt);
        localtionTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.location"));
        //set sexuality phrase
        TextView sexualityTxt = (TextView) view.findViewById(R.id.sexuality_txt);
        sexualityTxt.setText("Sexuality");
        //set relation phrase
        TextView relationTxt = (TextView) view.findViewById(R.id.relation_txt);
        relationTxt.setText("Relationship Status");
        //set last login phrase
        TextView lastLoginTxt = (TextView) view.findViewById(R.id.last_login_txt);
        lastLoginTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.last_login"));
        //set member since phrase
        TextView membersinceTxt = (TextView) view.findViewById(R.id.member_since_txt);
        membersinceTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.member_since"));
        //set membership phrase
        TextView membershipTxt = (TextView) view.findViewById(R.id.membership_txt);
        membershipTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.membership"));
        //set view profile phrase
        TextView viewProfileTxt = (TextView) view.findViewById(R.id.view_profile_txt);
        viewProfileTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.view_profile"));
        //set rss phrase
        TextView rssTxt = (TextView) view.findViewById(R.id.rss_subscribers_txt);
        rssTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.rss_subscribers"));

        profile_info = (RelativeLayout) view.findViewById(R.id.profile_info_arena);
        pages_info = (TextView) view.findViewById(R.id.pages_info);

        //no internet connection
        noInternetLayout = (RelativeLayout) view.findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) view.findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) view.findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) view.findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) view.findViewById(R.id.no_internet_image);
        noInternetLayoutInfo = (RelativeLayout) view.findViewById(R.id.no_internet_info_layout);

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
                }, 2000);
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
            if (isCancelled()) {
                return null;
            }

            String result = null;
            try {
                // String url
                String URL = null;
                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), null, false);
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, null, false);
                }

                // Use BasicNameValuePair to store POST data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));

                if (sUserId != null) {
                    pairs.add(new BasicNameValuePair("method", "accountapi.getUserInfo"));
                    pairs.add(new BasicNameValuePair("user_id", "" + params[0]));
                } else if (sPagesId != null) {
                    pairs.add(new BasicNameValuePair("method", "accountapi.getItem"));
                    pairs.add(new BasicNameValuePair("module", "pages"));
                    pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
                }

                result = networkUntil.makeHttpRequest(URL, "GET", pairs);

                JSONObject mainJSON = new JSONObject(result);

                JSONObject outputJson = mainJSON.getJSONObject("output");

                if (outputJson.has("full_name")) {
                    objProfileInfo.setFullname(Html.fromHtml(outputJson.getString("full_name")).toString());
                }

                // bronislaw
                if (outputJson.has("user_name")) {
                    objProfileInfo.setUsername(outputJson.getString("user_name"));
                }

                if (outputJson.has("quickbloxid")) {
                    objProfileInfo.setQuickbloxID(outputJson.getString("quickbloxid"));
                }

                if (outputJson.has("user_id")) {
                    objProfileInfo.setUser_id(outputJson.getString("user_id"));
                }

                if (outputJson.has("dob_setting")) {
                    objProfileInfo.setDob(Html.fromHtml(outputJson.getString("dob_setting")).toString());
                }

                if (outputJson.has("location_phrase")) {
                    objProfileInfo.setLocation(Html.fromHtml(outputJson.getString("location_phrase")).toString());
                } else {
                    objProfileInfo.setLocation(null);
                }

                if (outputJson.has("sexuality")) {
                    objProfileInfo.setSexuality(Html.fromHtml(outputJson.getString("sexuality")).toString());
                }


                if (outputJson.has("birthday_phrase") && !"false".equals(outputJson.getString("birthday_phrase"))) {
                    objProfileInfo.setBirthday(Html.fromHtml(outputJson.getString("birthday_phrase")).toString());
                } else {
                    objProfileInfo.setBirthday(null);
                }

                if (outputJson.has("photo_120px_square")) {
                    objProfileInfo.setUserImage(outputJson.getString("photo_120px_square"));
                }

                if (outputJson.has("photo_200px_square")) {
                    objProfileInfo.setUserImage(outputJson.getString("photo_200px_square"));
                }

                if (outputJson.has("cover_photo")) {
                    Object intervention = outputJson.get("cover_photo");

                    if (intervention instanceof JSONObject) {
                        JSONObject coverJson = outputJson.getJSONObject("cover_photo");
                        objProfileInfo.setCoverPhoto(coverJson.getString("500"));
                    } else {
                        objProfileInfo.setCoverPhoto(null);
                    }
                }

                //get data pages

                if (outputJson.has("title")) {
                    objProfileInfo.setTitle(Html.fromHtml(outputJson.getString("title")).toString());
                }

                if (outputJson.has("category_name")) {
                    objProfileInfo.setCategory(Html.fromHtml(outputJson.getString("category_name")).toString());
                }

                if (outputJson.has("photo_sizes")) {
                    Object intervention = outputJson.get("photo_sizes");

                    if (intervention instanceof JSONObject) {
                        JSONObject coverJson = outputJson.getJSONObject("photo_sizes");
                        objProfileInfo.setPagesImage(coverJson.getString("120"));
                    } else {
                        objProfileInfo.setPagesImage(null);
                    }
                }

                if (outputJson.has("feed_callback")) {
                    Object intervention = outputJson.get("feed_callback");

                    if (intervention instanceof JSONObject) {
                        callback = outputJson.getJSONObject("feed_callback");
                    }
                }

                if (outputJson.has("info")) {

                    JSONObject info = outputJson.getJSONObject("info");
                    if (info.has("Gender")) {
                        objProfileInfo.setGender(Html.fromHtml(info.getString("Gender")).toString());
                    }
                    if (info.has("Age")) {
                        objProfileInfo.setAge(Html.fromHtml(info.getString("Age")).toString());
                    }

                    if (info.has("Relationship Status")) {
                        objProfileInfo.setRelationship_status(Html.fromHtml(info.getString("Relationship Status")).toString());
                    }
                    if (info.has("Location")) {
                        objProfileInfo.setLocation_info(Html.fromHtml(info.getString("Location")).toString());
                    }
                    if (info.has("Last Login")) {
                        objProfileInfo.setLast_login(Html.fromHtml(info.getString("Last Login")).toString());
                    }
                    if (info.has("Member Since")) {
                        objProfileInfo.setMember_since(Html.fromHtml(info.getString("Member Since")).toString());
                    }
                    if (info.has("Membership")) {
                        objProfileInfo.setMembership(Html.fromHtml(info.getString("Membership")).toString());
                    }
                    if (info.has("Profile Views")) {
                        objProfileInfo.setProfile_views(Html.fromHtml(info.getString("Profile Views")).toString());
                    }
                    if (info.has("RSS Subscribers")) {
                        objProfileInfo.setRSS_Subscribers(Html.fromHtml(info.getString("RSS Subscribers")).toString());
                    }
                }

                if (outputJson.has("is_friend")) {
                    objProfileInfo.setIs_friend(outputJson.get("is_friend").toString());
                }

                if (outputJson.has("is_friend_request") && !outputJson.isNull("is_friend_request")) {
                    Log.d("psyh", "is_friend_request" + outputJson.getString("is_friend_request"));
                    if (outputJson.getString("is_friend_request").equals("2")) {
                        objProfileInfo.setIs_friend("2");
                        Log.d("psyh", "objProfileInfo.getIs_friend" + objProfileInfo.getIs_friend());
                    }
                }


                if (outputJson.has("total_friend")) {
                    objProfileInfo.setTotal_friend(outputJson.getString("total_friend"));
                }

                if (outputJson.has("request_id")) {
                    objProfileInfo.setRequest_id(Integer.parseInt(outputJson.getString("request_id")));
                }
                if (outputJson.has("text") && !outputJson.isNull("text")) {
                    objProfileInfo.setText(Html.fromHtml(outputJson.getString("text")).toString());
                }

                if (!outputJson.isNull("is_liked") && outputJson.getString("is_liked") != "false") {
                    objProfileInfo.setIs_liked("is_liked");
                }

                if (outputJson.has("total_like")) {
                    objProfileInfo.setTotal_like(outputJson.getString("total_like"));
                }


            } catch (Exception ex) {
                scrollViewLayout.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
                ex.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (sUserId != null || sPagesId != null) {
                    if (objProfileInfo.getCoverPhoto() != null && !"".equals(objProfileInfo.getCoverPhoto())) {
                        profile_cover.setVisibility(View.VISIBLE);
                        networkUntil.drawImageUrl(profile_cover, objProfileInfo.getCoverPhoto(), R.drawable.loading);
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
                        profile_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        networkUntil.drawImageUrl(profile_image, objProfileInfo.getPagesImage(), R.drawable.loading);
                        profile_fullname.setText(objProfileInfo.getTitle());
                        profile_location.setText(objProfileInfo.getCategory());

                        if (objProfileInfo.getIs_liked() == null) {
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

                        MessageBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "search.members")
                                + "(" + objProfileInfo.getTotal_like() + ")");

                        MessageBtn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), FriendActivity.class);
                                intent.putExtra("type", "pages");
                                intent.putExtra("item_id", sPagesId);
                                startActivity(intent);
                            }
                        });

                        info_profile.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "user.info"));
                        profile_info.setVisibility(View.GONE);

                        if (objProfileInfo.getText() != null) {
                            pages_info.setText(objProfileInfo.getText());
                        }
                    } else {

                        if (objProfileInfo.getUser_id() != null && objProfileInfo.getUser_id().equals(user.getUserId())) {
                            actionFriend.setVisibility(View.GONE);
                            MessageBtn.setVisibility(View.GONE);
                        }

                        networkUntil.drawImageUrl(profile_image, objProfileInfo.getUserImage(), R.drawable.loading);
                        profile_fullname.setText(objProfileInfo.getFullname());
                        profile_location.setText(objProfileInfo.getLocation());
                        profile_birthday.setText(objProfileInfo.getBirthday());
                        info_gender.setText(objProfileInfo.getGender());
                        info_age.setText(objProfileInfo.getAge());
                        info_location.setText(objProfileInfo.getLocation_info());
                        info_relation.setText(objProfileInfo.getRelationship_status());
                        info_sexuality.setText(objProfileInfo.getSexuality());
                        info_last_login.setText(objProfileInfo.getLast_login());
                        info_member_since.setText(objProfileInfo.getMember_since());
                        info_membership.setText(objProfileInfo.getMembership());
                        info_profile_view.setText(objProfileInfo.getProfile_views());
                        info_rss_subscribers.setText(objProfileInfo.getRSS_Subscribers());
                        Log.d("psyh", "objProfileInfo.getIs_friend()" + objProfileInfo.getIs_friend());
                        if (!"".equals(objProfileInfo.getIs_friend())) {
                            if ("false".equals(objProfileInfo.getIs_friend())) {
                                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.add_to_friends"));
                            } else if ("3".equals(objProfileInfo.getIs_friend())) {
                                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.confirm_friend_request"));
                            } else if ("2".equals(objProfileInfo.getIs_friend())) {
                                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.pending_friend_confirmation"));
                                Log.d("psyh", "actionFriend.getText()" + actionFriend.getText());
                            } else {
                                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.friends")
                                        + "(" + objProfileInfo.getTotal_friend() + ")");
                            }

                            actionFriend.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    onActionFriendClick();
                                }
                            });
                        }
                        MessageBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "friend.message"));

                        MessageBtn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (objProfileInfo.getIs_friend() != null && "true".equals(objProfileInfo.getIs_friend())) {
                                    Intent intent = null;
                                    if (user.getChatKey() != null) {
                                        intent = new Intent(getActivity(), ChatActivity.class);
                                        intent.putExtra("image", objProfileInfo.getUserImage());
                                    } else {
                                        intent = new Intent(getActivity(), ComposeActivity.class);
                                    }

                                    intent.putExtra("fullname", objProfileInfo.getFullname());
                                    intent.putExtra("user_id", objProfileInfo.getUser_id());

                                    if (intent != null) {
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            phraseManager.getPhrase(getActivity().getApplicationContext(), "mail.unable_to_send_a_private_message_to_this_user_at_the_moment"),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }

                }

            }
            Log.d("psyh", "actionFriend.getText() END" + actionFriend.getText());
        }

        protected void onActionFriendClick() {

            if (!"".equals(objProfileInfo.getIs_friend())) {
                if ("false".equals(objProfileInfo.getIs_friend())) {
                    new addFriend().execute();
                    actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.pending_friend_confirmation"));
                    objProfileInfo.setIs_friend("2");
                } else if ("3".equals(objProfileInfo.getIs_friend())) {

                } else if ("2".equals(objProfileInfo.getIs_friend())) {

                } else {
                    Intent intent = new Intent(getActivity(), FriendActivity.class);
                    intent.putExtra("user_id", objProfileInfo.getUser_id());
                    startActivity(intent);
                }

            }
        }

        protected void onActionLikeClick() {

            if (objProfileInfo.getIs_liked() == null) {
                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
                new ActionLike().execute(sPagesId, "pages", null, "like");
            } else {
                actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
                new ActionLike().execute(sPagesId, "pages", null, "unlike");
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

                    pairs.add(new BasicNameValuePair("user_id", objProfileInfo.getUser_id()));

                    likerequest = networkUntil.makeHttpRequest(URL, "POST", pairs);

                    Log.i("add Friend", likerequest);
                } catch (Exception ex) {
                    //Log.i(DEBUG_TAG, ex.getMessage());
                }
                return null;
            }

        }

        public class ActionLike extends AsyncTask<String, Void, String> {
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
                    Log.i("like pages", likerequest);
                } catch (Exception ex) {
                    //Log.i(DEBUG_TAG, ex.getMessage());
                }
                return null;
            }
        }

        public class ConfirmRequest extends AsyncTask<Integer, Void, String> {
            String resultstring;

            @Override
            protected String doInBackground(Integer... params) {

                if (isCancelled()) {
                    return null;
                }

                //get result from get method
                if (Config.CORE_URL == null) {
                    URL_APPROVE_FRIEND = Config.makeUrl(user.getCoreUrl(), "approveFriendRequest", true) + "&token=" + user.getTokenkey();
                } else {
                    URL_APPROVE_FRIEND = Config.makeUrl(Config.CORE_URL, "approveFriendRequest", true) + "&token=" + user.getTokenkey();
                }

                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("user_id", "" + params[0]));
                pairs.add(new BasicNameValuePair("action", "accept"));

                resultstring = networkUntil.makeHttpRequest(URL_APPROVE_FRIEND, "POST", pairs);

                Log.i("DEBUG", resultstring);

                return resultstring;
            }
        }

        /**
         * Class deny friend request
         */
        public class DenyRequest extends AsyncTask<Integer, Void, String> {
            String resultstring;

            @Override
            protected String doInBackground(Integer... params) {
                if (isCancelled()) {
                    return null;
                }

                if (Config.CORE_URL == null) {
                    URL_APPROVE_FRIEND = Config.makeUrl(user.getCoreUrl(), "denyFriendRequest", true) + "&token=" + user.getTokenkey();
                } else {
                    URL_APPROVE_FRIEND = Config.makeUrl(Config.CORE_URL, "denyFriendRequest", true) + "&token=" + user.getTokenkey();
                }

                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("user_id", "" + params[0]));
                pairs.add(new BasicNameValuePair("action", "deny"));

                resultstring = networkUntil.makeHttpRequest(URL_APPROVE_FRIEND, "POST", pairs);

                Log.i("DEBUG", resultstring);

                return resultstring;
            }
        }

        protected void onClickConfirmRequest() {
            new ConfirmRequest().execute(Integer.parseInt(objProfileInfo.getUser_id()));
            actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.friends")
                    + "(" + objProfileInfo.getTotal_friend() + ")");
            objProfileInfo.setIs_friend("true");
        }

        protected void onClickDenyRequest() {
            new DenyRequest().execute((Integer.parseInt(objProfileInfo.getUser_id())));
            actionFriend.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "profile.add_to_friends"));
            objProfileInfo.setIs_friend("false");
        }

    }

    @Override
    public void onResume() {
        photoTxt.setTextColor(Color.parseColor("#797979"));
        photo_button.setBackgroundColor(Color.parseColor("#f3f3f3"));
        photoImage.setImageResource(R.drawable.photo_black_icon);

        statusTxt.setTextColor(Color.parseColor("#797979"));
        share_button.setBackgroundColor(Color.parseColor("#f3f3f3"));
        shareImage.setImageResource(R.drawable.status_black_icon);
        super.onResume();
    }
}
