package com.brodev.socialapp.view.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brodev.socialapp.entity.Friend;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.mypinkpal.app.R;
import com.brodev.socialapp.view.chats.GroupDialogActivity;
import com.brodev.socialapp.view.chats.PrivateDialogActivity;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ActivityUIHelper {

    private Activity activity;
    private View newMessageView;
    private TextView newMessageTextView;
    private TextView senderMessageTextView;
    private Button replyMessageButton;

    private User senderUser;
    private QBDialog messagesDialog;
    private boolean isPrivateMessage;

    public ActivityUIHelper(Activity activity) {
        this.activity = activity;
        initUI();
        initListeners();
    }

    private void initUI() {
        newMessageView = activity.getLayoutInflater().inflate(R.layout.list_item_new_message,
                null);
        newMessageTextView = (TextView) newMessageView.findViewById(R.id.message_textview);
        senderMessageTextView = (TextView) newMessageView.findViewById(R.id.sender_textview);
        replyMessageButton = (Button) newMessageView.findViewById(R.id.replay_button);
    }

    private void initListeners() {
        replyMessageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                replyMessage();
            }
        });
    }

    protected void onReceiveMessage(Bundle extras) {
        senderUser = (User) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        senderUser.getUserId();
        final String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        isPrivateMessage = extras.getBoolean(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE);
        messagesDialog = DatabaseManager.getDialogByDialogId(activity, dialogId);
        if (messagesDialog == null) {
            QBAddFriendCommand.start(activity, senderUser.getUserId());
            QBChatService.getInstance().getPrivateChatManager().createDialog(senderUser.getUserId(), new QBEntityCallbackImpl<QBDialog>() {
                @Override
                public void onSuccess(QBDialog dialog, Bundle args) {
                    messagesDialog = dialog;
                    showNewMessageAlert(senderUser, message);
                }

                @Override
                public void onError(List<String> errors) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                    dialog.setMessage("dialog creation errors: " + errors).create().show();
                }
            });
        } else {
            showNewMessageAlert(senderUser, message);
        }
    }

    public void showNewMessageAlert(User senderUser, String message) {
        newMessageTextView.setText(message);
        senderMessageTextView.setText(senderUser.getFullName());
        Crouton.cancelAllCroutons();
        Crouton.show(activity, newMessageView);
    }

    protected void replyMessage() {
        if (isPrivateMessage) {
            startPrivateChatActivity();
        } else {
            startGroupChatActivity();
        }
        Crouton.cancelAllCroutons();
    }

    private void startPrivateChatActivity() {
        PrivateDialogActivity.start(activity, senderUser, messagesDialog);
    }

    private void startGroupChatActivity() {
        GroupDialogActivity.start(activity, messagesDialog);
    }
}