package com.brodev.socialapp.view;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.fragment.ConfirmFriendFragment;
import com.mypinkpal.app.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class ConfirmRequestActivity extends SherlockFragmentActivity {

	int mStackLevel = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_request);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Fragment fragment = ConfirmFriendFragment.newInstance(mStackLevel);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.confirm_request_layout, fragment).commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
	
			default:
		}
		return super.onOptionsItemSelected(item);
	}

}
