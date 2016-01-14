package com.brodev.socialapp.entity;

public class Conversation {

	/**
	 * @integer user id
	 */
	private int userId;
	
	/**
	 * @boolean is logged user
	 */
	private boolean isLogged;
	
	/**
	 * @String text
	 */
	private String text;

	/**
	 * @String full name
	 */
	private String fullname;

	/**
	 * @String image
	 */
	private String image;

	/**
	 * @String time phrase
	 */
	private String timePhrase;

	// Constructor
	public Conversation() {

	}

	public Conversation(int userId, String text, String fullname, String image,
			String timePhrase, boolean isLogged) {
		super();
		this.userId = userId;
		this.text = text;
		this.fullname = fullname;
		this.image = image;
		this.timePhrase = timePhrase;
		this.isLogged = isLogged;
	}
	
	public boolean isLogged() {
		return isLogged;
	}

	public void setLogged(boolean isLogged) {
		this.isLogged = isLogged;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getTimePhrase() {
		return timePhrase;
	}

	public void setTimePhrase(String timePhrase) {
		this.timePhrase = timePhrase;
	}

}
