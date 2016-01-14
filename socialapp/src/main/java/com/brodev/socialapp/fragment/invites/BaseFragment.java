package com.brodev.socialapp.fragment.invites;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class BaseFragment extends SherlockFragment {

    protected static final String ARG_TITLE = "title";

     protected Activity baseActivity;
    protected String title;

    public String getTitle() {
        return title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = (Activity) getActivity();
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getActionBar().setTitle(title);
    }

    public Activity getBaseActivity() {
        return (Activity) getActivity();
    }

    protected boolean isExistActivity() {
        return ((!isDetached()) && (getBaseActivity() != null));
    }
}