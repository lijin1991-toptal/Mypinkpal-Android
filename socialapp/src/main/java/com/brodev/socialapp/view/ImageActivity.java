/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.brodev.socialapp.view;

import java.io.File;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImageActivity extends SherlockFragmentActivity {

	private static final String STATE_POSITION = "STATE_POSITION";

	ViewPager pager;
	
	RelativeLayout likeArena;
	
	NetworkUntil networkUntil = new NetworkUntil();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ColorDrawable color = new ColorDrawable(Color.BLACK);
		color.setAlpha(128);
		getSupportActionBar().setBackgroundDrawable(color);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.market_place_image_view);
		
		File cacheDir = new File(this.getCacheDir(), "imgcachedir");
	    if (!cacheDir.exists())
	        cacheDir.mkdir();
	    
		Bundle bundle = getIntent().getExtras();
		
		String image = bundle.getString("image");
		String title = bundle.getString("title");
		getSupportActionBar().setTitle(title);
		
		ImageView imageView = (ImageView) findViewById(R.id.audiochatImg);
		
		networkUntil.drawImageUrl(imageView, image, R.drawable.loading);
		
		this.getWindow().setBackgroundDrawableResource(android.R.color.black);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	public void onResume() {
		super.onResume();
		getSupportActionBar().show();
		
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

}