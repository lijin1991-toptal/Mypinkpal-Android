package com.brodev.socialapp.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.brodev.socialapp.android.AlbumStorageDirFactory;
import com.brodev.socialapp.android.BaseAlbumDirFactory;
import com.brodev.socialapp.android.FroyoAlbumDirFactory;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.entity.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;

public class AlbumSelectedActivity extends BaseImageActivity {
	
	private ArrayList<String> imageUrls;
	private DisplayImageOptions options;
	private ImagesAdapter imageAdapter;
	private ImageButton capture, doneBtn;
	private String mCurrentPhotoPath;
	User user;
	private static final int CAMERA_REQUEST = 1;
	private static  String page_id = null;
	private static  String user_id = null;
	private static  String event_id = null;
	String privacy_value, owner_user_id, page_title, fullname, profile_page_id, sImagePages, sAlbumName, iAlbumId;
	
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	
	ProfilePicUtil profilePicUtil = new ProfilePicUtil();
	private PhraseManager phraseManager;
	
	private File setUpPhotoFile() throws IOException {
		File f = profilePicUtil.createImageFile(getApplicationContext(), mAlbumStorageDirFactory);
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}

	private void handleBigCameraPhoto() {
		if (mCurrentPhotoPath != null) {
			profilePicUtil.galleryAddPic(getApplicationContext(), mCurrentPhotoPath);
		}
	}
	
	/**
	 * Change Color
	 * @param colorCode
	 */
	private void changeColor(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			doneBtn.setImageResource(R.drawable.brown_done_btn);
			capture.setImageResource(R.drawable.brown_photo_capture);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			doneBtn.setImageResource(R.drawable.pink_done_btn);
			capture.setImageResource(R.drawable.pink_photo_capture);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			doneBtn.setImageResource(R.drawable.green_done_btn);
			capture.setImageResource(R.drawable.green_photo_capture);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			doneBtn.setImageResource(R.drawable.violet_done_btn);
			capture.setImageResource(R.drawable.violet_photo_capture);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			doneBtn.setImageResource(R.drawable.red_done_btn);
			capture.setImageResource(R.drawable.red_photo_capture);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			doneBtn.setImageResource(R.drawable.dark_violet_done_btn);
			capture.setImageResource(R.drawable.dark_violet_photo_capture);
		} else {
			doneBtn.setImageResource(R.drawable.done_btn);
			capture.setImageResource(R.drawable.photo_capture);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_selected);
		
		//widget
		capture = (ImageButton) findViewById(R.id.capture_photo);
		doneBtn = (ImageButton) findViewById(R.id.doneBtn);
		user =  (User) getApplicationContext();
		phraseManager = new PhraseManager(getApplicationContext());
		//init value
		user_id = null;
		page_id = null;
		owner_user_id = null;
		page_title = null;
		fullname = null;
		profile_page_id = null;
		sImagePages = null;
		
		changeColor(user.getColor());
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (getIntent().hasExtra("user_id")) {
				user_id = extras.getString("user_id");
			} else if (getIntent().hasExtra("page_id")) {
				page_id = extras.getString("page_id");
				owner_user_id = extras.getString("owner_user_id");
				page_title = extras.getString("page_title");
				fullname = extras.getString("fullname");
				profile_page_id = extras.getString("profile_page_id");
				sImagePages = extras.getString("pages_image");
			} else if (getIntent().hasExtra("event_id")) {
				event_id = extras.getString("event_id");				
			}				
		}
		
		final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
		final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
		@SuppressWarnings("deprecation")
		Cursor imagecursor = managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
				null, orderBy + " DESC");
		
		this.imageUrls = new ArrayList<String>();
		
		for (int i = 0; i < imagecursor.getCount(); i++) {
			imagecursor.moveToPosition(i);
			int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
			imageUrls.add(imagecursor.getString(dataColumnIndex));
			
			System.out.println("=====> Array path => "+imageUrls.get(i));
		}
		
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.loading)
				.showImageForEmptyUri(R.drawable.loading)
				.resetViewBeforeLoading(false).considerExifParams(true)
				.cacheInMemory(false).cacheOnDisc(false).build();

		imageAdapter = new ImagesAdapter(this, imageUrls);
		
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(imageAdapter);
		
		//action click image grid view 
		if (event_id != null || page_id != null || user_id != null) {
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ArrayList<String> selectedPhoto = new ArrayList<String>();
					selectedPhoto.add(imageUrls.get(position));
					if (selectedPhoto.size() > 0) {
						Intent intent = new Intent(AlbumSelectedActivity.this, ImageUpload.class);
						intent.putStringArrayListExtra("selected_photo", selectedPhoto);
						
						if(user_id != null || page_id != null) {
							if(page_id != null) {
								intent.putExtra("page_id", page_id);
								intent.putExtra("owner_user_id", owner_user_id);
								intent.putExtra("page_title",page_title);
								intent.putExtra("fullname", fullname);
								intent.putExtra("profile_page_id", profile_page_id);
								intent.putExtra("pages_image", sImagePages);
							} else if (user_id != null){
								intent.putExtra("user_id", user_id);
							} else if (event_id != null) {
								intent.putExtra("event_id", event_id);
							}
						} else {
							intent.putExtra("owner_user_id", owner_user_id);
						}
						
						startActivity(intent);
						finish();
					}
				}
			});
		}
		
		//action click capture new photo
		capture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 try {
					Toast.makeText(getApplicationContext(), "Please wait ...", Toast.LENGTH_LONG).show();
						
					Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					File f = null;
					try {
						f = setUpPhotoFile();
						if (!f.exists()) {
							Toast.makeText(getApplicationContext(), "Error while capturing image", Toast.LENGTH_LONG).show();
							return;
						}
						mCurrentPhotoPath = f.getAbsolutePath();
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					} catch (IOException e) {
						e.printStackTrace();
						f = null;
						mCurrentPhotoPath = null;
					}
					
					startActivityForResult(cameraIntent, CAMERA_REQUEST);
				 } catch(Exception e) {
					 Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
					 Log.e(e.getClass().getName(), e.getMessage(), e);
				 }
			}
		});
		
		//action done after select multiple photo
		doneBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//get selected photo
				ArrayList<String> selectedPhoto = imageAdapter.getCheckedItems();
				//intent
				if (selectedPhoto.size() == 0) {
					Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.you_haven_t_choosen_any_photo_yet"), Toast.LENGTH_LONG).show();
				} else if (selectedPhoto.size() > 0 && selectedPhoto.size() <= 4) {
					Intent intent = new Intent(AlbumSelectedActivity.this, ImageUpload.class);
					intent.putStringArrayListExtra("selected_photo", selectedPhoto);
					
					if(user_id != null || page_id != null || event_id != null){
						if(page_id != null) {
							intent.putExtra("page_id", page_id);
							intent.putExtra("owner_user_id", owner_user_id);
							intent.putExtra("page_title",page_title);
							intent.putExtra("fullname", fullname);
							intent.putExtra("profile_page_id", profile_page_id);
							intent.putExtra("pages_image", sImagePages);
						} else if (user_id != null){
							intent.putExtra("user_id", user_id);
						} else if (event_id != null) {
							intent.putExtra("event_id", event_id);
						}
					} else {
						intent.putExtra("owner_user_id", owner_user_id);
					}
					
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.select_10_files"), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(AlbumSelectedActivity.this, ImageUpload.class);
		
		if(user_id != null || page_id != null || event_id != null){
			if(page_id != null) {
				intent.putExtra("page_id", page_id);
				intent.putExtra("owner_user_id", owner_user_id);
				intent.putExtra("page_title",page_title);
				intent.putExtra("fullname", fullname);
				intent.putExtra("profile_page_id", profile_page_id);
				intent.putExtra("pages_image", sImagePages);
			} else if (user_id != null){
				intent.putExtra("user_id", user_id);
			} else if (event_id != null) {
				intent.putExtra("event_id", event_id);
			}
		} else {
			intent.putExtra("owner_user_id", owner_user_id);
		}
		
		startActivity(intent);
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CAMERA_REQUEST:
			if (resultCode == RESULT_OK) {
				try {
					handleBigCameraPhoto();
					if (mCurrentPhotoPath != null) {
						imageUrls.add(0, mCurrentPhotoPath);
						imageAdapter.updateAdapter(imageUrls);
						imageAdapter.notifyDataSetChanged();
					}
					mCurrentPhotoPath = null;
				} catch (Exception e) {
					mCurrentPhotoPath = null;
				}
			}
			break;
		default:
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onStop() {
		imageLoader.stop();
		super.onStop();
	}
	
	public class ImagesAdapter extends BaseAdapter {
		
		ArrayList<String> mList = new ArrayList<String>();
		LayoutInflater mInflater;
		Context mContext;
		SparseBooleanArray mSparseBooleanArray;
		
		public ImagesAdapter(Context context, ArrayList<String> imageList) {
			mContext = context;
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
			mSparseBooleanArray = new SparseBooleanArray();
			this.mList = imageList;
		}
		
		public void updateAdapter(ArrayList<String> results) {
			mList = results;
	    }
		
		public ArrayList<String> getCheckedItems() {
			ArrayList<String> mTempArry = new ArrayList<String>();

			for (int i = 0; i < mList.size(); i++) {
				if (mSparseBooleanArray.get(i)) {
					mTempArry.add(mList.get(i));
				}
			}

			return mTempArry;
		}
		
		@Override
		public int getCount() {
			return imageUrls.size();
		}

		@Override
		public Object getItem(int position) {
			return this.mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			  
	        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
	            holder = new ViewHolder();
	            holder.imageHolder = (ImageView) convertView.findViewById(R.id.imagePhoto);
	            holder.checkboxHolder = (CheckBox) convertView.findViewById(R.id.selected_photo);
	            convertView.setTag(holder);
	        }
	        else {
	            holder = (ViewHolder) convertView.getTag();
	        }
			
			imageLoader.displayImage("file://"+this.mList.get(position), holder.imageHolder, options);
			
			if (event_id != null || page_id != null || user_id != null) {
				holder.checkboxHolder.setVisibility(View.GONE);
			}
			changeColor(holder.checkboxHolder, user.getColor());
			holder.checkboxHolder.setTag(position);
			holder.checkboxHolder.setChecked(mSparseBooleanArray.get(position));
			holder.checkboxHolder.setOnCheckedChangeListener(mCheckedChangeListener);
			
			return convertView;
		}
		
		OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
			}
		};
		
		/**
		 * Change color when click checkbox
		 * @param checkbox
		 * @param colorCode
		 */
		private void changeColor(CheckBox checkbox, String colorCode) {
			if ("Brown".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.brown_checkbox_selector);
			} else if ("Pink".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.pink_checkbox_selector);
			} else if ("Green".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.green_checkbox_selector);
			} else if ("Violet".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.violet_checkbox_selector);
			} else if ("Red".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.red_checkbox_selector);
			} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
				checkbox.setButtonDrawable(R.drawable.dark_violet_checkbox_selector);
			} else {
				checkbox.setButtonDrawable(R.drawable.checkbox_selector);
			}
		}
	}
	
	/**
	 * Class view holder
	 */
	public class ViewHolder {
		ImageView imageHolder;
		CheckBox checkboxHolder;
	}
	
}
