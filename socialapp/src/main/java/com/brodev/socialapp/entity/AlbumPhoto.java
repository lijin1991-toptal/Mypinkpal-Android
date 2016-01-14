package com.brodev.socialapp.entity;

public class AlbumPhoto {
	
	/**
	 * @integer album id
	 */
	private int albumId;
	
	/**
	 * @string name
	 */
	private String name;

	//construct
	public AlbumPhoto() {
		super();
	}

	public AlbumPhoto(int albumId, String name) {
		super();
		this.albumId = albumId;
		this.name = name;
	}

	public int getAlbumId() {
		return albumId;
	}

	public void setAlbumId(int albumId) {
		this.albumId = albumId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
