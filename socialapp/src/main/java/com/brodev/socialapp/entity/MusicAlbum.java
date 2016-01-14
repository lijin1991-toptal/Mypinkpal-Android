package com.brodev.socialapp.entity;

import java.io.Serializable;

import android.text.Html;

public class MusicAlbum implements Serializable {

	private static final long serialVersionUID = 1L;

	private int album_id;

	private String name;

	private String text;

	private String time_phrase;

	private String user_full_name;

	private int album_total_track;

	private String album_image_path;

	private boolean is_like = false;

	private int total_like;

	private int total_comment;

	private String notice;

	public MusicAlbum(int album_id, String name, String text,
			String time_phrase, String user_full_name, int album_total_track,
			String album_image_path, boolean is_like, String notice,
			int total_like, int total_comment) {
		super();
		this.album_id = album_id;
		this.name = name;
		this.text = text;
		this.time_phrase = time_phrase;
		this.album_image_path = album_image_path;
		this.is_like = is_like;
		this.album_total_track = album_total_track;
		this.total_like = total_like;
		this.total_comment = total_comment;
		this.notice = notice;
		this.user_full_name = user_full_name;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public int getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(int album_id) {
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

	public String getText() {
		if (text != null) {
			return Html.fromHtml(text).toString();
		}
		return text;
	}

	public void setUser_full_name(String user_full_name) {
		this.user_full_name = user_full_name;
	}

	public String getUser_full_name() {
		if (user_full_name != null) {
			return Html.fromHtml(user_full_name).toString();
		}
		return user_full_name;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTime_phrase() {
		return time_phrase;
	}

	public void setTime_phrase(String time_phrase) {
		this.time_phrase = time_phrase;
	}

	public String getAlbum_image_path() {
		return album_image_path;
	}

	public void setAlbum_image_path(String album_image_path) {
		this.album_image_path = album_image_path;
	}

	public boolean getIs_like() {
		return is_like;
	}

	public void setIs_like(boolean is_like) {
		this.is_like = is_like;
	}

	public int getAlbum_total_track() {
		return album_total_track;
	}

	public void setAlbum_total_track(int album_total_track) {
		this.album_total_track = album_total_track;
	}

	public int getTotal_like() {
		return total_like;
	}

	public void setTotal_like(int total_like) {
		this.total_like = total_like;
	}

	public int getTotal_comment() {
		return total_comment;
	}

	public void setTotal_comment(int total_comment) {
		this.total_comment = total_comment;
	}

	public MusicAlbum() {
		super();
	}
}
