package com.brodev.socialapp.entity;

public class Post {

	/**
	 * @String post id
	 */
	private String postId;

	/**
	 * @String full name
	 */
	private String fullname;

	/**
	 * @String total post
	 */
	private String totalPost;

	/**
	 * @String text
	 */
	private String text;

	/**
	 * @String user image path
	 */
	private String userImagePath;

	/**
	 * @String time phrase
	 */
	private String timePhrase;

	/**
	 * @String total like
	 */
	private String totalLike;

	/**
	 * @String notice
	 */
	private String notice;

	/**
	 * @Integer count
	 */
	private int count;

	/**
	 * @String is liked
	 */
	private String isLiked;

	/**
	 * @String quote
	 */
	private String quote;

	/**
	 * @String link post
	 */
	private String linkSharePost;

	/**
	 * @String share url link post
	 */
	private String linkShareUrlPost;

	/**
	 * @boolean is continued
	 */
	private boolean isContinued;

	// construct
	public Post() {
	}

	public Post(String postId, String fullname, String totalPost, String text,
			String userImagePath, String timePhrase, String totalLike,
			String notice, int count, String isLiked, String quote,
			String linkSharePost, String linkShareUrlPost, boolean isContinued) {
		super();
		this.postId = postId;
		this.fullname = fullname;
		this.totalPost = totalPost;
		this.text = text;
		this.userImagePath = userImagePath;
		this.timePhrase = timePhrase;
		this.totalLike = totalLike;
		this.notice = notice;
		this.count = count;
		this.isLiked = isLiked;
		this.quote = quote;
		this.linkSharePost = linkSharePost;
		this.linkShareUrlPost = linkShareUrlPost;
		this.isContinued = isContinued;
	}

	public boolean isContinued() {
		return isContinued;
	}

	public void setContinued(boolean isContinued) {
		this.isContinued = isContinued;
	}

	public String getLinkShareUrlPost() {
		return linkShareUrlPost;
	}

	public void setLinkShareUrlPost(String linkShareUrlPost) {
		this.linkShareUrlPost = linkShareUrlPost;
	}

	public String getLinkSharePost() {
		return linkSharePost;
	}

	public void setLinkSharePost(String linkSharePost) {
		this.linkSharePost = linkSharePost;
	}

	public String getQuote() {
		return quote;
	}

	public void setQuote(String quote) {
		this.quote = quote;
	}

	public String getIsLiked() {
		return isLiked;
	}

	public void setIsLiked(String isLiked) {
		this.isLiked = isLiked;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getTotalPost() {
		return totalPost;
	}

	public void setTotalPost(String totalPost) {
		this.totalPost = totalPost;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUserImagePath() {
		return userImagePath;
	}

	public void setUserImagePath(String userImagePath) {
		this.userImagePath = userImagePath;
	}

	public String getTimePhrase() {
		return timePhrase;
	}

	public void setTimePhrase(String timePhrase) {
		this.timePhrase = timePhrase;
	}

	public String getTotalLike() {
		return totalLike;
	}

	public void setTotalLike(String totalLike) {
		this.totalLike = totalLike;
	}

}
