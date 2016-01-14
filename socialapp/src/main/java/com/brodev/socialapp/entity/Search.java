package com.brodev.socialapp.entity;

import android.text.Html;

public class Search {
	
	/**
	 * @integer item id
	 */
	private int itemId;
	
	/**
	 * @String item title
	 */
	private String itemTitle;
	
	/**
	 * @String item type id
	 */
	private String itemTypeId;
	
	/**
	 * @String full name
	 */
	private String fullname;
	
	/**
	 * @String user image
	 */
	private String userImage;
	
	/**
	 * @String time phrase
	 */
	private String timePhrase;
	
	/**
	 * @String item link
	 */
	private String itemLink;
	
	/**
	 * @String item name 
	 */
	private String itemName;
	
	/**
	 * @String item display photo mobile
	 */
	private String itemDisplayPhotoMobile;
	
	//construct
	public Search() {
		super();
	}

	public Search(int itemId, String itemTitle, String itemTypeId, String fullname, String userImage,
			String timePhrase, String itemLink, String itemName,
			String itemDisplayPhotoMobile) {
		super();
		this.itemId = itemId;
		this.itemTitle = itemTitle;
		this.itemTypeId = itemTypeId;
		this.fullname = fullname;
		this.userImage = userImage;
		this.timePhrase = timePhrase;
		this.itemLink = itemLink;
		this.itemName = itemName;
		this.itemDisplayPhotoMobile = itemDisplayPhotoMobile;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getItemTitle() {
		if (itemTitle != null) {
			return Html.fromHtml(itemTitle).toString();
		}
		return itemTitle;
	}

	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}

	public String getItemTypeId() {
		return itemTypeId;
	}

	public void setItemTypeId(String itemTypeId) {
		this.itemTypeId = itemTypeId;
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

	public String getTimePhrase() {
		if (timePhrase != null) {
			return Html.fromHtml(timePhrase).toString();
		}
		return timePhrase;
	}

	public void setTimePhrase(String timePhrase) {
		this.timePhrase = timePhrase;
	}

	public String getItemLink() {
		return itemLink;
	}

	public void setItemLink(String itemLink) {
		this.itemLink = itemLink;
	}

	public String getItemName() {
		if (itemName != null) {
			return Html.fromHtml(itemName).toString();
		}
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemDisplayPhotoMobile() {
		return itemDisplayPhotoMobile;
	}

	public void setItemDisplayPhotoMobile(String itemDisplayPhotoMobile) {
		this.itemDisplayPhotoMobile = itemDisplayPhotoMobile;
	}
	
	
}
