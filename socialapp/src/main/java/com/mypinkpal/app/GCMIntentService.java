package com.mypinkpal.app;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.support.v4.app.NotificationCompat;

import com.brodev.chat.ChatNotification;
import com.brodev.socialapp.android.RegisterGCM;
import com.brodev.socialapp.android.manager.NotificationNextActivity;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.view.ConfirmRequestActivity;
import com.brodev.socialapp.view.ConversationActivity;
import com.brodev.socialapp.view.DashboardActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.brodev.socialapp.view.SplashActivity;
import com.brodev.socialapp.view.WebviewActivity;
import com.brodev.socialapp.view.chats.GroupDialogActivity;
import com.brodev.socialapp.view.chats.PrivateDialogActivity;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.koushikdutta.urlimageviewhelper.Constants;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.core.gcm.NotificationHelper;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.PushMessage;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GCMIntentService extends GCMBaseIntentService {

    public final static int NOTIFICATION_ID = 1;
    public final static long VIBRATOR_DURATION = 1500;

    private NotificationManager notificationManager;
    private String TAG = "GCMIntentService";
    private String previewStr = null;
    static String threadId = null;
    static String itemId = null;
    static String typeId = null;
    static String fullname = null;
    static String action = null;
    static String link = null;
    static String message;
    static String dialogId;
    static String userId;
    static ChatNotification chatNotification;
    static User user;

    public GCMIntentService() {
        super("GcmIntentService");
    }

    /* bronislaw
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                parseMessage(extras);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    */

    @Override
    protected void onRegistered(Context context, String registrationId) {
        System.out.println("Device registered: regId = " + registrationId);
        Log.i(TAG, "Device registered: regId = " + registrationId);
        User userApp = (User) getApplication().getApplicationContext();
        String userId = userApp.getUserId();
        String token = userApp.getTokenkey();
        String email = userApp.getEmail();
        RegisterGCM rgcm = new RegisterGCM(getApplicationContext());
        rgcm.execute(token, email, userId, registrationId);

        GCMRegistrar.setRegisteredOnServer(context, true);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        System.out.println("Device unregistered regId = " + registrationId);
        Log.i(TAG, "Device unregisterd");
        if (GCMRegistrar.isRegisteredOnServer((context))) {
            //ServerUtilities.unregister(context, registerationId);
        } else {
            System.out.println("Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");

        if (intent.getExtras().getString("alert") != null && intent.getExtras().getString("notify") != null)
        {
            user = (User) context.getApplicationContext();
            String message = Html.fromHtml(intent.getExtras().getString("alert")).toString();
            String isNotify = intent.getExtras().getString("notify");

            if (isNotify.equals("invite_chatroom") || isNotify.equals("received_message")) {
                    userId = intent.getExtras().getString("dialog_id");
            } else if (isNotify.equals("notification")) {
                try {
                    JSONObject route = new JSONObject(intent.getExtras().getString("action"));
                    JSONObject request = route.getJSONObject("request");

                    if (request.has("item_id") && request.has("type_id")) {
                        itemId = request.getString("item_id");
                        typeId = request.getString("type_id");
                        link = intent.getExtras().getString("link");
                    } else if (request.has("user_id")) {
                        userId = request.getString("user_id");
                    }
                } catch (JSONException e) {
                    Log.i(TAG, "error json");
                }
            } else if (isNotify.equals("mail")) {
                //check if contain key in extra
                previewStr = intent.getExtras().getString("preview");
                threadId = intent.getExtras().getString("thread");

                Log.i(TAG, previewStr + " " + threadId);

                fullname = intent.getExtras().getString("full_name");
            } else if (isNotify.equals("admin")) {
                try {
                    JSONObject route = new JSONObject(intent.getExtras().getString("action"));
                    if (route.has("link")) {
                        action = route.getString("link").toString();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            displayMessage(context, isNotify, previewStr, threadId);

            try {
                // Using ACTIVITY_SERVICE with getSystemService(String)
                // to retrieve a ActivityManager for interacting with the global system state.
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

                // Return a list of the tasks that are currently running,
                // with the most recent being first and older ones after in order.
                // Taken 1 inside getRunningTasks method means want to take only
                // top activity from stack and forgot the olders.
                List<ActivityManager.RunningTaskInfo> alltasks = am.getRunningTasks(1);

                if (user.getTokenkey() != null && !alltasks.get(0).topActivity.getClassName().equals("com.brodev.socialapp.view.SplashActivity")
                        && !alltasks.get(0).topActivity.getClassName().equals("com.brodev.socialapp.view.LoginActivity")) {
                    generateNotification(context, message, isNotify, userId);
                }
            } catch (Exception ex) {

            }
        }
    }


    @Override
    protected void onDeletedMessages(Context context, int total) {

        Log.i(TAG, "Received deleted messages notification");

    }



    @Override

    public void onError(Context context, String errorId) {

        Log.i(TAG, "Received error: " + errorId);

    }



    @Override

    protected boolean onRecoverableError(Context context, String errorId) {

        // log message

        Log.i(TAG, "Received recoverable error: " + errorId);

        return super.onRecoverableError(context, errorId);

    }

    private void parseMessage(Bundle extras) {
        message = extras.getString(NotificationHelper.MESSAGE);
        dialogId = extras.getString(NotificationHelper.DIALOG_ID);
        userId = extras.getString(NotificationHelper.USER_ID);

        sendNotification();
    }

    private void sendNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, SplashActivity.class);

        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, ConstsCore.ZERO_INT_VALUE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(
                R.drawable.ic_launcher).setContentTitle(getString(R.string.push_title)).setStyle(
                new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message).setVibrate(
                new long[]{ConstsCore.ZERO_INT_VALUE, VIBRATOR_DURATION});

        builder.setAutoCancel(true);
        builder.setContentIntent(contentIntent);
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void sendBroadcast(PushMessage message) {
        Intent intent = new Intent();
        intent.setAction(NotificationHelper.ACTION_VIDEO_CALL);
        QBUser qbUser = new QBUser();
        qbUser.setId(message.getUserId());
        intent.putExtra(ConstsCore.USER, qbUser);
        sendBroadcast(intent);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
    private static void generateNotification(Context context, String message, String notify, String userId) {

        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);

        String title = context.getString(R.string.app_name);
        Intent notificationIntent = null;

        //if is notification
        if (notify.equals("invite_chatroom") || notify.equals("received_message")) {
            QBDialog dialog = DatabaseManager.getDialogByDialogId(context, userId);

            if (dialog.getType() == QBDialogType.PRIVATE) {
                int occupantId = ChatUtils.getOccupantIdFromList(dialog.getOccupants());
                com.quickblox.q_municate_core.models.User occupant = DatabaseManager.getUserById(context, occupantId);
                /*
                notificationIntent = new Intent(context, DashboardActivity.class);
                if (!action.startsWith(user.getCoreUrl())) {
                    if (!action.startsWith("http://") && !action.startsWith("https://"))
                        action = "http://" + action;
                    notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(action));
                } else if (action.startsWith(user.getCoreUrl())) {
                    notificationIntent = new Intent(context, WebviewActivity.class);
                    notificationIntent.putExtra("html", action);
                } */
                /*
                notificationIntent = new Intent(context, PrivateDialogActivity.class);
                notificationIntent.putExtra(QBServiceConsts.EXTRA_OPPONENT, occupant);
                notificationIntent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
                */
                notificationIntent = new Intent(context, DashboardActivity.class);
                notificationIntent.putExtra("email", occupant.getEmail());
            } else {
                notificationIntent = new Intent(context, GroupDialogActivity.class);
                notificationIntent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, dialog.getDialogId());
                notificationIntent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
            }
        } else if (notify.equals("notification")) {
            if (userId != null) {
                notificationIntent = new Intent(context, FriendTabsPager.class);
                notificationIntent.putExtra("user_id", userId);
            } else {
                if (typeId.equals("poke")) {
                    notificationIntent = new Intent(context, FriendTabsPager.class);
                    notificationIntent.putExtra("user_id", itemId);
                } else {
                    notificationIntent = new NotificationNextActivity(context).notificationLinkActivity(notificationIntent, typeId, itemId, link, false);
                }
            }
        } else if (notify.equals("mail")) {
            notificationIntent = new Intent(context, ConversationActivity.class);
            notificationIntent.putExtra("thread_id", Integer.parseInt(threadId));
            notificationIntent.putExtra("fullname", fullname);
            notificationIntent.putExtra("page", 1);
        } else if (notify.equals("add_friend")) {
            notificationIntent = new Intent(context, ConfirmRequestActivity.class);
        } else if (notify.equals("admin")) {
            notificationIntent = new Intent(context, DashboardActivity.class);
            if (!action.startsWith(user.getCoreUrl())) {
                if (!action.startsWith("http://") && !action.startsWith("https://"))
                    action = "http://" + action;
                notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(action));
            } else if (action.startsWith(user.getCoreUrl())) {
                notificationIntent = new Intent(context, WebviewActivity.class);
                notificationIntent.putExtra("html", action);
            }

        }

        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(context, title, message, intent);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        notificationManager.notify(R.id.notification_id, notification);
    }

    public static void displayMessage(Context context, String isNotify, String mail, String thread_id) {
        if (isNotify.equals("invite_chatroom") || isNotify.equals("received_message"))
            isNotify = "notification";
        Intent intent = new Intent(Config.DISPLAY_MESSAGE_ACTION);
        intent.putExtra("message", isNotify);
        intent.putExtra("mail", mail);
        intent.putExtra("thread_id", thread_id);

        context.sendBroadcast(intent);
    }
}