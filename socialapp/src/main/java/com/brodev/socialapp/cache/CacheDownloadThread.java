package com.brodev.socialapp.cache;

public class CacheDownloadThread extends Thread {
	
	private final PrioritisedDownloadQueue queue;
	private final CacheDownload singleDownload;

	public CacheDownloadThread(final PrioritisedDownloadQueue queue, final boolean start) {
		this(queue, null, start);
	}

	public CacheDownloadThread(final PrioritisedDownloadQueue queue, final CacheDownload singleDownload, final boolean start) {
		this.queue = queue;
		this.singleDownload = singleDownload;
		if(start) start();
	}
	
	@Override
	public void run() {

		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

		if(singleDownload != null) {
			singleDownload.doDownload();

		} else {

			while(true) {

				CacheDownload download = null;

				try {
					download = queue.getNextInQueue();
					download.doDownload();
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
}
