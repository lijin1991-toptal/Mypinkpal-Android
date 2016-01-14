package com.brodev.socialapp.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.pageradapter.ForumPagerAdapter;
import com.brodev.socialapp.entity.User;

public class ForumPagerFragment extends SherlockFragment {
	private PagerSlidingTabStrip tabs;
	private ColorView colorView;
	private User user;
	
	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return super.getView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		user = (User) getActivity().getApplicationContext();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.content_view_pager, container, false);

		// Set the pager with an adapter
		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		pager.setAdapter(new ForumPagerAdapter(getFragmentManager(), getActivity().getApplicationContext()));

		// Bind the widget to the adapter
		tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		tabs.setViewPager(pager);
		
		colorView = new ColorView(tabs, getActivity().getApplicationContext());
		colorView.changeColorTabs(user.getColor());
		
		return view;
	}
}
