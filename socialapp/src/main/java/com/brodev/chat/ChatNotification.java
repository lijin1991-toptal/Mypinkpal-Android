package com.brodev.chat;

import com.mypinkpal.app.R;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.view.ChatActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ChatNotification {
	
	/**
	 * generate chat notification 
	 * @param context
	 * @param message
	 * @param userId
	 */
	@SuppressWarnings("deprecation")
	public void generateNotification(Context context, String message, String fullname, String userImage, String userId) {
		int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = null;
        
        notificationIntent = new Intent(context, ChatActivity.class);
        notificationIntent.putExtra("fullname", fullname);
        notificationIntent.putExtra("user_id", userId);
        notificationIntent.putExtra("image", userImage);
        
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(context, title, message, intent);
        
        
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;
        
        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);      
	}
	
	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param message
	 */
	public void displayMessage(Context context) {
        Intent intent = new Intent(Config.DISPLAY_MESSAGE_ACTION);
        intent.putExtra("message", "mail");
        
        context.sendBroadcast(intent);
    }
}
