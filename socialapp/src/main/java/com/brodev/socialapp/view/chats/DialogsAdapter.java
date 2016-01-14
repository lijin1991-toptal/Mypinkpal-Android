package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brodev.socialapp.entity.Friend;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.mypinkpal.app.R;
import com.brodev.socialapp.view.base.BaseCursorAdapter;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;

public class DialogsAdapter extends BaseCursorAdapter {

    public DialogsAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView;
        convertView = layoutInflater.inflate(R.layout.list_item_dialog, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
        viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
        viewHolder.lastMessageTextView = (TextView) convertView.findViewById(R.id.last_message_textview);
        viewHolder.unreadMessagesTextView = (TextView) convertView.findViewById(
                R.id.unread_messages_textview);
        convertView.setTag(viewHolder);
        return convertView;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        QBDialog dialog = DatabaseManager.getDialogFromCursor(cursor);

        if (dialog.getType().equals(QBDialogType.PRIVATE)) {
            int occupantId = ChatUtils.getOccupantIdFromList(dialog.getOccupants());
            User occupant = getOccupantById(occupantId);
            viewHolder.nameTextView.setText(occupant.getFullName());
            displayAvatarImage(getAvatarUrlForFriend(occupant), viewHolder.avatarImageView);
        } else {
            viewHolder.nameTextView.setText(dialog.getName());
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(dialog.getPhoto(), viewHolder.avatarImageView);
        }

        if (dialog.getUnreadMessageCount() > ConstsCore.ZERO_INT_VALUE) {
            viewHolder.unreadMessagesTextView.setText(dialog.getUnreadMessageCount() + ConstsCore.EMPTY_STRING);
            viewHolder.unreadMessagesTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        viewHolder.lastMessageTextView.setText(dialog.getLastMessage());
    }

    public User getOccupantById(int occupantId) {
        User friend = DatabaseManager.getUserById(context, occupantId);
        if (friend == null) {
            friend = new User();
            friend.setUserId(occupantId);
            friend.setFullName(occupantId + ConstsCore.EMPTY_STRING);
        }
        return friend;
    }

    public User getOccupantByIdAndFriend(int occupantId, Friend friend) {
        User user = DatabaseManager.getUserById(context, occupantId);
        if (user == null) {
            user = new User();
            user.setUserId(occupantId);
            if (friend != null && occupantId == Integer.parseInt(friend.getQuickbloxid()) && friend.getFullname() != null)
                user.setFullName(friend.getFullname());
            else
                user.setFullName(occupantId + ConstsCore.EMPTY_STRING);
        } else if (user.getFullName() == null || user.getFullName().equals("***") == true) {
            if (friend != null && occupantId == Integer.parseInt(friend.getQuickbloxid()) && friend.getFullname() != null)
                user.setFullName(friend.getFullname());
            else
                user.setFullName(occupantId + ConstsCore.EMPTY_STRING);
        }

        user.setExternalId(friend.getUser_id());
        user.setAvatarUrl(friend.getIcon());

        return user;
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}