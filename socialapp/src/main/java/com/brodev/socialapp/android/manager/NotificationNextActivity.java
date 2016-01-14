package com.brodev.socialapp.android.manager;

import android.content.Context;
import android.content.Intent;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.entity.Blog;
import com.brodev.socialapp.entity.MarketPlace;
import com.brodev.socialapp.entity.Music;
import com.brodev.socialapp.entity.Video;
import com.brodev.socialapp.view.BlogDetail;
import com.brodev.socialapp.view.CommentActivity;
import com.brodev.socialapp.view.EventDetailActivity;
import com.brodev.socialapp.view.MarketPlaceDetail;
import com.brodev.socialapp.view.MusicPlaySong;
import com.brodev.socialapp.view.ThreadActivity;
import com.brodev.socialapp.view.VideoPlay;
import com.brodev.socialapp.view.WebviewActivity;

public class NotificationNextActivity {
	private Context context;
	private PhraseManager phraseManager;

	public NotificationNextActivity(Context context) {
		this.context = context;
		phraseManager = new PhraseManager(context);
	}
	
	public Intent notificationLinkActivity(Intent intent, String type, String item, String link, boolean isStart) 
	{
		if (type.indexOf("music_song") >= 0){
			intent = new Intent(context, MusicPlaySong.class);
			Music music = new Music();
			music.setTitle("");
			music.setSong_id(item);
			intent.putExtra("song", music);
		} else if (type.indexOf("blog") >= 0){
			intent = new Intent(context, BlogDetail.class);
			Blog blog = new Blog();
			blog.setTime_stamp("0");
			blog.setBlog_id(Integer.parseInt(item));
			intent.putExtra("blog", blog);
		} else if (type.contains("event") && !type.equals("event_comment")){
			intent = new Intent(context, EventDetailActivity.class);	
			intent.putExtra("event_id", item);
		} else if (type.equals("event_comment"))  {
			intent = new Intent(context, WebviewActivity.class);	
			intent.putExtra("html", link);
		} else if (type.indexOf("video") >= 0){
			intent = new Intent(context, VideoPlay.class);
			Video video = new Video();
			video.setTime_stamp("0");
			video.setVideo_id(Integer.parseInt(item));
			intent.putExtra("video", video);
		} else if (type.indexOf("marketplace") >= 0){
			intent = new Intent(context, MarketPlaceDetail.class);
			MarketPlace marketPlace = new MarketPlace();
			marketPlace.setTime_stamp("0");
			marketPlace.setListing_id(Integer.parseInt(item));
			intent.putExtra("marketplace", marketPlace);
		} else if (type.indexOf("forum_post") >= 0 || type.equals("forum_subscribed_post")) {
			intent = new Intent(context, ThreadActivity.class);
			intent.putExtra("title", phraseManager.getPhrase(context.getApplicationContext(), "forum.viewing_single_post"));
			intent.putExtra("post_id", item);
		} else {
			intent = new Intent(context, CommentActivity.class);
			intent.putExtra("item", item);
			intent.putExtra("type", type);
		}
		
		if (intent != null && isStart == true) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
		return intent;
	}
}
