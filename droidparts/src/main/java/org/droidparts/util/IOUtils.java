/**
 * Copyright 2013 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.util;

import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.contract.Constants.UTF8;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class IOUtils {

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("failed to encode", e);
		}
	}

	public static String urlDecode(String str) {
		try {
			return URLDecoder.decode(str, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("failed to decode", e);
		}
	}

	public static void silentlyClose(Closeable... closeables) {
		for (Closeable cl : closeables) {
			try {
				if (cl != null) {
					cl.close();
				}
			} catch (Exception e) {
				L.d(e);
			}
		}
	}

	public static String readToString(InputStream is) throws IOException {
		byte[] data = readToByteArray(is);
		return new String(data, UTF8);
	}

	public static byte[] readToByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		while ((len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		return baos.toByteArray();
	}

	public static ArrayList<File> getFileList(File dir,
			String... fileExtensions) {
		final ArrayList<File> files = new ArrayList<File>();
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				if (fileExtensions.length == 0) {
					files.add(file);
				} else {
					String fileName = file.getName().toLowerCase();
					for (String ext : fileExtensions) {
						if (fileName.endsWith(ext)) {
							files.add(file);
							break;
						}
					}
				}
			} else {
				files.addAll(getFileList(file, fileExtensions));
			}
		}
		return files;
	}

	public static void copy(File fileFrom, File fileTo) throws IOException {
		if (fileTo.exists()) {
			fileTo.delete();
		}
		FileChannel src = null;
		FileChannel dst = null;
		try {
			src = new FileInputStream(fileFrom).getChannel();
			dst = new FileOutputStream(fileTo).getChannel();
			dst.transferFrom(src, 0, src.size());
		} finally {
			silentlyClose(src, dst);
		}

	}

}
