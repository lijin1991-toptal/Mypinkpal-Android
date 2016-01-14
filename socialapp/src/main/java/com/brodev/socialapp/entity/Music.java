package com.brodev.socialapp.entity;

import java.io.Serializable;

import android.text.Html;

public class Music implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -460492480661382873L;

	private String song_id;

	private String title;

	private String description;

	private String song_path;

	private String album_id;

	private String user_id;

	private String duration;

	private String total_play;

	private String total_score;

	private String module_id;

	private String user_image_path;

	private String short_text;

	private String total_like;

	private String total_comment;

	private String notice;

	private String time_stamp;

	private boolean isShare;

	private boolean isLiked;

	private boolean canPostComment;

	public Music() {
		super();
	}

	public Music(String song_id, String title, String description,
			String song_path, String album_id, String user_id, String duration,
			String module_id, String total_play, String total_score,
			String user_image_path, String short_text, String total_like,
			String total_comment, String time_stamp, String notice,
			boolean isShare, boolean isLiked, boolean canPostComment) {
		super();
		this.song_id = song_id;
		this.title = title;
		this.description = description;
		this.song_path = song_path;
		this.album_id = album_id;
		this.user_id = user_id;
		this.duration = duration;
		this.module_id = module_id;
		this.total_play = total_play;
		this.total_score = total_play;
		this.user_image_path = user_image_path;
		this.short_text = short_text;
		this.total_like = total_like;
		this.total_comment = total_comment;
		this.time_stamp = time_stamp;
		this.notice = notice;
		this.isShare = isShare;
		this.isLiked = isLiked;
		this.canPostComment = canPostComment;
	}

	public boolean isCanPostComment() {
		return canPostComment;
	}

	public void setCanPostComment(boolean canPostComment) {
		this.canPostComment = canPostComment;
	}

	public boolean isLiked() {
		return isLiked;
	}

	public void setLiked(boolean isLiked) {
		this.isLiked = isLiked;
	}

	public boolean isShare() {
		return isShare;
	}

	public void setShare(boolean isShare) {
		this.isShare = isShare;
	}

	public String getSong_id() {
		return song_id;
	}

	public void setSong_id(String song_id) {
		this.song_id = song_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getSong_path() {
		return song_path;
	}

	public void setSong_path(String song_path) {
		this.song_path = song_path;
	}

	public String getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(String album_id) {
		this.album_id = album_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getTotal_play() {
		return total_play;
	}

	public void setTotal_play(String total_play) {
		this.total_play = total_play;
	}

	public String getTotal_score() {
		return total_score;
	}

	public void setTotal_score(String total_score) {
		this.total_score = total_score;
	}

	public String getModule_id() {
		return module_id;
	}

	public void setModule_id(String module_id) {
		this.module_id = module_id;
	}

	public String getUser_image_path() {
		return user_image_path;
	}

	public void setUser_image_path(String user_image_path) {
		this.user_image_path = user_image_path;
	}

	public String getShort_text() {
		if (short_text != null) {
			return Html.fromHtml(short_text).toString();
		}
		return short_text;
	}

	public void setShort_text(String short_text) {

		this.short_text = short_text;
	}

	public String getTotal_like() {
		return total_like;
	}

	public void setTotal_like(String total_like) {
		this.total_like = total_like;
	}

	public String getTotal_comment() {
		return total_comment;
	}

	public void setTotal_comment(String total_comment) {
		this.total_comment = total_comment;
	}

	public String getTime_stamp() {
		return time_stamp;
	}

	public void setTime_stamp(String time_stamp) {
		this.time_stamp = time_stamp;
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
}
