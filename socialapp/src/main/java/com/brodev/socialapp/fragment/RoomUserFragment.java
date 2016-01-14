package com.brodev.socialapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.brodev.socialapp.android.UsersAdapter;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.view.ChatActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mypinkpal.app.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bebel on 1/15/15.
 */
public class RoomUserFragment extends Fragment implements QBEntityCallback<ArrayList<QBUser>> {
    private static final int PAGE_SIZE = 10;
    private PullToRefreshListView usersList;
    private Button createChatButton;
    private int listViewIndex;
    private int listViewTop;
    private ProgressBar progressBar;
    private UsersAdapter usersAdapter;

    private int currentPage = 0;
    private List<QBUser> users = new ArrayList<QBUser>();

    public static RoomUserFragment getInstance() {
        return new RoomUserFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_roomuser, container, false);
//        usersList = (PullToRefreshListView) v.findViewById(R.id.usersList);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        createChatButton = (Button) v.findViewById(R.id.createChatButton);
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((User)getActivity().getApplication()).addDialogsUsers(usersAdapter.getSelected());

                // Create new group dialog
                //
                QBDialog dialogToCreate = new QBDialog();
                dialogToCreate.setName(usersListToChatName());
                if(usersAdapter.getSelected().size() == 1){
                    dialogToCreate.setType(QBDialogType.PRIVATE);
                }else {
                    dialogToCreate.setType(QBDialogType.GROUP);
                }
                dialogToCreate.setOccupantsIds(getUserIds(usersAdapter.getSelected()));

                /*
                QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallbackImpl<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        if(usersAdapter.getSelected().size() == 1){
                            startSingleChat(dialog);
                        } else {
                            startGroupChat(dialog);
                        }
                    }

                    @Override
                    public void onError(List<String> errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setMessage("dialog creation errors: " + errors).create().show();
                    }
                }); */
            }
        });

//        usersList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
//            @Override
//            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//                // Do work to refresh the list here.
//                loadNextPage();
//                listViewIndex = usersList.getRefreshableView().getFirstVisiblePosition();
//                View v = usersList.getRefreshableView().getChildAt(0);
//                listViewTop = (v == null) ? 0 : v.getTop();
//            }
//        });
        loadNextPage();
        return v;
    }


    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }


    @Override
    public void onSuccess(ArrayList<QBUser> newUsers, Bundle bundle){

        // save users
        //
        users.addAll(newUsers);

        // Prepare users list for simple adapter.
        //
        usersAdapter = new UsersAdapter(users, getActivity());
//        usersList.setAdapter(usersAdapter);
//        usersList.onRefreshComplete();
//        usersList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess(){

    }

    @Override
    public void onError(List<String> errors){
        AlertDialog.Builder dialog = new AlertDialog.Builder(RoomUserFragment.getInstance().getActivity());
        dialog.setMessage("get users errors: " + errors).create().show();
    }


    private String usersListToChatName(){
        String chatName = "";
        for(QBUser user : usersAdapter.getSelected()){
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getLogin();
        }
        return chatName;
    }

    public void startSingleChat(QBDialog dialog) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.PRIVATE);
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);

        ChatActivity.start(getActivity(), bundle);
    }

    private void startGroupChat(QBDialog dialog){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);
        bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.GROUP);

        ChatActivity.start(getActivity(), bundle);
    }

    private void loadNextPage() {
        ++currentPage;

        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), RoomUserFragment.this);
    }

    public static ArrayList<Integer> getUserIds(List<QBUser> users){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(QBUser user : users){
            ids.add(user.getId());
        }
        return ids;
    }
}
