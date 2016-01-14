package com.brodev.socialapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.utils.TextViewHelper;
import com.brodev.socialapp.view.ChangePassword;
import com.brodev.socialapp.view.EditProfileAbout;
import com.brodev.socialapp.view.EditProfileBasic;
import com.brodev.socialapp.view.EditProfileDetails;
import com.brodev.socialapp.view.EditProfileInterests;
import com.mypinkpal.app.R;

import org.w3c.dom.Text;

/**
 * Created by Bebel on 3/11/15.
 */
public class EditProfileFragment extends SherlockFragment {

    private View view;

    private TextView basicTextView;
    private TextView aboutTextView;
    private TextView interestsTextView;
    private TextView detailsTextView;


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

        view = inflater.inflate(R.layout.edit_profile, container, false);

        basicTextView = (TextView) view.findViewById(R.id.basicinformationTxt);
        aboutTextView = (TextView) view.findViewById(R.id.aboutmeTxt);
        interestsTextView = (TextView) view.findViewById(R.id.interestsTxt);
        detailsTextView = (TextView) view.findViewById(R.id.detailsTxt);

        basicTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), EditProfileBasic.class);
                startActivity(intent);
            }
        });

        aboutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileAbout.class);
                startActivity(intent);
            }
        });

        interestsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileInterests.class);
                startActivity(intent);
            }
        });

        detailsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileDetails.class);
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
