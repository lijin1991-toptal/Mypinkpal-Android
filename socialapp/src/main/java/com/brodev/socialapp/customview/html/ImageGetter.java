package com.brodev.socialapp.customview.html;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.brodev.socialapp.customview.LibraryConstants;
import com.mypinkpal.app.R;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 
 * @author suchan_j
 * 
 */
public class ImageGetter {
	private Resources resources;

	private static Drawable placeholderLoading;
	private static Drawable placeholderFailed;

	private static Executor executor = Executors.newSingleThreadExecutor();
	private static Map<String, WeakReference<Drawable>> cache = Collections.synchronizedMap(new WeakHashMap<String, WeakReference<Drawable>>());

	/**
	 * 
	 * @param context
	 */
	public ImageGetter(Context context) {
		resources = context.getResources();

		placeholderLoading = resources.getDrawable(R.drawable.loading_placeholder);
		placeholderLoading.setBounds(0, 0, placeholderLoading.getIntrinsicWidth(), placeholderLoading.getIntrinsicHeight());

		placeholderFailed = resources.getDrawable(R.drawable.loading_placeholder);
		placeholderFailed.setBounds(0, 0, placeholderFailed.getIntrinsicWidth(), placeholderFailed.getIntrinsicHeight());
	}

	/**
	 * 
	 */
	private static final ImageGetterTaskListener taskListener = new ImageGetterTaskListener() {
		@Override
		public void onSuccess(ImageGetterTaskData taskData, Drawable resultDrawable) {
			cache.put(taskData.getSourceUrl(), new WeakReference<Drawable>(resultDrawable));

			TextView textView = taskData.getTextView();
			if (textView == null) {
				Log.w(LibraryConstants.TAG, "taskListener: widget no longer exists.");
				return;
			}

			Object tag = textView.getTag();
			if (!(tag instanceof Integer)) {
				Log.w(LibraryConstants.TAG, "taskListener: widget was not properly tagged.");
				return;
			}

			Integer currentPosition = (Integer) tag;
			if (currentPosition != taskData.getSpawnPosition()) {
				Log.w(LibraryConstants.TAG, "taskListener: widget missed position.");
				return;
			}

			textView.setText(Html.fromHtml(taskData.getHtmlContent(), imageGetterCached, null));

            textView.setMovementMethod(LinkMovementMethod.getInstance());

        }

		@Override
		public void onFailed(ImageGetterTaskData taskData) {
			Log.e(LibraryConstants.TAG, "taskListener: failed loading image");
		}
	};

	/**
	 * 
	 */
	private static final Html.ImageGetter imageGetterCached = new Html.ImageGetter() {
		@Override
		public Drawable getDrawable(String sourceUrl) {
			if (!cache.containsKey(sourceUrl)) {
				return placeholderLoading;
			}

			WeakReference<Drawable> drawableRef = cache.get(sourceUrl);
			if (drawableRef == null || drawableRef.get() == null) {
				cache.remove(sourceUrl);
				return placeholderFailed;
			}

			return drawableRef.get();
		}
	};

	/**
	 * 
	 * @param initialPosition
	 * @param htmlContent
	 * @param textView
	 * @return
	 */
	public Html.ImageGetter create(int initialPosition, String htmlContent, TextView textView) {
		final int finalInitialPosition = initialPosition;
		final String finalHtmlContent = htmlContent;
		final TextView finalTextView = textView;

		return new Html.ImageGetter() {
			@Override
			public Drawable getDrawable(String sourceUrl) {
				if (cache.containsKey(sourceUrl)) {
					WeakReference<Drawable> drawableRef = cache.get(sourceUrl);
					if (drawableRef != null && drawableRef.get() != null) {
						return drawableRef.get();
					}
				}

				cache.remove(sourceUrl);

				ImageGetterTaskData taskData = ImageGetterTaskData.create(finalInitialPosition, sourceUrl, finalHtmlContent, finalTextView);
				ImageGetterTask task = ImageGetterTask.create(resources, taskListener, taskData);

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    task.execute();
                } else {
                    task.executeOnExecutor(executor, (Void[]) null);
                }

				return placeholderLoading;
			}
		};
	}
}
