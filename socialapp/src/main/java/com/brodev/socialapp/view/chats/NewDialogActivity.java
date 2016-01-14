package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.http.NetworkUntil;
import com.quickblox.chat.model.QBDialog;
import com.mypinkpal.app.R;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBCreateGroupDialogCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class NewDialogActivity extends BaseSelectableFriendListActivity implements NewDialogCounterFriendsListener {

    private com.brodev.socialapp.entity.User user;
    private ArrayList<User> friendLists = new ArrayList<User>();
    private QBDialog newDialog;

    public static void start(Context context) {
        Intent intent = new Intent(context, NewDialogActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2b2b2b")));
        actionBar.setTitle("New Group Chat");

        addActions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
        }
        return false;
    }

    @Override
    protected Cursor getFriends() {
        return DatabaseManager.getAllFriends(this);
    }

    @Override
    protected void onFriendsSelected(ArrayList<User> selectedFriends, QBDialogType type, String groupName) {
        createChat(selectedFriends, type, groupName);
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);
    }

    protected void addActions() {
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION, new CreateChatSuccessAction());
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    private void createChat(ArrayList<User> friendList, QBDialogType type, String groupName) {
        showProgress();
        this.friendLists = friendList;
//        String groupName = createChatName(friendList);
        QBCreateGroupDialogCommand.start(this, groupName, friendList, type);
    }

    private String createChatName(ArrayList<User> friendList) {
        String userFullname = AppSession.getSession().getUser().getFullName();
        String friendsFullnames = TextUtils.join(", ", friendList);
        return userFullname + ", " + friendsFullnames;
    }

    private class CreateChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            QBDialog dialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            if (dialog.getRoomJid() != null) {
                // GCM Add Friends to Group
                newDialog = dialog;
                AddFriendGCM afg = new AddFriendGCM(getApplicationContext());
                afg.execute(friendLists);

                GroupDialogActivity.start(NewDialogActivity.this, dialog);
                finish();
            } else {
                ErrorUtils.showError(NewDialogActivity.this, getString(R.string.dlg_fail_create_groupchat));
            }
        }
    }

    public class AddFriendGCM extends AsyncTask<ArrayList<User>, View, String> {
        private NetworkUntil network = new NetworkUntil();

        public AddFriendGCM(Context context) {
            user = (com.brodev.socialapp.entity.User) context.getApplicationContext();
        }

        @Override
        protected String doInBackground(ArrayList<User>... params) {
            String URL = null;
            ArrayList<User> lists = new ArrayList<User>();
            lists = params[0];

            if (Config.CORE_URL == null) {
                URL = Config.makeUrl(user.getCoreUrl(), null, false);
            } else {
                URL = Config.makeUrl(Config.CORE_URL, null, false);
            }

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
            pairs.add(new BasicNameValuePair("method", "accountapi.notifyInvitationChatroom"));
            for (int i = 0; i < lists.size(); i++) {
                pairs.add(new BasicNameValuePair("user_id[" + i + "]", String.valueOf(lists.get(i).getExternalId())));
            }
            pairs.add(new BasicNameValuePair("dialog_id", newDialog.getDialogId()));

            String result = network.makeHttpRequest(URL, "POST", pairs);

            return result;

        }
    }
}