 package com.brodev.socialapp.entity;

import android.text.Html;

public class Menu {
	/**
	 * @string phrase
	 */
	private String phrase;

	/**
	 * @string url 
	 */
	private String url = "";

	/**
	 * @string link
	 */
	private String link;

	/**
	 * @string icon link
	 */
	private String icon;

	/**
	 * @string active
	 */
	private String isActive;
	
	/**
	 * @int counter
	 */
	private int counter;
	
	/**
	 * @boolean header
	 */
	private boolean isHeader;
	
	/**
	 * @int id icon 
	 */
	private int idIcon;
	
	/**
	 * @int id title
	 */
	private int idTitle;
	
	/**
	 * @String page_id
	 */
	private String page_id;

	/**
	 * @String isUser
	 */
	private boolean isUser;
	
	// constructor
	public Menu() {
		super();
	}

	public Menu(String phrase, String url, String link, String icon, String isActive, int counter, boolean isHeader) {
		super();
		this.phrase = phrase;
		this.url = url;
		this.link = link;
		this.icon = icon;
		this.isActive = isActive;
		this.counter = counter;
		this.isHeader = isHeader;
	}
	
	public Menu(String phrase, String url, String link, String icon, String isActive, boolean isHeader) {
		super();
		this.phrase = phrase;
		this.url = url;
		this.link = link;
		this.icon = icon;
		this.isActive = isActive;
		this.isHeader = isHeader;
	}
	

	public String getPage_id() {
		return page_id;
	}

	public void setPage_id(String page_id) {
		this.page_id = page_id;
	}

	public Menu(String phrase, String url, String link, String icon,
			String isActive, int counter, boolean isHeader, int idIcon,
			int idTitle, String page_id, boolean isUser) {
		super();
		this.phrase = phrase;
		this.url = url;
		this.link = link;
		this.icon = icon;
		this.isActive = isActive;
		this.counter = counter;
		this.isHeader = isHeader;
		this.idIcon = idIcon;
		this.idTitle = idTitle;
		this.page_id = page_id;
		this.isUser = isUser;
	}



	public Menu(String phrase, int id_icon, String url, boolean isHeader) {
		super();
		this.phrase = phrase;
		this.idIcon = id_icon;
		this.url = url;
		this.isHeader = isHeader;
	}
	
	public Menu(String phrase, String url, String link, String icon, String isActive) {
		super();
		this.phrase = phrase;
		this.url = url;
		this.link = link;
		this.icon = icon;
		this.isActive = isActive;
	}
	
	public Menu(String phrase, boolean isHeader) {
		super();
		this.phrase = phrase;
		this.isHeader = isHeader;
	}
	
	public boolean isUser() {
		return isUser;
	}

	public void setUser(boolean isUser) {
		this.isUser = isUser;
	}
	
	
	public int getIdTitle() {
		return idTitle;
	}

	public void setIdTitle(int idTitle) {
		this.idTitle = idTitle;
	}

	public int getIdIcon() {
		return idIcon;
	}

	public void setIdIcon(int idIcon) {
		this.idIcon = idIcon;
	}

	public int getCounter() {
		if(counter == -1) {
			counter = 0;
		}
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}

	public String getPhrase() {
		if (phrase != null) {
			return Html.fromHtml(phrase).toString();
		}
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

}
