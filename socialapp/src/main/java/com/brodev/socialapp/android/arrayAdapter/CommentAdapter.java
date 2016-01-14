package com.brodev.socialapp.android.arrayAdapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.asyncTask.LikeAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Comment;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.FriendActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.mypinkpal.app.R;

import java.util.List;

public class CommentAdapter {
	private NetworkUntil networkUtil = new NetworkUntil();
	
	//parent layout
	private Context context;
	private ColorView colorView;
	private User user;
    private ImageGetter imageGetter;
	
	public CommentAdapter(Context context) {
		this.context = context;
		colorView = new ColorView(context);
		user = (User) context.getApplicationContext();
        this.imageGetter = new ImageGetter(context);
	}
	
	/**
	 * Add Comment to View
	 * @param listComment2
	 */
	public void addCommentToView(List<Comment> listComment2, LinearLayout parentLayout, int posAddView, PhraseManager phraseManager) {
	    
		if (listComment2.size() == 1) {
			RelativeLayout comment_item = getRelativeLayoutComment(context, listComment2.get(0), parentLayout, phraseManager);
			parentLayout.addView(comment_item, posAddView);
		} else {
			for (int i = 0; i < listComment2.size(); i++) {
		    	Comment item = listComment2.get(i); 

		    	RelativeLayout comment_item = getRelativeLayoutComment(context, item, parentLayout, phraseManager);
		    	
	            // Add the text view to the parent layout
		    	parentLayout.addView(comment_item, posAddView + i);
		    }
		}
	}
	
	/**
	 * Get relative layout for comment
	 * @param comment
	 * @return relative layout
	 */
	public RelativeLayout getRelativeLayoutComment(final Context context, final Comment comment, LinearLayout parentLayout, final PhraseManager phraseManager) 
	{
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view;
	    
		// Add the text layout to the parent layout
    	view = layoutInflater.inflate(R.layout.comment_row, parentLayout, false);
		// In order to get the view we have to use the new view with text_layout in it
    	RelativeLayout comment_item = (RelativeLayout)view.findViewById(R.id.comment_item);
    	
    	ImageView user_image = (ImageView)view.findViewById(R.id.item_img);
    	TextView full_name = (TextView)view.findViewById(R.id.item_fullname);
    	TextView time_phrase = (TextView)view.findViewById(R.id.item_time);
        TextView text = (TextView)view.findViewById(R.id.item_text);
    	final TextView likeComment = (TextView) view.findViewById(R.id.item_comment_like);
    	TextView middotTv = (TextView) view.findViewById(R.id.middle_dot_total);
    	ImageView likeIcon = (ImageView) view.findViewById(R.id.item_comment_like_icon);
    	final TextView commentTotal = (TextView) view.findViewById(R.id.item_comment_total_like);
    	final LinearLayout commentLikeLayout = (LinearLayout) view.findViewById(R.id.comment_like_layout);
    	
    	//change color
    	colorView.changeColorText(full_name, user.getColor());
    	colorView.changeColorText(likeComment, user.getColor());
    	colorView.changeColorLikeIcon(likeIcon, user.getColor());
    	colorView.changeColorText(middotTv, user.getColor());
    	colorView.changeColorText(commentTotal, user.getColor());
    	
    	networkUtil.drawImageUrl(user_image, comment.getUserImage(), R.drawable.loading);
    	full_name.setText(comment.getFullname());
    	time_phrase.setText(comment.getTimePhrase());

        // interesting part starts from here here:
        Html.ImageGetter ig = imageGetter.create(0, comment.getText(), text);

        text.setTag(0);
        text.setText(Html.fromHtml(comment.getText(), ig, null));

    	if (comment.isLiked()) {
    		likeComment.setText(phraseManager.getPhrase(context, "feed.unlike"));
    	} else {
    		likeComment.setText(phraseManager.getPhrase(context, "feed.like"));
    	}
    	
		if (comment.getTotalLike() > 0) {
			commentLikeLayout.setVisibility(View.VISIBLE);
			commentTotal.setText(String.valueOf(comment.getTotalLike()));
		} else {
			commentLikeLayout.setVisibility(View.GONE);
		}
		
		//action click like comment
    	likeComment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				likeSingleComment(context, comment, likeComment, commentLikeLayout, commentTotal, phraseManager);
			}
		});
    	
    	//action view liked comment
    	commentLikeLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FriendActivity.class);
				intent.putExtra("type", "feed_mini");
				intent.putExtra("item_id", String.valueOf(comment.getCommentId()));
				intent.putExtra("total_like", comment.getTotalLike());
				context.startActivity(intent);
			}
		});
		
    	user_image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FriendTabsPager.class);
				intent.putExtra("user_id", comment.getUserId());
				context.startActivity(intent);
			}
		});
    	
    	return comment_item;
	}

    /**
     * like comment
     * @param context
     * @param item
     * @param likeComment
     * @param commentLikeLayout
     * @param totalLike
     * @param phraseManager
     */
	private void likeSingleComment(Context context, Comment item, TextView likeComment, LinearLayout commentLikeLayout, TextView totalLike, PhraseManager phraseManager) {
		if (item.isLiked()) {
			likeComment.setText(phraseManager.getPhrase(context, "feed.like"));
			new LikeAsyncTask(context.getApplicationContext()).execute(user.getTokenkey(), String.valueOf(item.getCommentId()), "feed_mini", null, "unlike");
			item.setLiked(false);
			if (item.getTotalLike() > 0) {
				commentLikeLayout.setVisibility(View.VISIBLE);
				item.setTotalLike(item.getTotalLike() - 1);
				totalLike.setText(String.valueOf(item.getTotalLike()));
				if (item.getTotalLike() > 0) {
					commentLikeLayout.setVisibility(View.VISIBLE);
				} else {
					commentLikeLayout.setVisibility(View.GONE);
				}
			} 
		} else {
			likeComment.setText(phraseManager.getPhrase(context, "feed.unlike"));
			new LikeAsyncTask(context.getApplicationContext()).execute(user.getTokenkey(), String.valueOf(item.getCommentId()), "feed_mini", null, "like");
			item.setLiked(true);
			commentLikeLayout.setVisibility(View.VISIBLE);
			item.setTotalLike(item.getTotalLike() + 1);
			totalLike.setText(String.valueOf(item.getTotalLike()));
		}
	}

}
