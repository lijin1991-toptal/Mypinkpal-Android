package com.brodev.socialapp.android;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.brodev.socialapp.entity.User;
import com.mypinkpal.app.R;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by Bebel on 1/14/15.
 */
public class ChatroomsAdapter extends BaseAdapter {
    private List<QBDialog> dataSource;
    private LayoutInflater inflater;
    private Activity ctx;

    public ChatroomsAdapter(List<QBDialog> dataSource, Activity ctx) {
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(ctx);
        this.ctx = ctx;
    }

    public List<QBDialog> getDataSource() {
        return dataSource;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // init view
        //
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_room, null);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.roomName);
            holder.lastMessage = (TextView)convertView.findViewById(R.id.lastMessage);
            holder.groupType = (TextView)convertView.findViewById(R.id.textViewGroupType);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set data
        //
        QBDialog dialog = dataSource.get(position);
        if(dialog.getType().equals(QBDialogType.GROUP) || dialog.getType().equals(QBDialogType.PUBLIC_GROUP)){
            holder.name.setText(dialog.getName());
        }

        holder.lastMessage.setText(dialog.getLastMessage());
        holder.groupType.setText(dialog.getType().toString());

        return convertView;
    }

    private static class ViewHolder{
        TextView name;
        TextView lastMessage;
        TextView groupType;
    }
}
