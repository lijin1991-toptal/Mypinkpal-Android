package com.brodev.socialapp.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

public final class General {
	
	private static final Pattern urlPattern = Pattern.compile("^(https?)://([^/]+)/+([^\\?#]+)((?:\\?[^#]+)?)((?:#.+)?)$");
	
	/**
	 * Get uri from string
	 * @param url
	 * @return
	 */
	public static URI uriFromString(String url) {

		try {
			return new URI(url);

		} catch(Throwable t1) {
			try {

				Log.i("RR DEBUG uri", "Beginning aggressive parse of '" + url + "'");

				final Matcher urlMatcher = urlPattern.matcher(url);

				if(urlMatcher.find()) {

					final String scheme = urlMatcher.group(1);
					final String authority = urlMatcher.group(2);
					final String path = urlMatcher.group(3).length() == 0 ? null : "/" + urlMatcher.group(3);
					final String query = urlMatcher.group(4).length() == 0 ? null : urlMatcher.group(4);
					final String fragment = urlMatcher.group(5).length() == 0 ? null : urlMatcher.group(5);

					try {
						return new URI(scheme, authority, path, query, fragment);
					} catch(Throwable t3) {

						if(path != null && path.contains(" ")) {
							return new URI(scheme, authority, path.replace(" ", "%20"), query, fragment);
						} else {
							return null;
						}
					}

				} else {
					return null;
				}

			} catch(Throwable t2) {
				return null;
			}
		}
	}
	
	public static File getBestCacheDir(final Context context) {

		final File externalCacheDir = context.getExternalCacheDir();

		if(externalCacheDir != null) {
			return externalCacheDir;
		}

		return context.getCacheDir();
	}
	
	public static void moveFile(final File src, final File dst) throws IOException {

		if(!src.renameTo(dst)) {

			copyFile(src, dst);

			if(!src.delete()) {
				src.deleteOnExit();
			}
		}
	}
	
	public static void copyFile(final File src, final File dst) throws IOException {

		final FileInputStream fis = new FileInputStream(src);
		final FileOutputStream fos = new FileOutputStream(dst);

		copyFile(fis, fos);
	}

	public static void copyFile(final InputStream fis, final File dst) throws IOException {
		final FileOutputStream fos = new FileOutputStream(dst);
		copyFile(fis, fos);
	}

	public static void copyFile(final InputStream fis, final OutputStream fos) throws IOException {

		final byte[] buf = new byte[32 * 1024];

		int bytesRead;
		while((bytesRead = fis.read(buf)) > 0) {
			fos.write(buf, 0, bytesRead);
		}

		fis.close();
		fos.close();
	}
}
