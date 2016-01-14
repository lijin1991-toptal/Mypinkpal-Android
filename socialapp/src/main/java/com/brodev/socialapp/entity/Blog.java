package com.brodev.socialapp.entity;

import android.text.Html;

import java.io.Serializable;

public class Blog implements Serializable {

	private static final long serialVersionUID = 1L;

	private int blog_id;

	private String title;

	private String text;

	private String short_text;

	private boolean is_like = false;

	private int total_like;

	private int total_comment;

	private String full_name;

	private String user_image_path;

	private String notice;

	private String time_stamp;

	private boolean share;

	private boolean canPostComment;

	public Blog(int blog_id, String title, String text, String short_text,
			boolean is_like, int total_like, int total_comment,
			String full_name, String user_image_path, String notice,
			String time_stamp, boolean share, boolean canPostComment) {
		super();
		this.blog_id = blog_id;
		this.title = title;
		this.text = text;
		this.short_text = short_text;
		this.is_like = is_like;
		this.total_like = total_like;
		this.total_comment = total_comment;
		this.full_name = full_name;
		this.user_image_path = user_image_path;
		this.notice = notice;
		this.time_stamp = time_stamp;
		this.share = share;
		this.canPostComment = canPostComment;
	}

	public Blog() {
		super();
	}

	public boolean isCanPostComment() {
		return canPostComment;
	}

	public void setCanPostComment(boolean canPostComment) {
		this.canPostComment = canPostComment;
	}

	public int getBlog_id() {
		return blog_id;
	}

	public void setBlog_id(int blog_id) {
		this.blog_id = blog_id;
	}

	public String getTitle() {
		if (title != null) {
			return Html.fromHtml(title).toString();
		}
		return null;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		if (text != null) {
			return text;
		}
		return null;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getShort_text() {
		if (short_text != null) {
			return Html.fromHtml(short_text).toString();
		}
		return null;
	}

	public void setShort_text(String short_text) {
		this.short_text = short_text;
	}

	public boolean getIs_like() {
		return is_like;
	}

	public void setIs_like(boolean is_like) {
		this.is_like = is_like;
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

	public String getFull_name() {
		if (full_name != null) {
			return Html.fromHtml(full_name).toString();
		}
		return null;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public String getUser_image_path() {
		return user_image_path;
	}

	public void setUser_image_path(String user_image_path) {
		this.user_image_path = user_image_path;
	}

	public String getTime_stamp() {
		if (time_stamp != null) {
			return Html.fromHtml(time_stamp).toString();
		}
		return time_stamp;
	}

	public void setTime_stamp(String time_stamp) {
		this.time_stamp = time_stamp;
	}

	public String getNotice() {
		if (notice != null) {
			return Html.fromHtml(notice).toString();
		}
		return null;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public void setShare(boolean share) {
		this.share = share;
	}

	public boolean getShare() {
		return this.share;
	}
}
