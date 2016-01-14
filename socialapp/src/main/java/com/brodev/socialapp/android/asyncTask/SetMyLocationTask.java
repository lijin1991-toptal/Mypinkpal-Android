package com.brodev.socialapp.android.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.brodev.socialapp.android.manager.UTILS;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class SetMyLocationTask extends AsyncTask<String, Void, String> {

    private NetworkUntil networkUntil = new NetworkUntil();
    private User user;

    public SetMyLocationTask(Context context) {
        user = (User) context.getApplicationContext();
    }

    @Override
    protected String doInBackground(String... params) {

        return doTask();
    }

    private String doTask() {
        String resultstring = "GPS Error";
        LatLng latLng = UTILS.getLocation(user);
        if (latLng != null) {
            String URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("mode", "saveLocation"));
            pairs.add(new BasicNameValuePair("userId", user.getUserId()));
            pairs.add(new BasicNameValuePair("lat", String.valueOf(latLng.latitude)));
            pairs.add(new BasicNameValuePair("long", String.valueOf(latLng.longitude)));

            resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
            Log.d("psyh", "SetMyLocationTask result: " + resultstring);
        }
        return resultstring;
    }


}
