package com.brodev.socialapp.customview.html;

import android.graphics.drawable.Drawable;

/**
 * 
 * @author suchan_j
 *
 */
public interface ImageGetterTaskListener {
	void onSuccess(ImageGetterTaskData taskData, Drawable resultDrawable);
	void onFailed(ImageGetterTaskData taskData);
}
