package com.brodev.socialapp.android.manager;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.entity.Blog;
import com.brodev.socialapp.entity.MarketPlace;
import com.brodev.socialapp.entity.Music;
import com.brodev.socialapp.entity.Video;
import com.brodev.socialapp.view.BlogDetail;
import com.brodev.socialapp.view.EventDetailActivity;
import com.brodev.socialapp.view.MarketPlaceDetail;
import com.brodev.socialapp.view.MusicPlaySong;
import com.brodev.socialapp.view.NoInternetActivity;
import com.brodev.socialapp.view.ThreadActivity;
import com.brodev.socialapp.view.VideoPlay;
import com.brodev.socialapp.view.WebviewActivity;

public class NextActivity {

	private Context context;
	private PhraseManager phraseManager;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

	public NextActivity(Context context) {
		this.context = context;
		phraseManager = new PhraseManager(context);
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
	}

	/**
	 * Link next activity
	 * 
	 * @param type
	 * @param item
	 * @param link
	 */
	public void linkActivity(String type, String item, String itemCache,
			String title, String link) {
		Intent intent = null;

        if (networkInfo == null || !networkInfo.isConnected()) {
            intent = new Intent(context, NoInternetActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }

		if (type.equals("forum")) {
			intent = new Intent(context, ThreadActivity.class);
			intent.putExtra("thread_id", item);
			intent.putExtra("title", title);
		} else if (type.equals("forum_post")) {
			intent = new Intent(context, ThreadActivity.class);
			intent.putExtra("thread_id", itemCache);
			intent.putExtra("title", phraseManager.getPhrase(context, "forum.viewing_single_post"));
			intent.putExtra("post_id", item);
		} else if (type.equals("music_song")) {
			intent = new Intent(context, MusicPlaySong.class);
			Music music = new Music();
			music.setSong_id(item);
			music.setTitle("");
			intent.putExtra("song", music);
		} else if (type.equals("marketplace")) {
			intent = new Intent(context, MarketPlaceDetail.class);
			MarketPlace marketPlace = new MarketPlace();
			marketPlace.setListing_id(Integer.parseInt(item));
			marketPlace.setTime_stamp("0");
			intent.putExtra("marketplace", marketPlace);	
		} else if (type.equals("blog")) {
			intent = new Intent(context, BlogDetail.class);
			Blog blog = new Blog();
			blog.setBlog_id(Integer.parseInt(item));
			blog.setTime_stamp("0");
			intent.putExtra("blog", blog);
		} else if (type.equals("event")) {
			intent = new Intent(context, EventDetailActivity.class);	
			intent.putExtra("event_id", item);
		} else if (type.equals("video")) {
			intent = new Intent(context, VideoPlay.class);
			Video video = new Video();			
			video.setVideo_id(Integer.parseInt(item));
			video.setTime_stamp("0");
			intent.putExtra("video", video);
		} else {
			intent = new Intent(context, WebviewActivity.class);
			intent.putExtra("html", link);
		}

		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}

	}

}
