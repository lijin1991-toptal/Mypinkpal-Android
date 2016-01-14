package com.brodev.socialapp.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.brodev.socialapp.view.FriendTabsPager;

/**
 * Created by psyh on 7/29/14.
 */
public class BROADCAST extends BroadcastReceiver {

    public static SideBarFragment sideBarFragment;
    public static ListView listview;
    public static View view;
    public static int position;
    public static long id;

    public static void saveAfterFirstClick(ListView listview_, View view_, int position_, long id_) {
        listview = listview_;
        view = view_;
        position = position_;
        id = id_;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        sideBarFragment.onListItemClick(listview, view, position, id);

        /*

        Bundle extras = intent.getExtras();

        Intent newIntent = new Intent(context, FriendTabsPager.class);

        if (extras != null) {
            if (intent.hasExtra("user_id")) {
                newIntent.putExtra("user_id", extras.getString("user_id"));
            } else if (intent.hasExtra("page_id")) {
                newIntent.putExtra("page_id", extras.getString("page_id"));
            }
            if (intent.hasExtra("uri_image")) {
                newIntent.putExtra("uri_image", extras.getString("uri_image"));
            }
            if (intent.hasExtra("take_photo")) {
                newIntent.putExtra("take_photo", extras.getString("take_photo"));
            }

            context.startActivity(newIntent);
        }
*/
    }
}
