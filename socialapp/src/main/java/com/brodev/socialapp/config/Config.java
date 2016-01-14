package com.brodev.socialapp.config;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.res.AssetManager;

import com.brodev.socialapp.badgeview.BadgeView;
import com.brodev.socialapp.entity.Event;
import com.brodev.socialapp.entity.Feed;
import com.brodev.socialapp.entity.ForumThread;
import com.brodev.socialapp.entity.Post;
import com.brodev.socialapp.handler.CoreXMLHandler;
import com.quickblox.q_municate_core.models.User;

public class Config 
{
	//input stream
	private InputStream is;
	
	public static String CORE_URL = null;
	// project id on google
    public static String SENDER_ID = null;
	
	public static final String CORE_URL_REGISTER = "index.php?do=/mobile/user/register/";
	
	public static final String URL_CHECKKEY = "index.php?do=/accountapi/checkkey/";
	
	public static final String URL_GET_SETTING = "index.php?do=/accountapi/setting/";
	
	public static final String URL_REPORT = "index.php?do=/accountapi/report/";


	//Log in with facebook
	public static final String URL_FACEBOOK = "index.php?do=/accountapi/facebook/";
	
	public static final String URL_API = "api.php";
	
	public static final String METHOD = "?method=accountapi.";
		
	public static final String URL_REQUEST_URL = "&method=accountapi.redirect&url=";

	/******************************************************************************
	 * CHAT SERVER
	 *****************************************************************************/
	//register chat server	
	public static String CHAT_REGISTER = "/register/";
	//get log chat
	public static String CHAT_LOG = "/chat/get/";
	//get online user list
	public static String CHAT_ONLINE_LIST = "/online";
	//get sticker group
	public static String CHAT_STICKER_GROUP = "/stickers/";
	//get unread message list
	public static String CHAT_UNREAD = "/chat/unread/";
	//request online friend time
	public static int ONLINE_FRIEND_REQUEST_TIME = 30000;
	
	public static long TIME_REQUEST = 0;
	
	public static int CHAT_STICKER = 1;
	
	public static String hashDevice = null;
	
	public static String MESSENGER_AGENT_INFO = null;
	
	public static long TIME_STICKER_GROUP = 21600000;
	public static long TIME_STICKER_GROUP_REQUEST = 0;
	
	public static long TIME_STICKER = 21600000;
	//******************************************************************************

    /******************************************************************************
     * QuickBlox SERVER
     *****************************************************************************/
    // Quickblox API
    public static String QB_ID = "7052";
    public static String QBAUTH_KEY = "TFpRCByLuSQ8mCN";
    public static String QBAUTH_SECRET = "XMpzyKX6wKJKba7";
    public static int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    //******************************************************************************

	public static final String DISPLAY_MESSAGE_ACTION = "com.brodev.socialnaap.DISPLAY_MESSAGE";
	
	public static final String DISPLAY_CHAT_ACTION = "com.brodev.socialnaap.DISPLAY_CHAT";
	
	public static final String DISPLAY_CHAT_STICKER_ACTION = "com.brodev.socialnaap.DISPLAY_CHAT_STICKER";
	
	public static final String DISPLAY_SHOW_STICKER_ACTION = "com.brodev.socialnaap.DISPLAY_SHOW_STICKER";
	
	public static final String REQUEST_GET_FRIEND_ONLINE_ACTION = "com.brodev.socialnaap.REQUEST_GET_FRIEND_ONLINE_ACTION";
	
	public static final String REQUEST_GET_SIDEBAR = "com.brodev.socialnaap.REQUEST_GET_SIDEBAR";
	
	public static final String DISPLAY_UPDATE_RSVP = "com.brodev.socialnaap.RSVP";
	
	public static final String DISPLAY_REQUEST_CHILDREN_COUNTRY = "com.brodev.socialnaap.CHILDREN_COUNTRY";
	
	public static final String DISPLAY_REQUEST_CHILDREN_CATEGORY = "com.brodev.socialnaap.CHILDREN_CATEGORY";
	
	public static final String DISPLAY_CHAT_ACTION_READ = "com.brodev.socialnaap.DISPLAY_CHAT_ACTION_READ";
	
	public static Feed feed = new Feed();
	public static Event event = new Event();
	public static ForumThread forumThread = new ForumThread();
	public static Post post = new Post();
	
	//sliding mode
	public static final int LEFT_SLIDING = 1;
	public static final int RIGHT_SLIDEING = 2;
	public static final int LEFT_RIGHT_SLIDING = 3;
	
	/**
	 * notification count
	 */
	public static int notifyFriendCount = 0;
	public static int notifyMailCount = 0;
	public static int notifyCount = 0;

	public static BadgeView friendBadge, mailBadge, notifyBadge = null;
	
	/**
	 * Setting for facebook
	 */
	public static boolean displayFB = false;

	public static int NETWORK_CONNECT_TIMEOUT = 40000;
	public static int NETWORK_READ_TIMEOUT = 40000;


    /**
     * Fyber for video Advertise
     */
    public static final String fyberAppId = "28462";
    public static final String fyberUserId = null;
    public static final String fyberSecurityToken = "97985441ba0e9f528b9c683fae28998f";


	public Config() {
	}

	/**
	 * Get xml handler
	 * @param context
	 * @param assetManager
	 * @return xml handler
	 */
	public CoreXMLHandler getUrlXmlHandler(Context context, AssetManager assetManager) {
		//create core handler
		CoreXMLHandler coreXMLHandler = new CoreXMLHandler();
		
		try {
			is = assetManager.open("config.xml");
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			
			xr.setContentHandler(coreXMLHandler);
			InputSource inStream = new InputSource(is);
			xr.parse(inStream);
			
			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return coreXMLHandler;
	}
	
	/**
	 * Read core url
	 * @param coreXML
	 * @return url
	 */
	public String readUrl(CoreXMLHandler coreXML) {
		String url = null;
		try {
			url = coreXML.getUrl();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return url;
	}
	
	/**
	 * Read gcm key
	 * @param coreXML
	 * @return gcm key
	 */
	public String readGCMKey(CoreXMLHandler coreXML) {
		String gcmKey = null;
		try {
			gcmKey = coreXML.getGcmKey();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return gcmKey;
	}
	
	/**
	 * Make url request
	 * @param params
	 * @param post
	 * @return
	 */
	public static String makeUrl(String url, String params, Boolean post) {
		String URL = null;
		if (post == true) {
			URL = url + URL_API + METHOD + params;
		} else {
			URL = url + URL_API;
		}
		return URL;
	}
	
}
