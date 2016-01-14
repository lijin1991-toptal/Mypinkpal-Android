package com.brodev.socialapp.fragment.membership;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.mypinkpal.app.R;

/**
 * Created by psyh on 8/7/14.
 */
public class BalanceFragment extends SherlockFragment{

    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.balance_fragmt, null);

        return root;
    }

}
