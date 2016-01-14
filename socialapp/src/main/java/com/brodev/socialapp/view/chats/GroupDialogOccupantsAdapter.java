package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brodev.socialapp.view.BaseActivity;
import com.mypinkpal.app.R;
import com.brodev.socialapp.view.base.BaseListAdapter;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class GroupDialogOccupantsAdapter extends BaseListAdapter<User> {

    public GroupDialogOccupantsAdapter(BaseActivity baseActivity, List<User> objectsList) {
        super(baseActivity, objectsList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        User user = getItem(position);
        QBUser currendUser = AppSession.getSession().getUser();
        boolean status = false;
        ArrayList<User> userlist = new ArrayList<User>();
        userlist = ChatUtils.getJoinUsers();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_dialog_friend, null);
            viewHolder = new ViewHolder();

            viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            viewHolder.onlineImageView = (ImageView) convertView.findViewById(R.id.online_imageview);
            viewHolder.onlineImageView.setVisibility(View.GONE);
//            viewHolder.onlineStatusMessageTextView = (TextView) convertView.findViewById(
//                    R.id.statusMessageTextView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // self account
        if (currendUser.getId() == user.getUserId())
            status = true;

        for (int i = 0; i < userlist.size(); i++) {
            if (user.getUserId() == userlist.get(i).getUserId())
                status = true;
        }

        String fullName;
        if (isFriend(user)) {
            fullName = user.getFullName();
//            viewHolder.onlineStatusMessageTextView.setVisibility(View.VISIBLE);
        } else {
            fullName = String.valueOf(user.getUserId());
//            viewHolder.onlineStatusMessageTextView.setVisibility(View.GONE);
        }

        viewHolder.nameTextView.setText(fullName);
        if (status == true)
            viewHolder.nameTextView.setTextColor(Color.BLACK);
        else
            viewHolder.nameTextView.setTextColor(Color.GRAY);

        setOnlineStatusVisibility(viewHolder, user);

        /* get avatar url */
        //bronislaw
        if (user.getAvatarUrl() == null || user.getAvatarUrl().equals("") || user.getAvatarUrl().equals("null")) {
            SharedPreferences pref = baseActivity.getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
            String aUrl = pref.getString(fullName, "");
            user.setAvatarUrl(aUrl);
        }

        displayImage(user.getAvatarUrl(), viewHolder.avatarImageView);

        return convertView;
    }

    private void setOnlineStatusVisibility(ViewHolder viewHolder, User user) {
        if(isMe(user)) {
            user.setOnline(true);
        }

//        viewHolder.onlineStatusMessageTextView.setText(user.getOnlineStatus(baseActivity));
//        if (user.isOnline()) {
//            viewHolder.onlineImageView.setVisibility(View.VISIBLE);
//        } else {
//            viewHolder.onlineImageView.setVisibility(View.GONE);
//        }
    }

    private boolean isFriend(User user) {
        return user.getFullName() != null;
    }

    private boolean isMe(User inputUser) {
        QBUser currentUser = AppSession.getSession().getUser();
        return currentUser.getId() == inputUser.getUserId();
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        TextView nameTextView;
        ImageView onlineImageView;
//        TextView onlineStatusMessageTextView;
    }
}