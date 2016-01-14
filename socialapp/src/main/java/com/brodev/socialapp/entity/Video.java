package com.brodev.socialapp.entity;

import java.io.Serializable;

import org.json.JSONObject;

import android.text.Html;

public class Video implements Serializable {

	private static final long serialVersionUID = 1L;
	private int video_id;
	private String title;
	private String short_text;
	private String text;
	private boolean is_like = false;
	private int total_like;
	private int total_comment;
	private String full_name;
	private String image_path;
	private String user_image_path;
	private int user_id;
	private String notice;
	private String time_stamp;
	private String video_link;
	private String video_embed;
	private boolean can_post_comment;
	private String duration;
	private String web_link;

	public Video(int video_id, String title, String text, String short_text,
			boolean is_like, int total_like, int total_comment,
			String full_name, String image_path, String notice,
			String time_stamp, String user_image_path, String video_link,
			int user_id) {
		super();
		this.video_id = video_id;
		this.title = title;
		this.text = text;
		this.short_text = short_text;
		this.is_like = is_like;
		this.total_like = total_like;
		this.total_comment = total_comment;
		this.full_name = full_name;
		this.image_path = image_path;
		this.notice = notice;
		this.time_stamp = time_stamp;
		this.user_image_path = user_image_path;
		this.video_link = video_link;
		this.user_id = user_id;
	}

	public Video() {
		super();
	}

	public void setVideo_id(int video_id) {
		this.video_id = video_id;
	}

	public int getVideo_id() {
		return video_id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		if (title != null) {
			return Html.fromHtml(title).toString();
		}
		return null;
	}

	public String getText() {
		if (text != null) {
			return Html.fromHtml(text).toString();
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

	public boolean getCan_post_comment() {
		return can_post_comment;
	}

	public void setCan_post_comment(boolean can_post_comment) {
		this.can_post_comment = can_post_comment;
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

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
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

	public String getImage_path() {
		return image_path;
	}

	public void setImage_path(String image_path) {
		this.image_path = image_path;
	}

	public String getVideo_link() {
		return video_link;
	}

	public void setVideo_link(String video_link) {
		this.video_link = video_link;
	}

	public String getVideo_embed() {
		return video_embed;
	}

	public void setVideo_embed(String video_embed) {
		this.video_embed = video_embed;
	}

	public String getUser_image_path() {
		return user_image_path;
	}

	public void setUser_image_path(String user_image_path) {
		this.user_image_path = user_image_path;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
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

	public String getTubeId() {
		String[] temp = video_link.split("/?v=");
		return temp[1];
	}

	public String GetBigImage() {
		String[] temp = video_link.split("/?v=");
		return "http://img.youtube.com/vi/" + temp[1] + "/0.jpg";
	}

	public String getWeb_link() {
		return web_link;
	}

	public void setWeb_link(String web_link) {
		this.web_link = web_link;
	}

	public boolean isYoutube() {

		if (video_link.indexOf("youtube") > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Convert comment from jsonObject
	 * 
	 * @param objComment
	 * @param pagesObj
	 * @return Object
	 */
	public Video convertVideo(JSONObject pagesObj) {
		Video objVideo = new Video();
		try {

			objVideo.setVideo_id(pagesObj.getInt("id"));
			// set title
			objVideo.setTitle(pagesObj.getString("title"));
			// set text
			objVideo.setText(pagesObj.getString("text"));

            if (pagesObj.has("text_html")) {
                objVideo.setText(pagesObj.getString("text"));
            }

			// set is like
			if (!pagesObj.isNull("is_liked")) {
				objVideo.setIs_like(pagesObj.getBoolean("is_liked"));
			}

			objVideo.setCan_post_comment(pagesObj
					.getBoolean("can_post_comment"));

			// set total like
			objVideo.setTotal_like(pagesObj.getInt("total_like"));

			// set total comment
			objVideo.setTotal_comment(pagesObj.getInt("total_comment"));

			// set user name
			objVideo.setFull_name(pagesObj.getString("full_name"));

			// set user image path
			objVideo.setUser_image_path(pagesObj.getString("user_image_path"));

			// set photo
			objVideo.setImage_path(pagesObj.getString("photo"));

			// set video link
			objVideo.setVideo_link(pagesObj.getString("video_url"));

			objVideo.setDuration(pagesObj.getString("duration"));
			objVideo.setVideo_embed(pagesObj.getString("embed_code"));
			// set time stamp
			objVideo.setTime_stamp(pagesObj.getString("time_stamp"));

			objVideo.setWeb_link(pagesObj.getString("permalink"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return objVideo;
	}
}
