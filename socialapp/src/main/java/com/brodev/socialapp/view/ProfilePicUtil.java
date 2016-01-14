package com.brodev.socialapp.view;

import java.io.File;
import java.io.IOException;

import com.brodev.socialapp.android.AlbumStorageDirFactory;
import com.mypinkpal.app.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

public class ProfilePicUtil {
	
	private Bitmap bitmap;
	
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	static Resources mResources;
    static DisplayMetrics mMetrics;
    int orientation;
    
	//construct
	public ProfilePicUtil() {
	}
    
	public Bitmap decodeFile(String filePath) {
		// Decode image size
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 1024;

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			bitmap = BitmapFactory.decodeFile(filePath, o2);
			
			ExifInterface exif = new ExifInterface(filePath);
	        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
	        Matrix m = new Matrix();

	        if ((orientation == 3)) {
	            m.postRotate(180);
	            m.postScale((float) bitmap.getWidth(), (float) bitmap.getHeight());
	          
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	        } else if (orientation == 6) {
	            m.postRotate(90);
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	        } else if (orientation == 8) {
	            m.postRotate(270);
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	        }
		} catch (Exception ex) {
			ex.printStackTrace();
			bitmap = null;
		}
		
		return bitmap;
	}
	
	
	public String getPath(Uri uri, Activity activity) {
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		return null;
	}
	
	public Bitmap getResizedBitmap(Bitmap bm) {

		int width = bm.getWidth();
		int height = bm.getHeight();
		final int REQUIRED_SIZE = 2048;

		// create a matrix for the manipulation
		int width_tmp = bm.getWidth(), height_tmp = bm.getHeight();
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		int scaleWidth = width / scale;

		int scaleHeight = height / scale;

		Bitmap scaledphoto = Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
		return scaledphoto;
	}
	
	/* Photo album for this application */
	private String getAlbumName(Context context) {
		return context.getString(R.string.app_name);
	}
	
	private File getAlbumDir(Context context, AlbumStorageDirFactory mAlbumStorageDirFactory) {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName(context));

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(context.getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	
	public File createImageFile(Context context,AlbumStorageDirFactory mAlbumStorageDirFactory) throws IOException {
		// Create an image file name
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp;
		File albumF = getAlbumDir(context, mAlbumStorageDirFactory);
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}
	
	/**
	 * Add picture to gallery
	 * @param context
	 * @param mCurrentPhotoPath
	 */
	public void galleryAddPic(Context context, String mCurrentPhotoPath) {
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    context.sendBroadcast(mediaScanIntent);
	}
	
	/**
	 * Set Pic 
	 * @param bitmap
	 * @param profilePic
	 * @param mCurrentPhotoPath
	 * @return
	 */
	public Bitmap setPic(Context context, Bitmap bitmap, ImageView profilePic, String mCurrentPhotoPath) {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */
		
		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		
		int scaleFactor = 1;
		
		if (profilePic != null) {
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;
	
			/* Get the size of the ImageView */
			int targetW = profilePic.getWidth();
			int targetH = profilePic.getHeight();
			Log.i("Scale Pic", String.valueOf(photoW) + " " +String.valueOf(photoH) + " "  + String.valueOf(targetW) + " " +String.valueOf(targetH));
			
			
			/* Figure out which way needs to be reduced less */
			if ((targetW > 0) || (targetH > 0)) {
				scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
			}
			Log.i("Scale Pic", String.valueOf(scaleFactor));
			bmOptions.inSampleSize = scaleFactor;
		} else {
			final int tw;
	        final int th;
	        if (mMetrics == null)
	            prepareResources(context);
	        tw = mMetrics.widthPixels;
	        th = mMetrics.heightPixels;
	        
	        final int targetWidth = tw <= 0 ? Integer.MAX_VALUE : tw;
	        final int targetHeight = th <= 0 ? Integer.MAX_VALUE : th;
	        
            while ((bmOptions.outWidth >> scaleFactor) > targetWidth || (bmOptions.outHeight >> scaleFactor) > targetHeight) {
            	scaleFactor++;
            }
            bmOptions = new Options();
            bmOptions.inSampleSize = 1 << scaleFactor;
		}
		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inPurgeable = true;
		
		/* Decode the JPEG file into a Bitmap */
		bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		try {

			ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
	        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
	        Matrix m = new Matrix();

	        if ((orientation == 3)) {
	            m.postRotate(180);
	            m.postScale((float) bitmap.getWidth(), (float) bitmap.getHeight());
	          
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	        } else if (orientation == 6) {
	            m.postRotate(90);
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	        } else if (orientation == 8) {
	            m.postRotate(270);
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	        }
		} catch (Exception ex) {
			bitmap = null;
		}
		return bitmap;
	}
	
    private static void prepareResources(final Context context) {
        if (mMetrics != null) {
            return;
        }
        mMetrics = new DisplayMetrics();
       
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
        .getDefaultDisplay().getMetrics(mMetrics);
        final AssetManager mgr = context.getAssets();
        mResources = new Resources(mgr, mMetrics, context.getResources().getConfiguration());
    }
}
