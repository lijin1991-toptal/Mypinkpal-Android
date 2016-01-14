package com.brodev.socialapp.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.mypinkpal.app.R;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("ValidFragment")
public class WebviewFragmentPoll extends Fragment {

	private String ohtml;
	private ProgressBar progress;
	private WebView webView;
	private User user;
	private String URL;
    private static final int FILECHOOSER_RESULTCODE = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;

	public WebviewFragmentPoll(String html) {
		try {
			Uri uri = Uri.parse(html);
			if (uri.getQueryParameter("do") != null) {
				ohtml = uri.getQueryParameter("do");
			} else {
				ohtml = new URL(html).getPath().toString();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		setRetainInstance(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		user = (User) getActivity().getApplicationContext();
		URL = null;
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// construct the RelativeLayout
		View view = inflater.inflate(R.layout.link, container, false);
		webView = (WebView) view.findViewById(R.id.link);
		progress = (ProgressBar) view.findViewById(R.id.progress);
		setupWebView(webView);
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("mColorRes", ohtml);
	}

	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	private void setupWebView(WebView webView) {
		WebSettings settings = webView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptEnabled(true);
		settings.setLoadWithOverviewMode(true);
		settings.setSupportZoom(true);
		settings.setDomStorageEnabled(true);
		settings.setPluginState(PluginState.ON_DEMAND);
		settings.setUseWideViewPort(true);
		settings.setUserAgentString("Android");
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);

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
                    final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

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
                    Toast.makeText(getActivity(), "Exception:" + e,Toast.LENGTH_LONG).show();
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		URL = Config.CORE_URL + "index.php?do=/mobile" + ohtml + "&loginToken=" + user.getTokenkey() + "&";
        Log.d("psyh", "URL: " + URL);
		webView.loadUrl(URL);
       // loadUrl(webView, URL);
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == FILECHOOSER_RESULTCODE) {

            if (null == this.mUploadMessage) {
                return;
            }

            Uri result = null;

            try {
                if (resultCode != getActivity().RESULT_OK) {
                    result = null;
                } else {
                    // retrieve from the private variable if the intent is null
                    result = intent == null ? mCapturedImageURI : intent.getData();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "activity :" + e, Toast.LENGTH_LONG).show();
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

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

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		webView.destroy();
		webView = null;
		progress = null;
	}


}