package com.brodev.socialapp.entity;

import org.json.JSONObject;

import android.os.Build;
import android.util.Log;

public class Photo {

	private int photo_id;

	private int total_like = 0;

	private int total_comment = 0;

	private int item_id;

	private String is_liked = null;

	private String type_id;

	private String thumb_image;

	private String image_path;

	// construct
	public Photo() {
		super();
	}

	public int getPhoto_id() {
		return photo_id;
	}

	public void setPhoto_id(int photo_id) {
		this.photo_id = photo_id;
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

	public int getItem_id() {
		return item_id;
	}

	public void setItem_id(int item_id) {
		this.item_id = item_id;
	}	

	public String getIs_liked() {
		return is_liked;
	}

	public void setIs_liked(String is_liked) {
		this.is_liked = is_liked;
	}

	public String getType_id() {
		return type_id;
	}

	public void setType_id(String type_id) {
		this.type_id = type_id;
	}

	public String getThumb_image() {
		return thumb_image;
	}

	public void setThumb_image(String thumb_image) {
		this.thumb_image = thumb_image;
	}

	public String getImage_path() {
		return image_path;
	}

	public void setImage_path(String image_path) {
		this.image_path = image_path;
	}
	
	public Photo convert(JSONObject objItem) {
		Photo item = new Photo();
		try {
			
			item.setThumb_image(objItem.getJSONObject("photo_sizes").getString("100"));
			
			item.setPhoto_id(objItem.getInt("photo_id"));
			
			if (objItem.has("feed_total_like")) {
				item.setTotal_like(objItem.getInt("feed_total_like"));
			}
			
			item.setItem_id(objItem.getInt("item_id"));
			
			if (!objItem.isNull("feed_is_liked")
					&& objItem.getString("feed_is_liked") != "false") {
				if (!"".equals(objItem.getString("feed_is_liked"))) {
					item.setIs_liked("feed_is_liked");					
				} 

			}
			 
			if (objItem.has("total_comment")){
				item.setTotal_comment(objItem.getInt("total_comment"));
			}
			
			item.setType_id(objItem.getJSONObject("social_app").getString("type_id"));
			
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD) {
				item.setImage_path(objItem.getJSONObject("photo_sizes")
						.getString("240"));
			} else {
				item.setImage_path(objItem.getJSONObject("photo_sizes")
						.getString("500"));
			}
			Log.i("CHAYHET",  item.getImage_path());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return item;
	}
}
