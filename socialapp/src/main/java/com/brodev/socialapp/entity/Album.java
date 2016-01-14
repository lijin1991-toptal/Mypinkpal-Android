package com.brodev.socialapp.entity;

import android.text.Html;

public class Album {

	private String album_id;
	
	private String name;
	
	private String description;
	
	private String time_phrase;
	
	private String album_pic;
	
	private String user_id;
	
	private String album_total;
	
	private String module_id;
	
	private String group_id;
	
	private String notice;
	

	public Album(String album_id, String name, String description,
			String time_phrase, String album_pic, String user_id,
			String album_total, String module_id, String group_id, String notice) {
		super();
		this.album_id = album_id;
		this.name = name;
		this.description = description;
		this.time_phrase = time_phrase;
		this.album_pic = album_pic;
		this.user_id = user_id;
		this.album_total = album_total;
		this.module_id = module_id;
		this.group_id = group_id;
		this.notice = notice;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getModule_id() {
		return module_id;
	}


	public void setModule_id(String module_id) {
		this.module_id = module_id;
	}


	public String getGroup_id() {
		return group_id;
	}


	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}


	public Album() {
		super();
	}
	
	
	public String getAlbum_total() {
		return album_total;
	}

	public void setAlbum_total(String album_total) {
		this.album_total = album_total;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(String album_id) {
		this.album_id = album_id;
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

	public String getDescription() {
		if (description != null) {
			return Html.fromHtml(description).toString();
		}
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTime_phrase() {
		if (time_phrase != null) {
			return Html.fromHtml(time_phrase).toString();
		}
		return time_phrase;
	}

	public void setTime_phrase(String time_phrase) {
		this.time_phrase = time_phrase;
	}

	public String getAlbum_pic() {
		return album_pic;
	}

	public void setAlbum_pic(String album_pic) {
		this.album_pic = album_pic;
	}
	
	
}
