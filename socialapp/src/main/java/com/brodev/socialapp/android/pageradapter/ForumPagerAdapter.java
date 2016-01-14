package com.brodev.socialapp.android.pageradapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.fragment.ForumFragment;
import com.brodev.socialapp.fragment.ThreadFragment;


public class ForumPagerAdapter extends FragmentStatePagerAdapter {
	
	private PhraseManager phraseManager;
	private final int PAGES = 3;
	private String[] titles = new String[PAGES];
	
	public ForumPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		phraseManager = new PhraseManager(context);
		titles[0] = phraseManager.getPhrase(context, "forum.forums");
		titles[1] = phraseManager.getPhrase(context, "forum.my_threads");
		titles[2] = phraseManager.getPhrase(context, "forum.new_posts");
	}
	
	@Override
	public Fragment getItem(int position) {
		switch (position) {
        case 0:
            return new ForumFragment();
        case 1:
           return new ThreadFragment("my-thread");
        case 2:
            return new ThreadFragment("new");
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
