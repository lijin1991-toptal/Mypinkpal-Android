package com.brodev.socialapp.view;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.android.AlbumStorageDirFactory;
import com.brodev.socialapp.android.BaseAlbumDirFactory;
import com.brodev.socialapp.android.FroyoAlbumDirFactory;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.fragment.BROADCAST;
import com.mypinkpal.app.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/*
 * Class edit profile picture and cover picture
 */
public class ProfilePicActivity extends SherlockActivity {

	private ImageView profilePic;
	private Button chooseImageBtn, takePhotoBtn;

	ProfilePicUtil profilePicUtil = new ProfilePicUtil();
	Bitmap bitmap = null;
	private ProgressDialog dialog;

	private final int SELECT_FILE = 1;
	private final int REQUEST_CAMERA = 0;
	
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	
	private String mCurrentPhotoPath;
	
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	
	//phrase manager
	private PhraseManager phraseManager;

	private File setUpPhotoFile() throws IOException {
		File f = profilePicUtil.createImageFile(getApplicationContext(), mAlbumStorageDirFactory);
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}
	
	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			bitmap = profilePicUtil.setPic(getApplicationContext(), bitmap, profilePic, mCurrentPhotoPath);
			takePhoto(bitmap);
			profilePicUtil.galleryAddPic(getApplicationContext(), mCurrentPhotoPath);
			mCurrentPhotoPath = null;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_pic);

		phraseManager = new PhraseManager(getApplicationContext());
		
		profilePic = (ImageView) findViewById(R.id.ivImage);
		chooseImageBtn = (Button) findViewById(R.id.choose_from_gallery);
		chooseImageBtn.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.choose_from_gallery"));
		takePhotoBtn = (Button) findViewById(R.id.take_photo);
		takePhotoBtn.setText(phraseManager.getPhrase(getApplicationContext(), "accountapi.take_photo"));
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// change title
		getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "accountapi.edit_profile_picture"));
		
		Bundle extras = getIntent().getExtras();

		// if choose from gallery
		if (extras.containsKey("uri_image")) {
			String uri = extras.getString("uri_image");
			// convert string to uri
			Uri uriImage = Uri.parse(uri);
			if (uriImage != null) {
				selectFromGallery(uriImage);
			}
		}

		// if take photo from camera
		if (extras.containsKey("take_photo")) {
			byte[] img = extras.getByteArray("take_photo");
			bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
			takePhoto(bitmap);
		}

		// action click choose picture from gallery button
		chooseImageBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent, SELECT_FILE);
				} catch (Exception e) {
					Log.e(e.getClass().getName(), e.getMessage(), e);
				}
			}
		});

		// action click take new photo button
		takePhotoBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.please_wait"), Toast.LENGTH_LONG).show();
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				File f = null;
				try {
					f = setUpPhotoFile();
					if (!f.exists()) {
						Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.error_while_capturing_image"), Toast.LENGTH_LONG).show();
						return;
					}
					mCurrentPhotoPath = f.getAbsolutePath();
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				} catch (IOException e) {
					e.printStackTrace();
					f = null;
					mCurrentPhotoPath = null;
				}
				startActivityForResult(cameraIntent, REQUEST_CAMERA);
			}
		});
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SELECT_FILE) {
			Uri selectedImageUri = null;
			try {
				selectedImageUri = data.getData();
				selectFromGallery(selectedImageUri);
			} catch (Exception ex) {
				selectedImageUri = null;
			}
		} else if (requestCode == REQUEST_CAMERA) {
			try {
				handleBigCameraPhoto();
			} catch (Exception ex) {
				bitmap = null;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.profile_pic_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			finish();
			return true;

		case R.id.upload_photo:
			if (bitmap == null) {
				Toast.makeText(getApplicationContext(),
						phraseManager.getPhrase(getApplicationContext(), "accountapi.take_or_choose_picture"), Toast.LENGTH_LONG).show();
			} else {
				dialog = ProgressDialog.show(ProfilePicActivity.this,
							phraseManager.getPhrase(getApplicationContext(), "accountapi.uploading"), phraseManager.getPhrase(getApplicationContext(), "accountapi.please_wait"), true);
				new ImageUploadTask().execute();
			}
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	public class ImageUploadTask extends AsyncTask<Void, Void, String> {
		User user = (User) getApplicationContext();

		@Override
		protected String doInBackground(Void... unsued) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();

				String URL = null;
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), "uploadProfilePic", true) + "&token=" + user.getTokenkey();	
				} else {
					URL = Config.makeUrl(Config.CORE_URL, "uploadProfilePic", true) + "&token=" + user.getTokenkey();
				}
				
				HttpPost httpPost = new HttpPost(URL);

				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 70, bos);

				byte[] data = bos.toByteArray();
				
				entity.addPart("edit_profile", new StringBody("1"));

				entity.addPart("image", new ByteArrayBody(data, JPEG_FILE_PREFIX + String.valueOf(System.currentTimeMillis()) + ".jpg"));

				httpPost.setEntity(entity);
				HttpResponse response = httpClient.execute(httpPost, localContext);
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

				String sResponse = reader.readLine();
				
				Log.i("Response", sResponse);
				return sResponse;
			} catch (Exception e) {
				if (dialog.isShowing())
					dialog.dismiss();
			
				Log.e(e.getClass().getName(), e.getMessage(), e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(String sResponse) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();

				JSONObject resultJSON = new JSONObject(sResponse);

				JSONObject output = resultJSON.getJSONObject("output");
				if (output.has("image_url")) {
					user.setUserImage(output.getString("image_url"));
					Intent intent = new Intent(ProfilePicActivity.this, FriendTabsPager.class);
					startActivity(intent);
                    Intent i = new Intent(ProfilePicActivity.this, BROADCAST.class);
                    sendBroadcast(i);
					finish();
				}

			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.upload_error"), Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
		}
	}

	/**
	 * Take new photo
	 * 
	 * @param bitmap
	 */
	public void takePhoto(Bitmap bitmap) {
		bitmap = profilePicUtil.getResizedBitmap(bitmap);
		profilePic.setImageBitmap(bitmap);
	}

	/**
	 * Function get photo from gallery
	 * 
	 * @param selectedImageUri
	 */
	public void selectFromGallery(Uri selectedImageUri) {
		String filePath = null;
		try {
			String filemanagerstring = selectedImageUri.getPath();

			// MEDIA GALLERY
			String selectedImagePath = profilePicUtil.getPath(selectedImageUri, this);

			if (selectedImagePath != null) {
				filePath = selectedImagePath;
			} else if (filemanagerstring != null) {
				filePath = filemanagerstring;
			} else {
				Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.unknown_path"), Toast.LENGTH_LONG).show();
				Log.e("Bitmap", "Unknown path");
			}

			if (filePath != null) {
				bitmap = profilePicUtil.decodeFile(filePath);
				profilePic.setImageBitmap(bitmap);
			} else {
				bitmap = null;
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), phraseManager.getPhrase(getApplicationContext(), "accountapi.internal_error"), Toast.LENGTH_LONG).show();
			Log.e(e.getClass().getName(), e.getMessage(), e);
		}
	}

	
	// Some lifecycle callbacks so that the image can survive orientation change
		@Override
		protected void onSaveInstanceState(Bundle outState) {
			outState.putParcelable(BITMAP_STORAGE_KEY, bitmap);
			outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (bitmap != null) );
			super.onSaveInstanceState(outState);
		}

		@Override
		protected void onRestoreInstanceState(Bundle savedInstanceState) {
			super.onRestoreInstanceState(savedInstanceState);
			bitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
			profilePic.setImageBitmap(bitmap);
		}

}
