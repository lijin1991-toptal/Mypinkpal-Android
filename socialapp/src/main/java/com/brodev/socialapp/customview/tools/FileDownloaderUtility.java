package com.brodev.socialapp.customview.tools;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.brodev.socialapp.customview.LibraryConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 
 * @author suchan_j
 *
 */
public class FileDownloaderUtility extends LibraryConstants {
	protected static void downloadTo(String sourceUrl, File target) throws IOException {
		URL url = new URL(sourceUrl);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		connection.setReadTimeout(READ_TIMEOUT);
		connection.setInstanceFollowRedirects(true);
		
		InputStream inputStream = connection.getInputStream();
		OutputStream outputStream = new FileOutputStream(target);
		
		copyStream(inputStream, outputStream);
		
		outputStream.close();
	}
	
	protected static File getTempDirectory() {
		File root = null;
		File temp = null;

		String externalStorageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
			root = Environment.getExternalStorageDirectory();
		} else {
			root = Environment.getDownloadCacheDirectory();
		}

		temp = new File(root, LibraryConstants.TEMP_DIRECTORY);
		temp.mkdirs();

		return temp;
	}

	// http://stackoverflow.com/a/1856542
	protected static String getFileNameFromUrl(String sourceUrl) {
		String filename = "";
		String path = Uri.parse(sourceUrl).getPath();
		String[] pathContents = path.split("[\\\\/]");
		if (pathContents != null) {
			int pathContentsLength = pathContents.length;
			String lastPart = pathContents[pathContentsLength - 1];
			String[] lastPartContents = lastPart.split("\\.");
			if (lastPartContents != null && lastPartContents.length > 1) {
				int lastPartContentLength = lastPartContents.length;
				String name = "";
				for (int i = 0; i < lastPartContentLength; i++) {
					if (i < (lastPartContents.length - 1)) {
						name += lastPartContents[i];
						if (i < (lastPartContentLength - 2)) {
							name += ".";
						}
					}
				}
				String extension = lastPartContents[lastPartContentLength - 1];
				filename = name + "." + extension;
			}
		}
		return filename;
	}
	
	protected static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Throwable e) {
			Log.e(LibraryConstants.TAG, String.format("Unable to copyStream: %s", e.getMessage()));
		}
	}
}
