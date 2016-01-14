package com.brodev.socialapp.entity;

public class UserComposeSearch {
	
	/**
	 * @integer user id
	 */
	private int userId;
	
	/**
	 * @String full name
	 */
	private String fullname;
	
	/**
	 * @String image url
	 */
	private String image;

	public UserComposeSearch() {}
	
	public UserComposeSearch(int userId, String fullname, String image) {
		super();
		this.userId = userId;
		this.fullname = fullname;
		this.image = image;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
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
	
}
