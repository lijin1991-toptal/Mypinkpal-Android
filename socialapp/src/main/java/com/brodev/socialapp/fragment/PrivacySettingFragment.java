package com.brodev.socialapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import com.brodev.socialapp.view.EditProfileBasic;
import com.brodev.socialapp.view.PrivacySettingsBlockedUsers;
import com.brodev.socialapp.view.PrivacySettingsInvisibleMode;
import com.brodev.socialapp.view.PrivacySettingsItems;
import com.brodev.socialapp.view.PrivacySettingsNotifications;
import com.brodev.socialapp.view.PrivacySettingsProfile;
import com.mypinkpal.app.R;

import org.w3c.dom.Text;

/**
 * Created by Bebel on 3/11/15.
 */
public class PrivacySettingFragment extends SherlockFragment {

    private View view;

    private TextView profileTxt;
    private TextView itemsTxt;
    private TextView notificationsTxt;
    private TextView blockedUsersTxt;
    private TextView invisibleModeTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
                                    com.actionbarsherlock.view.MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.privacy_setting, container, false);

        profileTxt = (TextView) view.findViewById(R.id.privacyprofileTxt);
        itemsTxt = (TextView) view.findViewById(R.id.privacyitemsTxt);
        notificationsTxt = (TextView) view.findViewById(R.id.privacynotificationsTxt);
        blockedUsersTxt = (TextView) view.findViewById(R.id.privacyblockTxt);
        invisibleModeTxt = (TextView) view.findViewById(R.id.privacyinvisibleTxt);

        profileTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivacySettingsProfile.class);
                startActivity(intent);
            }
        });

        itemsTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivacySettingsItems.class);
                startActivity(intent);
            }
        });

        notificationsTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivacySettingsNotifications.class);
                startActivity(intent);
            }
        });

        blockedUsersTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivacySettingsBlockedUsers.class);
                startActivity(intent);
            }
        });

        invisibleModeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivacySettingsInvisibleMode.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
