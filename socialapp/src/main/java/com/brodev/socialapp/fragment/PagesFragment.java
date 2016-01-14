package com.brodev.socialapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.Pages;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.FriendTabsPager;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PagesFragment extends SherlockListFragment {

    private User user;
    private NetworkUntil networkUntil = new NetworkUntil();
    private PagesAdapter pa = null;
    private int page, total, currentpos;
    private PullToRefreshListView mPullRefreshListView;
    private ListView actualListView;
    private PhraseManager phraseManager;
    private ColorView colorView;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pages_fragment, container, false);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.friend_fragment_list);

        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                pa = new PagesAdapter(getActivity().getApplicationContext());
                new PagesTask().execute(page);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ++page;
                new PagesTask().execute(page);
            }

        });

        //no internet connection
        noInternetLayout = (RelativeLayout) view.findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) view.findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) view.findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) view.findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) view.findViewById(R.id.no_internet_image);

        //change color for no internet
        colorView.changeImageForNoInternet(noInternetImg, noInternetBtn, user.getColor());

        //set text for no internet element
        noInternetBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.try_again"));
        noInternetTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_title"));
        noInternetContent.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_content"));

        //action click load try again
        noInternetBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //fetch data
                        loadPages();
                    }
                }, 1000);
            }
        });

        return view;
    }

    private void loadPages() {
        try {
            connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                mPullRefreshListView.setVisibility(View.VISIBLE);

                //fetch data
                new PagesTask().execute(page);
            } else {
                // display error
                mPullRefreshListView.setVisibility(View.GONE);
                noInternetLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mPullRefreshListView.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        actualListView = mPullRefreshListView.getRefreshableView();
        try {
            loadPages();
        } catch (Exception ex) {
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        page = 1;
        total = 1;
        colorView = new ColorView(getActivity().getApplicationContext());
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        user = (User) getActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Pages pages = (Pages) actualListView.getAdapter().getItem(position);

        if (pages.getNotice() == null) {
            Intent intent = new Intent(this.getActivity(), FriendTabsPager.class);
            intent.putExtra("page_id", pages.getPagesId());
            startActivity(intent);
        }

        super.onListItemClick(l, v, position, id);
    }

    /**
     * Class request to server
     */
    public class PagesTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            String resString = null;

            if (isCancelled()) {
                return null;
            }

            //init pages adapter
            if (pa == null || page == 1) {
                pa = new PagesAdapter(getActivity().getApplicationContext());
            }

            //get result from get method
            resString = getResultFromGET(params[0]);

            return resString;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    pa = getPagesAdapter(pa, result);
                    if (pa != null) {
                        if (page == 1) {
                            actualListView.setAdapter(pa);
                        } else {
                            currentpos = getListView().getFirstVisiblePosition();

                            actualListView.setAdapter(pa);
                            getListView().setSelectionFromTop(currentpos + 1, 0);

                            pa.notifyDataSetChanged();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            mPullRefreshListView.onRefreshComplete();
            super.onPostExecute(result);
        }

    }

    /**
     * function get result from get method
     * @param page
     * @return string result
     */
    public String getResultFromGET(int page)
    {
        String resultstring;

        if (pa != null && pa.getCount() == total) {
            return null;
        }

        // Use BasicNameValuePair to create GET data
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
        pairs.add(new BasicNameValuePair("method", "accountapi.getAllPage"));
        pairs.add(new BasicNameValuePair("page", "" + page));

        // url request
        String URL = null;
        if (Config.CORE_URL == null) {
            URL = Config.makeUrl(user.getCoreUrl(), null, false);
        } else {
            URL = Config.makeUrl(Config.CORE_URL, null, false);
        }

        // request GET method to server
        resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);

        return resultstring;
    }

    /**
     * Function create pages adapter
     * @return Pages Adapter
     */
    public PagesAdapter getPagesAdapter(PagesAdapter madapter, String resString)
    {
        if (resString != null)
        {
            try {
                JSONObject mainJSON = new JSONObject(resString);

                // save total forum
                total = mainJSON.getJSONObject("api").getInt("total");

                Object intervention = mainJSON.get("output");

                if (intervention instanceof JSONArray) {
                    JSONArray outputJSON = (JSONArray) intervention;

                    JSONObject pagesJSON = null;
                    Pages pages;

                    for (int i = 0; i < outputJSON.length(); i++) {
                        pagesJSON = outputJSON.getJSONObject(i);
                        pages = new Pages();

                        // set pages id
                        if (pagesJSON.has("page_id") && !pagesJSON.isNull("page_id"))
                            pages.setPagesId(pagesJSON.getString("page_id"));

                        // set pages title
                        if (pagesJSON.has("title") && !pagesJSON.isNull("title"))
                            pages.setTitlePages(Html.fromHtml(pagesJSON.getString("title")).toString());

                        // set pages category
                        if (pagesJSON.has("category_name") && !pagesJSON.isNull("category_name"))
                            pages.setCategoryPages(Html.fromHtml(pagesJSON.getString("category_name")).toString());

                        // set image
                        if (pagesJSON.has("user_image_path") && !pagesJSON.isNull("user_image_path"))
                            pages.setImagePages(pagesJSON.getString("user_image_path"));

                        madapter.add(pages);
                    }
                } else if (intervention instanceof JSONObject) {
                    JSONObject pagesJSON = (JSONObject) intervention;

                    Pages pages = new Pages();
                    pages.setNotice(Html.fromHtml(pagesJSON.getString("notice")).toString());
                    madapter.add(pages);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                Pages pages = new Pages();
                pages.setNotice(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_pages"));
                madapter.add(pages);

                return madapter;
            }
        }
        return madapter;
    }


    /**
     * Create Pages browse adapter
     */
    public class PagesAdapter extends ArrayAdapter<Pages>
    {
        public PagesAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;
            Pages item = getItem(position);
            PagesViewHolder holder = null;

            if (view == null) {
                int layout = R.layout.pages_list_row;

                view = LayoutInflater.from(getContext()).inflate(layout, null);

                //call element from xml
                ImageView icon = (ImageView) view.findViewById(R.id.image_pages);
                TextView title = (TextView) view.findViewById(R.id.title_pages);
                TextView category = (TextView) view.findViewById(R.id.category_pages);

                //notice
                TextView notice = (TextView) view.findViewById(R.id.notice);

                view.setTag(new PagesViewHolder(icon, title, category, notice));
            }

            if (holder == null && view != null) {
                Object tag = view.getTag();
                if (tag instanceof PagesViewHolder) {
                    holder = (PagesViewHolder) tag;
                }
            }

            if (item != null && holder != null) {

                //if no found pages
                if (item.getNotice() != null) {
                    view.findViewById(R.id.pages_content_layout).setVisibility(View.GONE);
                    view.findViewById(R.id.pages_notice_layout).setVisibility(View.VISIBLE);
                    holder.notice.setText(item.getNotice());
                    colorView.changeColorText(holder.notice, user.getColor());
                } else {
                    view.findViewById(R.id.pages_content_layout).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.pages_notice_layout).setVisibility(View.GONE);

                    //set pages image
                    if (holder.imageHolder != null) {
                        if (item.getImagePages() != null) {
                            networkUntil.drawImageUrl(holder.imageHolder, item.getImagePages(), R.drawable.loading);
                        }
                    }

                    //set pages title
                    if (holder.title != null) {
                        if (item.getTitlePages() != null) {
                            holder.title.setText(item.getTitlePages());
                            colorView.changeColorText(holder.title, user.getColor());
                        }
                    }

                    //set pages category
                    if (holder.category != null) {
                        if (item.getCategoryPages() != null) {
                            holder.category.setText(item.getCategoryPages());
                        }
                    }
                }

            }

            return view;
        }
    }

    /**
     * Class pages view holder
     */
    public class PagesViewHolder {
        public final ImageView imageHolder;
        public final TextView title;
        public final TextView category;
        public final TextView notice;

        public PagesViewHolder(ImageView icon, TextView title, TextView category, TextView notice) {
            this.imageHolder = icon;
            this.title = title;
            this.category = category;
            this.notice = notice;
        }
    }

}
