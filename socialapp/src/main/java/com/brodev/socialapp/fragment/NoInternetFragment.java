package com.brodev.socialapp.fragment;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.entity.User;
import com.mypinkpal.app.R;

public class NoInternetFragment extends SherlockFragment {

    private PhraseManager phraseManager;
    private User user;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    /**
     * Create a new instance of CountingFragment, providing "num"
     * as an argument.
     */
    public static NoInternetFragment newInstance(int num) {
        NoInternetFragment f = new NoInternetFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", 1);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = (User) getActivity().getApplicationContext();
        colorView = new ColorView(getActivity().getApplicationContext());
        phraseManager = new PhraseManager(getActivity().getApplicationContext());

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create view from forum_fragment xml
        View view = inflater.inflate(R.layout.no_internet_fragment, container, false);

        //no internet connection
        noInternetLayout = (RelativeLayout) view.findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) view.findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) view.findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) view.findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) view.findViewById(R.id.no_internet_image);

        //change color for no internet
        colorView.changeImageForNoInternet(noInternetImg, noInternetBtn, user.getColor());

        //set text for no internet element
        noInternetBtn.setVisibility(View.GONE);
        noInternetTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_title"));
        noInternetContent.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_content"));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

}
