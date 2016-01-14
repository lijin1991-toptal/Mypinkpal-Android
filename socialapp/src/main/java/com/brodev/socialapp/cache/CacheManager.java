package com.brodev.socialapp.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.util.Log;

public final class CacheManager {
	
	private static final String ext = ".sa_cache_data", tempExt = ".sa_cache_data_tmp";
	
	private static final AtomicBoolean isAlreadyInitialized = new AtomicBoolean(false);
	
	private final CacheDbManager dbManager;
	
	private final Context context;

	private static CacheManager singleton;
	
	private final RequestHandlerThread requestHandler;
	
	private final PrioritisedDownloadQueue downloadQueue;
	
	private final UniqueSynchronizedQueue<Long> fileDeletionQueue = new UniqueSynchronizedQueue<Long>();
	
	private final PriorityBlockingQueue<CacheRequest> requests = new PriorityBlockingQueue<CacheRequest>();
	
	private final LinkedList<CacheDownloadThread> downloadThreads = new LinkedList<CacheDownloadThread>();
	
	public static synchronized CacheManager getInstance(final Context context) {
		if(singleton == null) singleton = new CacheManager(context.getApplicationContext());
		return singleton;
	}
	
	private CacheManager(final Context context) {

		if(!isAlreadyInitialized.compareAndSet(false, true)) {
			throw new RuntimeException("Attempt to initialize the cache twice.");
		}

		this.context = context;
		dbManager = new CacheDbManager(context);
		requestHandler = new RequestHandlerThread();

		// TODo put somewhere else -- make request specific, no restart needed on prefs change!
		final HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000); 
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
		params.setParameter(CoreConnectionPNames.MAX_HEADER_COUNT, 100);
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
		params.setParameter(ClientPNames.MAX_REDIRECTS, 2);
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 50);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRoute() {
			public int getMaxForRoute(HttpRoute route) {
				return 25;
			}
		});

		final SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		final ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(params, schemeRegistry);

		final DefaultHttpClient defaultHttpClient = new DefaultHttpClient(connManager, params);
		defaultHttpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));

		defaultHttpClient.addResponseInterceptor(new HttpResponseInterceptor() {

			public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {

				final HttpEntity entity = response.getEntity();
				final Header encHeader = entity.getContentEncoding();

				if(encHeader == null) return;

				for (final HeaderElement elem : encHeader.getElements()) {
					if ("gzip".equalsIgnoreCase(elem.getName())) {
						response.setEntity(new GzipDecompressingEntity(entity));
						return;
					}
				}
			}
		});

		downloadQueue = new PrioritisedDownloadQueue(defaultHttpClient);

		requestHandler.start();
		
		for(int i = 0; i < 5; i++) { // TODO remove constant --- customizable
			final CacheDownloadThread downloadThread = new CacheDownloadThread(downloadQueue, true);
			downloadThreads.add(downloadThread);
		}
	}
	
	public void makeRequest(final CacheRequest request) {
		requests.put(request);
	}
	
	public class ReadableCacheFile {

		private final long id;
		private final boolean compressed;

		private ReadableCacheFile(final long id, final boolean compressed) {
			this.id = id;
			this.compressed = compressed;
		}

		public InputStream getInputStream() throws IOException {
			return getCacheFileInputStream(id, compressed);
		}

		@Override
		public String toString() {
			return String.format("[ReadableCacheFile : id %d]", id);
		}
	}
	
	private InputStream getCacheFileInputStream(final long id, final boolean compressed) throws IOException {
		if(compressed) {
			return new BufferedInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(getExistingCacheFile(id)), 8 * 1024)), 8 * 1024);
		} else {
			return new BufferedInputStream(new FileInputStream(getExistingCacheFile(id)), 8 * 1024);
		}
	}
	
	private File getExistingCacheFile(final long id) {

		final File externalCacheDir = context.getExternalCacheDir();

		if(externalCacheDir != null) {
			final File fExternal = new File(externalCacheDir, id + ext);

			if(fExternal.exists()) {
				return fExternal;
			}
		}

		final File fInternal = new File(context.getCacheDir(), id + ext);

		if(fInternal.exists()) {
			return fInternal;
		}

		return null;
	}
	
	private class RequestHandlerThread extends Thread {
		@Override
		public void run() {

			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

			try {
				CacheRequest request;
				while((request = requests.take()) != null) {
					processDeletionQueue();
					handleRequest(request);
				}

			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void handleRequest(final CacheRequest request) {
		
		if(request.url == null) {
			request.notifyFailure(RequestFailureType.MALFORMED_URL, new NullPointerException("URL was null"), null, "URL was null");
			return;
		}
		
		switch(request.downloadType) {
			case NEVER: {
				final LinkedList<CacheEntry> result = dbManager.select(request.url, request.user, request.requestSession);
	
				if (result.size() == 0) {
					request.notifyFailure(RequestFailureType.CACHE_MISS, null, null, "Could not find this data in the cache");
				} else {
					final CacheEntry entry = mostRecentFromList(result);
					handleCacheEntryFound(entry, request);
				}
				break;
			}
			
			case IF_NECESSARY: {

				final LinkedList<CacheEntry> result = dbManager.select(request.url, request.user, request.requestSession);

				if(result.size() == 0) {
					queueDownload(request);

				} else {
					final CacheEntry entry = mostRecentFromList(result);
					handleCacheEntryFound(entry, request);
				}

				break;
			}
			
			case FORCE:
				queueDownload(request);
				break;
		}
	}
	
	private void handleCacheEntryFound(final CacheEntry entry, final CacheRequest request) {

		new Thread() {

			public void run() {

				final File cacheFile = getExistingCacheFile(entry.id);

				if(cacheFile == null) {

					if(request.downloadType == CacheRequest.DownloadType.IF_NECESSARY) {
						queueDownload(request);
					} else {
						request.notifyFailure(RequestFailureType.STORAGE, null, null, "A cache entry was found in the database, but the actual data couldn't be found. Press refresh to download the content again.");
					}

					return;
				}
				request.notifySuccess(new ReadableCacheFile(entry.id, request.isJson), entry.timestamp, entry.session, true, entry.mimetype);
			}
		}.start();
	}

	private void queueDownload(final CacheRequest request) {
		request.notifyDownloadNecessary();
		downloadQueue.add(request, CacheManager.this);
	}
	
	private CacheEntry mostRecentFromList(final LinkedList<CacheEntry> list) {

		CacheEntry entry = null;

		for(final CacheEntry e : list) {
			if(entry == null || entry.timestamp < e.timestamp) {
				entry = e;
			}
		}

		return entry;
	}
	
	private void processDeletionQueue() {

		final int maxToDelete = 2;

		int deleted = 0;

		Long toDelete;

		while(maxToDelete > deleted++ && (toDelete = fileDeletionQueue.dequeue()) != null) {

			// Attempt to delete file
			final File f = getExistingCacheFile(toDelete);

			if(f != null && !f.delete()) {
				f.deleteOnExit();
			}
		}
	}
	
	public class WritableCacheFile {

		private final NotifyOutputStream os;
		private long cacheFileId = -1;
		private ReadableCacheFile readableCacheFile = null;
		private final CacheRequest request;
		private final boolean compressed;

		private WritableCacheFile(final CacheRequest request, final UUID session, final String mimetype) throws IOException {

			this.request = request;
			compressed = request.isJson;

			final File tmpFile = new File(General.getBestCacheDir(context), UUID.randomUUID().toString() + tempExt);
			final FileOutputStream fos = new FileOutputStream(tmpFile);

			final OutputStream compressedOs;

			if(compressed) {
				compressedOs = new GZIPOutputStream(new BufferedOutputStream(fos, 8 * 1024));
			} else {
				compressedOs = new BufferedOutputStream(fos, 8 * 1024);
			}

			final NotifyOutputStream.Listener listener = new NotifyOutputStream.Listener() {
				public void onClose() throws IOException {

					cacheFileId = dbManager.newEntry(request, session, mimetype);

					final File dstFile = new File(General.getBestCacheDir(context), cacheFileId + ext);
					General.moveFile(tmpFile, dstFile);

					dbManager.setEntryDone(cacheFileId);

					readableCacheFile = new ReadableCacheFile(cacheFileId, compressed);
				}
			};

			this.os = new NotifyOutputStream(compressedOs, listener);
		}

		public NotifyOutputStream getOutputStream() {
			return os;
		}

		public ReadableCacheFile getReadableCacheFile() {

			if(readableCacheFile == null) {
				try {
					os.flush();
					os.close();
				} catch(IOException e) {Log.e("RR DEBUG getReadableCacheFile", "Error closing " + cacheFileId);
					throw new RuntimeException(e);
				}
			}

			return readableCacheFile;
		}
	}
	
	public WritableCacheFile openNewCacheFile(final CacheRequest request, final UUID session, final String mimetype) throws IOException {
		return new WritableCacheFile(request, session, mimetype);
	}

}
