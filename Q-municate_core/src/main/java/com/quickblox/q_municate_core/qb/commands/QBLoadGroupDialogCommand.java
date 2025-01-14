package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.models.GroupDialog;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.FriendUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class QBLoadGroupDialogCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBLoadGroupDialogCommand(Context context, QBMultiChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = chatHelper;
    }

    public static void start(Context context, QBDialog dialog, String roomJid) {
        Intent intent = new Intent(QBServiceConsts.LOAD_GROUP_DIALOG_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, roomJid);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        String roomJid = extras.getString(QBServiceConsts.EXTRA_ROOM_JID);

        GroupDialog groupDialog = new GroupDialog(dialog);

        List<Integer> participantIdsList = dialog.getOccupants();
        List<Integer> onlineParticipantIdsList = multiChatHelper.getRoomOnlineParticipantList(roomJid);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(ConstsCore.FL_FRIENDS_PAGE_NUM);
        requestBuilder.setPerPage(ConstsCore.FL_FRIENDS_PER_PAGE);

        Bundle requestParams = new Bundle();
        List<QBUser> userList = QBUsers.getUsersByIDs(participantIdsList, requestBuilder, requestParams);
        Map<Integer, User> friendMap = FriendUtils.createUserMap(userList);
        for (Integer onlineParticipantId : onlineParticipantIdsList) {
            friendMap.get(onlineParticipantId).setOnline(true);
        }

        ArrayList<User> friendList = new ArrayList<User>(friendMap.values());
        Collections.sort(friendList, new UserComparator());
        groupDialog.setOccupantList(friendList);

        Bundle params = new Bundle();
        params.putSerializable(QBServiceConsts.EXTRA_GROUP_DIALOG, groupDialog);
        return params;
    }

    private class UserComparator implements Comparator<User> {

        @Override
        public int compare(User firstUser, User secondUser) {
            if (firstUser.getFullName() == null || secondUser.getFullName() == null) {
                return ConstsCore.ZERO_INT_VALUE;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(firstUser.getFullName(), secondUser.getFullName());
        }
    }
}