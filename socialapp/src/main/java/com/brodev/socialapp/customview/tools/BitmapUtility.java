package com.brodev.socialapp.customview.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.brodev.socialapp.customview.LibraryConstants;

import java.io.File;
import java.io.FileInputStream;

/**
 * 
 * @author suchan_j
 *
 */
public class BitmapUtility {
	public static Bitmap getBitmapFromFile(File file) {
		return getBitmapFromFile(file, true);
	}

	// http://stackoverflow.com/a/823966
	public static Bitmap getBitmapFromFile(File file, boolean rescale) {
		final int REQUIRED_SIZE = 70;
		try {
			// decode image size
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			if (rescale) {
				int width_tmp = opts.outWidth, height_tmp = opts.outHeight;
				while (true) {
					if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
						break;
					}
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}
			}
			// decode with inSampleSize
			BitmapFactory.Options outopts = new BitmapFactory.Options();
			outopts.inSampleSize = scale;
			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file), null, outopts);
			return b;
		} catch (Throwable e) {
			Log.e(LibraryConstants.TAG, String.format("Unable to getBitmapFromFile: %s", e.getMessage()));
		}
		return null;
	}
}
