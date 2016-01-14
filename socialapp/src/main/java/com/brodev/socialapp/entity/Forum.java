package com.brodev.socialapp.entity;

import android.text.Html;

public class Forum {

	/**
	 * @Integer forum id
	 */
	private int forumId;

	/**
	 * @String forum name
	 */
	private String name;

	/**
	 * @Integer total thread
	 */
	private int totalThread;

	/**
	 * @Integer total post
	 */
	private int totalPost;

	/**
	 * @Integer is category
	 */
	private int isCategory;

	/**
	 * @Integer parent id
	 */
	private int parentId;

	/**
	 * @String image forum
	 */
	private String imageForum;

	/**
	 * @String thread id
	 */
	private String threadId;

	/**
	 * @String thread title
	 */
	private String threadTitle;

	/**
	 * @String phrase thread
	 */
	private String phraseThread;

	/**
	 * @String notice
	 */
	private String notice;
	
	// construct
	public Forum() {
	}

	public Forum(int forumId, String name, int totalThread, int totalPost,
			int isCategory, int parentId, String imageForum, String threadId,
			String threadTitle, String phraseThread, String notice) {
		super();
		this.forumId = forumId;
		this.name = name;
		this.totalThread = totalThread;
		this.totalPost = totalPost;
		this.isCategory = isCategory;
		this.parentId = parentId;
		this.imageForum = imageForum;
		this.threadId = threadId;
		this.threadTitle = threadTitle;
		this.phraseThread = phraseThread;
	}

	public String getNotice() {
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

	public String getPhraseThread() {
		return phraseThread;
	}

	public void setPhraseThread(String phraseThread) {
		this.phraseThread = phraseThread;
	}

	public String getImageForum() {
		return imageForum;
	}

	public void setImageForum(String imageForum) {
		this.imageForum = imageForum;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getForumId() {
		return forumId;
	}

	public void setForumId(int forumId) {
		this.forumId = forumId;
	}

	public String getName() {
		if (name != null) {
			return Html.fromHtml(name).toString();
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotalThread() {
		return totalThread;
	}

	public void setTotalThread(int totalThread) {
		this.totalThread = totalThread;
	}

	public int getTotalPost() {
		return totalPost;
	}

	public void setTotalPost(int totalPost) {
		this.totalPost = totalPost;
	}

	public int getIsCategory() {
		return isCategory;
	}

	public void setIsCategory(int isCategory) {
		this.isCategory = isCategory;
	}

}
