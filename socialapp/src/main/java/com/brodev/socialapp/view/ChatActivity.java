package com.brodev.socialapp.view;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.chat.ChatManager;
//import com.brodev.chat.GroupChatManagerImpl;
import com.brodev.chat.PrivateChatManagerImpl;
import com.brodev.chat.adapter.ChatAdapter;
import com.mypinkpal.app.R;
import com.brodev.socialapp.entity.User;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
//import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatHistoryMessage;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBMessage;


public class ChatActivity extends SherlockFragmentActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private String fullname, userId, imageUser, username, quickbloxId;
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_DIALOG = "dialog";
    private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    private User user;
    private EditText messageEditText;
    private ListView messagesContainer;
    private Button sendButton;
    private ProgressBar progressBar;

    private Mode mode = Mode.PRIVATE;
    private ChatManager chat;
    private ChatAdapter adapter;
    private QBDialog selectedDialog;
    private QBDialog dialog;

    private ArrayList<QBChatHistoryMessage> history;

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        user = (User) getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (getIntent().hasExtra("fullname"))
                fullname = bundle.getString("fullname");

            if (getIntent().hasExtra("user_id"))
                userId = bundle.getString("user_id");

            if (getIntent().hasExtra("image"))
                imageUser = bundle.getString("image");

            // bronislaw
            if (getIntent().hasExtra("username"))
                username = bundle.getString("username");

            if (getIntent().hasExtra("quickbloxid"))
                quickbloxId = bundle.getString("quickbloxid");
        }

        initViews();
        getSupportActionBar().setTitle(fullname);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (quickbloxId != null)
            getSupportMenuInflater().inflate(R.menu.chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            /*
            case R.id.action_view_profile:
                Intent intent = new Intent(this, FriendTabsPager.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                return true;
                */
            case R.id.action_audio_call:
                intent = new Intent(this, VoiceActivity.class);
                intent.putExtra("fullname", fullname);
                intent.putExtra("quickbloxid", quickbloxId);
                intent.putExtra("calltype", "voice_type");
                startActivity(intent);
                return true;
            case R.id.action_video_call:
                intent = new Intent(this, VideoActivity.class);
                intent.putExtra("fullname", fullname);
                intent.putExtra("quickbloxid", quickbloxId);
                intent.putExtra("calltype", "video_type");
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        try {
            chat.release();
            finish();
        } catch (XMPPException e) {
            Log.e(TAG, "failed to release chat", e);
        }
        super.onBackPressed();
    }

    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);

        TextView meLabel = (TextView) findViewById(R.id.meLabel);
        final TextView companionLabel = (TextView) findViewById(R.id.companionLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (quickbloxId == null) {
        /* group chat and chatrooms */
            Intent intent = getIntent();
            dialog = (QBDialog)intent.getSerializableExtra(EXTRA_DIALOG);
            fullname = dialog.getName();
            mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);
//            chat = new GroupChatManagerImpl(this);
            container.removeView(meLabel);
            container.removeView(companionLabel);

            // Join group chat
            //
            progressBar.setVisibility(View.VISIBLE);
//            //
//            ((GroupChatManagerImpl) chat).joinGroupChat(dialog, new QBEntityCallbackImpl() {
//                @Override
//                public void onSuccess() {
//
//                    // Load Chat history
//                    //
//                    loadChatHistory(dialog);
//                }
//
//                @Override
//                public void onError(List list) {
//                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
//                    dialog.setMessage("error when join group chat: " + list.toString()).create().show();
//                }
//            });
        } else {
            /* private chat */
            // Create chat dialog
            //
            selectedDialog = new QBDialog();
            selectedDialog.setName(username);
            selectedDialog.setType(QBDialogType.PRIVATE);

            ArrayList<Integer> ids = new ArrayList();
            int intID = (Integer.valueOf(quickbloxId));
            ids.add(intID);
            selectedDialog.setOccupantsIds(ids);

            QBChatService.getInstance().getPrivateChatManager().createDialog(intID, new QBEntityCallbackImpl<QBDialog>() {
                @Override
                public void onSuccess(QBDialog dialog, Bundle args) {

                    Integer opponentID = ((User) getApplication()).getOpponentIDForPrivateDialog(dialog);

                    chat = new PrivateChatManagerImpl(ChatActivity.this, opponentID);

                    companionLabel.setText(fullname);

                    // Load CHat history
                    //
                    loadChatHistory(dialog);

                }

                @Override
                public void onError(List<String> errors) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                    dialog.setMessage("dialog creation errors: " + errors).create().show();
                }
            });
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                // Send chat message
                //
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(messageText);
                chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");

                try {
                    chat.sendMessage(chatMessage);
                } catch (XMPPException e) {
                    Log.e(TAG, "failed to send a message", e);
                } catch (SmackException sme){
                    Log.e(TAG, "failed to send a message", sme);
                }

                messageEditText.setText("");

                if(mode == Mode.PRIVATE) {
                    showMessage(chatMessage);
                }
            }
        });
    }

    private void loadChatHistory(QBDialog dialog){
        /*
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(100);

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatHistoryMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatHistoryMessage> messages, Bundle args) {
                history = messages;

                adapter = new ChatAdapter(ChatActivity.this, new ArrayList<QBMessage>());
                messagesContainer.setAdapter(adapter);

                for (QBMessage msg : messages) {
                    showMessage(msg);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                dialog.setMessage("load chat history errors: " + errors).create().show();
            }
        });
        */
    }


    public void showMessage(QBMessage message) {
        adapter.add(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    public static enum Mode {PUBLIC_GROUP, GROUP, PRIVATE}

}
