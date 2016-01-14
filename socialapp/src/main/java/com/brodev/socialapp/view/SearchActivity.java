package com.brodev.socialapp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.NextActivity;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Blog;
import com.brodev.socialapp.entity.MarketPlace;
import com.brodev.socialapp.entity.Music;
import com.brodev.socialapp.entity.Search;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.Video;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends SherlockListActivity implements
        SearchView.OnQueryTextListener {

    private boolean isSearchAll = true;
    private TextView allResultView, memberView;
    private NetworkUntil network = new NetworkUntil();
    private User user;
    private int page;
    private Timer tTimer = new Timer();
    private PhraseManager phraseManager;

    /**
     * Change color
     *
     * @param colorCode
     */
    private void changeColor(String colorCode, boolean allSearch) {
        if ("Brown".equalsIgnoreCase(colorCode)) {
            if (allSearch) {
                allResultView.setBackgroundResource(R.drawable.brown_action_search);
                memberView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(Color.parseColor("#da6e00"));
                allResultView.setTextColor(getResources().getColor(R.color.white));
            } else {
                memberView.setBackgroundResource(R.drawable.brown_action_search);
                allResultView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(getResources().getColor(R.color.white));
                allResultView.setTextColor(Color.parseColor("#da6e00"));
            }
        } else if ("Pink".equalsIgnoreCase(colorCode)) {
            if (allSearch) {
                allResultView.setBackgroundResource(R.drawable.pink_action_search);
                memberView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(Color.parseColor("#ef4964"));
                allResultView.setTextColor(getResources().getColor(R.color.white));
            } else {
                memberView.setBackgroundResource(R.drawable.pink_action_search);
                allResultView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(getResources().getColor(R.color.white));
                allResultView.setTextColor(Color.parseColor("#ef4964"));
            }
        } else if ("Green".equalsIgnoreCase(colorCode)) {
            if (allSearch) {
                allResultView.setBackgroundResource(R.drawable.green_action_search);
                memberView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(Color.parseColor("#348105"));
                allResultView.setTextColor(getResources().getColor(R.color.white));
            } else {
                memberView.setBackgroundResource(R.drawable.green_action_search);
                allResultView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(getResources().getColor(R.color.white));
                allResultView.setTextColor(Color.parseColor("#348105"));
            }

        } else if ("Violet".equalsIgnoreCase(colorCode)) {
            if (allSearch) {
                allResultView.setBackgroundResource(R.drawable.violet_action_search);
                memberView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(Color.parseColor("#8190db"));
                allResultView.setTextColor(getResources().getColor(R.color.white));
            } else {
                memberView.setBackgroundResource(R.drawable.violet_action_search);
                allResultView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(getResources().getColor(R.color.white));
                allResultView.setTextColor(Color.parseColor("#8190db"));
            }
        } else if ("Red".equalsIgnoreCase(colorCode)) {
            if (allSearch) {
                allResultView.setBackgroundResource(R.drawable.red_action_search);
                memberView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(Color.parseColor("#ff0606"));
                allResultView.setTextColor(getResources().getColor(R.color.white));
            } else {
                memberView.setBackgroundResource(R.drawable.red_action_search);
                allResultView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(getResources().getColor(R.color.white));
                allResultView.setTextColor(Color.parseColor("#ff0606"));
            }
        } else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
            if (allSearch) {
                allResultView.setBackgroundResource(R.drawable.dark_violet_action_search);
                memberView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(Color.parseColor("#4e529b"));
                allResultView.setTextColor(getResources().getColor(R.color.white));
            } else {
                memberView.setBackgroundResource(R.drawable.dark_violet_action_search);
                allResultView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(getResources().getColor(R.color.white));
                allResultView.setTextColor(Color.parseColor("#4e529b"));
            }
        } else {
            if (allSearch) {
                allResultView.setBackgroundResource(R.drawable.action_search);
                memberView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(getResources().getColor(R.color.text_button));
                allResultView.setTextColor(getResources().getColor(R.color.white));
            } else {
                memberView.setBackgroundResource(R.drawable.action_search);
                allResultView.setBackgroundResource(R.drawable.inactive_search);
                memberView.setTextColor(getResources().getColor(R.color.white));
                allResultView.setTextColor(getResources().getColor(R.color.text_button));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        user = (User) getApplication().getApplicationContext();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phraseManager = new PhraseManager(getApplicationContext());

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (getIntent().hasExtra("page"))
                page = extras.getInt("page");
        }
        if (page == 0) page = 1;
        // call view from xml
        allResultView = (TextView) findViewById(R.id.all_result_action);
        String allResult = phraseManager.getPhrase(getApplicationContext(), "search.all_results");
        allResultView.setText(allResult);

        memberView = (TextView) findViewById(R.id.member_action);
        String member = phraseManager.getPhrase(getApplicationContext(), "search.members");
        memberView.setText(member);

        //change color
        changeColor(user.getColor(), true);

        // action click view "all results"
        allResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearchAll = true;
                // set background after change
                changeColor(user.getColor(), isSearchAll);
            }
        });

        // action click view "member"
        memberView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearchAll = false;
                // set background after change
                changeColor(user.getColor(), isSearchAll);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        final SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint(getString(R.string.search));
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(true);

        // add action search query for search view
        searchView.setOnQueryTextListener(this);

        menu.add("Search").setActionView(searchView).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (newText.toString().trim().length() > 0) {
            tTimer.cancel();
            tTimer = new Timer();
            tTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new SearchViewTask().execute(newText);
                }
            }, 300);
        }

        return false;
    }

    /**
     *
     */
    public class SearchViewTask extends AsyncTask<String, Void, String> {
        String resultstring = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            if (isCancelled()) {
                return null;
            }
            // Simulates a background task
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            // get result from get method
            resultstring = getResultFromGET(params[0]);

            return resultstring;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    searchAdapter sa = new searchAdapter(getApplicationContext());
                    sa = getSearchAdapter(sa, result);
                    setListAdapter(sa);
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * function get result from get method
     *
     * @param sSearch
     * @return result
     */
    public String getResultFromGET(String sSearch) {

        String resultstring;

        // url link
        String url = null;
        if (Config.CORE_URL == null) {
            url = Config.makeUrl(user.getCoreUrl(), null, false);
        } else {
            url = Config.makeUrl(Config.CORE_URL, null, false);
        }

        String URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);

        // Use BasicNameValuePair to create GET data
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        //pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
        pairs.add(new BasicNameValuePair("mode", "searchUsers"));
        pairs.add(new BasicNameValuePair("srch", sSearch));
        pairs.add(new BasicNameValuePair("page", "" + page));
/*
        if (isSearchAll) {
			pairs.add(new BasicNameValuePair("type", "all"));
		} else {
			pairs.add(new BasicNameValuePair("type", "user"));
		}
*/

        Log.d("psyh", "URL: \"" + URL + "\"");
        Log.d("psyh", "sSearch: \"" + sSearch + "\"");
        Log.d("psyh", "page: \"" + page + "\"");
        resultstring = network.makeHttpRequest(URL, "GET", pairs);

        Log.d("psyh", "resultstring: " + resultstring);
        return resultstring;
    }
//http://mypinkpal.com/mypinkpalapi.php?mode=searchUsers&srch=p&page=1
    /**
     * function get search adapter
     *
     * @param mAdapter
     * @param resString
     * @return searchAdapter
     */
    public searchAdapter getSearchAdapter(searchAdapter mAdapter,
                                          String resString) {
        if (resString != null) {
            try {
                JSONObject mainJSON = new JSONObject(resString);
                JSONObject outputJSON = mainJSON.getJSONObject("output");

                JSONArray searchResults = outputJSON.getJSONArray("aSearchResults");

                Search ser = null;
                JSONObject searchJSON = null;
                for (int i = 0; i < searchResults.length(); i++) {
                    ser = new Search();
                    searchJSON = searchResults.getJSONObject(i);

                    ser.setItemId(Integer.parseInt(searchJSON.getString("item_id")));
                    ser.setItemTitle(Html.fromHtml(searchJSON.getString("item_title")).toString());
                    ser.setItemTypeId(searchJSON.getString("item_type_id"));
                    ser.setUserImage(searchJSON.getString("user_image"));
                    ser.setItemName(searchJSON.getString("item_name"));
                    ser.setItemLink(searchJSON.getString("item_link"));

                    if (!searchJSON.isNull("item_display_photo_mobile")
                            && !searchJSON.getString("item_display_photo_mobile").equals("null")) {
                        ser.setItemDisplayPhotoMobile(searchJSON.getString("item_display_photo_mobile"));
                    }

                    mAdapter.add(ser);
                }

            } catch (Exception ex) {
                return null;
            }
        }

        return mAdapter;
    }

    /**
     * Change color text
     *
     * @param textview
     * @param colorCode
     */
    private void changeColorText(TextView textview, String colorCode) {
        if ("Brown".equalsIgnoreCase(colorCode)) {
            textview.setTextColor(Color.parseColor("#da6e00"));
        } else if ("Pink".equalsIgnoreCase(colorCode)) {
            textview.setTextColor(Color.parseColor("#ef4964"));
        } else if ("Green".equalsIgnoreCase(colorCode)) {
            textview.setTextColor(Color.parseColor("#348105"));
        } else if ("Violet".equalsIgnoreCase(colorCode)) {
            textview.setTextColor(Color.parseColor("#8190db"));
        } else if ("Red".equalsIgnoreCase(colorCode)) {
            textview.setTextColor(Color.parseColor("#ff0606"));
        } else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
            textview.setTextColor(Color.parseColor("#4e529b"));
        } else {
            textview.setTextColor(Color.parseColor("#0084c9"));
        }
    }

    /**
     * class search adapter
     *
     * @author ducpham
     */
    public class searchAdapter extends ArrayAdapter<Search> {

        public searchAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            Search item = getItem(position);
            SearchHolder holder = null;

            if (view == null) {
                int layout = R.layout.search_view_row;

                view = LayoutInflater.from(getContext()).inflate(layout, null);

                ImageView image = (ImageView) view.findViewById(R.id.image);
                TextView itemTitle = (TextView) view.findViewById(R.id.item_title);
                TextView itemName = (TextView) view.findViewById(R.id.item_name);
                ImageView imagePhoto = (ImageView) view.findViewById(R.id.item_photo);

                view.setTag(new SearchHolder(image, itemTitle, itemName, imagePhoto));
            }

            if (holder == null && view != null) {
                Object tag = view.getTag();
                if (tag instanceof SearchHolder) {
                    holder = (SearchHolder) tag;
                }
            }

            if (item != null && holder != null) {

                if (item.getItemDisplayPhotoMobile() != null) {
                    holder.imagePhoto.setVisibility(View.VISIBLE);
                } else {
                    holder.imagePhoto.setVisibility(View.GONE);
                }
                // set image friend;
                if (holder.imageHolder != null) {
                    if (!"".equals(item.getUserImage())) {
                        network.drawImageUrl(holder.imageHolder, item.getUserImage(), R.drawable.loading);
                    }
                }
                // set item title
                if (holder.itemTitle != null) {
                    //change color
                    changeColorText(holder.itemTitle, user.getColor());
                    holder.itemTitle.setText(item.getItemTitle());
                }

                // set item name
                if (holder.itemName != null) {
                    holder.itemName.setText(item.getItemName());
                }

                // set display photo
                if (holder.imagePhoto != null) {
                    network.drawImageUrl(holder.imagePhoto, item.getItemDisplayPhotoMobile(), R.drawable.loading);
                }
            }

            return view;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Search search = (Search) getListAdapter().getItem(position);

        String itemTypeId = search.getItemTypeId();
        Intent intent = null;
        if (itemTypeId.equals("user")) {
            intent = new Intent(SearchActivity.this, FriendTabsPager.class);
            intent.putExtra("user_id", String.valueOf(search.getItemId()));
        } else if (itemTypeId.equals("pages")) {
            intent = new Intent(SearchActivity.this, FriendTabsPager.class);
            intent.putExtra("page_id", String.valueOf(search.getItemId()));

        } else if (itemTypeId.equals("music")) {
            intent = new Intent(SearchActivity.this, MusicPlaySong.class);
            Music music = new Music();
            music.setSong_id(String.valueOf(search.getItemId()));
            music.setTitle("");
            intent.putExtra("song", music);
        } else if (itemTypeId.equals("blog")) {
            intent = new Intent(SearchActivity.this, BlogDetail.class);
            Blog blog = new Blog();
            blog.setBlog_id(search.getItemId());
            blog.setTime_stamp("0");
            intent.putExtra("blog", blog);
        } else if (itemTypeId.equals("video")) {
            intent = new Intent(SearchActivity.this, VideoPlay.class);
            Video video = new Video();
            video.setVideo_id(search.getItemId());
            video.setTime_stamp("0");
            intent.putExtra("video", video);
        } else if (itemTypeId.equals("event")) {
            intent = new Intent(SearchActivity.this, EventDetailActivity.class);
            intent.putExtra("event_id", String.valueOf(search.getItemId()));
        } else if (itemTypeId.equals("marketplace")) {
            intent = new Intent(SearchActivity.this, MarketPlaceDetail.class);
            MarketPlace marketPlace = new MarketPlace();
            marketPlace.setTime_stamp("0");
            marketPlace.setListing_id(search.getItemId());
            intent.putExtra("marketplace", marketPlace);
        } else if (itemTypeId.equals("photo")) {
            intent = new Intent(SearchActivity.this, CommentActivity.class);
            intent.putExtra("item", String.valueOf(search.getItemId()));
            intent.putExtra("type", search.getItemTypeId());
        } else {
            new NextActivity(getApplicationContext()).linkActivity(itemTypeId,
                    String.valueOf(search.getItemId()), null,
                    search.getItemTitle(), search.getItemLink());
        }

        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        super.onListItemClick(l, v, position, id);
    }

    /**
     * class search holder
     *
     * @author ducpham
     */
    public class SearchHolder {
        public final ImageView imageHolder;
        public final TextView itemTitle;
        public final TextView itemName;
        public final ImageView imagePhoto;

        public SearchHolder(ImageView image, TextView itemTitle,
                            TextView itemName, ImageView imagePhoto) {
            this.imageHolder = image;
            this.itemTitle = itemTitle;
            this.itemName = itemName;
            this.imagePhoto = imagePhoto;
        }
    }

}
