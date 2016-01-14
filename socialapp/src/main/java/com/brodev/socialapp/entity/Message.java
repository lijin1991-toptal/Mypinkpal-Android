package com.brodev.socialapp.entity;

import android.text.Html;

public class Message {

	/**
	 * @integer thread id
	 */
	private int threadId;

	/**
	 * @String preview
	 */
	private String preview;

	/**
	 * @string time phrase
	 */
	private String timephrase;

	/**
	 * @integer to user id
	 */
	private int toUserId;

	/**
	 * @String full name
	 */
	private String fullname;

	/**
	 * @String user image (url)
	 */
	private String userImage;

	/**
	 * @boolean is read
	 */
	private boolean isRead;

	/**
	 * @boolean is block
	 */
	private boolean isBlock;

	/**
	 * @String notice
	 */
	private String notice;

	public Message() {
		super();
	}

	public Message(int threadId, String preview, String timephrase,
			int toUserId, String fullname, String userImage, String notice,
			boolean isRead, boolean isBlock) {
		super();
		this.threadId = threadId;
		this.preview = preview;
		this.timephrase = timephrase;
		this.toUserId = toUserId;
		this.fullname = fullname;
		this.userImage = userImage;
		this.notice = notice;
		this.isRead = isRead;
		this.isBlock = isBlock;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public boolean isBlock() {
		return isBlock;
	}

	public void setBlock(boolean isBlock) {
		this.isBlock = isBlock;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public String getTimephrase() {
		if (timephrase != null) {
			return Html.fromHtml(timephrase).toString();
		}
		return timephrase;
	}

	public void setTimephrase(String timephrase) {
		this.timephrase = timephrase;
	}

	public int getToUserId() {
		return toUserId;
	}

	public void setToUserId(int toUserId) {
		this.toUserId = toUserId;
	}

	public String getFullname() {
		if (fullname != null) {
			return Html.fromHtml(fullname).toString();
		}
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

}
