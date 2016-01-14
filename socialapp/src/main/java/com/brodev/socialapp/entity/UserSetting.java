package com.brodev.socialapp.entity;

import org.json.JSONObject;

import android.app.Application;
import android.text.Html;

public class UserSetting extends Application {

	private String user_name;

	private String full_name;

	private String email;

	private int total_user_change;

	private int total_full_name_change;

	private String password;

	private String password_salt;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword_salt() {
		return password_salt;
	}

	public void setPassword_salt(String password_salt) {
		this.password_salt = password_salt;
	}

	public int getTotal_user_change() {
		return total_user_change;
	}

	public void setTotal_user_change(int total_user_change) {
		this.total_user_change = total_user_change;
	}

	public int getTotal_full_name_change() {
		return total_full_name_change;
	}

	public void setTotal_full_name_change(int total_full_name_change) {
		this.total_full_name_change = total_full_name_change;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	private String currency;

	private String language_id;

	private String time_zone;

	private boolean can_change_user_name;

	private boolean can_change_full_name;

	private boolean can_change_email;

	public boolean isCan_change_user_name() {
		return can_change_user_name;
	}

	public void setCan_change_user_name(boolean can_change_user_name) {
		this.can_change_user_name = can_change_user_name;
	}

	public boolean isCan_change_full_name() {
		return can_change_full_name;
	}

	public void setCan_change_full_name(boolean can_change_full_name) {
		this.can_change_full_name = can_change_full_name;
	}

	public boolean isCan_change_email() {
		return can_change_email;
	}

	public void setCan_change_email(boolean can_change_email) {
		this.can_change_email = can_change_email;
	}

	public String getTime_zone() {
		return time_zone;
	}

	public void setTime_zone(String time_zone) {
		this.time_zone = time_zone;
	}

	public String getLanguage_id() {
		return language_id;
	}

	public void setLanguage_id(String language_id) {
		this.language_id = language_id;
	}

	// construct
	public UserSetting() {
		super();
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	public UserSetting convert(JSONObject objItem) {
		UserSetting item = new UserSetting();
		try {

			item.setFull_name(Html.fromHtml(objItem.getString("full_name")).toString());

			item.setUser_name(Html.fromHtml(objItem.getString("user_name")).toString());

			if (!objItem.isNull("default_currency")) {
				item.setCurrency(objItem.getString("default_currency"));
			}

			item.setLanguage_id(objItem.getString("language_id"));

			item.setTime_zone(objItem.getString("time_zone"));

			item.setCan_change_email(objItem.getBoolean("can_change_email"));

			item.setCan_change_full_name(objItem.getBoolean("can_change_full_name"));

			item.setCan_change_user_name(objItem.getBoolean("can_change_user_name"));

			item.setEmail(objItem.getString("email"));

			item.setTotal_full_name_change(objItem.getInt("total_full_name_change"));

			item.setTotal_user_change(objItem.getInt("total_user_change"));

			item.setPassword(objItem.getString("password"));

			item.setPassword_salt(objItem.getString("password_salt"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return item;
	}

}
