package com.brodev.socialapp.view.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.view.BaseActivity;

public abstract class BaseFragment extends Fragment {

    protected static final String ARG_TITLE = "title";

    protected User user;
    protected BaseActivity baseActivity;
    protected BaseActivity.FailAction failAction;
    protected String title;

    public String getTitle() {
        return title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = (BaseActivity) getActivity();
        failAction = baseActivity.getFailAction();
//        user = User.getInstance();
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

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    protected boolean isExistActivity() {
        return ((!isDetached()) && (getBaseActivity() != null));
    }
}