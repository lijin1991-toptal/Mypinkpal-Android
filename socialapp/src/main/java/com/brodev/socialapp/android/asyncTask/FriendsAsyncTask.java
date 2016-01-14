package com.brodev.socialapp.android.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class FriendsAsyncTask extends AsyncTask<String, Void, String> {

    private NetworkUntil networkUntil = new NetworkUntil();
    private User user;

    public FriendsAsyncTask(Context context) {
        user = (User) context.getApplicationContext();
    }

    @Override
    protected String doInBackground(String... params) {
        String resultstring;

        String URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);

        // Use BasicNameValuePair to create GET data
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();

        pairs.add(new BasicNameValuePair("mode", "getUserFriends"));
        pairs.add(new BasicNameValuePair("userId", params[1]));
        pairs.add(new BasicNameValuePair("srch", params[2]));
        try {
            pairs.add(new BasicNameValuePair("sexuality", params[3]));
        } catch (Exception e) {
        }
        //   pairs.add(new BasicNameValuePair("page", "1"));

        Log.d("psyh", "userId: " + params[1]);

        resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
        Log.d("psyh", "SearchAsyncTask: " + resultstring);
        return resultstring;
    }

}
