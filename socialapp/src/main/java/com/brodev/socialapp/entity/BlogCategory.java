package com.brodev.socialapp.entity;

import java.io.Serializable;

import android.text.Html;

public class BlogCategory implements Serializable {

	private static final long serialVersionUID = 1L;

	private int category_id;

	private String name;

	private boolean isChecked = false;

	public BlogCategory(int category_id, String name, boolean isChecked) {
		super();
		this.category_id = category_id;
		this.name = name;
		this.isChecked = isChecked;
	}

	public BlogCategory() {
		super();
	}

	public int getCategory_id() {
		return category_id;
	}

	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}

	public String getName() {
		if (name != null) {
			return Html.fromHtml(name).toString();
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsChecked() {
		return this.isChecked;
	}

	public void setIsChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
