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

public class UnFriendsAsyncTask extends AsyncTask<String, Void, String> {

    private NetworkUntil networkUntil = new NetworkUntil();
    private User user;

    public UnFriendsAsyncTask(Context context) {
        user = (User) context.getApplicationContext();
    }

    @Override
    protected String doInBackground(String... params) {
        String resultstring;

        String URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);

        // Use BasicNameValuePair to create GET data
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();

        pairs.add(new BasicNameValuePair("mode", "deleteFriend"));
        pairs.add(new BasicNameValuePair("userId", params[0]));
        pairs.add(new BasicNameValuePair("friendId", params[1]));

        resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
        Log.d("psyh", "SearchAsyncTask: " + resultstring);
        return resultstring;
    }

}
