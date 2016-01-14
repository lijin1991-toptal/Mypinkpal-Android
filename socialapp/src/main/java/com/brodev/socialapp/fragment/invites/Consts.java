package com.brodev.socialapp.fragment.invites;

import com.mypinkpal.app.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * Created by psyh on 11/19/14.
 */
public class Consts {

    // Facebook Wall
    public static final String FB_WALL_PARAM_NAME = "name";
    public static final String FB_WALL_PARAM_DESCRIPTION = "description";
    public static final String FB_WALL_PARAM_LINK = "link";
    public static final String FB_WALL_PARAM_PICTURE = "picture";
    public static final String FB_WALL_PARAM_PLACE = "place";
    public static final String FB_WALL_PARAM_TAGS = "tags";
    public static final String FB_WALL_PARAM_FEED = "me/feed";

    // Facebook Request
    public static final String FB_REQUEST_PARAM_MESSAGE = "message";
    public static final String FB_REQUEST_PARAM_TITLE = "title";

    public static final String TYPE_OF_EMAIL = "message/rfc822";

    public static final int ZERO_INT_VALUE = 0;
    public static final long ZERO_LONG_VALUE = 0L;
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";

    public static final DisplayImageOptions UIL_USER_AVATAR_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.placeholder_user).showImageForEmptyUri(R.drawable.placeholder_user)
            .showImageOnFail(R.drawable.placeholder_user).cacheOnDisc(true).cacheInMemory(true).build();

}
