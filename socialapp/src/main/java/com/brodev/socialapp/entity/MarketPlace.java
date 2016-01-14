package com.brodev.socialapp.entity;

import android.text.Html;

import org.json.JSONObject;

import java.io.Serializable;

public class MarketPlace implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 679726372644896482L;

	private int listing_id;

	private String title;

	private String short_text;

	private String text;

	private boolean is_liked = false;

	private int total_like;

	private int total_comment;
	
	private String currency;

	private String country_name;

	private String country_child_name = "";

	private String city_name = "";

	private String time_stamp;

	private String full_name;

	private String user_image_path;

	private boolean is_close;

	private boolean can_post_comment;
	
	private String notice;
	
	private Float price;
	
	private int user_id;
	
	private String images_path;
	
	private String images;

	public MarketPlace() {
		super();
	}

	public int getListing_id() {
		return listing_id;
	}

	public void setListing_id(int listing_id) {
		this.listing_id = listing_id;
	}

	public String getTitle() {
		if (title != null) {
			return Html.fromHtml(title).toString();
		}
		return title;
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

	public boolean getIs_liked() {
		return is_liked;
	}

	public void setIs_liked(boolean is_liked) {
		this.is_liked = is_liked;
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
	public String getTime_stamp() {
		if (time_stamp != null) {
			return Html.fromHtml(time_stamp).toString();
		}
		return time_stamp;
	}

	public void setTime_stamp(String time_stamp) {
		this.time_stamp = time_stamp;
	}
	
	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
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
	
	public boolean getCan_post_comment() {
		return can_post_comment;
	}

	public void setCan_post_comment(boolean can_post_comment) {
		this.can_post_comment = can_post_comment;
	}
	
	public void setCountry_name(String country_name) {
		this.country_name = country_name;
	}

	public String getCountry_name() {
		if (country_name != null) {
			return Html.fromHtml(country_name).toString();
		}
		return null;
	}
	
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCurrency() {
		if (currency != null) {
			return Html.fromHtml(currency).toString();
		}
		return null;
	}
	
	public void setCountry_child_name(String country_child_name) {
		this.country_child_name = country_child_name;
	}

	public String getCountry_child_name() {
		if (country_child_name != null) {
			return Html.fromHtml(country_child_name).toString();
		}
		return null;
	}
	
	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public String getCity_name() {
		if (city_name != null) {
			return Html.fromHtml(city_name).toString();
		}
		return null;
	}
	
	public void setUser_image_path(String user_image_path) {
		this.user_image_path = user_image_path;
	}
	
	public String getUser_image_path() {
		return user_image_path;
	}
	
	public boolean getIs_close() {
		return is_close;
	}

	public void setIs_close(boolean is_close) {
		this.is_close = is_close;
	}
	
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	
	public int getUser_id() {
		return user_id;
	}
	
	public void setImage_path(String image_path) {
		this.images_path = image_path;
	}
	
	public String getImage_path() {
		return images_path;
	}
	
	public String getImages() {
		return images;
	}
	
	public void setImages(String images){
		this.images = images;
	}

    /**
     * Convert comment from jsonObject
     * @param pagesObj
     * @return
     */
	public MarketPlace convertMarketPlace(JSONObject pagesObj) {
		MarketPlace objMarketPlace = new MarketPlace();
		try {
			
			objMarketPlace.setListing_id(pagesObj.getInt("listing_id"));
			
			if (pagesObj.has("title")) {
				objMarketPlace.setTitle(pagesObj.getString("title"));
			}
			
			objMarketPlace.setShort_text(pagesObj.getString("short_text"));
			
			objMarketPlace.setText(pagesObj.getString("text"));

            if (pagesObj.has("text_html"))
                objMarketPlace.setText(pagesObj.getString("text_html"));
			
			if (!pagesObj.isNull("is_liked")) {
				objMarketPlace.setIs_liked(true);
			}
			
			objMarketPlace.setTotal_like(pagesObj.getInt("total_like"));
			
			objMarketPlace.setTotal_comment(pagesObj.getInt("total_comment"));
			
			objMarketPlace.setCurrency(pagesObj.getString("currency"));
			
			objMarketPlace.setCountry_name(pagesObj.getString("country_name"));
			
			if (!pagesObj.isNull("country_chil_name")) {
				objMarketPlace.setCountry_child_name(pagesObj.getString("country_child_name"));
			}
			
			if (!pagesObj.isNull("city")) {
				objMarketPlace.setCity_name(pagesObj.getString("city"));
			}
			
			objMarketPlace.setTime_stamp(pagesObj.getString("time_stamp"));

			objMarketPlace.setFull_name(pagesObj.getString("full_name"));

			objMarketPlace.setUser_image_path(pagesObj.getString("user_image_path"));

			objMarketPlace.setCan_post_comment(pagesObj.getBoolean("can_post_comment"));
			
			objMarketPlace.setImage_path(pagesObj.getString("image_path"));			
				
			objMarketPlace.setPrice(Float.valueOf(pagesObj.getString("price")));		
			if (pagesObj.has("user_id")) {
				objMarketPlace.setUser_id(pagesObj.getInt("user_id"));
			}
			
			objMarketPlace.setImages(pagesObj.getString("images"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return objMarketPlace;
	}
}
