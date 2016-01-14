package com.brodev.socialapp.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.brodev.socialapp.android.ChatroomsAdapter;
import com.brodev.socialapp.entity.User;
import com.mypinkpal.app.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
//import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bebel on 1/14/15.
 */
public class ChatroomsActivity extends SherlockFragmentActivity {

    private ListView dialogsListView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatrooms);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chatrooms");

        dialogsListView = (ListView) findViewById(R.id.roomsList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        // get dialogs
        //
        /*
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(100);

        QBChatService.getChatDialogs(null, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> dialogs, Bundle args) {

                // collect all occupants ids
                //
                final List<QBDialog> groupDialogs = new ArrayList<QBDialog>();
                List<Integer> usersIDs = new ArrayList<Integer>();

                for (QBDialog dialog : dialogs) {
                    usersIDs.addAll(dialog.getOccupants());
                    if (dialog.getName() != null && dialog.getName().length() != 0 && dialog.getType() != QBDialogType.PRIVATE) {
                        groupDialogs.add(dialog);
                    }
                }

                // Get all occupants info
                //
                QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
                requestBuilder.setPage(1);
                requestBuilder.setPerPage(usersIDs.size());
                //
                QBUsers.getUsersByIDs(usersIDs, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {

                        // Save users
                        //
                        ((User) getApplication()).setDialogsUsers(users);

                        // build list view
                        //
                        buildListView(groupDialogs);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatroomsActivity.this);
                        dialog.setMessage("get occupants errors: " + errors).create().show();
                    }

                });
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChatroomsActivity.this);
                dialog.setMessage("get dialogs errors: " + errors).create().show();
            }
        });
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


    void buildListView(List<QBDialog> dialogs){
        final ChatroomsAdapter adapter = new ChatroomsAdapter(dialogs, ChatroomsActivity.this);
        dialogsListView.setAdapter(adapter);

        progressBar.setVisibility(View.GONE);

        // choose dialog
        //
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog)adapter.getItem(position);

                /*
                Intent intent = null;
                intent = new Intent(ChatroomsActivity.this, ChatActivity.class);
//                intent.putExtra("fullname", selectedDialog.get);
                intent.putExtra("user_id", friend.getUser_id());
                intent.putExtra("image", friend.getIcon());
                intent.putExtra("username", friend.getUsername());
                intent.putExtra("quickbloxid", friend.getQuickbloxid());

                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                } */

                Bundle bundle = new Bundle();
                bundle.putSerializable(ChatActivity.EXTRA_DIALOG, (QBDialog)adapter.getItem(position));

                // group
                if (selectedDialog.getType().equals(QBDialogType.GROUP)){
                    bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.GROUP);
                } else if (selectedDialog.getType().equals(QBDialogType.PUBLIC_GROUP)) {
                    bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.PUBLIC_GROUP);
                }

                // Open chat activity
                //
                ChatActivity.start(ChatroomsActivity.this, bundle);
            }
        });
    }

}
