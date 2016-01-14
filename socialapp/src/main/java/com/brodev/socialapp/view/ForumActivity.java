package com.brodev.socialapp.view;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.pageradapter.SubForumPagerAdapter;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.ForumFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;

public class ForumActivity extends SherlockFragmentActivity {

	private int forumId;
	private String title, isCategory;
	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private SubForumPagerAdapter adapter;
	private User user;
	private ColorView colorView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_view_pager);

		user = (User) getApplicationContext();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		title = null;
		isCategory = null;
				
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) 
		{
			if (getIntent().hasExtra("forum_id")) 
				forumId = bundle.getInt("forum_id");
			
			if (getIntent().hasExtra("category")) 
				// change title
				title = bundle.getString("category");
			
			if (getIntent().hasExtra("sub_forum")) 
				// change title
				isCategory = bundle.getString("sub_forum");
		}
		
		if (title != null) {
			getSupportActionBar().setTitle(title);
		}
		
		if (isCategory.equals("0")) {
			findViewById(R.id.no_sub_forum).setVisibility(View.GONE);
			findViewById(R.id.tabs).setVisibility(View.VISIBLE);
			findViewById(R.id.pager).setVisibility(View.VISIBLE);
			
			tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
			pager = (ViewPager) findViewById(R.id.pager);
			adapter = new SubForumPagerAdapter(getSupportFragmentManager(), getApplicationContext(), forumId);

			pager.setAdapter(adapter);

			final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
			pager.setPageMargin(pageMargin);

			tabs.setViewPager(pager);
			
			colorView = new ColorView(tabs, getApplicationContext());
			colorView.changeColorTabs(user.getColor());
			
		} else {

			findViewById(R.id.no_sub_forum).setVisibility(View.VISIBLE);
			findViewById(R.id.tabs).setVisibility(View.GONE);
			findViewById(R.id.pager).setVisibility(View.GONE);
			
			Fragment tFragment = ForumFragment.newInstance(forumId);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.no_sub_forum, tFragment).commit();

		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.forum, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_add_thread:
				Intent intent = new Intent(this, AddThreadActivity.class);
				intent.putExtra("forum_id", forumId);
				startActivity(intent);
				return true;
			default:
		}
		return super.onOptionsItemSelected(item);
	}

}
