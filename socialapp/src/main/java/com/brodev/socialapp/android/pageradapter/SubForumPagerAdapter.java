package com.brodev.socialapp.android.pageradapter;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.fragment.ForumFragment;
import com.brodev.socialapp.fragment.ThreadFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class SubForumPagerAdapter extends FragmentStatePagerAdapter 
{
	private PhraseManager phraseManager;
	private final int PAGES = 2;
	private String[] titles = new String[PAGES];
	private int forumId;
	
	public SubForumPagerAdapter(FragmentManager fm, Context context, int forumId) {
		super(fm);
		phraseManager = new PhraseManager(context);
		
		titles[0] = phraseManager.getPhrase(context, "forum.threads");
		titles[1] = phraseManager.getPhrase(context, "forum.sub_forum");
		this.forumId = forumId;
	}
	
	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
	        return ThreadFragment.newInstance(forumId);
        case 1:
            return ForumFragment.newInstance(forumId);
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
