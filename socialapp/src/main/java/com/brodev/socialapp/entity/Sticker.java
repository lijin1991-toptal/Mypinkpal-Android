package com.brodev.socialapp.entity;

public class Sticker {

	/**
	 * @String sticker id
	 */
	private String stickerId;

	/**
	 * @String sticker name
	 */
	private String stickerName;

	/**
	 * @String sticker url
	 */
	private String stickerUrl;

	/**
	 * @String sticker url 100
	 */
	private String stickerUrl100;

	/**
	 * @Integer height
	 */
	private int stickerHeight;

	/**
	 * @Integer width
	 */
	private int stickerWidth;

	/**
	 * @String type
	 */
	private String stickerType;

	// construct
	public Sticker() {

	}

	public Sticker(String stickerId, String stickerName, String stickerUrl,
			String stickerUrl100, int stickerHeight, int stickerWidth,
			String stickerType) {
		super();
		this.stickerId = stickerId;
		this.stickerName = stickerName;
		this.stickerUrl = stickerUrl;
		this.stickerUrl100 = stickerUrl100;
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

	public String getStickerUrl100() {
		return stickerUrl100;
	}

	public void setStickerUrl100(String stickerUrl100) {
		this.stickerUrl100 = stickerUrl100;
	}

	public String getStickerId() {
		return stickerId;
	}

	public void setStickerId(String stickerId) {
		this.stickerId = stickerId;
	}

	public String getStickerName() {
		return stickerName;
	}

	public void setStickerName(String stickerName) {
		this.stickerName = stickerName;
	}

	public String getStickerUrl() {
		return stickerUrl;
	}

	public void setStickerUrl(String stickerUrl) {
		this.stickerUrl = stickerUrl;
	}
}
