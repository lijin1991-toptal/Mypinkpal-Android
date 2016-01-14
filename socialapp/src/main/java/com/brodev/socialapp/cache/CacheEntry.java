package com.brodev.socialapp.cache;

import java.net.URI;
import java.util.UUID;


import android.database.Cursor;

public class CacheEntry {
	public final long id;
	private final URI url;
	private final String user;
	public final UUID session;

	public final long timestamp;
	private final int status;
	private final int type;
	public final String mimetype;

	CacheEntry(final Cursor cursor) {

		id = cursor.getLong(0);
		url = General.uriFromString(cursor.getString(1));
		user = cursor.getString(2);
		session = UUID.fromString(cursor.getString(3));
		timestamp = cursor.getLong(4);
		status = cursor.getInt(5);
		type = cursor.getInt(6);
		mimetype = cursor.getString(7);
	}
}
