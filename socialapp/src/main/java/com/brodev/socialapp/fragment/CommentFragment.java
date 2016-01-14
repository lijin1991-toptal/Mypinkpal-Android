package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.arrayAdapter.CommentAdapter;
import com.brodev.socialapp.android.asyncTask.LikeAsyncTask;
import com.brodev.socialapp.android.asyncTask.LoadCommentAsyncTask;
import com.brodev.socialapp.android.asyncTask.PostCommentAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.entity.Comment;
import com.brodev.socialapp.entity.User;

public class CommentFragment extends SherlockFragment {

	private User user;
	private List<Comment> lstComment;
	private LinearLayout parentLayout;
	private String type, typeComment;
	private boolean isLiked, isShare, canPostComment;
	private int page, totalComment, totalLike, itemId;
	private RelativeLayout viewMoreLayout, likeLayout, commentLayout, shareLayout;
	private Button sendMessageBtn;
	private EditText messageTxt;
	private TextView likeTv, commentTv, itemViewMore, shareTv;
	private PhraseManager phraseManager;
	private ColorView colorView;
	
	@Override
	public View getView() {
		return super.getView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		LoadCommentAsyncTask loadCommentAsyncTask = new LoadCommentAsyncTask(getActivity().getApplicationContext());
		
		//request to server
		loadCommentAsyncTask.execute(user.getTokenkey(), typeComment, String.valueOf(itemId), null, String.valueOf(totalComment), String.valueOf(page));
		
		final CommentAdapter cmtAdapter = new CommentAdapter(getActivity());

		try {
			//get list comment from asynctask
			lstComment = loadCommentAsyncTask.get();
			//add list comment to view
			cmtAdapter.addCommentToView(lstComment, parentLayout, 0, phraseManager);
			
			//check previous comment
			if (totalComment > lstComment.size()) {
				//show load previous comment
				viewMoreLayout.setVisibility(View.VISIBLE);
				
				totalComment -= lstComment.size();
				
				//action click load previous comment
				viewMoreLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						++page;
						LoadCommentAsyncTask loadCommentAsyncTask = new LoadCommentAsyncTask(getActivity().getApplicationContext());
						loadCommentAsyncTask.execute(user.getTokenkey(), typeComment, String.valueOf(itemId), null, String.valueOf(totalComment), String.valueOf(page));
						try {
							lstComment = loadCommentAsyncTask.get();
							if (lstComment.size() > 0) {
								cmtAdapter.addCommentToView(lstComment, parentLayout, 0, phraseManager);
								if (totalComment > lstComment.size()) {
									viewMoreLayout.setVisibility(View.VISIBLE);
								} else {
									viewMoreLayout.setVisibility(View.GONE);
								}
							} else {
								viewMoreLayout.setVisibility(View.GONE);
							}
						} catch (Exception ex) {
						}
					}
				});
			} else {
				viewMoreLayout.setVisibility(View.GONE);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		//action click send comment
		sendMessageBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//get string in message text
				String cmtTxt = messageTxt.getText().toString().trim();
				//request comment to server
				if (cmtTxt.length() == 0) {
					Toast.makeText(getActivity().getApplicationContext(), 
							phraseManager.getPhrase(getActivity().getApplicationContext(), "comment.add_some_text_to_your_comment"), 
							Toast.LENGTH_LONG).show();
				} else {
					PostCommentAsyncTask postCommentAsyncTask = new PostCommentAsyncTask(getActivity().getApplicationContext());
					
					postCommentAsyncTask.execute(user.getTokenkey(), typeComment, String.valueOf(itemId), null, cmtTxt);
					
					try {
						//get comment from asynctask
						List<Comment> lst = new ArrayList<Comment>();
						Comment comment = postCommentAsyncTask.get();
						lst.add(comment);
						cmtAdapter.addCommentToView(lst, parentLayout, -1, phraseManager);
						
						messageTxt.setText("");
					} catch (Exception ex) {
						
					}
				}
				
			}
		});
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//init phrase manager
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		user = (User) getActivity().getApplicationContext();
		colorView = new ColorView(getActivity().getApplicationContext());
		lstComment = new ArrayList<Comment>();
		page = 1;
		totalComment = -1;
		totalLike = -1;
		type = null;
		itemId = 0;
		isLiked = false;
		isShare = false;
		canPostComment = true;
		typeComment = null;
		
		//get data
		if (getArguments() != null) {
			type = getArguments().getString("type");
			itemId = getArguments().getInt("itemId");
			totalComment = getArguments().getInt("totalComment");
			isLiked = getArguments().getBoolean("is_liked");
			//isShare = getArguments().getBoolean("no_share");
			totalLike =  getArguments().getInt("total_like");
			canPostComment = getArguments().getBoolean("can_post_comment");
		}
		
		if (type != null) {
			typeComment = type;
			if (type.equals("feed_comment"))
				typeComment = "feed";
		}
		
		super.onCreate(savedInstanceState);
	}

	/**
	 * Like/Unlike UI
	 * @param view
	 */
	public void likeUI(View view) {
		likeLayout = (RelativeLayout) view.findViewById(R.id.like_view_like);
		likeTv = (TextView) view.findViewById(R.id.like);
		
		if (totalLike != -1) {
 			//show like
			showView(likeLayout, View.VISIBLE);
			//set phrase 
			likeTv.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
			if (isLiked == true) {
				likeTv.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
			}
			
		} else {
			showView(likeLayout, View.GONE);
		}
		
		//action click like/unlike
		likeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (likeTv.getText().toString().equals(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"))) {
					new LikeAsyncTask(getActivity().getApplicationContext()).execute(user.getTokenkey(), String.valueOf(itemId), type, null, "like");
					likeTv.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
				} else {
					new LikeAsyncTask(getActivity().getApplicationContext()).execute(user.getTokenkey(), String.valueOf(itemId), type, null, "unlike");
					likeTv.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
				}
			}
		});
	}
	
	/**
	 * Comment view
	 * @param view
	 */
	public void commentUI(View view) {
		commentLayout = (RelativeLayout) view.findViewById(R.id.like_view_comment);
		commentTv = (TextView) view.findViewById(R.id.comment);
		
		if (canPostComment == true) {
			showView(commentLayout, View.VISIBLE);
			view.findViewById(R.id.post_comment_view).setVisibility(View.VISIBLE);
			//set phrase
			commentTv.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.comment"));
		} else {
			showView(commentLayout, View.GONE);
			view.findViewById(R.id.post_comment_view).setVisibility(View.GONE);
		}
	}
	/**
	 * Share view
	 * @param view
	 */
	public void shareUI(View view) {
		shareLayout = (RelativeLayout) view.findViewById(R.id.like_view_share);
		shareTv = (TextView) view.findViewById(R.id.share);
		
		if (isShare == true) {
			shareTv.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.share"));
			showView(shareLayout, View.VISIBLE);
			
			//action share
			shareLayout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
				}
			});
		} else {
			showView(shareLayout, View.GONE);
			
		}
	}
	
	/**
	 * Show View
	 * @param show
	 */
	public void showView(RelativeLayout layout, int show) {
		layout.setVisibility(show);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// create view from comment layout xml
		View view = inflater.inflate(R.layout.view_comment, container, false);
		parentLayout = (LinearLayout) view.findViewById(R.id.parent_layout);
		viewMoreLayout = (RelativeLayout) view.findViewById(R.id.view_more_layout);
		itemViewMore = (TextView) view.findViewById(R.id.item_viewmore);
		itemViewMore.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "comment.view_previous_comments"));
		colorView.changeColorText(itemViewMore, user.getColor());
		
		//visible like/comment view
		if (totalComment != -1 || totalLike != -1) {
			view.findViewById(R.id.like_comment_view).setVisibility(View.VISIBLE);
			likeUI(view);
			commentUI(view);
			shareUI(view);
			commentTv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					messageTxt.requestFocus();
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		        	imm.showSoftInput(messageTxt, InputMethodManager.SHOW_IMPLICIT);
				}
			});
		} else {
			view.findViewById(R.id.like_comment_view).setVisibility(View.GONE);
		}
		
		//for send message
		sendMessageBtn = (Button) view.findViewById(R.id.send_email);
		sendMessageBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "mail.send"));
		changeColor(user.getColor());
		messageTxt = (EditText) view.findViewById(R.id.write_message);
		messageTxt.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.write_a_comment"));
		return view;
	}
	
	/**
	 * Change background
	 * @param colorCode
	 */
	private void changeColor(String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			sendMessageBtn.setBackgroundResource(R.drawable.brown_comment_post_icon);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			sendMessageBtn.setBackgroundResource(R.drawable.pink_comment_post_icon);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			sendMessageBtn.setBackgroundResource(R.drawable.green_comment_post_icon);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			sendMessageBtn.setBackgroundResource(R.drawable.violet_comment_post_icon);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			sendMessageBtn.setBackgroundResource(R.drawable.red_comment_post_icon);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			sendMessageBtn.setBackgroundResource(R.drawable.dark_violet_comment_post_icon);
		} else {
			sendMessageBtn.setBackgroundResource(R.drawable.comment_post_icon);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}
