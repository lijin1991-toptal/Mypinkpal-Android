package com.brodev.socialapp.fragment.invites;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by psyh on 11/19/14.
 */
public abstract class BaseListAdapter <T> extends BaseAdapter {

    protected LayoutInflater layoutInflater;
    protected Activity baseActivity;
    protected List<T> objectsList;
    protected Resources resources;

    public BaseListAdapter(Activity baseActivity, List<T> objectsList) {
        this.baseActivity = baseActivity;
        this.objectsList = objectsList;
        this.layoutInflater = LayoutInflater.from(baseActivity);
        resources = baseActivity.getResources();
    }

    @Override
    public int getCount() {
        return objectsList.size();
    }

    @Override
    public T getItem(int position) {
        return objectsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setNewData(List<T> newData) {
        objectsList = newData;
        notifyDataSetChanged();
    }

    protected void displayImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }
}
