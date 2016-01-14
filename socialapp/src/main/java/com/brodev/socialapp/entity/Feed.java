package com.brodev.socialapp.entity;

import java.util.ArrayList;

import org.json.JSONArray;

import android.text.Html;

public class Feed {
	private String user_id;
	private String feed_id;
	private String phrase;
	private String module;
	private String link;
	private String feed_icon;
	private Boolean enable_like;
	private JSONArray feed_image;
	private String time_phrase;
	private String title_phrase;
	private String title_feed;
	private String full_name;
	private String user_image;
	private int age;
	private String birthdate;

	private ArrayList<String> feedImage;
	private ArrayList<String> ImagesId;
	// photo image feed
	private String feed_image_1;
	private String feed_image_2;
	private String feed_image_3;
	private String feed_image_4;
	private String image_id_1;
	private String image_id_2;
	private String image_id_3;
	private String image_id_4;

	private String feed_status;
	private String type_id;
	private String feed_link;
	private int total_like;
	private String total_comment;
	private Boolean can_post_comment;
	private String feed_is_liked;
	private String has_like_total;
	private String item_id;
	private String comment_type_id;
	private String profile_page_id;
	// set request id
	private String page_id_request;
	private String photo_id_request;
	private String user_id_request;
	// report
	private String report_module;
	private String report_phrase;
	// share
	private Boolean no_share;
	private String privacy;
	private Boolean can_share_item_on_feed;
	private String parent_feed_id;
	private String parent_module_id;

	/**
	 * @String share feed link
	 */
	private String shareFeedLink;

	/**
	 * @String share feed link url
	 */
	private String shareFeedLinkUrl;

	/**
	 * @String feed title extra
	 */
	private String feedTitleExtra;

	/**
	 * @String feed content
	 */
	private String feedContent;

	/**
	 * @String notice
	 */
	private String notice;

	/**
	 * @boolean one feed
	 */
	private boolean continueFeed;

	/**
	 * @String like type id
	 */
	private String likeTypeId;

	/**
	 * @String like item id
	 */
	private String likeItemId;

	/**
	 * @String data cache (forum - thread_id)
	 */
	private String dataCacheId;

	/**
	 * @Object FeedMini feedMini
	 */
	private FeedMini feedMini;

	/**
	 * @String title info
	 */
	private String titleInfo;

    /**
     * @String location image
     */
    private String locationImg;

    /**
     * @String location link
     */
    private String locationLink;

	public Feed() {
		super();
	}

	public Feed(String user_id, String feed_id, String phrase, String module,
			String link, String feed_icon, Boolean enable_like,
			JSONArray feed_image, String time_phrase, String title_phrase,
			String title_feed, String full_name, String user_image, int age,
			String birthdate, ArrayList<String> feedImage,
			ArrayList<String> imagesId, String feed_image_1,
			String feed_image_2, String feed_image_3, String feed_image_4,
			String image_id_1, String image_id_2, String image_id_3,
			String image_id_4, String feed_status, String type_id,
			String feed_link, int total_like, String total_comment,
			Boolean can_post_comment, String feed_is_liked,
			String has_like_total, String item_id, String comment_type_id,
			String profile_page_id, String page_id_request,
			String photo_id_request, String user_id_request, String likeTypeId,
			String report_module, String report_phrase, Boolean no_share,
			String privacy, Boolean can_share_item_on_feed,
			String parent_feed_id, String parent_module_id,
			String feedTitleExtra, String feedContent, String notice,
			String likeItemId, boolean continueFeed, FeedMini feedMini,
			String shareFeedLink, String shareFeedLinkUrl, String dataCacheId,
			String titleInfo) {
		super();
		this.user_id = user_id;
		this.feed_id = feed_id;
		this.phrase = phrase;
		this.module = module;
		this.link = link;
		this.feed_icon = feed_icon;
		this.enable_like = enable_like;
		this.feed_image = feed_image;
		this.time_phrase = time_phrase;
		this.title_phrase = title_phrase;
		this.title_feed = title_feed;
		this.full_name = full_name;
		this.user_image = user_image;
		this.age = age;
		this.birthdate = birthdate;
		this.feedImage = feedImage;
		ImagesId = imagesId;
		this.feed_image_1 = feed_image_1;
		this.feed_image_2 = feed_image_2;
		this.feed_image_3 = feed_image_3;
		this.feed_image_4 = feed_image_4;
		this.image_id_1 = image_id_1;
		this.image_id_2 = image_id_2;
		this.image_id_3 = image_id_3;
		this.image_id_4 = image_id_4;
		this.feed_status = feed_status;
		this.type_id = type_id;
		this.feed_link = feed_link;
		this.total_like = total_like;
		this.total_comment = total_comment;
		this.can_post_comment = can_post_comment;
		this.feed_is_liked = feed_is_liked;
		this.has_like_total = has_like_total;
		this.item_id = item_id;
		this.comment_type_id = comment_type_id;
		this.profile_page_id = profile_page_id;
		this.page_id_request = page_id_request;
		this.photo_id_request = photo_id_request;
		this.user_id_request = user_id_request;
		this.report_module = report_module;
		this.report_phrase = report_phrase;
		this.no_share = no_share;
		this.privacy = privacy;
		this.can_share_item_on_feed = can_share_item_on_feed;
		this.feedTitleExtra = feedTitleExtra;
		this.feedContent = feedContent;
		this.notice = notice;
		this.continueFeed = continueFeed;
		this.feedMini = feedMini;
		this.parent_feed_id = parent_feed_id;
		this.parent_module_id = parent_module_id;
		this.likeTypeId = likeTypeId;
		this.likeItemId = likeItemId;
		this.shareFeedLink = shareFeedLink;
		this.shareFeedLinkUrl = shareFeedLinkUrl;
		this.dataCacheId = dataCacheId;
		this.titleInfo = titleInfo;
	}

    public String getLocationLink() {
        return locationLink;
    }

    public void setLocationLink(String locationLink) {
        this.locationLink = locationLink;
    }

    public String getLocationImg() {
        return locationImg;
    }

    public void setLocationImg(String locationImg) {
        this.locationImg = locationImg;
    }

    public String getTitleInfo() {
		return titleInfo;
	}

	public void setTitleInfo(String titleInfo) {
		this.titleInfo = titleInfo;
	}

	public String getDataCacheId() {
		return dataCacheId;
	}

	public void setDataCacheId(String dataCacheId) {
		this.dataCacheId = dataCacheId;
	}

	public String getShareFeedLinkUrl() {
		return shareFeedLinkUrl;
	}

	public void setShareFeedLinkUrl(String shareFeedLinkUrl) {
		this.shareFeedLinkUrl = shareFeedLinkUrl;
	}

	public String getShareFeedLink() {
		return shareFeedLink;
	}

	public void setShareFeedLink(String shareFeedLink) {
		this.shareFeedLink = shareFeedLink;
	}

	public String getLikeItemId() {
		return likeItemId;
	}

	public void setLikeItemId(String likeItemId) {
		this.likeItemId = likeItemId;
	}

	public String getParent_feed_id() {
		return parent_feed_id;
	}

	public void setParent_feed_id(String parent_feed_id) {
		this.parent_feed_id = parent_feed_id;
	}

	public String getParent_module_id() {
		return parent_module_id;
	}

	public String getLikeTypeId() {
		return likeTypeId;
	}

	public void setLikeTypeId(String likeTypeId) {
		this.likeTypeId = likeTypeId;
	}

	public void setParent_module_id(String parent_module_id) {
		this.parent_module_id = parent_module_id;
	}

	public FeedMini getFeedMini() {
		return feedMini;
	}

	public void setFeedMini(FeedMini feedMini) {
		this.feedMini = feedMini;
	}

	public boolean isContinueFeed() {
		return continueFeed;
	}

	public void setContinueFeed(boolean continueFeed) {
		this.continueFeed = continueFeed;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getFeedTitleExtra() {
		return feedTitleExtra;
	}

	public void setFeedTitleExtra(String feedTitleExtra) {
		this.feedTitleExtra = feedTitleExtra;
	}

	public String getFeedContent() {
		if (feedContent != null && feedContent.equals("null")) {
			return "";
		}
		return feedContent;
	}

	public void setFeedContent(String feedContent) {
		this.feedContent = feedContent;
	}

	public Boolean getCan_share_item_on_feed() {
		return can_share_item_on_feed;
	}

	public void setCan_share_item_on_feed(Boolean can_share_item_on_feed) {
		this.can_share_item_on_feed = can_share_item_on_feed;
	}

	public Boolean getNo_share() {
		return no_share;
	}

	public void setNo_share(Boolean no_share) {
		this.no_share = no_share;
	}

	public String getPrivacy() {
		return privacy;
	}

	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}

	public String getReport_module() {
		return report_module;
	}

	public void setReport_module(String report_module) {
		this.report_module = report_module;
	}

	public String getReport_phrase() {
		if (report_phrase != null) {
			return Html.fromHtml(report_phrase).toString();
		}
		return report_phrase;
	}

	public void setReport_phrase(String report_phrase) {
		this.report_phrase = report_phrase;
	}

	public ArrayList<String> getImagesId() {
		return ImagesId;
	}

	public void setImagesId(ArrayList<String> imagesId) {
		ImagesId = imagesId;
	}

	public String getImage_id_1() {
		return image_id_1;
	}

	public void setImage_id_1(String image_id_1) {
		this.image_id_1 = image_id_1;
	}

	public String getImage_id_2() {
		return image_id_2;
	}

	public void setImage_id_2(String image_id_2) {
		this.image_id_2 = image_id_2;
	}

	public String getImage_id_3() {
		return image_id_3;
	}

	public void setImage_id_3(String image_id_3) {
		this.image_id_3 = image_id_3;
	}

	public String getImage_id_4() {
		return image_id_4;
	}

	public void setImage_id_4(String image_id_4) {
		this.image_id_4 = image_id_4;
	}

	public String getProfile_page_id() {
		return profile_page_id;
	}

	public void setProfile_page_id(String profile_page_id) {
		this.profile_page_id = profile_page_id;
	}

	public String getComment_type_id() {
		return comment_type_id;
	}

	public void setComment_type_id(String comment_type_id) {
		this.comment_type_id = comment_type_id;
	}

	public void setFeed_Image(ArrayList<String> feedImage) {
		this.feedImage = feedImage;
	}

	public ArrayList<String> getFeed_Image() {
		return feedImage;
	}

	public String getUserId() {
		return user_id;
	}

	public void setUserId(String user_id) {
		this.user_id = user_id;
	}

	public String getFeedId() {
		return feed_id;
	}

	public void setFeedId(String feed_id) {
		this.feed_id = feed_id;
	}

	public String getItemId() {
		return item_id;
	}

	public void setItemId(String item_id) {
		this.item_id = item_id;
	}

	public String getIcon() {
		return feed_icon;
	}

	public void setIcon(String icon) {
		this.feed_icon = icon;
	}

	public String getTitleFeed() {
		return title_feed;
	}

	public void setTitleFeed(String title_feed) {
		this.title_feed = title_feed;
	}

	public String getStatus() {
		return feed_status;
	}

	public void setStatus(String feed_status) {
		this.feed_status = feed_status;
	}

	public String getFeedLink() {
		return feed_link;
	}

	public void setFeedLink(String feed_link) {
		this.feed_link = feed_link;
	}

	public String getType() {
		return type_id;
	}

	public void setType(String type_id) {
		this.type_id = type_id;
	}

	public int getTotalLike() {
		return total_like;
	}

	public void setTotalLike(int total_like) {
		this.total_like = total_like;
	}

	public String getTotalComment() {
		return total_comment;
	}

	public void setTotalComment(String total_comment) {
		this.total_comment = total_comment;
	}

	public String getImage1() {
		return feed_image_1;
	}

	public void setImage1(String image) {
		this.feed_image_1 = image;
	}

	public String getImage2() {
		return feed_image_2;
	}

	public void setImage2(String image) {
		this.feed_image_2 = image;
	}

	public String getImage3() {
		return feed_image_3;
	}

	public void setImage3(String image) {
		this.feed_image_3 = image;
	}

	public String getImage4() {
		return feed_image_4;
	}

	public void setImage4(String image) {
		this.feed_image_4 = image;
	}

	public JSONArray getFeedImage() {
		return feed_image;
	}

	public void setFeedImage(JSONArray jsonArray) {
		this.feed_image = jsonArray;
	}

	public String getUserImage() {
		return user_image;
	}

	public void setUserImage(String userimage) {
		this.user_image = userimage;
	}

	// get time
	public String getTime() {
		if (time_phrase != null) {
			return Html.fromHtml(time_phrase).toString();
		}
		return time_phrase;
	}

	public void setTime(String time) {
		this.time_phrase = time;
	}

	// get title
	public String getTitle() {
		return title_phrase;
	}

	public void setTitle(String title) {
		this.title_phrase = title;
	}

	// get full name
	public String getFullName() {
		return full_name;
	}

	public void setFullName(String name) {
		this.full_name = name;
	}

	// get enable like
	public Boolean getEnableLike() {
		return enable_like;
	}

	public void setEnableLike(Boolean like) {
		this.enable_like = like;
	}

	public Boolean getCanPostComment() {
		return can_post_comment;
	}

	public void setCanPostComment(Boolean can_post_comment) {
		this.can_post_comment = can_post_comment;
	}

	public String getFeedIsLiked() {
		return feed_is_liked;
	}

	public void setFeedIsLiked(String feed_is_liked) {
		this.feed_is_liked = feed_is_liked;
	}

	public String getHasLike() {
		return has_like_total;
	}

	public void setHasLike(String has_like_total) {
		this.has_like_total = has_like_total;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	public String getPage_id_request() {
		return page_id_request;
	}

	public void setPage_id_request(String page_id_request) {
		this.page_id_request = page_id_request;
	}

	public String getPhoto_id_request() {
		return photo_id_request;
	}

	public void setPhoto_id_request(String photo_id_request) {
		this.photo_id_request = photo_id_request;
	}

	public String getUser_id_request() {
		return user_id_request;
	}

	public void setUser_id_request(String user_id_request) {
		this.user_id_request = user_id_request;
	}
}
