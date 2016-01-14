package com.brodev.socialapp.customview.tools;

/**
 * 
 * @author suchan_j
 *
 */
public final class FileDownloaderFactory {
	private static FileDownloader instance;
	
	public static synchronized FileDownloader get() {
		if (instance == null) {
			instance = new FileDownloaderImpl();
		}
		return instance;
	}
}
