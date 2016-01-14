package com.brodev.socialapp.entity;

import android.text.Html;

public class FeedMini {

	/**
	 * @String full name
	 */
	private String fullname;
	
	/**
	 * @String feed title
	 */
	private String feedTitle;
	
	/**
	 * @String feed info
	 */
	private String feedInfo;
	
	/**
	 * @String feed image
	 */
	private String feedImage;
	
	/**
	 * @String feed status
	 */
	private String feedStatus;
	
	/**
	 * @String feed link
	 */
	private String feedLink;

	private String module;
	
	//CONSTRUCT
	public FeedMini() {
		
	}
	
	public FeedMini(String fullname, String feedTitle, String feedInfo,
			String feedImage, String feedStatus, String feedLink, String module) {
		super();
		this.fullname = fullname;
		this.feedTitle = feedTitle;
		this.feedInfo = feedInfo;
		this.feedImage = feedImage;
		this.feedStatus = feedStatus;
		this.feedLink = feedLink;
		this.module = module;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getFeedTitle() {
		if (feedTitle != null) {
			return Html.fromHtml(feedTitle).toString();
		}
		return feedTitle;
	}

	public void setFeedTitle(String feedTitle) {
		this.feedTitle = feedTitle;
	}

	public String getFeedInfo() {
		if (feedInfo != null) {
			return Html.fromHtml(feedInfo).toString();
		}
		return feedInfo;
	}

	public void setFeedInfo(String feedInfo) {
		this.feedInfo = feedInfo;
	}

	public String getFeedImage() {
		return feedImage;
	}

	public void setFeedImage(String feedImage) {
		this.feedImage = feedImage;
	}

	public String getFeedStatus() {
		if (feedStatus != null) {
			Html.fromHtml(feedStatus).toString();
		}
		return feedStatus;
	}

	public void setFeedStatus(String feedStatus) {
		this.feedStatus = feedStatus;
	}

	public String getFeedLink() {
		return feedLink;
	}

	public void setFeedLink(String feedLink) {
		this.feedLink = feedLink;
	}
}
