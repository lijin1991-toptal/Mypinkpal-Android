package com.brodev.socialapp.cache;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public final class CacheDownload {
	
	public final CacheRequest initiator;
	
	private final CacheManager manager;
	
	private final UUID session;
	
	private HttpGet httpGet = null;
	
	private boolean cancelled = false;
	
	private final LinkedList<CacheRequest> lateJoiners = new LinkedList<CacheRequest>();
	
	private volatile CacheRequest highestPriorityReq;
	
	private final PrioritisedDownloadQueue queue;
	
	private boolean success = false;
	private CacheManager.WritableCacheFile cacheFile = null;
	
	private String mimetype;
	
	public CacheDownload(final CacheRequest initiator, final CacheManager manager, final PrioritisedDownloadQueue queue) {

		this.initiator = initiator;

		this.manager = manager;
		this.queue = queue;
		highestPriorityReq = initiator;

		if(!initiator.setDownload(this)) {
			cancel();
		}

		if(initiator.requestSession != null) {
			session = initiator.requestSession;
		} else {
			session = UUID.randomUUID();
		}
	}
	
	public synchronized void cancel() {

		cancelled = true;

		new Thread() {
			public void run() {
				if(httpGet != null) httpGet.abort();
				queue.exterminateDownload(CacheDownload.this);
				notifyAllOnFailure(RequestFailureType.CANCELLED, null, null, "Cancelled");
			}
		}.start();
	}
	
	private synchronized void notifyAllOnFailure(final RequestFailureType type, final Throwable t, final StatusLine status, final String readableMessage) {

		initiator.notifyFailure(type, t, status, readableMessage);

		for(final CacheRequest req : lateJoiners) {
			req.notifyFailure(type, t, status, readableMessage);
		}
	}
	
	// TODO potential concurrency problem -- late joiner may be added after failure
	public synchronized void addLateJoiner(final CacheRequest request) {

		if(cancelled) {
			request.notifyFailure(RequestFailureType.CANCELLED, null, null, "Cancelled");
			return;
		}

		if(!request.setDownload(this)) {
			notifyAllOnFailure(RequestFailureType.CANCELLED, null, null, "Cancelled");
			return;
		}

		if(request.isJson != initiator.isJson) {
			return;
		}

		lateJoiners.add(request);

		if(request.isHigherPriorityThan(highestPriorityReq)) {
			highestPriorityReq = request;
		}
	}
	
	public void doDownload() {

		if(cancelled) {
			queue.removeDownload(this);
			notifyAllOnFailure(RequestFailureType.CANCELLED, null, null, "Cancelled");
			return;
		}

		notifyAllDownloadStarted();
		try {
			downloadGet(queue.getHttpClient());
		} catch(Throwable t) {
		} finally {
			queue.removeDownload(this);
			finishGet();
		}
	}
	
	private void downloadGet(final HttpClient httpClient) {
		Log.i("check GIF url >>> ", initiator.url.toString());
		httpGet = new HttpGet(initiator.url);
		if(initiator.isJson) httpGet.setHeader("Accept-Encoding", "gzip");

		final HttpContext localContext = new BasicHttpContext();

		final HttpResponse response;
		final StatusLine status;

		try {
			if(cancelled) {
				notifyAllOnFailure(RequestFailureType.CANCELLED, null, null, "Cancelled");
				return;
			}
			response = httpClient.execute(httpGet, localContext);
			status = response.getStatusLine();

		} catch(Throwable t) {
			t.printStackTrace();
			notifyAllOnFailure(RequestFailureType.CONNECTION, t, null, "Unable to open a connection");
			return;
		}

		if(status.getStatusCode() != 200) {
			notifyAllOnFailure(RequestFailureType.REQUEST, null, status, String.format("HTTP error %d (%s)", status.getStatusCode(), status.getReasonPhrase()));
			return;
		}

		if(cancelled) {
			notifyAllOnFailure(RequestFailureType.CANCELLED, null, null, "Cancelled");
			return;
		}

		final HttpEntity entity = response.getEntity();

		if(entity == null) {
			notifyAllOnFailure(RequestFailureType.CONNECTION, null, status, "Did not receive a valid HTTP response");
			return;
		}

		final InputStream is;

		try {
			is = entity.getContent();
			mimetype = entity.getContentType() == null ? null : entity.getContentType().getValue();
		} catch (Throwable t) {
			t.printStackTrace();
			notifyAllOnFailure(RequestFailureType.CONNECTION, t, status, "Could not open an input stream");
			return;
		}

		final NotifyOutputStream cacheOs;
		if(initiator.cache) {
			try {
				cacheFile = manager.openNewCacheFile(initiator, session, mimetype);
				cacheOs = cacheFile.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
				notifyAllOnFailure(RequestFailureType.STORAGE, e, null, "Could not access the local cache");
				return;
			}
		} else {
			cacheOs = null;
		}

		final long contentLength = entity.getContentLength();

		if(initiator.isJson) {

			final InputStream bis;

			if(initiator.cache) {
				final CachingInputStream cis = new CachingInputStream(is, cacheOs, new CachingInputStream.BytesReadListener() {
					public void onBytesRead(final long total) {
						notifyAllOnProgress(total, contentLength);
					}
				});

				bis = new BufferedInputStream(cis, 8 * 1024);
			} else {
				bis = new BufferedInputStream(is, 8 * 1024);
			}

			success = true;

		} else {

			if(!initiator.cache) {
				return;
			}

			try {
				final byte[] buf = new byte[8 * 1024];

				int bytesRead;
				long totalBytesRead = 0;
				while((bytesRead = is.read(buf)) > 0) {
					totalBytesRead += bytesRead;
					cacheOs.write(buf, 0, bytesRead);
					notifyAllOnProgress(totalBytesRead, contentLength);
				}

				cacheOs.flush();
				cacheOs.close();
				success = true;

			} catch(Throwable t) {
				t.printStackTrace();
				notifyAllOnFailure(RequestFailureType.CONNECTION, t, null, "The connection was interrupted");
			}
		}
	}

	
	private synchronized void notifyAllDownloadStarted() {

		initiator.notifyDownloadStarted();

		for(final CacheRequest req : lateJoiners) {
			req.notifyDownloadStarted();
		}
	}
	
	public synchronized boolean isHigherPriorityThan(final CacheDownload another) {
		return highestPriorityReq.isHigherPriorityThan(another.highestPriorityReq);
	}
	
	public RequestIdentifier createIdentifier() {
		return initiator.createIdentifier();
	}
	
	private synchronized void notifyAllOnProgress(final long bytesRead, final long bytesTotal) {

		initiator.notifyProgress(bytesRead, bytesTotal);

		for(final CacheRequest req : lateJoiners) {
			req.notifyProgress(bytesRead, bytesTotal);
		}
	}
	
	private synchronized void finishGet() {

		if(success && initiator.cache) {

			if(cacheFile == null) {
				throw new RuntimeException("Cache file was null, but success was true");
			}

			if(session == null) {
				throw new RuntimeException("Session was null, but success was true");
			}

			notifyAllOnSuccess(cacheFile.getReadableCacheFile(), RRTime.utcCurrentTimeMillis(), session, mimetype);
		}
	}
	
	private synchronized void notifyAllOnSuccess(final CacheManager.ReadableCacheFile cacheFile, final long timestamp, final UUID session, final String mimetype) {

		initiator.notifySuccess(cacheFile, timestamp, session, false, mimetype);

		for(final CacheRequest req : lateJoiners) {
			req.notifySuccess(cacheFile, timestamp, session, false, mimetype);
		}
	}
}
