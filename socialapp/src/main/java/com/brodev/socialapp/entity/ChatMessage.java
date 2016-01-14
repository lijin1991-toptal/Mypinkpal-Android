package com.brodev.socialapp.entity;

public class ChatMessage {

	/**
	 * @String id
	 */
	private String id;

	/**
	 * @String user id
	 */
	private String fromUserId;

	/**
	 * @String to user id
	 */
	private String toUserId;

	/**
	 * @String message
	 */
	private String message;

	/**
	 * @String time
	 */
	private String time;

	/**
	 * @String image
	 */
	private String image;

	/**
	 * @String sticker
	 */
	private String sticker;

	/**
	 * @String type
	 */
	private String type;

	/**
	 * @boolean is logged
	 */
	private boolean isLogged;

	/**
	 * @Integer height
	 */
	private int stickerHeight;

	/**
	 * @Integer width
	 */
	private int stickerWidth;

	/**
	 * @String sticker type
	 */
	private String stickerType;

	// construct
	public ChatMessage() {
	}

	public ChatMessage(String id, String fromUserId, String message,
			String time, boolean isLogged, String image, String toUserId,
			String sticker, String type, int stickerHeight, int stickerWidth,
			String stickerType) {
		super();
		this.id = id;
		this.fromUserId = fromUserId;
		this.message = message;
		this.time = time;
		this.isLogged = isLogged;
		this.image = image;
		this.toUserId = toUserId;
		this.sticker = sticker;
		this.type = type;
		this.stickerHeight = stickerHeight;
		this.stickerWidth = stickerWidth;
		this.stickerType = stickerType;
	}

	public String getStickerType() {
		return stickerType;
	}

	public void setStickerType(String stickerType) {
		this.stickerType = stickerType;
	}

	public int getStickerHeight() {
		return stickerHeight;
	}

	public void setStickerHeight(int stickerHeight) {
		this.stickerHeight = stickerHeight;
	}

	public int getStickerWidth() {
		return stickerWidth;
	}

	public void setStickerWidth(int stickerWidth) {
		this.stickerWidth = stickerWidth;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSticker() {
		return sticker;
	}

	public void setSticker(String sticker) {
		this.sticker = sticker;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public boolean isLogged() {
		return isLogged;
	}

	public void setLogged(boolean isLogged) {
		this.isLogged = isLogged;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTime() {
		return time;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
