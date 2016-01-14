package com.brodev.socialapp.facebook;

public class Item {
	private ActionListener listener;

	public Item(ActionListener listener) {
		this.listener = listener;
	}

	public ActionListener getListener() {
		return listener;
	}
}
