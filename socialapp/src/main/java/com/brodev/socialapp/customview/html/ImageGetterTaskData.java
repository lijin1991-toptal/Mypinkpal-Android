package com.brodev.socialapp.customview.html;

import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * 
 * @author suchan_j
 *
 */
public final class ImageGetterTaskData {
	private int spawnPosition;
	private String sourceUrl;
	private String htmlContent;
	private WeakReference<TextView> textViewContentRef;
	
	private ImageGetterTaskData() {		
	}
	
	public static ImageGetterTaskData create(int spawnPosition, String sourceUrl, String htmlContent, TextView textViewContent) {
		ImageGetterTaskData taskData = new ImageGetterTaskData();		
		
		taskData.spawnPosition = spawnPosition;
		taskData.sourceUrl = sourceUrl;
		taskData.htmlContent = htmlContent;
		taskData.textViewContentRef = new WeakReference<TextView>(textViewContent);
		
		return taskData;
	}
	
	public int getSpawnPosition() {
		return this.spawnPosition;
	}
	
	public String getSourceUrl() {
		return this.sourceUrl;
	}
	
	public String getHtmlContent() {
		return this.htmlContent;
	}
	
	public TextView getTextView() {
		if (textViewContentRef != null) {
			return textViewContentRef.get();
		}
		return null;
	}
}
