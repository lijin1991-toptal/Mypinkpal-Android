package com.mypinkpal.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.brodev.socialapp.view.SplashActivity;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by quanmt on 5/8/14.
 */
public class AppActivity extends SplashActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                Log.i("getPackageName():", getPackageName());
            }
        } catch (Exception e) {
            Log.e("FEnix", " ", e);
        }
    }

}
