package com.brodev.socialapp.customview.html;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.brodev.socialapp.customview.LibraryConstants;
import com.brodev.socialapp.customview.tools.BitmapUtility;
import com.brodev.socialapp.customview.tools.FileDownloader;
import com.brodev.socialapp.customview.tools.FileDownloaderFactory;
import com.mypinkpal.app.R;

import java.io.File;

/**
 * 
 * @author suchan_j
 *
 */
public class ImageGetterTask extends AsyncTask<Void, Void, Drawable> {
	private Resources resources;
	private ImageGetterTaskListener taskListener;
	private ImageGetterTaskData taskData;

    private static Drawable placeholderLoading;
	
	private ImageGetterTask() {		
	}
	
	public static ImageGetterTask create(Resources resources, ImageGetterTaskListener taskListener, ImageGetterTaskData taskData) {
		ImageGetterTask task = new ImageGetterTask();
		
		task.resources = resources;
		task.taskListener = taskListener;
		task.taskData = taskData;

        placeholderLoading = resources.getDrawable(R.drawable.loading_placeholder);
		
		return task;
	}
	
	@Override
	protected Drawable doInBackground(Void... params) {
		try {
			FileDownloader fileDownloader = FileDownloaderFactory.get();
			File file = fileDownloader.download(taskData.getSourceUrl());
			if (file == null) {
				return null;
			}
			
			Bitmap bitmap = BitmapUtility.getBitmapFromFile(file);
			if (bitmap == null) {
				return null;
			}
			
			Drawable resultDrawable = new BitmapDrawable(resources, bitmap);

            if (resultDrawable.getIntrinsicWidth() < placeholderLoading.getIntrinsicWidth())
            {
                resultDrawable.setBounds(0, 0, 0 + placeholderLoading.getIntrinsicWidth(), 0 + placeholderLoading.getIntrinsicHeight());
            } else {
                resultDrawable.setBounds(0, 0, 0 + resultDrawable.getIntrinsicWidth(), 0 + resultDrawable.getIntrinsicHeight());
            }

			return resultDrawable;
		} catch (Throwable e) {
			Log.e(LibraryConstants.TAG, String.format("Unable to obtain drawable: %s", e.getMessage()));
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Drawable resultDrawable) {
		if (resultDrawable == null) {
			taskListener.onFailed(taskData);
		} else {
			taskListener.onSuccess(taskData, resultDrawable);
		}
	}
}
