package com.brodev.socialapp.entity;

import android.text.Html;

public class ForumThread {

	/**
	 * @String thread id
	 */
	private String threadId;

	/**
	 * @String thread title
	 */
	private String threadTitle;

	/**
	 * @String thread phrase
	 */
	private String threadPhrase;

	/**
	 * @String total reply
	 */
	private String totalReply;

	/**
	 * @String total view
	 */
	private String totalView;

	/**
	 * @String notice
	 */
	private String notice;

	/**
	 * @String user image
	 */
	private String userImage;

	/**
	 * @boolean is continued
	 */
	private boolean isContinued;

	public ForumThread() {

	}

	public ForumThread(String threadId, String threadTitle,
			String threadPhrase, String totalReply, String totalView,
			String notice, String userImage, boolean isContinued) {
		super();
		this.threadId = threadId;
		this.threadTitle = threadTitle;
		this.threadPhrase = threadPhrase;
		this.totalReply = totalReply;
		this.totalView = totalView;
		this.notice = notice;
		this.userImage = userImage;
		this.isContinued = isContinued;
	}

	public boolean isContinued() {
		return isContinued;
	}

	public void setContinued(boolean isContinued) {
		this.isContinued = isContinued;
	}

	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	public String getNotice() {
		if (notice != null) {
			return Html.fromHtml(notice).toString();
		}
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getThreadTitle() {
		if (threadTitle != null) {
			return Html.fromHtml(threadTitle).toString();
		}
		return threadTitle;
	}

	public void setThreadTitle(String threadTitle) {
		this.threadTitle = threadTitle;
	}

	public String getThreadPhrase() {
		if (threadPhrase != null) {
			return Html.fromHtml(threadPhrase).toString();
		}
		return threadPhrase;
	}

	public void setThreadPhrase(String threadPhrase) {
		this.threadPhrase = threadPhrase;
	}

	public String getTotalReply() {
		return totalReply;
	}

	public void setTotalReply(String totalReply) {
		this.totalReply = totalReply;
	}

	public String getTotalView() {
		return totalView;
	}

	public void setTotalView(String totalView) {
		this.totalView = totalView;
	}

}
