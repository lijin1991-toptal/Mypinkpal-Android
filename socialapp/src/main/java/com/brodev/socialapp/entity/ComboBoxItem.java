package com.brodev.socialapp.entity;

import org.json.JSONObject;

import android.text.Html;

public class ComboBoxItem {
	
	/**
	 * @String name
	 */
	private String name;

	/**
	 * @String value
	 */
	private String value;

	public ComboBoxItem() {
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
	
	/**
	 * Convert json to object
	 * @param objItem
	 * @return
	 */
	public ComboBoxItem convert(JSONObject objItem) {
		ComboBoxItem item = new ComboBoxItem();
		try {

			item.setName(Html.fromHtml(objItem.getString("name")).toString());

			item.setValue(objItem.getString("value"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return item;
	}

    public ComboBoxItem convertLocation(JSONObject objItem) {
        ComboBoxItem item = new ComboBoxItem();
        try {
            item.setName(Html.fromHtml(objItem.getString("name")).toString());
            item.setValue(objItem.getString("iso"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }
}
