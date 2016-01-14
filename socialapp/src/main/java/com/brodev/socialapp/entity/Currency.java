package com.brodev.socialapp.entity;

import org.json.JSONObject;

import android.text.Html;

public class Currency {

	private String name;

	private String value = "";

	private String symbol;

	private boolean isChoose = false;

	public Currency() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public boolean isChoose() {
		return isChoose;
	}

	public void setChoose(boolean isChoose) {
		this.isChoose = isChoose;
	}

	public Currency convert(JSONObject objItem) {
		Currency item = new Currency();
		try {

			item.setName(Html.fromHtml(objItem.getString("name")).toString());
			
			item.setSymbol(Html.fromHtml(objItem.getString("symbol")).toString());
			
			item.setValue(objItem.getString("value"));	
			
			item.setChoose(objItem.getBoolean("isChoose"));
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return item;
	}
}
