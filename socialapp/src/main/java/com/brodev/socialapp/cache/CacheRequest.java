package com.brodev.socialapp.cache;

import java.net.URI;
import java.util.UUID;

import org.apache.http.StatusLine;

import android.content.Context;
import android.util.Log;

public abstract class CacheRequest implements Comparable<CacheRequest> {
	
	public enum DownloadType {
		NEVER, IF_NECESSARY, FORCE
	}
	
	public final URI url;
	
	public final UUID requestSession;
	
	public final int priority;
	
	public final boolean cache;
	
	public final DownloadType downloadType;
	
	public final boolean cancelExisting;
	
	private boolean cancelled;
	
	private CacheDownload download;
	
	public final boolean isJson;
	
	public final Context context;
	
	private final int listId;
	
	public final int fileType;
	
	public final String user;

	// Can be called to cancel the request
	public synchronized void cancel() {

		cancelled = true;

		if(download != null) {
			download.cancel();
			download = null;
		}
	}

	protected CacheRequest(final URI url, final String user,
			final UUID requestSession, final int priority, final int listId,
			final DownloadType downloadType, final int fileType,
			final boolean isJson, final boolean cancelExisting,
			final Context context) {
		
		this(url, user, requestSession, priority, listId, downloadType, cancelExisting, isJson, fileType, cancelExisting, true, context);
	}
	
	protected CacheRequest(final URI url, final String user, final UUID requestSession,
			final int priority, final int listId, final DownloadType downloadType,
			final boolean cancelled, final boolean isJson, final int fileType,
			final boolean cancelExisting,  final boolean cache,  final Context context) {
		super();
		this.url = url;
		this.requestSession = requestSession;
		this.priority = priority;
		this.downloadType = downloadType;
		this.cancelExisting = cancelExisting;
		this.cancelled = cancelled;
		this.isJson = isJson;
		this.context = context;
		this.listId = listId;
		this.fileType = fileType;
		this.user = user;
		this.cache = cache;

		if (url == null) {
			notifyFailure(RequestFailureType.MALFORMED_URL, null, null,
					"Malformed URL");
			cancel();
		}
	}


	public final RequestIdentifier createIdentifier() {
		return new RequestIdentifier(url, requestSession, cache);
	}
	
	protected abstract void onCallbackException(Throwable t);

	protected abstract void onDownloadNecessary();
	protected abstract void onDownloadStarted();

	protected abstract void onFailure(RequestFailureType type, Throwable t, StatusLine status, String readableMessage);
	protected abstract void onProgress(long bytesRead, long totalBytes);
	protected abstract void onSuccess(CacheManager.ReadableCacheFile cacheFile, long timestamp, UUID session, boolean fromCache, String mimetype);
	
	public final void notifySuccess(final CacheManager.ReadableCacheFile cacheFile, final long timestamp, final UUID session, final boolean fromCache, final String mimetype) {
		try {
			onSuccess(cacheFile, timestamp, session, fromCache, mimetype);
		} catch(Throwable t1) {
			try {
				onCallbackException(t1);
			} catch(Throwable t2) {
			}
		}
	}
	
	public final void notifyFailure(final RequestFailureType type, final Throwable t, final StatusLine status, final String readableMessage) {
		try {
			onFailure(type, t, status, readableMessage);
		} catch(Throwable t1) {
			try {
				onCallbackException(t1);
			} catch(Throwable t2) {
			}
		}
	}
	
	// Called by CacheDownload
	synchronized boolean setDownload(final CacheDownload download) {
		if(cancelled) return false;
		this.download = download;
		return true;
	}
	
	// Queue helpers

	public final boolean isHigherPriorityThan(final CacheRequest another) {

		if(priority != another.priority) {
			return priority < another.priority;
		} else {
			return listId < another.listId;
		}
	}

	public int compareTo(final CacheRequest another) {
		return isHigherPriorityThan(another) ? -1 : (another.isHigherPriorityThan(this) ? 1 : 0);
	}
	
	public final void notifyDownloadNecessary() {
		try {
			onDownloadNecessary();
		} catch(Throwable t1) {
			try {
				onCallbackException(t1);
			} catch(Throwable t2) {
			}
		}
	}
	
	public final void notifyProgress(final long bytesRead, final long totalBytes) {
		try {
			onProgress(bytesRead, totalBytes);
		} catch(Throwable t1) {
			try {
				onCallbackException(t1);
			} catch(Throwable t2) {
			}
		}
	}
	
	public final void notifyDownloadStarted() {
		try {
			onDownloadStarted();
		} catch(Throwable t1) {
			try {
				onCallbackException(t1);
			} catch(Throwable t2) {
			}
		}
	}
}
