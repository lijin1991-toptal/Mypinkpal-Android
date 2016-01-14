package com.brodev.socialapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.pageradapter.EventPagerAdapter;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.view.CreateNewEventActivity;

public class EventFragment extends SherlockFragment {
	
	private PagerSlidingTabStrip tabs;
	private ColorView colorView;
	private User user;
	private String categoryId, categoryName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setHasOptionsMenu(true); 
		user = (User) getActivity().getApplicationContext();
		
		categoryId = null;
		categoryName = null;
		
		if (getArguments() != null) {
			categoryId = getArguments().getString("categoryId");
			categoryName = getArguments().getString("name");
		}

	}
	
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		//disable search menu item
		MenuItem search = menu.findItem(R.id.actionBar_chat);
		search.setVisible(false);
		
		inflater.inflate(R.menu.message, menu);
		 
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_compose:
			Intent intent = new Intent(getActivity(), CreateNewEventActivity.class);
			getActivity().startActivity(intent);
			break;
		}
		return false;
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.view_pager, container, false);
		// Set the pager with an adapter
		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		pager.setAdapter(new EventPagerAdapter(getFragmentManager(), getActivity().getApplicationContext(), categoryId, categoryName));

		// Bind the widget to the adapter
		tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		tabs.setViewPager(pager);
		
		colorView = new ColorView(tabs, getActivity().getApplicationContext());
		colorView.changeColorTabs(user.getColor());
		
		return view;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
}