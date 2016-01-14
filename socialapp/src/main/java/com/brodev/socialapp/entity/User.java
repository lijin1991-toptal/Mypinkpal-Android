package com.brodev.socialapp.entity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;

import com.brodev.socialapp.utils.ActivityLifecycleHandler;
import com.brodev.socialapp.view.media.MediaPlayerManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBSettings;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User extends MultiDexApplication {

    private static User instance;
    private MediaPlayerManager soundPlayer;

    public static User getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initApplication();
        registerActivityLifecycleCallbacks(new ActivityLifecycleHandler());

        // init value
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(5000000)
                        // 5 Mb
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    /**
     * core url
     */
    private String coreUrl;

    /**
     * Token Key
     */
    private String tokenkey;

    /**
     * User ID after login
     */
    private String userId;

    private String quickbloxid;
    private String quickbloxpswd;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String userName;


    /**
     * Status info
     */
    private String status;

    /**
     * message info
     */
    private String message;

    /**
     * full name
     */
    private String full_name;

    /**
     * dob_setting
     */
    private String dob_setting;

    /**
     * location_phrase
     */
    private String location_phrase;

    /*
     * country_iso
     */
    private String country_iso;

    /*
     * city_location
     */
    private String city_location;

    /*
     * postal_code
     */
    private String postal_code;

    /*
     * birthday_time_stamp
     */
    private String birthday_time_stamp;

    /*
     * sexuality
     */
    private String sexuality;

    /*
     * religion
     */
    private String religion;

    /*
     * relation_id
     */
    private String relation_id;

    /*
     * relation_with
     */
    private String relation_with;

    /*
     * relation
     */
    private String relation;

    /*
     * previous_relation_type
     */
    private String previous_relation_type;

    /*
     * previous_relation_with
     */
    private String previous_relation_with;

    /*
     * signature
     */
    private String signature;

    /*
     * use_timeline
     */
    private String use_timeline;

    /*
     * custom about_me
     */
    private String custom_aboutme;

    /*
     * custom who_i_d_like_to_meet
     */
    private String custom_whomeet;

    /*
     * custom movies
     */
    private String custom_movies;

    /*
     * custom interests
     */
    private String custom_interests;

    /*
     * custom music
     */
    private String custom_music;

    /*
     * custom smoker
     */
    private String custom_smoker;

    /*
     * custom drinker
     */
    private String custom_drinker;

    // privacy settings
    private String view_your_wall;

    private String share_your_wall;

    private String view_your_friends;

    private String received_gifts;

    private String send_you_message;

    private String view_photos_your_profile;

    private String can_send_pokes;

    private String view_your_profile;

    private String view_your_basic_information;

    private String view_your_profile_information;

    private String view_your_location;

    private String rate_your_profile;

    private String display_rss_subscribers;

    private String subscribe_your_rss_feed;

    private String view_who_your_profile;

    private String who_tag_written_contexts;

    private String date_of_birth;

    private String privacy_blogs;

    private String privacy_events;

    private String privacy_songs;

    private String privacy_photos;

    private String privacy_polls;

    private String privacy_quizzes;

    private String privacy_videos;

    private String new_comments;

    private String comments_for_approval;

    private String forum_subscriptions;

    private String new_friend;

    private String friend_request;

    private String new_gift;

    private String notification_for_likes;

    private String new_messages;

    private String receive_newsletter;

    private String subscribe_all_mails;

    private ArrayList<String> blocked_users;

    private String enable_invisible_mode;


    /**
     * user_image
     */
    private String user_image;

    /**
     * birthday_phrase
     */
    private String birthday_phrase;

    /**
     * @String email
     */
    private String email;

    /**
     * cover_photo
     */
    private String cover_photo;

    /**
     * @string Gender
     */
    private String gender;

    /**
     * @string User Gender
     */
    private String userGender;

    /**
     * @String admob key
     */
    private String key_admob;

    /**
     * @Boolean registerGcm
     */
    private boolean registerGCM;

    /**
     * @String chat key
     */
    private String chatKey;

    /**
     * @String secret key
     */
    private String chatSecretKey;

    /**
     * @String chat server url
     */
    private String chatServerUrl;

    /**
     * @String token chat server
     */
    private String tokenChatServer;

    /**
     * @String color
     */
    private String color;

    /**
     * @String check in
     */
    private String checkin;

    /**
     * @String google key

     */
    private String googleKey;


    private long latitude;
    private long longitude;

    private int credits;

    /* Quickblox */
    private QBUser currentUser;
    private Map<Integer, QBUser> dialogsUsers = new HashMap<Integer, QBUser>();

    public QBUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(QBUser currentUser) {
        this.currentUser = currentUser;
    }

    public Map<Integer, QBUser> getDialogsUsers() {
        return dialogsUsers;
    }

    public void setDialogsUsers(List<QBUser> setUsers) {
        dialogsUsers.clear();

        for (QBUser user : setUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }

    public void addDialogsUsers(List<QBUser> newUsers) {
        for (QBUser user : newUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }

    public Integer getOpponentIDForPrivateDialog(QBDialog dialog){
        Integer opponentID = -1;
        for(Integer userID : dialog.getOccupants()){
            if(!userID.equals(getCurrentUser().getId())){
                opponentID = userID;
                break;
            }
        }
        return opponentID;
    }
    /* end */

    public String getGoogleKey() {
        if (googleKey == null) {
            googleKey = getValue(googleKey, "googleKey");
        }
        return googleKey;
    }

    public void setGoogleKey(String googleKey) {
        this.googleKey = googleKey;
    }

    public String getCheckin() {
        if (checkin == null) {
            checkin = getValue(checkin, "checkin");
        }
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getColor() {
        if (color == null) {
            color = getValue(color, "color");
        }
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTokenChatServer() {
        return tokenChatServer;
    }

    public void setTokenChatServer(String tokenChatServer) {
        this.tokenChatServer = tokenChatServer;
    }

    public String getChatKey() {
        if (chatKey == null) {
            chatKey = getValue(chatKey, "chat_server_key");
        }
        return chatKey;
    }

    public void setChatKey(String chatKey) {
        this.chatKey = chatKey;
    }

    public String getChatSecretKey() {
        if (chatSecretKey == null) {
            chatSecretKey = getValue(chatSecretKey, "chat_server_secret");
        }
        return chatSecretKey;
    }

    public void setChatSecretKey(String chatSecretKey) {
        this.chatSecretKey = chatSecretKey;
    }

    public String getChatServerUrl() {
        if (chatServerUrl == null) {
            chatServerUrl = getValue(chatServerUrl, "chat_server_url");
        }
        return chatServerUrl;
    }

    public void setChatServerUrl(String chatServerUrl) {
        this.chatServerUrl = chatServerUrl;
    }

    public String getCoreUrl() {
        if (coreUrl == null) {
            coreUrl = getValue(coreUrl, "core_url");
        }
        return coreUrl;
    }

    public void setCoreUrl(String coreUrl) {
        this.coreUrl = coreUrl;
    }

    public boolean isRegisterGCM() {
        return registerGCM;
    }

    public void setRegisterGCM(boolean registerGCM) {
        this.registerGCM = registerGCM;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String gender) {
        this.userGender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getLocation_info() {
        return location_info;
    }

    public void setLocation_info(String location_info) {
        this.location_info = location_info;
    }

    public String getLast_login() {
        return Last_login;
    }

    public void setLast_login(String last_login) {
        Last_login = last_login;
    }

    public String getMember_since() {
        return Member_since;
    }

    public void setMember_since(String member_since) {
        Member_since = member_since;
    }

    public String getMembership() {
        return Membership;
    }

    public void setMembership(String membership) {
        Membership = membership;
    }

    public String getProfile_views() {
        return Profile_views;
    }

    public void setProfile_views(String profile_views) {
        Profile_views = profile_views;
    }

    public String getRSS_Subscribers() {
        return RSS_Subscribers;
    }

    public void setRSS_Subscribers(String rSS_Subscribers) {
        RSS_Subscribers = rSS_Subscribers;
    }


    public String getKey_admob() {
        if (key_admob == null) {
            key_admob = getValue(key_admob, "key_admob");
        }
        return key_admob;
    }

    public void setKey_admob(String key_admob) {
        this.key_admob = key_admob;
    }

    /* Edit profile */
    public String getCountry_iso() {
        return country_iso;
    }

    public void setCountry_iso(String country) {
        this.country_iso = country;
    }

    public String getCity_location() {
        if (city_location == null)
            city_location = "";
        return city_location;
    }

    public void setCity_location(String city) {
        this.city_location = city;
    }

    public String getPostal_code() {
        if (postal_code == null)
            postal_code = "";
        return postal_code;
    }

    public void setPostal_code(String postal) {
        this.postal_code = postal;
    }

    public String getBirthday_time_stamp() {
        return birthday_time_stamp;
    }

    public void setBirthday_time_stamp(String time_stamp) {
        this.birthday_time_stamp = time_stamp;
    }

    public String getSexuality() {
        return sexuality;
    }

    public void setSexuality(String sex) {
        this.sexuality = sex;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getRelation_id() {
        return relation_id;
    }

    public void setRelation_id(String relation_id) {
        this.relation_id = relation_id;
    }

    public String getRelation_with() {
        return relation_with;
    }

    public void setRelation_with(String relation_with) {
        this.relation_with = relation_with;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String rel) {
        this.relation = rel;
    }

    public String getPrevious_relation_with() {
        return previous_relation_with;
    }

    public void setPrevious_relation_with(String previous_relation_with) {
        this.previous_relation_with = previous_relation_with;
    }

    public String getPrevious_relation_type() {
        return previous_relation_type;
    }

    public void setPrevious_relation_type(String previous_relation_type) {
        this.previous_relation_type = previous_relation_type;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getUse_timeline() {
        return use_timeline;
    }

    public void setUse_timeline(String use_timeline) {
        this.use_timeline = use_timeline;
    }

    public String getCustom_aboutme() {
        return custom_aboutme;
    }

    public void setCustom_aboutme(String custom_aboutme) {
        this.custom_aboutme = custom_aboutme;
    }

    public String getCustom_whomeet() {
        return custom_whomeet;
    }

    public void setCustom_whomeet(String custom_whomeet) {
        this.custom_whomeet = custom_whomeet;
    }

    public String getCustom_interests() {
        return custom_interests;
    }

    public void setCustom_interests(String custom_interests) {
        this.custom_interests = custom_interests;
    }

    public String getCustom_music() {
        return custom_music;
    }

    public void setCustom_music(String custom_music) {
        this.custom_music = custom_music;
    }

    public String getCustom_movies() {
        return custom_movies;
    }

    public void setCustom_movies(String custom_movies) {
        this.custom_movies = custom_movies;
    }

    public String getCustom_smoker() {
        return custom_smoker;
    }

    public void setCustom_smoker(String custom_smoker) {
        this.custom_smoker = custom_smoker;
    }

    public String getCustom_drinker() {
        return custom_drinker;
    }

    public void setCustom_drinker(String custom_drinker) {
        this.custom_drinker = custom_drinker;
    }

    public String getView_your_wall() {
        return view_your_wall;
    }

    public void setView_your_wall(String wall) {
        this.view_your_wall = wall;
    }

    public String getShare_your_wall() {
        return share_your_wall;
    }

    public void setShare_your_wall(String share) {
        this.share_your_wall = share;
    }

    public String getView_your_friends() {
        return view_your_friends;
    }

    public void setView_your_friends(String friends) {
        this.view_your_friends = friends;
    }

    public String getReceived_gifts() {
        return received_gifts;
    }

    public void setReceived_gifts(String gifts) {
        this.received_gifts = gifts;
    }

    public String getSend_you_message() {
        return send_you_message;
    }

    public void setSend_you_message(String message) {
        this.send_you_message = message;
    }

    public String getView_photos_your_profile() {
        return view_photos_your_profile;
    }

    public void setView_photos_your_profile(String profile) {
        this.view_photos_your_profile = profile;
    }

    public String getCan_send_pokes() {
        return can_send_pokes;
    }

    public void setCan_send_pokes(String pokes) {
        this.can_send_pokes = pokes;
    }

    public String getView_your_profile() {
        return view_your_profile;
    }

    public void setView_your_profile(String profile) {
        this.view_your_profile = profile;
    }

    public String getView_your_basic_information() {
        return view_your_basic_information;
    }

    public void setView_your_basic_information(String info) {
        this.view_your_basic_information = info;
    }

    public String getView_your_profile_information() {
        return view_your_profile_information;
    }

    public void setView_your_profile_information(String info) {
        this.view_your_profile_information = info;
    }

    public String getView_your_location() {
        return view_your_location;
    }

    public void setView_your_location(String location) {
        this.view_your_location = location;
    }

    public String getRate_your_profile() {
        return rate_your_profile;
    }

    public void setRate_your_profile(String rate) {
        this.rate_your_profile = rate;
    }

    public String getDisplay_rss_subscribers() {
        return display_rss_subscribers;
    }

    public void setDisplay_rss_subscribers(String rss) {
        this.display_rss_subscribers = rss;
    }

    public String getSubscribe_your_rss_feed() {
        return subscribe_your_rss_feed;
    }

    public void setSubscribe_your_rss_feed(String feed) {
        this.subscribe_your_rss_feed = feed;
    }

    public String getView_who_your_profile() {
        return view_who_your_profile;
    }

    public void setView_who_your_profile(String profile) {
        this.view_who_your_profile = profile;
    }

    public String getWho_tag_written_contexts() {
        return who_tag_written_contexts;
    }

    public void setWho_tag_written_contexts(String tag) {
        this.who_tag_written_contexts = tag;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date) {
        this.date_of_birth = date;
    }

    public String getPrivacy_blogs() {
        return privacy_blogs;
    }

    public void setPrivacy_blogs(String blogs) {
        this.privacy_blogs = blogs;
    }

    public String getPrivacy_events() {
        return privacy_events;
    }

    public void setPrivacy_events(String event) {
        this.privacy_events = event;
    }

    public String getPrivacy_songs() {
        return privacy_songs;
    }

    public void setPrivacy_songs(String songs) {
        this.privacy_songs = songs;
    }

    public String getPrivacy_photos() {
        return privacy_photos;
    }

    public void setPrivacy_photos(String photos) {
        this.privacy_photos = photos;
    }

    public String getPrivacy_polls() {
        return privacy_polls;
    }

    public void setPrivacy_polls(String polls) {
        this.privacy_polls = polls;
    }

    public String getPrivacy_quizzes() {
        return privacy_quizzes;
    }

    public void setPrivacy_quizzes(String quizzes) {
        this.privacy_quizzes = quizzes;
    }

    public String getPrivacy_videos() {
        return privacy_videos;
    }

    public void setPrivacy_videos(String videos) {
        this.privacy_videos = videos;
    }

    public String getNew_comments() {
        return new_comments;
    }

    public void setNew_comments(String comments) {
        this.new_comments = comments;
    }

    public String getComments_for_approval() {
        return comments_for_approval;
    }

    public void setComments_for_approval(String approval) {
        this.comments_for_approval = approval;
    }

    public String getForum_subscriptions() {
        return forum_subscriptions;
    }

    public void setForum_subscriptions(String forum) {
        this.forum_subscriptions = forum;
    }

    public String getNew_friend() {
        return new_friend;
    }

    public void setNew_friend(String friend) {
        this.new_friend = friend;
    }

    public String getFriend_request() {
        return friend_request;
    }

    public void setFriend_request(String request) {
        this.friend_request = request;
    }

    public String getNew_gift() {
        return new_gift;
    }

    public void setNew_gift(String gifts) {
        this.new_gift = gifts;
    }

    public String getNotification_for_likes() {
        return notification_for_likes;
    }

    public void setNotification_for_likes(String likes) {
        this.notification_for_likes = likes;
    }

    public String getNew_messages() {
        return new_messages;
    }

    public void setNew_messages(String message) {
        this.new_messages = message;
    }

    public String getReceive_newsletter() {
        return receive_newsletter;
    }

    public void setReceive_newsletter(String news) {
        this.receive_newsletter = news;
    }

    public String getSubscribe_all_mails() {
        return subscribe_all_mails;
    }

    public void setSubscribe_all_mails(String mail) {
        this.subscribe_all_mails = mail;
    }

    public ArrayList<String> getBlocked_users() {
        return blocked_users;
    }

    public void setBlocked_users(ArrayList<String> arr) {
        this.blocked_users = arr;
    }

    public String getEnable_invisible_mode() {
        return enable_invisible_mode;
    }

    public void setEnable_invisible_mode(String mode) {
        this.enable_invisible_mode = mode;
    }

    /**
     * @string Age
     */
    private String Age;

    /**
     * @string Location
     */
    private String location_info;

    /**
     * @string Last_login
     */
    private String Last_login;

    /**
     * @string Member_since
     */
    private String Member_since;

    /**
     * @string Membership
     */
    private String Membership;

    /**
     * @string Profile_views
     */
    private String Profile_views;

    /**
     * @string RSS_Subscribers
     */
    private String RSS_Subscribers;

    //construct
    public User() {

    }

    /**
     * Get Value from key
     *
     * @param value
     * @param key
     * @return value
     */
    public String getValue(String value, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString(key, null) != null) {
            value = prefs.getString(key, null);
        }
        return value;
    }


    public String getEmail() {
        if (email == null) {
            email = getValue(email, "email");
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenkey() {
        if (tokenkey == null) {
            tokenkey = getValue(tokenkey, "token_key");
        }
        return tokenkey;
    }

    public void setTokenkey(String tokenkey) {
        this.tokenkey = tokenkey;
    }

    public String getUserId() {
        if (userId == null) {
            userId = getValue(userId, "user_id");
        }
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFullname() {
        if (full_name == null) {
            full_name = getValue(full_name, "full_name");
        }
        return full_name;
    }

    public void setFullname(String full_name) {
        this.full_name = full_name;
    }

    public String getDob() {
        if (dob_setting == null) {
            dob_setting = getValue(dob_setting, "dob_setting");
        }
        return dob_setting;
    }

    public void setDob(String dob_setting) {
        this.dob_setting = dob_setting;
    }

    public String getLocation() {
        if (location_phrase == null) {
            location_phrase = getValue(location_phrase, "location_phrase");
        }
        return location_phrase;
    }

    public void setLocation(String location_phrase) {
        this.location_phrase = location_phrase;
    }

    public String getUserImage() {
        if (user_image == null) {
            user_image = getValue(user_image, "photo_75px_square");
        }
        return user_image;
    }

    public void setUserImage(String user_image) {
        this.user_image = user_image;
    }

    public String getBirthday() {
        if (birthday_phrase == null) {
            birthday_phrase = getValue(birthday_phrase, "birthday_phrase");
        }
        return birthday_phrase;
    }

    public void setBirthday(String birthday_phrase) {
        this.birthday_phrase = birthday_phrase;
    }

    public String getCoverPhoto() {
        if (cover_photo == null) {
            cover_photo = getValue(cover_photo, "cover_photo");
        }
        return cover_photo;
    }


    public String getQuickbloxid() {
        return quickbloxid;
    }

    public void setQuickbloxid(String quickbloxid) {
        this.quickbloxid = quickbloxid;
    }

    public String getQuickbloxpswd() {
        return quickbloxpswd;
    }

    public void setQuickbloxpswd(String quickbloxpswd) {
        this.quickbloxpswd = quickbloxpswd;
    }


    public void setCoverPhoto(String cover_photo) {
        this.cover_photo = cover_photo;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public MediaPlayerManager getMediaPlayer() {
        return soundPlayer;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getCredits() {
        return this.credits;
    }

    private void initApplication() {
        instance = this;
        QBChatService.setDebugEnabled(true);
        QBSettings.getInstance().fastConfigInit(ConstsCore.QB_APP_ID, ConstsCore.QB_AUTH_KEY, ConstsCore.QB_AUTH_SECRET);
        soundPlayer = new MediaPlayerManager(this);
        new PrefsHelper(this);
    }
}
