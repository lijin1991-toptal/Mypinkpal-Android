package com.brodev.socialapp.entity;

public class Pages {
	
	/**
	 * @String pages id
	 */
	private String pagesId;
	
	/**
	 * @String pages title
	 */
	private String titlePages;
	
	/**
	 * @String pages category
	 */
	private String categoryPages;
	
	/**
	 * @String notice
	 */
	private String notice;
	
	/**
	 * @String pages image
	 */
	private String imagePages;
	
	//construct
	public Pages() {
	}

	public Pages(String pagesId, String titlePages, String categoryPages,
			String imagePages, String notice) {
		super();
		this.pagesId = pagesId;
		this.titlePages = titlePages;
		this.categoryPages = categoryPages;
		this.imagePages = imagePages;
		this.notice = notice;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getPagesId() {
		return pagesId;
	}

	public void setPagesId(String pagesId) {
		this.pagesId = pagesId;
	}

	public String getTitlePages() {
		return titlePages;
	}

	public void setTitlePages(String titlePages) {
		this.titlePages = titlePages;
	}

	public String getCategoryPages() {
		return categoryPages;
	}

	public void setCategoryPages(String categoryPages) {
		this.categoryPages = categoryPages;
	}

	public String getImagePages() {
		return imagePages;
	}

	public void setImagePages(String imagePages) {
		this.imagePages = imagePages;
	}
}
