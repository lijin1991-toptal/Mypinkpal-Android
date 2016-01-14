package com.brodev.socialapp.entity;

import android.text.Html;

import org.json.JSONObject;

public class Comment {

	/**
	 * @string user id
	 */
	private String user_id;

	/**
	 * @string user_image url link
	 */
	private String user_image;

	/**
	 * @string time (day/month/year)
	 */
	private String time_phrase;

	/**
	 * @string fullname
	 */
	private String fullname;

	/**
	 * @boolean is liked
	 */
	private boolean isLiked;

	/**
	 * @Integer total like
	 */
	private int totalLike;

	/**
	 * @string text
	 */
	private String text;

	/**
	 * @Integer comment id
	 */
	private int commentId;

	public int getCommentId() {
		return commentId;
	}

	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}

	public boolean isLiked() {
		return isLiked;
	}

	public void setLiked(boolean isLiked) {
		this.isLiked = isLiked;
	}

	public int getTotalLike() {
		return totalLike;
	}

	public void setTotalLike(int totalLike) {
		this.totalLike = totalLike;
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

	public String getUserId() {
		return user_id;
	}

	public void setUserId(String user_id) {
		this.user_id = user_id;
	}

	public String getUserImage() {
		return user_image;
	}

	public void setUserImage(String user_image) {
		this.user_image = user_image;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTimePhrase() {
		if (time_phrase != null) {
			return Html.fromHtml(time_phrase).toString();
		}
		return time_phrase;
	}

	public void setTimePhrase(String time_phrase) {
		this.time_phrase = time_phrase;
	}

	/**
	 * Convert comment from jsonObject
	 * 
	 * @param objComment
	 * @param pagesObj
	 * @return Object
	 */
	public Comment convertComment(Comment objComment, JSONObject pagesObj) {
		try {
			if (pagesObj.has("comment_id")) {
				objComment.setCommentId(Integer.parseInt(pagesObj.getString("comment_id")));
			}
			
			if (pagesObj.has("user_id")) {
				objComment.setUserId(pagesObj.getString("user_id"));
			}

			if (pagesObj.has("full_name")) {
				objComment.setFullname(Html.fromHtml(pagesObj.getString("full_name")).toString());
			}

			if (pagesObj.has("time_phrase")) {
				objComment.setTimePhrase(pagesObj.getString("time_phrase"));
			}

            if (pagesObj.has("textx")) {
                objComment.setText(pagesObj.getString("text"));
            }

			if (pagesObj.has("text_html")) {
				objComment.setText(pagesObj.getString("text_html"));
			}

			if (pagesObj.has("user_image")) {
				objComment.setUserImage(pagesObj.getString("user_image"));
			}

			if (pagesObj.has("is_liked") && !pagesObj.isNull("is_liked")) {
				objComment.setLiked(true);
			} else {
				objComment.setLiked(false);
			}
			if (pagesObj.has("total_like")) {
				objComment.setTotalLike(Integer.parseInt(pagesObj.getString("total_like")));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return objComment;
	}

}
