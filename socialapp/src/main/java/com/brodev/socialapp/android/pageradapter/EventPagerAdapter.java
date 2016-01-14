package com.brodev.socialapp.android.pageradapter;


import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.fragment.EventListFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


public class EventPagerAdapter extends FragmentStatePagerAdapter {

	private final int PAGES = 3;
	private String[] titles = new String[PAGES];
	private PhraseManager phraseManager;
	private String categoryId, categoryName;
	
	public EventPagerAdapter(FragmentManager fm, Context context, String categoryId, String categoryName) {
		super(fm);
		phraseManager = new PhraseManager(context);
		
		titles[0] = phraseManager.getPhrase(context, "event.all_events");
		titles[1] = phraseManager.getPhrase(context, "event.my_events");
		titles[2] = phraseManager.getPhrase(context, "event.friends_events");
		
		this.categoryId = categoryId;
		this.categoryName = categoryName;
	}

	@Override
	public Fragment getItem(int position) {
		
		switch (position) {
			case 0:
				return new EventListFragment(null, categoryId, categoryName);
			case 1:
				return new EventListFragment("my", categoryId, categoryName);
			case 2:
				return new EventListFragment("friend", categoryId, categoryName);
			default:
				throw new IllegalArgumentException("The item position should be less or equal to:" + PAGES);
		}
		
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public int getCount() {
		return PAGES;
	}
}