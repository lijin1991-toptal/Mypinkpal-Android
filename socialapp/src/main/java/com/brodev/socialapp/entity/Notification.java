package com.brodev.socialapp.entity;

import android.text.Html;

public class Notification {
	
	/**
	 * @integer notification id
	 */
	private int notificationId;
	
	/**
	 * @String link web
	 */
	private String link;
	
	/**
	 * @String message
	 */
	private String mesage;
	
	/**
	 * @String time phrase
	 */
	private String timePhrase;
	
	/**
	 * @String user image link
	 */
	private String userImage;
	
	/**
	 * @String icon link
	 */
	private String icon;
	
	/**
	 * String request route
	 */
	private String route;
	
	/**
	 * @String notice
	 */
	private String notice;
	
	/**
	 * @String item id
	 */
	private String itemId;
	
	/**
	 * @String type id
	 */
	private String typeId;
	
	/**
	 * @String user id
	 */
	private String userId;

    /**
     * @String type id
     */
    private String qbDialogId;
	
	//construct
	public Notification() {
		
	}
	
	public Notification(int notificationId, String link, String mesage,
			String timePhrase, String userImage, String icon, String route, String notice, String itemId, String typeId, String userId, String qbDialogId) {
		super();
		this.notificationId = notificationId;
		this.link = link;
		this.mesage = mesage;
		this.timePhrase = timePhrase;
		this.userImage = userImage;
		this.icon = icon;
		this.route = route;
		this.notice = notice;
		this.itemId = itemId;
		this.typeId = typeId;
		this.userId = userId;
        this.qbDialogId = qbDialogId;
	}

    public String getQbDialogId() {
        return qbDialogId;
    }

    public void setQbDialogId(String qbDialogId) {
        this.qbDialogId = qbDialogId;
    }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
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

	public int getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getMesage() {
		return mesage;
	}

	public void setMesage(String mesage) {
		this.mesage = mesage;
	}

	public String getTimePhrase() {
		if (timePhrase != null) {
			return Html.fromHtml(timePhrase).toString();
		}
		return timePhrase;
	}

	public void setTimePhrase(String timePhrase) {
		this.timePhrase = timePhrase;
	}

	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
}
