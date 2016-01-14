package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.http.NetworkUntil;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.GroupDialog;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendsToGroupCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.FriendUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class AddFriendsToGroupActivity extends BaseAddFriendListActivity implements NewDialogCounterFriendsListener {

    private static final String EXTRA_GROUP_DIALOG = "extra_group_dialog";

    private GroupDialog dialog;

    private com.brodev.socialapp.entity.User user;

    public static void start(Context context, GroupDialog dialog) {
        Intent intent = new Intent(context, AddFriendsToGroupActivity.class);
        intent.putExtra(EXTRA_GROUP_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2b2b2b")));
        actionBar.setTitle("Add Friends");

        addActions();
    }

    @Override
    protected Cursor getFriends() {
        dialog = (GroupDialog) getIntent().getExtras().getSerializable(EXTRA_GROUP_DIALOG);
        return DatabaseManager.getFriendsFilteredByIds(this, FriendUtils.getFriendIds(
                dialog.getOccupantList()));
    }

    @Override
    protected void onFriendsSelected(ArrayList<User> selectedFriends, QBDialogType type, String groupName) {
        dialog = (GroupDialog) getIntent().getExtras().getSerializable(EXTRA_GROUP_DIALOG);
        showProgress();
        QBAddFriendsToGroupCommand.start(this, dialog.getId(), FriendUtils.getFriendIds(selectedFriends));

        // GCM Add Friends to Group
        AddFriendGCM afg = new AddFriendGCM(this);
        afg.execute(selectedFriends);
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

    private void addActions() {
        addAction(QBServiceConsts.ADD_FRIENDS_TO_GROUP_SUCCESS_ACTION, new AddFriendsToGroupSuccessCommand());
        updateBroadcastActionList();
    }

    private class AddFriendsToGroupSuccessCommand implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            finish();
        }
    }

    public class AddFriendGCM extends AsyncTask<ArrayList<User>, Void, String> {
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
            pairs.add(new BasicNameValuePair("dialog_id", dialog.getId()));

            String result = network.makeHttpRequest(URL, "POST", pairs);

            return result;

        }
    }
}