package com.brodev.socialapp.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.SessionManager;
import com.brodev.socialapp.android.animation.DisplayNextView;
import com.brodev.socialapp.android.animation.Rotate3dAnimation;
import com.brodev.socialapp.android.manager.UIHelper;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.facebook.SettingFacebookApp;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginAndJoinDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends Activity {

	private ImageView startupImage;
	private ImageView nextImage;
	private Timer mStartupAdsTimer;
	private boolean advertisingClickPerformed = false;
	private static int DEFAULT_ADV_DELAY = 1000;
	private PhraseManager phraseManager;
	private boolean isFirstImage = true, enableRate;
	private String imageUrl = "";
	private User user;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private AlertDialog alertDialog;


    public static void start(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        context.startActivity(intent);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		startupImage = (ImageView) findViewById(R.id.splash_image);
		nextImage = (ImageView) findViewById(R.id.splash_next_image);
		phraseManager = new PhraseManager(getApplicationContext());
		user = (User) getApplicationContext();
		final AssetManager assetManager = getBaseContext().getAssets();

        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            new InitSocialTask(getApplicationContext(), assetManager).execute();
        } else {
            // display error
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.no_internet_connection_title));
            alertDialog.setMessage(getString(R.string.no_internet_connection_content));
            alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                    UIHelper.killApp(true);
                }
            });
            alertDialog.setIcon(R.drawable.ic_launcher);
            alertDialog.show();
        }

	}
	
	/**
	 * Request to server get setting
	 * @author ducpham
	 */
	public class InitSocialTask extends AsyncTask<Void, Void, String> {
		
		private Context context;
		private NetworkUntil networkUntil;
		
		public InitSocialTask(Context context, AssetManager assetManager) {
			this.context = context;
			networkUntil = new NetworkUntil(this.context, assetManager);
		}
		
		@Override
		protected String doInBackground(Void... params) {
			String resultstring = null;
			try {
				//get check key url
				String url_getSetting = Config.CORE_URL + Config.URL_GET_SETTING;
				
				resultstring = networkUntil.makeHttpRequest(url_getSetting, "GET", null);


			} catch (Exception ex) {
				return null;
			}
			 
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			if (result == null) {
				onStartLoginActivity(DEFAULT_ADV_DELAY);
			} else {
				if (isFirstImage) {
					applyRotation(0, 90);
					isFirstImage = !isFirstImage;
				} else {
					applyRotation(0, -90);
					isFirstImage = !isFirstImage;
				}
				
				try {
					JSONObject settingJSON = new JSONObject(result);
					SettingFacebookApp settingFb = new SettingFacebookApp(getApplicationContext());
					if (settingJSON.has("phrases")) {
						phraseManager.saveJSONObject(getApplicationContext(), settingJSON.getJSONObject("phrases"));
					}
					if (settingJSON.has("login_background")) {
						imageUrl = settingJSON.getString("login_background");	
					}
					//store facebook app id 
					settingFb.storeAppId(getResources().getString(R.string.app_id), settingJSON.getBoolean("display_fb"));
					enableRate = settingJSON.getBoolean("enable_rate");
					
					//set color 
					user.setColor(settingJSON.getString("color_app"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				onStartLoginActivity(2000);
			}
		}

	}
	
	private void startNextActivity() {
		Intent i = new Intent(SplashActivity.this, LoginActivity.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra("imageUrl", imageUrl);
		i.putExtra("enalbe_rate", enableRate);
		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(i);
		finish();
	}
	
	protected void onStartLoginActivity(int delay) {

        if (delay == 0) {
            startNextActivity();
            return;
        }
        mStartupAdsTimer = new Timer();
        mStartupAdsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!advertisingClickPerformed) {
                    startNextActivity();
                }
            }
        }, delay);

	}
	
	private void applyRotation(float start, float end) {
		// Find the center of image
		final float centerX = startupImage.getWidth() / 2.0f;
		final float centerY = startupImage.getHeight() / 2.0f;

		// Create a new 3D rotation with the supplied parameter
		// The animation listener is used to trigger the next animation
		final Rotate3dAnimation rotation =
				new Rotate3dAnimation(start, end, centerX, centerY, 0, false);
		rotation.setDuration(500);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateInterpolator());
		rotation.setAnimationListener(new DisplayNextView(isFirstImage, startupImage, nextImage));

		if (isFirstImage){
			startupImage.startAnimation(rotation);
		} else {
			nextImage.startAnimation(rotation);
		}

	}

    private void showKeyHashes() throws PackageManager.NameNotFoundException, NoSuchAlgorithmException {
    }

}
