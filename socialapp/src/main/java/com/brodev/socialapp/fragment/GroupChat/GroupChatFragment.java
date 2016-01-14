package com.brodev.socialapp.fragment.GroupChat;

//import android.app.LoaderManager;
//import android.content.Loader;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.BaseActivity;
import com.brodev.socialapp.view.ChatActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.brodev.socialapp.view.chats.GroupDialogActivity;
import com.brodev.socialapp.view.chats.GroupDialogsAdapter;
import com.brodev.socialapp.view.chats.NewDialogActivity;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mypinkpal.app.R;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.commands.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Created by Bebel on 2/3/15.
 */
public class GroupChatFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DIALOGS_LOADER_ID = 0;

    private User user;
    private int page, total, currentPos;
    private NetworkUntil networkUntil = new NetworkUntil();
    private PhraseManager phraseManager;
    private ColorView colorView;
    private String notiMess, sAvatarUrl;
    private Pattern pattern;
    private EditText searchEdt;
    private ImageView createGroupImg;
    private FrameLayout searchLayout;
    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;
    private BaseActivity baseActivity;
    private BaseActivity.FailAction failAction;
    private TextView emptyListTextView;
    private FriendAdapter fa = null;
    private FriendAdapter faSearch;
    private Timer tTimer = new Timer();

    private GroupDialogsAdapter dialogsAdapter;
    private GroupDialogsAdapter tempAdapter;

    public static GroupChatFragment newInstance() {
        return new GroupChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = (User) getActivity().getApplicationContext();
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());
        page = 1;
        total = 1;
        notiMess = null;
        sAvatarUrl = null;
        //  chatNotification = new ChatNotification();
        pattern = Pattern.compile("src=\"(.*?)\"");

//        Log.d("psyh", "MEMBERSHIP: " + user.getMembership());

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groupchat_fragment, container, false);

        searchLayout = (FrameLayout) view.findViewById(R.id.frame_search);

        /*
        searchEdt = (EditText) view.findViewById(R.id.searchEdit);
        searchEdt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                asyncSearch(s.toString());
            }
        });
        */

        createGroupImg = (ImageView) view.findViewById(R.id.create_groupchat_imageview);
        createGroupImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewDialogPage();
            }
        });

        emptyListTextView = (TextView) view.findViewById(R.id.empty_list_textview);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.groupchat_fragment_list);
        /*
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                fa = new FriendAdapter(getActivity().getApplicationContext());
                new FriendTask().execute(page);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ++page;
                new FriendTask().execute(page);
            }

        });
        */

        /*

        dialogsListview = (ListView) view.findViewById(R.id.groupchat_listview);
        dialogsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                Cursor selectedChatCursor = (Cursor) dialogsAdapter.getItem(position);
                QBDialog dialog = DatabaseManager.getDialogFromCursor(selectedChatCursor);
                startGroupChatActivity(dialog);
            }
        });
        */

        baseActivity = (BaseActivity) getActivity();
        failAction = baseActivity.getFailAction();

        Crouton.cancelAllCroutons();
        addActions();
        initCursorLoaders();

//        QBLoadDialogsCommand.start(getActivity());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actualListView = mPullRefreshListView.getRefreshableView();

        TextView view = new TextView(getActivity().getApplicationContext());
        view.setLines(1);
        actualListView.addFooterView(view, null, true);

        initListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dialogs_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
//                startNewDialogPage();
                break;
        }
        return true;
    }
    */

    private void asyncSearch(final String s) {
        if (fa != null && fa.getCount() > -1) {
            tTimer.cancel();
            tTimer = new Timer();
            tTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //request search
                    if (s.toString().trim().length() > -1) {
                        faSearch = new FriendAdapter(getActivity());
//                        faSearch = searchFriend(user.getUserId(), s.toString().trim(), faSearch);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actualListView.setAdapter(faSearch);
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actualListView.setAdapter(fa);
                            }
                        });
                    }
                }
            }, 300);
        }
    }

    private GroupDialogsAdapter searchFriend(String query) {
        GroupDialogsAdapter qda = null;
        Cursor chatCursor;
        QBDialog dialog = null;
        tempAdapter = null;

        for (int i = 0; i < dialogsAdapter.getCount(); i++) {
            chatCursor = (Cursor) dialogsAdapter.getItem(i);
            int pos = dialog.getName().indexOf(query);
            if (pos != -1) {
                tempAdapter = new GroupDialogsAdapter(baseActivity, chatCursor);
            }
        }
        return qda;
    }

    private void initListeners() {
        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0)
                    position = position - 1;
                Cursor selectedChatCursor = (Cursor) dialogsAdapter.getItem(position);
                QBDialog dialog = DatabaseManager.getDialogFromCursor(selectedChatCursor);
                startGroupChatActivity(dialog);
            }
        });
    }

    private void initChatsDialogs(Cursor dialogsCursor) {
        dialogsAdapter = new GroupDialogsAdapter(baseActivity, dialogsCursor);
        dialogsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkVisibilityEmptyLabel();
            }
        });
        actualListView.setAdapter(dialogsAdapter);
    }

    private void startNewDialogPage() {
//        boolean isFriends = DatabaseManager.getAllFriends(baseActivity).getCount() > ConstsCore.ZERO_INT_VALUE;
        boolean isFriends = true;
        if (isFriends) {
            NewDialogActivity.start(baseActivity);
        } else {
            DialogUtils.showLong(baseActivity, getResources().getString(R.string.ndl_no_friends_for_new_chat));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /* last sent date change */
        ArrayList<String> dialogIds = new ArrayList<String>();
        ArrayList<String> fixedChatroomList = new ArrayList<String>();
        fixedChatroomList.add("Gay Chat Room");
        fixedChatroomList.add("Lesbian Chat Room");
        fixedChatroomList.add("Bisexual Chat Room");
        fixedChatroomList.add("Transexual Chat Room");
        fixedChatroomList.add("LGBT Chat Room");
        fixedChatroomList.add("Religious Chat Room");
        fixedChatroomList.add("Ethnic Chat Room");

        List<QBDialog> dialogs = DatabaseManager.getDialogs(getActivity().getApplicationContext());
        for (int i = 0; i < dialogs.size(); i++) {
            for (int j = 0; j < fixedChatroomList.size(); j++) {
                QBDialog dg = dialogs.get(i);
                if (dg != null && dg.getName() != null && dg.getName().equals(fixedChatroomList.get(j))) {
                    dialogIds.add(dg.getDialogId());
                    dg.setLastMessageDateSent(DateUtilsCore.getCurrentTime());
                    DatabaseManager.saveDialog(getActivity().getApplicationContext(), dg);
                }
            }
        }

        return DatabaseManager.getAllGroupDialogsCursorLoader(baseActivity);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dialogsCursor) {
        System.out.println("dkdk");
        initChatsDialogs(dialogsCursor);
        checkVisibilityEmptyLabel();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void checkVisibilityEmptyLabel() {
        emptyListTextView.setVisibility(dialogsAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void startGroupChatActivity(QBDialog dialog) {
        GroupDialogActivity.start(baseActivity, dialog);
    }

    private void initCursorLoaders() {
        getLoaderManager().initLoader(DIALOGS_LOADER_ID, null, this);
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION,
                new LoadChatsDialogsSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
    }

    private class LoadChatsDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            ArrayList<ParcelableQBDialog> parcelableDialogsList = bundle.getParcelableArrayList(
                    QBServiceConsts.EXTRA_CHATS_DIALOGS);
            if (parcelableDialogsList == null) {
                emptyListTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Create friend browse adapter
     *
     * @author ducpham
     */
    public class FriendAdapter extends ArrayAdapter<Friend> {
        public FriendAdapter(Context context) {
            super(context, 0);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Friend item = getItem(position);
            FriendViewHolder holder = null;

            if (view == null) {
                int layout = R.layout.friends_new_list_row;

                view = LayoutInflater.from(getContext()).inflate(layout, null);
                if (user.getChatKey() != null) {
                    view.findViewById(R.id.user_online).setVisibility(View.VISIBLE);
                }
                String sex = item.getSexuality();
                if (null != sex && !"".equals(sex)) {
                    ((TextView) view.findViewById(R.id.sexualityTv)).setText("(" + sex + ")");
                }
                String age = item.getAge();
                if (null != age && !"".equals(age)) {
                    ((TextView) view.findViewById(R.id.ageTv)).setText("" + age + "");
                }
                //call element from xml
                ImageView icon = (ImageView) view.findViewById(R.id.friend_image_friend);
                TextView title = (TextView) view.findViewById(R.id.title);
                ImageView onlineImg = (ImageView) view.findViewById(R.id.user_online);

                TextView mutualFriends = (TextView) view.findViewById(R.id.mutualFriends);
                int friendsCount = item.getMutualFriends();
                if (friendsCount > 0) {
                    mutualFriends.setVisibility(View.VISIBLE);
                    mutualFriends.setText(friendsCount + " " + getResources().getString(
                            (friendsCount == 1) ? R.string.mutual_friend_text : R.string.mutual_friends_text));
                } else {
                    mutualFriends.setVisibility(View.INVISIBLE);
                }
                ((TextView) view.findViewById(R.id.location)).setText(item.getLocation());


                icon.setOnClickListener(new View.OnClickListener() {
                    Friend friend = (Friend) getItem(position);

                    @Override
                    public void onClick(View v) {
                        //    contentLoading.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(getActivity(), FriendTabsPager.class);
                        intent.putExtra("user_id", friend.getUser_id());
                        startActivity(intent);

                    }
                });


                view.findViewById(R.id.sendMessBtn).setOnClickListener(new View.OnClickListener() {
                    Friend friend = (Friend) getItem(position);

                    @Override
                    public void onClick(View v) {
                        if (friend.getNotice() == null) {
                            Intent intent = null;
                            //init intent
                            if (user.getChatSecretKey() == null) {
                                intent = new Intent(getActivity(), FriendTabsPager.class);
                                intent.putExtra("user_id", friend.getUser_id());
                            } else {
                                intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("fullname", friend.getFullname());
                                intent.putExtra("user_id", friend.getUser_id());
                                intent.putExtra("image", friend.getIcon());
                            }

                            if (intent != null) {
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getActivity().startActivity(intent);
                            }
                        }
                    }
                });

                //notice
                TextView notice = (TextView) view.findViewById(R.id.notice);

                view.setTag(new FriendViewHolder(icon, title, notice, onlineImg));
            }

            if (holder == null && view != null) {
                Object tag = view.getTag();
                if (tag instanceof FriendViewHolder) {
                    holder = (FriendViewHolder) tag;
                }
            }

            if (item != null && holder != null) {
                //if has notice
                if (item.getNotice() != null) {
                    view.findViewById(R.id.friend_image_friend).setVisibility(View.GONE);
                    view.findViewById(R.id.friend_content_view).setVisibility(View.GONE);
                    view.findViewById(R.id.user_online_layout).setVisibility(View.GONE);

                    //enable friend requests view
                    view.findViewById(R.id.friend_notice_layout).setVisibility(View.VISIBLE);
                    holder.notice.setText(item.getNotice());
                    colorView.changeColorText(holder.notice, user.getColor());
                }

                //set image friend;
                if (holder.imageHolder != null) {
                    if (!"".equals(item.getIcon())) {
                        networkUntil.drawImageUrl(holder.imageHolder, item.getIcon(), R.drawable.loading);
                    }
                }
                //set full name;
                if (holder.title != null) {
                    holder.title.setText(item.getFullname());
                    colorView.changeColorText(holder.title, user.getColor());
                }

                //set online
                if (holder.onlineImg != null) {
                    if (item.isOnline() == true)
                        holder.onlineImg.setImageResource(R.drawable.user_online);
                    else
                        holder.onlineImg.setImageResource(R.drawable.user_offline);
                }
            }

            return view;
        }
    }

    /**
     * Class friend view holder
     *
     * @author ducpham
     */
    public class FriendViewHolder {
        public final ImageView imageHolder;
        public final TextView title;
        public final TextView notice;
        public final ImageView onlineImg;

        public FriendViewHolder(ImageView icon, TextView title,
                                TextView notice, ImageView onlineImage) {
            this.imageHolder = icon;
            this.title = title;
            this.notice = notice;
            this.onlineImg = onlineImage;
        }
    }

}

