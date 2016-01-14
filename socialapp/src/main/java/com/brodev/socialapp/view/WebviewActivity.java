package com.brodev.socialapp.view;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;

import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.mypinkpal.app.R;

public class WebviewActivity extends SherlockActivity {

	private ProgressBar progress;
	private WebView webView;
	private User user;
	private String URL, webUrl, html;
	private boolean header;
	public Uri imageUri;
    private static final int FILECHOOSER_RESULTCODE = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;

	public static Intent newInstance(Activity activity, String pos) {
		Intent intent = new Intent(activity, WebviewActivity.class);
		intent.putExtra("html", pos);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.link);
		html = null;
		header = false;

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		webView = (WebView)findViewById(R.id.link);
		progress = (ProgressBar)findViewById(R.id.progress);
		setupWebView(webView);

		user = (User) getApplicationContext();		

		if (getIntent().getExtras() != null) {
			 html = getIntent().getExtras().getString("html");

			 if (getIntent().hasExtra("header")) {
				 header = getIntent().getExtras().getBoolean("header");
			 }
		}

		if (!header) {
			html = html.replace("www.", "");
			webUrl = Config.CORE_URL.replace("www.", "");
			html = html.substring(webUrl.length());		
			if (html.indexOf("index.php") >= 0) {
				html = html.substring(14);
			}

			URL = Config.CORE_URL + "index.php?do=/mobile/" + html + "&loginToken=" + user.getTokenkey() + "&";
			
			webView.loadUrl(URL);
		} else {
			webView.loadUrl(html);
		}

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

	@SuppressLint("SetJavaScriptEnabled")
	private void setupWebView(WebView webView) {
		WebSettings settings = webView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
		settings.setSupportZoom(true);
		settings.setPluginState(PluginState.ON_DEMAND);
		settings.setUseWideViewPort(true);	
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				progress.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progress.setVisibility(View.GONE);
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				progress.setProgress(newProgress);
			}
			
			// openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){  
                
                // Update message
                mUploadMessage = uploadMsg;
                 
				try {
					// Create AndroidExampleFolder at sdcard
					File imageStorageDir = new File(
							Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");

					if (!imageStorageDir.exists()) {
						// Create AndroidExampleFolder at sdcard
						imageStorageDir.mkdirs();
					}

					// Create camera captured image file path and name
					File file = new File(imageStorageDir + File.separator
							+ "IMG_"
							+ String.valueOf(System.currentTimeMillis())
							+ ".jpg");

					mCapturedImageURI = Uri.fromFile(file);

					// Camera capture image intent
					final Intent captureIntent = new Intent(
							android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

					captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.addCategory(Intent.CATEGORY_OPENABLE);
					i.setType("image/*");

					// Create file chooser intent
					Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

					// Set camera intent to file chooser
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
							new Parcelable[] { captureIntent });

					// On select image call onActivityResult method of activity
					startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

				} catch (Exception e) {
					Toast.makeText(getBaseContext(), "Exception:" + e,Toast.LENGTH_LONG).show();
				}
                 
            }
             
            // openFileChooser for Android < 3.0
            @SuppressWarnings("unused")
			public void openFileChooser(final ValueCallback<Uri> uploadMsg){
                openFileChooser(uploadMsg, "");
            }
             
            //openFileChooser for other Android versions
            @SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                                        
                openFileChooser(uploadMsg, acceptType);
            }
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == FILECHOOSER_RESULTCODE) {

			if (null == this.mUploadMessage) {
				return;
			}

			Uri result = null;

			try {
				if (resultCode != RESULT_OK) {
					result = null;
				} else {
					// retrieve from the private variable if the intent is null
					result = intent == null ? mCapturedImageURI : intent.getData();
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "activity :" + e, Toast.LENGTH_LONG).show();
			}

			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;

		}

	}

	@Override
    // Detect when the back button is pressed
    public void onBackPressed() {
     
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }
	
	@Override
	public void onResume() {
		super.onResume();
		webView.resumeTimers();
	}

	@Override
	public void onPause() {
		super.onPause();
		webView.pauseTimers();
	}
}