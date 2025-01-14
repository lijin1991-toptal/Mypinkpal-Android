package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brodev.socialapp.view.imageview.MaskedImageView;
import com.quickblox.chat.model.QBDialog;
import com.mypinkpal.app.R;
import com.brodev.socialapp.view.chats.emoji.EmojiTextView;
import com.brodev.socialapp.view.imageview.MaskedImageView;
import com.brodev.socialapp.utils.DateUtils;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.FriendsNotificationType;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.qb.commands.QBUpdateStatusMessageCommand;

public class PrivateDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    private static int TYPE_REQUEST_MESSAGE = 0;
    private static int TYPE_OWN_MESSAGE = 1;
    private static int TYPE_OPPONENT_MESSAGE = 2;
    private static int COMMON_TYPE_COUNT = 3;

    private static int EMPTY_POSITION = -1;

    private int lastRequestPosition = EMPTY_POSITION;
    private int lastInfoRequestPosition = EMPTY_POSITION;
    private PrivateDialogActivity.FriendOperationListener friendOperationListener;

    public PrivateDialogMessagesAdapter(Context context,
            PrivateDialogActivity.FriendOperationListener friendOperationListener, Cursor cursor,
            ScrollMessagesListener scrollMessagesListener, QBDialog dialog) {
        super(context, cursor);
        this.friendOperationListener = friendOperationListener;
        this.scrollMessagesListener = scrollMessagesListener;
        this.dialog = dialog;
    }

    private int getItemViewType(Cursor cursor) {
        MessageCache messageCache = DatabaseManager.getMessageCacheFromCursor(cursor);
        boolean ownMessage = isOwnMessage(messageCache.getSenderId());
        boolean friendsRequestMessage = messageCache.getFriendsNotificationType() != null;

        if (!friendsRequestMessage) {
            if (ownMessage) {
                return TYPE_OWN_MESSAGE;
            } else {
                return TYPE_OPPONENT_MESSAGE;
            }
        } else {
            return TYPE_REQUEST_MESSAGE;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    @Override
    public int getViewTypeCount() {
        return COMMON_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();

        MessageCache messageCache = DatabaseManager.getMessageCacheFromCursor(cursor);
        boolean ownMessage = isOwnMessage(messageCache.getSenderId());

        if (messageCache.getFriendsNotificationType() == null) {
            if (ownMessage) {
                view = layoutInflater.inflate(R.layout.list_item_message_own, null, true);
            } else {
                view = layoutInflater.inflate(R.layout.list_item_private_message_opponent, null, true);
            }

            viewHolder.attachMessageRelativeLayout = (RelativeLayout) view.findViewById(
                    R.id.attach_message_relativelayout);
            viewHolder.timeAttachMessageTextView = (TextView) view.findViewById(
                    R.id.time_attach_message_textview);
            viewHolder.progressRelativeLayout = (RelativeLayout) view.findViewById(
                    R.id.progress_relativelayout);
            viewHolder.textMessageView = view.findViewById(R.id.text_message_view);
            viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
            viewHolder.attachImageView = (MaskedImageView) view.findViewById(R.id.attach_imageview);
            viewHolder.timeTextMessageTextView = (TextView) view.findViewById(
                    R.id.time_text_message_textview);
            viewHolder.verticalProgressBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
            viewHolder.verticalProgressBar.setProgressDrawable(context.getResources().getDrawable(
                    R.drawable.vertical_progressbar));
            viewHolder.centeredProgressBar = (ProgressBar) view.findViewById(R.id.centered_progressbar);
        } else {
            view = layoutInflater.inflate(R.layout.list_item_friends_request_message, null, true);

            viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
            viewHolder.timeTextMessageTextView = (TextView) view.findViewById(
                    R.id.time_text_message_textview);
            viewHolder.acceptFriendImageView = (ImageView) view.findViewById(R.id.accept_friend_imagebutton);
            viewHolder.dividerView = view.findViewById(R.id.divider_view);
            viewHolder.rejectFriendImageView = (ImageView) view.findViewById(R.id.reject_friend_imagebutton);
        }

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        MessageCache messageCache = DatabaseManager.getMessageCacheFromCursor(cursor);

        boolean ownMessage = isOwnMessage(messageCache.getSenderId());
        boolean friendsRequestMessage = FriendsNotificationType.REQUEST.equals(
                messageCache.getFriendsNotificationType());
        boolean friendsInfoRequestMessage = messageCache
                .getFriendsNotificationType() != null && !friendsRequestMessage;

        if (friendsRequestMessage) {
            viewHolder.messageTextView.setText(messageCache.getMessage());
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));

            setVisibilityFriendsActions(viewHolder, View.GONE);
        } else if (friendsInfoRequestMessage) {
            viewHolder.messageTextView.setText(messageCache.getMessage());
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));

            setVisibilityFriendsActions(viewHolder, View.GONE);

            lastInfoRequestPosition = cursor.getPosition();
        } else if (!TextUtils.isEmpty(messageCache.getAttachUrl())) {
            resetUI(viewHolder);

            setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
            viewHolder.timeAttachMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
            displayAttachImage(messageCache.getAttachUrl(), viewHolder);
        } else {
            resetUI(viewHolder);

            setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
            viewHolder.messageTextView.setText(messageCache.getMessage());
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
        }

        if (ownMessage && !friendsRequestMessage && !friendsInfoRequestMessage) {
            setMessageStatus(view, viewHolder, R.id.text_message_delivery_status_imageview,
                    messageCache.isDelivered(), messageCache.isRead());
        }

        if (!messageCache.isRead() && !ownMessage) {
            messageCache.setRead(true);
            QBUpdateStatusMessageCommand.start(context, dialog, messageCache, true);
        }

        // check if last message is request message
        boolean lastRequestMessage = cursor.getPosition() == cursor.getCount() - 1 && friendsRequestMessage;
        if (lastRequestMessage) {
            findLastFriendsRequestForCursor(cursor);
        }

        // check if friend was rejected/deleted.
        if (lastRequestPosition != EMPTY_POSITION && lastRequestPosition < lastInfoRequestPosition) {
            lastRequestPosition = EMPTY_POSITION;
        } else if ((lastRequestPosition != EMPTY_POSITION && lastRequestPosition == cursor.getPosition())) { // set visible friends actions
            setVisibilityFriendsActions(viewHolder, View.VISIBLE);
            initListeners(viewHolder, messageCache.getSenderId());
        }
    }

    public void clearLastRequestMessagePosition() {
        lastRequestPosition = EMPTY_POSITION;
    }

    public void findLastFriendsRequestMessagesPosition() {
        new FindLastFriendsRequestThread().run();
    }

    private void setVisibilityFriendsActions(ViewHolder viewHolder, int visibility) {
        setViewVisibility(viewHolder.acceptFriendImageView, visibility);
        setViewVisibility(viewHolder.dividerView, visibility);
        setViewVisibility(viewHolder.rejectFriendImageView, visibility);
    }

    private void initListeners(ViewHolder viewHolder, final int userId) {
        viewHolder.acceptFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onAcceptUserClicked(userId);
            }
        });

        viewHolder.rejectFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onRejectUserClicked(userId);
            }
        });
    }

    private void findLastFriendsRequest() {
        Cursor cursor = getCursor();
        for (int i = 0; i < getCursor().getCount(); i++) {
            cursor.moveToPosition(i);
            findLastFriendsRequestForCursor(cursor);
        }
    }

    private void findLastFriendsRequestForCursor(Cursor cursor) {
        boolean ownMessage;
        boolean friendsRequestMessage;
        boolean isFriend;

        MessageCache messageCache = DatabaseManager.getMessageCacheFromCursor(cursor);
        if (messageCache.getFriendsNotificationType() != null) {
            ownMessage = isOwnMessage(messageCache.getSenderId());
            friendsRequestMessage = FriendsNotificationType.REQUEST.equals(
                    messageCache.getFriendsNotificationType());

            if (friendsRequestMessage && !ownMessage) {
//                isFriend = DatabaseManager.isFriendInBase(context, messageCache.getSenderId());
                  isFriend = true;
                if (!isFriend) {
                    lastRequestPosition = cursor.getPosition();
                }
            }
        }
    }

    private class FindLastFriendsRequestThread extends Thread {

        @Override
        public void run() {
            findLastFriendsRequest();
        }
    }
}