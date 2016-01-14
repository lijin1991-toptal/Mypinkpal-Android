/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brodev.socialapp.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.AlbumsFragment;
import com.brodev.socialapp.fragment.CommentDetailFragment;
import com.brodev.socialapp.fragment.InfoFragment;
import com.brodev.socialapp.fragment.UserFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;

/**
 * Demonstrates combining a TabHost with a ViewPager to implement a tab UI that
 * switches between tabs and also allows the user to perform horizontal flicks
 * to move between the tabs.
 */
public class FriendTabsPager extends SherlockFragmentActivity {
	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private PhraseManager phraseManager;
	private TextView tv;
	private User user;
	private static String colorCode;
	private ColorView colorView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setTheme(SampleList.THEME); //Used for theme switching in samples
		super.onCreate(savedInstanceState);
		phraseManager = new PhraseManager(getApplicationContext());
		user = (User) getApplication().getApplicationContext();
		colorView = new ColorView(getApplicationContext());
		
		colorCode = colorView.getColorCode(getApplicationContext(), user);
		
		setContentView(R.layout.fragment_tabs_pager);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(3);
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle extras = getIntent().getExtras();

		mTabsAdapter.addTab(mTabHost.newTabSpec("wall").setIndicator(phraseManager.getPhrase(getApplicationContext(), "profile.wall")),
				UserFragment.class, extras);
		
		mTabsAdapter.addTab(mTabHost.newTabSpec("info").setIndicator(phraseManager.getPhrase(getApplicationContext(), "profile.info")),
				InfoFragment.class, extras);
		
		mTabsAdapter.addTab(mTabHost.newTabSpec("photo").setIndicator(phraseManager.getPhrase(getApplicationContext(), "profile.photos")),
				AlbumsFragment.class, extras);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
		
		//set color for tabhost
		for (int i = 0; i< mTabHost.getTabWidget().getChildCount(); i++) {
			mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#272727"));
			tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.parseColor("#ffffffff"));
		}
		mTabHost.getTabWidget().setCurrentTab(0);
		mTabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.parseColor(colorCode));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}
	
	@Override
    	public boolean onOptionsItemSelected(MenuItem item) {
    		switch (item.getItemId()) {
    		case android.R.id.home:
    			finish();
    			return true;
    		}
    		return super.onOptionsItemSelected(item);
    	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter implements
			TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			Log.i("Fragment name", info.clss.getName());

			return Fragment.instantiate(mContext, info.clss.getName(), info.args);
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();
			for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
				mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#272727"));
			}

			mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).setBackgroundColor(Color.parseColor(colorCode));
			mViewPager.setCurrentItem(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			// Unfortunately when TabHost changes the current tab, it kindly
			// also takes care of putting focus on it when not in touch mode.
			// The jerk.
			// This hack tries to prevent this from pulling focus out of our
			// ViewPager.
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

	}
	
	public void onWebHtml(String pos) {
		Intent intent = WebviewActivity.newInstance(this, pos);
		startActivity(intent);
	}

	public void doShowCommentDetail(int position, String type_id, String item_id, String module_id) {

		CommentDetailFragment.newInstance(position, type_id, item_id, module_id, "").show(this);
	}
}
