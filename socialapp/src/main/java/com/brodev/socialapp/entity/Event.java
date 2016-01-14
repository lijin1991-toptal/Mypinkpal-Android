package com.brodev.socialapp.entity;

public class Event {

	/**
	 * @String event id
	 */
	private String eventId;

	/**
	 * @Integer rsvp id
	 */
	private int rsvpId;

	/**
	 * @String user id
	 */
	private String userId;

	/**
	 * @String title
	 */
	private String title;

	/**
	 * @String event image
	 */
	private String eventImage;

	/**
	 * @String user image
	 */
	private String userImage;

	/**
	 * @boolean is liked
	 */
	private boolean isLiked;

	/**
	 * @String start time
	 */
	private String startTime;

	/**
	 * @String full name
	 */
	private String fullname;

	/**
	 * @String header phrase
	 */
	private String headerPhrase;

	/**
	 * @Integer total like
	 */
	private int totalLike;

	/**
	 * @String notice
	 */
	private String notice;

	/**
	 * @String can post comment
	 */
	private boolean canPostComment;

	/**
	 * @String time phrase
	 */
	private String timePhrase;

	/**
	 * @String category
	 */
	private String category;

	/**
	 * @String description
	 */
	private String description;

	/**
	 * @String location
	 */
	private String location;

	/**
	 * @String feed callback
	 */
	private String FeedCallBack;

	/**
	 * @String map
	 */
	private String map;

	public Event() {
	}

	public String getMap() {
		return map;
	}

	public void setMap(String map) {
		this.map = map;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFeedCallBack() {
		return FeedCallBack;
	}

	public void setFeedCallBack(String feedCallBack) {
		FeedCallBack = feedCallBack;
	}

	public String getTimePhrase() {
		return timePhrase;
	}

	public void setTimePhrase(String timePhrase) {
		this.timePhrase = timePhrase;
	}

	public boolean isCanPostComment() {
		return canPostComment;
	}

	public void setCanPostComment(boolean canPostComment) {
		this.canPostComment = canPostComment;
	}

	public int getTotalLike() {
		return totalLike;
	}

	public void setTotalLike(int totalLike) {
		this.totalLike = totalLike;
	}

	public int getRsvpId() {
		return rsvpId;
	}

	public void setRsvpId(int rsvpId) {
		this.rsvpId = rsvpId;
	}

	public String getHeaderPhrase() {
		return headerPhrase;
	}

	public void setHeaderPhrase(String headerPhrase) {
		this.headerPhrase = headerPhrase;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEventImage() {
		return eventImage;
	}

	public void setEventImage(String eventImage) {
		this.eventImage = eventImage;
	}

	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	public boolean isLiked() {
		return isLiked;
	}

	public void setLiked(boolean isLiked) {
		this.isLiked = isLiked;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

}
