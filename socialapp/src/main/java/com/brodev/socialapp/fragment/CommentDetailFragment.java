package com.brodev.socialapp.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.asyncTask.LikeAsyncTask;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.Comment;
import com.brodev.socialapp.entity.Feed;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.DashboardActivity;
import com.brodev.socialapp.view.FriendActivity;
import com.brodev.socialapp.view.FriendTabsPager;
import com.brodev.socialapp.view.WebviewActivity;
import com.mypinkpal.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.fragment.sherlock.DialogFragment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommentDetailFragment extends DialogFragment {

	private NetworkUntil networkUntil = new NetworkUntil();
	private static String typeId;
	private static String itemId, itemModuleId = "";
	private static String moduleId;
	private static final String TIME = "time_phrase";
	private static final String FULLNAME = "full_name";
	private static final String ICON = "feed_icon";
	private static final String TITLE = "title_phrase";
	private static final String IMAGE = "feed_image";
	private static final String USERIMAGE = "user_image";
	private int page = 1;
	private Comment OneComment;
	private Feed objFeed;
	private PhraseManager phraseManager;
	private TextView commentTxt;	
	private List<Comment> arrayOfList;
	private User user;
	private ColorView colorView;
    private ImageGetter imageGetter;
	
	public static CommentDetailFragment newInstance( int position, String type_id, String item_id, String module_id, String item_module_id) {
		Bundle b = new Bundle();
		typeId = type_id;
		itemId = item_id;
		moduleId = module_id;
		itemModuleId = item_module_id;
		CommentDetailFragment f = new CommentDetailFragment();
		f.setArguments(b);
		return f;
	}
	
	public void setItemModuleId(String item_id) {
		itemModuleId = item_id;
	}
	
	//like area
	@InjectView(id = R.id.grid_item_img)
	private ImageView iv;
	
	@InjectView(id = R.id.grid_item_fullname)
	private TextView Title;

	@InjectView(id = R.id.grid_item_time)
	private TextView Time;
	
	@InjectView(id = R.id.grid_item_icon)
	private ImageView icon;
	
	@InjectView(id = R.id.total_like)
	private TextView total_like;
	
	@InjectView(id = R.id.total_comment)
	private TextView total_comment;
	
	@InjectView(id = R.id.grid_item_like_icon)
	private ImageView like_icon;
	
	@InjectView(id = R.id.scroll_comment)
	private LinearLayout scroll_comment;
	
	@InjectView(id = R.id.like)
	private TextView like;
	
	@InjectView(id = R.id.comment)
	private TextView comment;
	
	//feed photo
	@InjectView(id = R.id.grid_feedimg_small)
	private ImageView feedimg_small;
	
	@InjectView(id = R.id.grid_feedimg)
	private ImageView feedimg;
	
	@InjectView(id = R.id.grid_feedimg1)
	private ImageView feedimg1;
	
	@InjectView(id = R.id.grid_feedimg2)
	private ImageView feedimg2;
	
	@InjectView(id = R.id.grid_feedimg3)
	private ImageView feedimg3;
	
	@InjectView(id = R.id.item_viewmore_icon)
	private ImageView item_viewmore_icon;
	
	@InjectView(id = R.id.grid_item_status)
	private TextView feedStatus;
	
	@InjectView(id = R.id.grid_item_feed_content)
	private TextView feedContent;
	
	@InjectView(id = R.id.grid_item_title)
	private TextView title_feed;
	
	@InjectView(id = R.id.item_viewmore)
	private TextView view_more;
	
	@InjectView(id = R.id.send_email)
	private Button post_comment;
	
	@InjectView(id = R.id.grid_feedimg4)
	private ImageView feedimg4;
	
	@InjectView(id = R.id.write_message)
	private EditText text_comment;
	
	@InjectView(id = R.id.scrollview_comment)
	private ScrollView scrollview_comment;
	
	@InjectView(id = R.id.grid_repost)
	private ImageView report_button;
	
	@InjectView(id = R.id.grid_item_like_icon)
	private ImageView likeImg;
	
	@InjectView(id = R.id.grid_item_comment_icon)
	private ImageView commentImg;
	
	@InjectView(id = R.id.middle_dot)
	private TextView tvMiddleDot;
	
	@Override
	public View onCreateView(Bundle savedInstanceState, LayoutInflater inflater, ViewGroup container) {
		View view = inflater.inflate(R.layout.dialog_comment, null);		
		commentTxt = (TextView) view.findViewById(R.id.comment);	
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
	    super.onCreate(savedInstanceState);
	    setStyle(STYLE_NO_TITLE, 0); // remove title from dialog fragment
	    
		user = (User) getActivity().getApplicationContext();
	    phraseManager = new PhraseManager(getActivity().getApplicationContext());
	    colorView = new ColorView(getActivity().getApplicationContext());
        this.imageGetter = new ImageGetter(getActivity().getApplicationContext());
	}
	
	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		// view one feed
		commentTxt.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.comment"));
		post_comment.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "mail.send"));
		text_comment.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.write_a_comment"));
		
		//change color
		changeColor(post_comment, user.getColor());
		colorView.changeColorLikeCommnent(likeImg, commentImg, user.getColor());
		
		try {
			new SingleFeed().execute(typeId, itemId, moduleId);	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public class SingleFeed extends AsyncTask<String, Void, String> {
	
		String resultstring = null;
		JSONObject mainJSON = null;
		JSONArray outJson = null;
		JSONObject total = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			if (isCancelled()) {
				return null;
			}
			
			try {
				// Use BasicNameValuePair to create GET data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method", "accountapi.getItem"));
				if(params[1] != null) {
					pairs.add(new BasicNameValuePair("type_id", "" + params[0]));
					pairs.add(new BasicNameValuePair("item_id", "" + params[1]));
				} else {
					pairs.add(new BasicNameValuePair("module", "" + params[2]));
					pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
				}
				
				// url request
				String URL = Config.makeUrl(user.getCoreUrl(), null, false);
				// request GET method to server
				
				resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
				
				JSONObject mainJSON = new JSONObject(resultstring);
				JSONObject  pagesObj = mainJSON.getJSONObject("output");
				
					objFeed = new Feed();
					if (pagesObj.has("feed_id")) {
					objFeed.setFeedId(pagesObj.getString("feed_id"));
					}
					
					if (pagesObj.has("item_id")) {
						objFeed.setItemId(pagesObj.getString("item_id"));
					}
					
					if (pagesObj.has(FULLNAME)) {
						objFeed.setFullName(pagesObj.getString(FULLNAME));
					}
					
					if (pagesObj.has(TIME)) {
						objFeed.setTime(pagesObj.getString(TIME));
					}
					
					if (pagesObj.has(ICON)) {
						objFeed.setIcon(pagesObj.getString(ICON));
					}
					
					if (pagesObj.has(TITLE)) {
						objFeed.setTitle(Html.fromHtml(pagesObj.getString(TITLE)).toString());
					}
					
					if (pagesObj.has(USERIMAGE)) {
						objFeed.setUserImage(pagesObj.getString(USERIMAGE));
					}
					
					if (pagesObj.has("feed_title")) {
						objFeed.setTitleFeed(Html.fromHtml(pagesObj.getString("feed_title")).toString());
					}
					
					if (pagesObj.has("feed_content") && !pagesObj.isNull("feed_content")) {
						objFeed.setFeedContent(Html.fromHtml(pagesObj.getString("feed_content")).toString());
					}
					
					if (pagesObj.has("comment_type_id")) {
						objFeed.setComment_type_id(pagesObj.getString("comment_type_id"));
					}
					
					if (pagesObj.has("feed_link")) {
						objFeed.setFeedLink(pagesObj.getString("feed_link"));
					}
					
					if (pagesObj.has("parent_module_id") && !pagesObj.isNull("parent_module_id")) {
						objFeed.setModule(pagesObj.getString("parent_module_id"));
					}
					
					if (pagesObj.has("enable_like")) {
						if(!pagesObj.isNull("feed_is_liked") && !"false".equals(pagesObj.getString("feed_is_liked"))){
							objFeed.setFeedIsLiked("feed_is_liked");
						}
						objFeed.setEnableLike(pagesObj.getBoolean("enable_like"));
					} else {
						objFeed.setEnableLike(false);
					}

					if (pagesObj.has("can_post_comment")) {
						objFeed.setCanPostComment(pagesObj.getBoolean("can_post_comment"));
					} else {
						objFeed.setCanPostComment(false);
					}
					
					if (pagesObj.has("total_comment")) {
						objFeed.setTotalComment(pagesObj.getString("total_comment"));
					}
					
					if (pagesObj.has("profile_page_id")) {
						objFeed.setProfile_page_id(pagesObj.getString("profile_page_id"));
					}
					
					if (pagesObj.has("feed_total_like")) {
						objFeed.setHasLike(pagesObj.getString("feed_total_like"));
						objFeed.setTotalLike(Integer.parseInt(pagesObj.getString("feed_total_like")));
					}
					
					if (pagesObj.has("feed_status_html")) {
						objFeed.setStatus(pagesObj.getString("feed_status_html"));
					}
					
					if (pagesObj.has("social_app")) {
						objFeed.setType(pagesObj.getJSONObject("social_app").getString("type_id"));	
					}
					
					if(!pagesObj.isNull(IMAGE)){
						Object photoImgObj = pagesObj.get(IMAGE);
						if (photoImgObj instanceof JSONArray) {
							for(int m = 0; m< pagesObj.getJSONArray(IMAGE).length(); m++) {
								if(m == 0) {
									objFeed.setImage1(pagesObj.getJSONArray(IMAGE).getString(m));
								} else if (m == 1) {
									objFeed.setImage2(pagesObj.getJSONArray(IMAGE).getString(m));
								} else if (m == 2) {
									objFeed.setImage3(pagesObj.getJSONArray(IMAGE).getString(m));
								} else if (m == 3) {
									objFeed.setImage4(pagesObj.getJSONArray(IMAGE).getString(m));
								}
							}
						}
					}
					
					if (pagesObj.has("photo_sizes")) {
						objFeed.setImage1(pagesObj.getJSONObject("photo_sizes").getString("500"));
					}
					
					if (pagesObj.has("report_module")) {
						objFeed.setReport_module(pagesObj.getString("report_module"));
					}
					
					if (pagesObj.has("report_phrase")) {
						objFeed.setReport_phrase(pagesObj.getString("report_phrase"));
					}
			} catch(Exception ex) {
				ex.printStackTrace();
				return null;
			}
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					//set image user;
					if (iv != null) {
						if (!"".equals(objFeed.getUserImage())) {
							iv.setVisibility(View.VISIBLE);
							networkUntil.drawImageUrl(iv, objFeed.getUserImage(), R.drawable.loading);
							iv.setOnClickListener(new View.OnClickListener() {
					            @Override
					            public void onClick(View v) {
					            	if (objFeed.getProfile_page_id() != null && !("0").equals(objFeed.getProfile_page_id())) {
					            		Intent intent = new Intent(getActivity(), FriendTabsPager.class);
						            	intent.putExtra("page_id", objFeed.getProfile_page_id());
						            	startActivity(intent);
					            	} else {
					            		Intent intent = new Intent(getActivity(), FriendTabsPager.class);
						            	intent.putExtra("user_id",objFeed.getUserId());
						            	startActivity(intent);
					            	}
					            }
					        });
						}
					}
					
					//set action report
					if (report_button != null) {
						
						//check if feed haven't report
						if (objFeed.getReport_phrase() == null) {
							report_button.setVisibility(View.GONE);
						}
						
						report_button.setOnClickListener(new View.OnClickListener() {
				            @Override
				            public void onClick(View v) {
				        			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				        			
				        			String sCancel = phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.cancel");
				        			
				    				final CharSequence[] items = { objFeed.getReport_phrase(), sCancel };
				    				builder.setItems(items, new DialogInterface.OnClickListener() {
				    					public void onClick(DialogInterface dialog, int which) {
				    						// The 'which' argument contains the index position
				    						// of the selected item
				    						if (which == 0) {
				    							Intent intent = new Intent(getActivity(), WebviewActivity.class);
				    							String reportUrl = Config.CORE_URL + "accountapi/report/" + "type_" + objFeed.getReport_module() + "/item_" + objFeed.getItemId() + "&loginToken=" + user.getTokenkey() + "&";
				    			            	intent.putExtra("html", reportUrl);
				    			            	startActivity(intent);
				    						}
				    					}
				    				});
				    			
				    			// Create the AlertDialog
				    			AlertDialog dialog = builder.create();
				    			dialog.show();
				            }
				        });
					}
		
					//set title;
					if (Title != null) {
						Title.setVisibility(View.VISIBLE);
						Title.setText(objFeed.getTitle());
                        colorView.changeColorText(Title, user.getColor());
					}
					//set feed time
					if (Time != null) {
						Time.setVisibility(View.VISIBLE);
						Time.setText(objFeed.getTime());
					}
					//set feed icon
					if (icon != null) {	
						if (!"".equals(objFeed.getIcon())) {
							icon.setVisibility(View.VISIBLE);
							networkUntil.drawImageUrl(icon, objFeed.getIcon(), R.drawable.loading);
						}
					}
					if (feedStatus != null && objFeed.getStatus() != null && !("null").equals(objFeed.getStatus())) {
						feedStatus.setVisibility(View.VISIBLE);
                        // interesting part starts from here here:
                        Html.ImageGetter ig = imageGetter.create(0, objFeed.getStatus(), feedStatus);
                        feedStatus.setTag(0);
                        feedStatus.setText(Html.fromHtml(objFeed.getStatus(), ig, null));
                    } else {
						feedStatus.setVisibility(View.GONE);
					}
					
					if (feedContent != null) {
						feedContent.setVisibility(View.VISIBLE);
						feedContent.setText(objFeed.getFeedContent());
					}
					
					//set_total_like
					
					if (total_like != null && objFeed.getHasLike() != null && objFeed.getEnableLike() != null && objFeed.getEnableLike() != false) {
						like.setVisibility(View.VISIBLE);
						if (objFeed.getFeedIsLiked() == null) {
							like.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
						} else {
							like.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
						}
						
				        like.setOnClickListener(new View.OnClickListener() {

				            @Override
				            public void onClick(View v) {
				                onListItemClick(objFeed.getFeedIsLiked());
				            }
				        });

						total_like.setVisibility(View.VISIBLE);
						like_icon.setVisibility(View.VISIBLE);
						total_like.setText(Integer.toString(objFeed.getTotalLike()));
						colorView.changeColorText(total_like, user.getColor());
						colorView.changeColorText(like, user.getColor());
					} else {
						total_like.setVisibility(View.GONE);
						like_icon.setVisibility(View.GONE);
						like.setVisibility(View.GONE);
					}
					
					//set_total_comment
					
					if (total_comment != null && objFeed.getTotalComment() != null) {
						comment.setVisibility(View.VISIBLE);
						total_comment.setVisibility(View.VISIBLE);
						tvMiddleDot.setVisibility(View.VISIBLE);
						total_comment.setText(objFeed.getTotalComment());
						comment.setOnClickListener(new View.OnClickListener() {
				            @Override
				            public void onClick(View v) {
				                //onCommentClick(position);
				            }
				        });
						colorView.changeColorText(total_comment, user.getColor());
						colorView.changeColorText(comment, user.getColor());
						colorView.changeColorText(tvMiddleDot, user.getColor());
					} else {
						comment.setVisibility(View.GONE);
						total_comment.setVisibility(View.GONE);
						tvMiddleDot.setVisibility(View.GONE);
					}
					
					if(("photo").equals(objFeed.getType()) || ("photo_comment").equals(objFeed.getType()) ){
						title_feed.setVisibility(View.GONE);
						feedimg_small.setVisibility(View.GONE);
						feedimg_small.setImageBitmap(null);
						if(objFeed.getImage1() != null){
							
			            	if(objFeed.getImage2() != null ){
			            		
			        			if(objFeed.getImage3() != null && objFeed.getImage4() == null){
			        				feedimg1.setVisibility(View.VISIBLE);
			        				feedimg2.setVisibility(View.VISIBLE);
			        				feedimg3.setVisibility(View.VISIBLE);
			        				feedimg4.setImageBitmap(null);
			        				feedimg4.setVisibility(View.GONE);
			        				feedimg.setImageBitmap(null);
			        				feedimg.setVisibility(View.GONE);
			        				networkUntil.drawImageUrl(feedimg1, objFeed.getImage1(), R.drawable.loading);
			        				networkUntil.drawImageUrl(feedimg2, objFeed.getImage2(), R.drawable.loading);
			        				networkUntil.drawImageUrl(feedimg3, objFeed.getImage3(), R.drawable.loading);
			        			}else if(objFeed.getImage4() != null){
			        				feedimg1.setVisibility(View.VISIBLE);
			        				feedimg2.setVisibility(View.VISIBLE);
			        				feedimg3.setVisibility(View.VISIBLE);	
			        				feedimg4.setVisibility(View.VISIBLE);
			        				feedimg.setImageBitmap(null);
			        				feedimg.setVisibility(View.GONE);
			        				
			        				networkUntil.drawImageUrl(feedimg1, objFeed.getImage1(), R.drawable.loading);
			        				networkUntil.drawImageUrl(feedimg2, objFeed.getImage2(), R.drawable.loading);
			        				networkUntil.drawImageUrl(feedimg3, objFeed.getImage3(), R.drawable.loading);
			        				networkUntil.drawImageUrl(feedimg4, objFeed.getImage4(), R.drawable.loading);
			        			}else{
			        				feedimg1.setVisibility(View.VISIBLE);
			        				feedimg2.setVisibility(View.VISIBLE);
			        				feedimg3.setImageBitmap(null);
			        				feedimg4.setImageBitmap(null);
			        				feedimg3.setVisibility(View.GONE);
			        				feedimg4.setVisibility(View.GONE);
			        				feedimg.setImageBitmap(null);
			        				feedimg.setVisibility(View.GONE);
			        				networkUntil.drawImageUrl(feedimg1, objFeed.getImage1(), R.drawable.loading);
			        				networkUntil.drawImageUrl(feedimg2, objFeed.getImage2(), R.drawable.loading);				
			        			}
			        			
			            	}else{
			            		feedimg.setVisibility(View.VISIBLE);
			            		feedimg1.setVisibility(View.GONE);
			            		feedimg2.setVisibility(View.GONE);
			            		feedimg3.setVisibility(View.GONE);	
			            		feedimg4.setVisibility(View.GONE);
			            		feedimg1.setImageBitmap(null);
			            		feedimg2.setImageBitmap(null);
			            		feedimg3.setImageBitmap(null);
			    				feedimg4.setImageBitmap(null);
			            		
			    				networkUntil.drawImageUrl(feedimg, objFeed.getImage1(), R.drawable.loading);			
			            	}
			            	
			            }else{
			            	feedimg.setImageBitmap(null);
			            	feedimg.setVisibility(View.GONE);
			            }
					}else{
						feedimg.setVisibility(View.GONE);
						feedimg1.setVisibility(View.GONE);
	            		feedimg2.setVisibility(View.GONE);
	            		feedimg3.setVisibility(View.GONE);	
	            		feedimg4.setVisibility(View.GONE);
	            		feedimg1.setImageBitmap(null);
	            		feedimg2.setImageBitmap(null);
	            		feedimg3.setImageBitmap(null);
	    				feedimg4.setImageBitmap(null);
	    				feedimg.setImageBitmap(null);
	    				
	    				
	    				if (title_feed != null && objFeed.getTitleFeed() != null && !("").equals(objFeed.getTitleFeed())) {
	    					title_feed.setVisibility(View.VISIBLE);
	    					title_feed.setText(objFeed.getTitleFeed());
	    					
	    					title_feed.setOnClickListener(new View.OnClickListener() {
	    						public void onClick(View v) {
	    							if (objFeed.getPage_id_request() != null) { 
					            		Intent intent = new Intent(getActivity(), FriendTabsPager.class);
						            	intent.putExtra("page_id",objFeed.getPage_id_request());
						            	startActivity(intent);
					            	} else if(objFeed.getUser_id_request() != null) {
					            		Intent intent = new Intent(getActivity(), FriendTabsPager.class);
						            	intent.putExtra("user_id",objFeed.getUser_id_request());
						            	startActivity(intent);
					            	}else{
					            		if (getActivity() == null)
		    								return;
		    							DashboardActivity activity = (DashboardActivity) getActivity();
		    							activity.onWebHtml(objFeed.getFeedLink());
					            	}
	    						}
	    					});
	    				}else{
	    					title_feed.setVisibility(View.GONE);
	    				}
	    				
	    				if (feedimg_small != null) {
	    					if (objFeed.getImage1() != null && !"".equals(objFeed.getUserImage())) {
	    						feedimg_small.setVisibility(View.VISIBLE);
	    						networkUntil.drawImageUrl(feedimg_small, objFeed.getImage1(), R.drawable.loading);
	    						feedimg_small.setOnClickListener(new View.OnClickListener() {
						            @Override
						            public void onClick(View v) {
						            	if (objFeed.getPage_id_request() != null) {
						            		Intent intent = new Intent(getActivity(), FriendTabsPager.class);
							            	intent.putExtra("page_id",objFeed.getPage_id_request());
							            	startActivity(intent);
						            	} else if (objFeed.getUser_id_request() != null) {
						            		Intent intent = new Intent(getActivity(), FriendTabsPager.class);
							            	intent.putExtra("user_id",objFeed.getUser_id_request());
							            	startActivity(intent);
						            	} else {
						            		if (getActivity() == null)
			    								return;
			    							DashboardActivity activity = (DashboardActivity) getActivity();
			    							activity.onWebHtml(objFeed.getFeedLink());
						            	}
						            	
						            }
						        });
	    						
	    					}else{
	        					feedimg_small.setVisibility(View.GONE);
	        					feedimg_small.setImageBitmap(null);
	    					}
	    				}
					}
					
					if(Integer.parseInt(objFeed.getTotalComment()) > 10){
						item_viewmore_icon.setVisibility(View.VISIBLE);
						view_more.setVisibility(View.VISIBLE);
						view_more.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "comment.view_previous_comments"));
						colorView.changeColorText(view_more, user.getColor());
						
						view_more.setOnClickListener(new View.OnClickListener() {

				            @Override
				            public void onClick(View v) {
				            	String more = "1";
				            	page++;
				            	if (itemModuleId.equals("")) {
				            		new Showcomment().execute(objFeed.getComment_type_id(), itemId, objFeed.getFeedId(), objFeed.getTotalComment(), more);
				            	} else {
				            		new Showcomment().execute(objFeed.getComment_type_id(), itemModuleId, objFeed.getFeedId(), objFeed.getTotalComment(), more);
				            	}
				            	
				            }
				        });
					}else{
						item_viewmore_icon.setVisibility(View.GONE);
						view_more.setVisibility(View.GONE);
					}
					
					// post button on click
					post_comment.setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View v) {
			            	if(text_comment.getText().toString().trim().length() > 0){
			            		new Postcomment().execute(objFeed.getComment_type_id(), objFeed.getItemId(), objFeed.getFeedId(), text_comment.getText().toString());
				            	text_comment.setText("");
			            	}      	
			            }
			        });
					if (itemModuleId.equals("")) {
	            		new Showcomment().execute(objFeed.getComment_type_id(), itemId, objFeed.getFeedId(), objFeed.getTotalComment());
	            	} else {
	            		new Showcomment().execute(objFeed.getComment_type_id(), itemModuleId, objFeed.getFeedId(), objFeed.getTotalComment());
	            	}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}	
		}		
		
		//action like
		protected void onListItemClick(String liked) {
	        if (liked == null) 
	        {
	        	like.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.unlike"));
	        	objFeed.setTotalLike(objFeed.getTotalLike() + 1);
	        	total_like.setText(String.valueOf(objFeed.getTotalLike()));
	        	new likeFeed().execute(itemId, typeId, objFeed.getFeedId(), "like");
	        	objFeed.setFeedIsLiked("1");
	        } else {
	        	like.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "feed.like"));
	        	if (objFeed.getTotalLike() > 0)
	        		objFeed.setTotalLike(objFeed.getTotalLike() - 1);
	        	
	        	total_like.setText(String.valueOf(objFeed.getTotalLike()));
	        	new likeFeed().execute(itemId, typeId, objFeed.getFeedId(), "unlike");
	        	objFeed.setFeedIsLiked(null);
	        }
			
	    }
		
		/**
		 * Action like feed
		 */
		public class likeFeed extends AsyncTask<String, Void, String> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}
			
			@Override
			protected String doInBackground(String... params) {
				
				if (isCancelled()) {
					return null;
				}
				
				try {					
					// Use BasicNameValuePair to create GET data
					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
					
					if(("like").equals(params[3])){
						pairs.add(new BasicNameValuePair("method", "accountapi.like"));
					} else {
						pairs.add(new BasicNameValuePair("method", "accountapi.unlike"));
					}
					
					if (params[1] != null) {
						pairs.add(new BasicNameValuePair("type", "" + params[1]));
						pairs.add(new BasicNameValuePair("item_id", "" + params[0]));
					} else {
						pairs.add(new BasicNameValuePair("feed_id", "" + params[2]));
					}
					
					// url request
					String URL = Config.CORE_URL + Config.URL_API;
					// request GET method to server
					
					networkUntil.makeHttpRequest(URL, "GET", pairs);
				} catch(Exception ex) {
					//Log.i(DEBUG_TAG, ex.getMessage());
				}
				return null;
			}
		}
	}
	
	/**
	 * Change color
	 * @param colorCode
	 */
	private void changeColor(Button btnSend, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			btnSend.setBackgroundResource(R.drawable.brown_comment_post_icon);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			btnSend.setBackgroundResource(R.drawable.pink_comment_post_icon);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			btnSend.setBackgroundResource(R.drawable.green_comment_post_icon);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			btnSend.setBackgroundResource(R.drawable.violet_comment_post_icon);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			btnSend.setBackgroundResource(R.drawable.red_comment_post_icon);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			btnSend.setBackgroundResource(R.drawable.dark_violet_comment_post_icon);
		} else {
			btnSend.setBackgroundResource(R.drawable.comment_post_icon);
		}
	}
	
	//show comments
	public class Showcomment extends AsyncTask<String, Void, String> {
		
		String resultstring = null;
		JSONObject mainJSON = null;
		JSONArray outJson = null;
		JSONObject total = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			if (isCancelled()) {
				return null;
			}
			
			try {
				// Use BasicNameValuePair to create GET data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
				pairs.add(new BasicNameValuePair("method", "accountapi.getFeedComments"));
				if (params[1] != null) {
					pairs.add(new BasicNameValuePair("type", "" + params[0]));
					pairs.add(new BasicNameValuePair("item_id", "" + params[1]));
				} else {
					pairs.add(new BasicNameValuePair("feed_id", "" + params[2]));
				}
				
				if (params[3] != null) {
					pairs.add(new BasicNameValuePair("total", "" + params[3]));
				}
				pairs.add(new BasicNameValuePair("page", "" + page));
				// url request
				String URL = Config.makeUrl(user.getCoreUrl(), null, false);
				// request GET method to server
				
				resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
				
				
				JSONObject mainJSON = new JSONObject(resultstring);
				JSONArray outJson = mainJSON.getJSONArray("output");
				
				JSONObject pagesObj = null;
				Comment objComment = null;
				arrayOfList = new ArrayList<Comment>();
				for (int i = 0; i < outJson.length(); i++) {
					pagesObj = outJson.getJSONObject(i);
						
					objComment = new Comment();
					if (pagesObj.has("comment_id")) {
						objComment.setCommentId(Integer.parseInt(pagesObj.getString("comment_id")));
					}
					if (pagesObj.has("user_id")) {
						objComment.setUserId(pagesObj.getString("user_id"));
					}
					if (pagesObj.has("full_name")) {
						objComment.setFullname(pagesObj.getString("full_name"));
					}
					if (pagesObj.has(TIME)) {
						objComment.setTimePhrase(pagesObj.getString(TIME));
					}
					if (pagesObj.has("text_html")) {
						objComment.setText(pagesObj.getString("text_html"));
					}
					if (pagesObj.has(USERIMAGE)) {
						objComment.setUserImage(pagesObj.getString(USERIMAGE));
					}
					if (pagesObj.has("is_liked") && !pagesObj.isNull("is_liked")) {
						objComment.setLiked(true);
					} else {
						objComment.setLiked(false);
					}
					if (pagesObj.has("total_like")) {
						objComment.setTotalLike(Integer.parseInt(pagesObj.getString("total_like")));
					}
					arrayOfList.add(objComment);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				    View view;
				   
				    for (int i = 0; i < arrayOfList.size() ; i++){
					    final Comment item = arrayOfList.get(i); 
				    	view = layoutInflater.inflate(R.layout.comment_row, scroll_comment, false);
			            // In order to get the view we have to use the new view with text_layout in it
				    	RelativeLayout comment_item = (RelativeLayout) view.findViewById(R.id.comment_item);
				    	
				    	ImageView user_image = (ImageView) view.findViewById(R.id.item_img);
				    	TextView full_name = (TextView) view.findViewById(R.id.item_fullname);
				    	TextView time_phrase = (TextView) view.findViewById(R.id.item_time);
				    	TextView text = (TextView) view.findViewById(R.id.item_text);
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
				    	
				    	networkUntil.drawImageUrl(user_image, item.getUserImage(), R.drawable.loading);
				    	full_name.setText(item.getFullname());
				    	time_phrase.setText(item.getTimePhrase());

                        // interesting part starts from here here:
                        Html.ImageGetter ig = imageGetter.create(0, item.getText(), text);
                        text.setTag(0);
                        text.setText(Html.fromHtml(item.getText(), ig, null));

                        if (item.isLiked()) {
				    		likeComment.setText(phraseManager.getPhrase(getActivity(), "feed.unlike"));
				    	} else {
				    		likeComment.setText(phraseManager.getPhrase(getActivity(), "feed.like"));
				    	}
				    	
						if (item.getTotalLike() > 0) {
							commentLikeLayout.setVisibility(View.VISIBLE);
							commentTotal.setText(String.valueOf(item.getTotalLike()));
						} else {
							commentLikeLayout.setVisibility(View.GONE);
						}
				    	
			            // Add the text view to the parent layout
				    	scroll_comment.addView(comment_item, 4 + i);
				    	
				    	//action click like comment
				    	likeComment.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								likeComment(item, likeComment, commentLikeLayout, commentTotal);
							}
						});
				    	
				    	//action view liked comment
				    	commentLikeLayout.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(getActivity(), FriendActivity.class);
								intent.putExtra("type", "feed_mini");
								intent.putExtra("item_id", String.valueOf(item.getCommentId()));
								intent.putExtra("total_like", item.getTotalLike());
								getActivity().startActivity(intent);
							}
						});
				    	
				    	user_image.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(getActivity(), FriendTabsPager.class);
								intent.putExtra("user_id", item.getUserId());
								getActivity().startActivity(intent);
							}
						});
				    }   
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

    /**
     * like comment
     * @param item
     * @param likeComment
     * @param commentLikeLayout
     * @param totalLike
     */
	private void likeComment(Comment item, TextView likeComment, LinearLayout commentLikeLayout, TextView totalLike) {
		if (item.isLiked()) {
			likeComment.setText(phraseManager.getPhrase(getActivity(), "feed.like"));
			new LikeAsyncTask(getActivity().getApplicationContext()).execute(user.getTokenkey(), String.valueOf(item.getCommentId()), "feed_mini",null, "unlike");
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
			likeComment.setText(phraseManager.getPhrase(getActivity(), "feed.unlike"));
			new LikeAsyncTask(getActivity().getApplicationContext()).execute(user.getTokenkey(), String.valueOf(item.getCommentId()), "feed_mini", null, "like");
			item.setLiked(true);
			commentLikeLayout.setVisibility(View.VISIBLE);
			item.setTotalLike(item.getTotalLike() + 1);
			totalLike.setText(String.valueOf(item.getTotalLike()));
		}
	}
	
	/**
	 * Post comment
	 */
	public class Postcomment extends AsyncTask<String, Void, String> {
	
		String resultstring = null;
		private ProgressDialog mProgressDialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setMessage(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.posting"));
			mProgressDialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			if (isCancelled()) {
				return null;
			}
			
			try {
				// Use BasicNameValuePair to create GET data
				String URL;
				
				if (params[1] != null) {
					 URL = Config.makeUrl(user.getCoreUrl(), "comment", true) + "&token=" + user.getTokenkey() + "&type=" + params[0] + "&item_id=" + params[1];
					 if ("custom_relation".equals(params[0])) {
						 URL += "&via_feed=" + params[2];
					 }
				} else {
					 URL = Config.makeUrl(user.getCoreUrl(), "comment", true) + "&token=" + user.getTokenkey() +  "&feed_id=" + params[2];
				}
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("comment", params[3]));
				
				// url request
				
				// request GET method to server
				resultstring = networkUntil.makeHttpRequest(URL, "POST", pairs);
				
				JSONObject mainJSON = new JSONObject(resultstring);
				JSONObject pagesObj = mainJSON.getJSONObject("output");
						
				OneComment = new Comment();
				if (pagesObj.has("comment_id")) {
					OneComment.setCommentId(Integer.parseInt(pagesObj.getString("comment_id")));
				}
				if (pagesObj.has("user_id")) {
					OneComment.setUserId(pagesObj.getString("user_id"));
				}
				if (pagesObj.has("full_name")) {
					OneComment.setFullname(pagesObj.getString("full_name"));
				}
				if (pagesObj.has(TIME)) {
					OneComment.setTimePhrase(pagesObj.getString(TIME));
				}
				if (pagesObj.has("text")) {
					OneComment.setText(pagesObj.getString("text"));
				}
				if (pagesObj.has(USERIMAGE)) {
					OneComment.setUserImage(pagesObj.getString(USERIMAGE));
				}
				if (pagesObj.has("is_liked") && !pagesObj.isNull("is_liked")) {
					OneComment.setLiked(true);
				} else {
					OneComment.setLiked(false);
				}		 
			} catch(Exception ex) {
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();
			}
		
			return resultstring;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					LayoutInflater layoutInflater = getActivity().getLayoutInflater();
				    View view;
				    
		            // Add the text layout to the parent layout
			    	view = layoutInflater.inflate(R.layout.comment_row, scroll_comment, false);
		 
		            // In order to get the view we have to use the new view with text_layout in it
			    	RelativeLayout comment_item = (RelativeLayout)view.findViewById(R.id.comment_item);
			    	
			    	ImageView user_image = (ImageView)view.findViewById(R.id.item_img);
			    	TextView full_name = (TextView)view.findViewById(R.id.item_fullname);
			    	TextView time_phrase = (TextView)view.findViewById(R.id.item_time);
                    TextView text = (TextView)view.findViewById(R.id.item_text);
			    	final TextView likeComment = (TextView) view.findViewById(R.id.item_comment_like);
			    	final TextView commentTotal = (TextView) view.findViewById(R.id.item_comment_total_like);
			    	final LinearLayout commentLikeLayout = (LinearLayout) view.findViewById(R.id.comment_like_layout);
			    	
			    	//change color
			    	colorView.changeColorText(full_name, user.getColor());
			    	colorView.changeColorText(likeComment, user.getColor());
			    	
			    	networkUntil.drawImageUrl(user_image, OneComment.getUserImage(), R.drawable.loading);
			    	full_name.setText(OneComment.getFullname());
			    	
			    	time_phrase.setText(OneComment.getTimePhrase());

                    // interesting part starts from here here:
                    Html.ImageGetter ig = imageGetter.create(0, OneComment.getText(), text);
                    text.setTag(0);
                    text.setText(Html.fromHtml(OneComment.getText(), ig, null));

                    if (OneComment.isLiked()) {
			    		likeComment.setText(phraseManager.getPhrase(getActivity(), "feed.unlike"));
			    	} else {
			    		likeComment.setText(phraseManager.getPhrase(getActivity(), "feed.like"));
			    	}
			    	mProgressDialog.dismiss();
		            // Add the text view to the parent layout
			    	scroll_comment.addView(comment_item);
			    	
			    	scrollview_comment.post(new Runnable() {
			    	     public void run() {
			    	    	 scrollview_comment.fullScroll(View.FOCUS_DOWN);
			    	     }
			    	 });
			    	
			    	//action click like comment
			    	likeComment.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							likeComment(OneComment, likeComment, commentLikeLayout, commentTotal);
						}
					});
			    	
			    	user_image.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(), FriendTabsPager.class);
							intent.putExtra("user_id", OneComment.getUserId());
							getActivity().startActivity(intent);
						}
					});
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

	}
	
}
