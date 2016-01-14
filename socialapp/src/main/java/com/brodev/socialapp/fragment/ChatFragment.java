package com.brodev.socialapp.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.brodev.chat.gifview.GIFView;
import com.brodev.chat.gifview.GifDecoderThread;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.cache.CacheManager;
import com.brodev.socialapp.cache.CacheManager.ReadableCacheFile;
import com.brodev.socialapp.cache.CacheRequest;
import com.brodev.socialapp.cache.General;
import com.brodev.socialapp.cache.RequestFailureType;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.customview.html.ImageGetter;
import com.brodev.socialapp.entity.ChatMessage;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@SuppressLint({ "SimpleDateFormat", "NewApi" })
public class ChatFragment extends SherlockListFragment {

	private User user;
	private String userId, imageUser, lastId, midId;
	private EditText messageEdt;
	private Button sendMessageBtn;
	private NetworkUntil networkUntil = new NetworkUntil();
	private int page;
	private ChatAdapter ca;
	private LinearLayout typingView;
	private RelativeLayout actionView;
	private TextView seenText;
	private ImageView seenImg, stickerImg, gifImageView;
	private Timer tTimer = new Timer();
	private PhraseManager phraseManager;
	private StringBuffer sb;
	private Calendar cal = Calendar.getInstance();
	private DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
	private View view;
	private boolean showSticker;
	private PullToRefreshListView mPullRefreshListView;
	private ListView actualListView;
	private GifDecoderThread gifThread;
	private ProgressBar progressBar;
	private InputMethodManager imm;
	private Display display;
	private int sizeDisplay;

    private ImageGetter imageGetter;
	
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
	
	/**
	 * Change background text
	 * @param textView
	 * @param colorCode
	 */
	private void changeBackgroundText(TextView textView, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.brown_bubble);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.pink_bubble);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.green_bubble);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.violet_bubble);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.red_bubble);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			textView.setBackgroundResource(R.drawable.dark_violet_bubble);
		} else {
			textView.setBackgroundResource(R.drawable.bubble);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// create view from forum_fragment xml
		view = inflater.inflate(R.layout.chat_fragment, container, false);

		messageEdt = (EditText) view.findViewById(R.id.write_message);
		sendMessageBtn = (Button) view.findViewById(R.id.send_email);
		typingView = (LinearLayout) view.findViewById(R.id.typingView);
		actionView = (RelativeLayout) view.findViewById(R.id.actionView);
		seenText = (TextView) view.findViewById(R.id.seenView);
		seenImg = (ImageView) view.findViewById(R.id.seenImg);
		stickerImg = (ImageView) view.findViewById(R.id.sticker);
		progressBar = (ProgressBar) view.findViewById(R.id.chat_progress_bar);
		
		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.chat_list);
		
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new GetLogChatAsyncTask().execute(user.getChatServerUrl(), user.getChatKey(), user.getUserId(), userId, String.valueOf(page), String.valueOf(10), midId);
			}
		});

		return view;
	}
	
	public void stopPullList() {
		mPullRefreshListView.onRefreshComplete();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		changeColor(user.getColor());
		
		actualListView = mPullRefreshListView.getRefreshableView();
		
		// get chat log
		if (ca == null) {
			ca = new ChatAdapter(getActivity().getApplicationContext());
		}

		new GetLogChatAsyncTask().execute(user.getChatServerUrl(), user.getChatKey(), user.getUserId(), userId, String.valueOf(page), String.valueOf(10), midId);
		
		getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_CHAT_ACTION));
		getActivity().registerReceiver(mHandleStickerReceiver, new IntentFilter(Config.DISPLAY_SHOW_STICKER_ACTION));
		
		messageEdt.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.write_a_message"));
		sendMessageBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.send"));
		
		messageEdt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() != 0) {
					tTimer.cancel();
					tTimer = new Timer();
					tTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							displayMessage(getActivity().getApplicationContext(), "composing", userId, null, null, null);		
						}
					}, 500);
				}
			}
		});
		
		//request is seen
		messageEdt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				 try {
					 showSticker = false;
					 displaySticker(getActivity().getApplicationContext(), showSticker);
					 
					 //request read
					 if (lastId != null && !lastId.equals(user.getUserId())) {
						 displayMessage(getActivity().getApplicationContext(), "read", userId, null, null, null);
					 }
				 } catch (Exception ex) {
					 ex.printStackTrace();
				 }
			}
		});
		
		//send message action
		sendMessageBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (messageEdt.getText().toString().trim().length() > 0) {
					try {
						displayMessage(getActivity().getApplicationContext(), "send", userId, messageEdt.getText().toString().trim(), user.getUserImage(), user.getFullname());
						
						seenText.setVisibility(View.GONE);
						seenImg.setVisibility(View.GONE);
						actionView.setVisibility(View.GONE);
						//add chat
						addChat(userId, messageEdt.getText().toString().trim());
						
						messageEdt.setText("");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
	
		//final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		stickerImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (showSticker == false)
					showSticker = true;
				
				imm.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
				
				displaySticker(getActivity().getApplicationContext(), showSticker);
				getListView().setSelection(ca.getCount() - 1);
				showSticker = !showSticker;
			}
		});
		
		super.onActivityCreated(savedInstanceState);
	}


    /**
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     * @param context
     * @param bSticker
     */
	public static void displaySticker(Context context, boolean bSticker) {
        Intent intent = new Intent(Config.DISPLAY_CHAT_STICKER_ACTION);
        
        intent.putExtra("show_sticker", bSticker);
        
        context.sendBroadcast(intent);
    }
	
	/**
	 * Add chat
	 * 
	 * @param userId
	 * @param message
	 */
	public void addChat(String userId, String message) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setToUserId(userId);
		chatMessage.setFromUserId(user.getUserId());
		lastId = user.getUserId();
		chatMessage.setLogged(true);
		chatMessage.setMessage(message);

		ca.add(chatMessage);

		setListAdapter(ca);
		ca.notifyDataSetChanged();
		getListView().setSelection(ca.getCount() - 1);
	}
	
	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param message
	 */
	public static void displayMessage(Context context, String type, String userId, String message, String image, String fullname) {
        Intent intent = new Intent(Config.DISPLAY_CHAT_ACTION);
        intent.putExtra("type", type);
        
        intent.putExtra("userId", userId);
        intent.putExtra("message", message);
        intent.putExtra("image", image);
        intent.putExtra("fullname", fullname);
        
        context.sendBroadcast(intent);
    }
	
	/**
	 * Class request get log chat
	 */
	public class GetLogChatAsyncTask extends AsyncTask<String, Void, String> {
		
		@Override
		protected String doInBackground(String... params) {
			String result = null;
			
			if (isCancelled()) {
				return null;
			}
			
			try { 
				String registerUrl = params[0] + "/" + params[1] + Config.CHAT_LOG;
				
				if (!params[0].startsWith("http://"));
					registerUrl = "http://" + registerUrl;
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();

				pairs.add(new BasicNameValuePair("from", params[2]));
				pairs.add(new BasicNameValuePair("to", params[3]));
				pairs.add(new BasicNameValuePair("page", params[4]));
				pairs.add(new BasicNameValuePair("size", params[5])); //default size = 10
				
				if (params[6] != null) {
					pairs.add(new BasicNameValuePair("mid", params[6])); 
				}
				
				result = networkUntil.makeHttpRequest(registerUrl, "GET", pairs);
				
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			getChatLog(view, result);
			super.onPostExecute(result);
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param isRead
	 */
	public static void displayMessageRead(Context context, boolean isRead) {
        Intent intent = new Intent(Config.DISPLAY_CHAT_ACTION_READ);
        intent.putExtra("read", true);
        
        context.sendBroadcast(intent);
    }
	
	/**
	 * Get chat log
	 */
	private void getChatLog(View view, String result) {
		try {
			if (result != null) {
				
				JSONObject logJson = new JSONObject(result);
				
				lastId = logJson.getString("lastId");
				
				if (logJson.has("isRead") && !logJson.isNull("isRead") && Integer.parseInt(logJson.getString("isRead")) == 0) {
					 displayMessage(getActivity().getApplicationContext(), "read", userId, null, null, null);
					 displayMessageRead(getActivity(), true);
				}
					
				if (logJson.has("isBlock") && !logJson.isNull("isBlock") && logJson.getBoolean("isBlock")) {
					view.findViewById(R.id.send_chat_view).setVisibility(View.GONE);
					actionView.setVisibility(View.VISIBLE);
					seenText.setVisibility(View.VISIBLE);
					seenText.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.contact_is_blocked"));
					
					seenImg.setVisibility(View.VISIBLE);
					seenImg.setImageResource(R.drawable.block);
				}
					
				JSONArray output = logJson.getJSONArray("logs");
				
				ChatMessage chatMessage = null;
				JSONObject messJson = null;
				
				for (int i = 0; i < output.length(); i++) {
					chatMessage = new ChatMessage();
					messJson = output.getJSONObject(i);
					//set id
					if (i == 0)
						midId = messJson.getString("id");
					
					chatMessage.setId(messJson.getString("id"));
					//set from user id
					chatMessage.setFromUserId(messJson.getString("from"));
					
					//set to user id
					chatMessage.setToUserId(messJson.getString("to"));
					
					if (user.getUserId().equals(messJson.getString("from"))) 
						chatMessage.setLogged(true);
					else 
						chatMessage.setImage(imageUser);
					
					//set text
					if (messJson.has("text") && !messJson.isNull("text")) {
						chatMessage.setMessage(messJson.getString("text"));
					}
					
					//set time
					if (messJson.has("time") && !messJson.isNull("time"))
						chatMessage.setTime(convertTime(messJson.getLong("time")));
					
					//set chat type
					if (messJson.has("type") && !messJson.isNull("type"))
						chatMessage.setType(messJson.getString("type"));
					
					//set sticker
					if (messJson.has("sticker") && !messJson.isNull("sticker")) {
						chatMessage.setSticker(messJson.getJSONObject("sticker").getString("url"));
						
						if (messJson.getJSONObject("sticker").has("height")) {
							chatMessage.setStickerHeight(messJson.getJSONObject("sticker").getInt("height"));
						} else {
							//default 
							chatMessage.setStickerHeight(60);
						}
						
						if (messJson.getJSONObject("sticker").has("width")) {
							chatMessage.setStickerWidth(messJson.getJSONObject("sticker").getInt("width"));
						} else {
							//default
							chatMessage.setStickerWidth(60);
						} 
						
						if (messJson.getJSONObject("sticker").has("type")) {
							chatMessage.setStickerType(messJson.getJSONObject("sticker").getString("type"));
						}
					}
					ca.insert(chatMessage, i);
				}
				//set list adapter
				if (ca != null) {
					actualListView.setAdapter(ca);
				}
			}
			ca.notifyDataSetChanged();
			stopPullList();
			progressBar.setVisibility(View.GONE);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Receiving message
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String type = intent.getExtras().getString("type");
			String params = intent.getExtras().getString("params");
			
			changeData(type, params);	
		}
	};
	
	/**
	 * Receiving message
	 */
	private final BroadcastReceiver mHandleStickerReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String type = intent.getExtras().getString("type");
			String params = intent.getExtras().getString("stickerUrl");
			int stickerHeight = intent.getExtras().getInt("stickerHeight");
			int stickerWidth = intent.getExtras().getInt("stickerWidth");
			String stickerType = intent.getExtras().getString("sticker_type");
			
			if (type.equals("send_sticker")) {
				ChatMessage chatMessage = new ChatMessage();
				chatMessage.setSticker(params);
				chatMessage.setLogged(true);
				chatMessage.setStickerHeight(stickerHeight);
				chatMessage.setStickerWidth(stickerWidth);
				chatMessage.setStickerType(stickerType);
				
				ca.add(chatMessage);
				setListAdapter(ca);
				ca.notifyDataSetChanged();
			}
		}
	};
	
	/**
	 * 
	 * @param type
	 * @param params
	 */
	public void changeData(String type, String params) {
		try {
			
			ChatMessage chatMessage = null;
			JSONObject json = null;
			
			if (params != null) {
				json = new JSONObject(params);	
				
				if (type.equals("chat") && json.getString("userId").equals(userId)) {
					chatMessage = new ChatMessage();
					setData(chatMessage, json, Html.fromHtml(json.getString("message")).toString(), null, false);
					
				} else if (type.equals("composing") && !json.getString("userId").equals(user.getUserId()) && json.getString("userId").equals(userId)) {
					setTypingState(view, false, false);
					composingTimer(7000);
				} else if (type.equals("read") && !json.getString("userId").equals(user.getUserId()) && json.getString("userId").equals(userId)) {
					actionView.setVisibility(View.VISIBLE);
					sb = new StringBuffer(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.seen"));
					seenText.setText(sb.toString());

					sb.append(" " + dateFormat.format(cal.getTime()));
					seenText.setText(sb.toString());
					seenText.setVisibility(View.VISIBLE);
					seenImg.setVisibility(View.VISIBLE);
				} else if (type.equals("sticker") && json.getString("userId").equals(userId)) {
					chatMessage = new ChatMessage();
					setData(chatMessage, json, null, json.getJSONObject("sticker").getString("url"), false);
				} 

				getListView().setSelection(ca.getCount() - 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setData(ChatMessage chatMessage, JSONObject json , String message, String stickerUrl, boolean isLogged) {
		try {
			chatMessage.setToUserId(json.getString("userId"));
			lastId = json.getString("userId");
			chatMessage.setFromUserId(user.getUserId());
			
			if (stickerUrl != null)
				chatMessage.setSticker(stickerUrl);
			
			if (message != null)
				chatMessage.setMessage(message);
			
			chatMessage.setImage(imageUser);
			chatMessage.setLogged(isLogged);
			
			if (json.has("sticker") && json.getJSONObject("sticker").has("width")) {
				chatMessage.setStickerWidth(json.getJSONObject("sticker").getInt("width"));
			} else {
				chatMessage.setStickerWidth(60);
			}
			
			if (json.has("sticker") && json.getJSONObject("sticker").has("height")) {
				chatMessage.setStickerHeight(json.getJSONObject("sticker").getInt("height"));
			} else {
				chatMessage.setStickerHeight(60);
			}
			
			//set sticker type
			if (json.has("sticker") && json.getJSONObject("sticker").has("type")) {
				chatMessage.setStickerType(json.getJSONObject("sticker").getString("type"));
			}
			
			setTypingState(view, false, true);
			
			ca.add(chatMessage);
			setListAdapter(ca);
			ca.notifyDataSetChanged();
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setTypingState(View view, boolean setVisible, boolean hVisible)
    {
		if (typingView == null)
            return;
		
		if (hVisible == true) {
			actionView.setVisibility(View.GONE);
			typingView.setVisibility(View.GONE);
			return;
		}
		
		ImageView typingUser = (ImageView) view.findViewById(R.id.typingTextView);	
		ImageView typingImgView = (ImageView) view.findViewById(R.id.typingAction);

        Drawable typingDrawable = typingImgView.getDrawable();
        
        if (!(typingDrawable instanceof AnimationDrawable))
        {
            typingImgView.setImageResource(R.drawable.typing_drawable);
            typingDrawable = typingImgView.getDrawable();
        }
        
        if(!((AnimationDrawable) typingDrawable).isRunning())
        {
            AnimationDrawable animatedDrawable = (AnimationDrawable) typingDrawable;
            animatedDrawable.setOneShot(false);
            animatedDrawable.start();
        }
        
        networkUntil.drawImageUrl(typingUser, imageUser, R.drawable.loading);
        
        setVisible = true;
        
        if (setVisible) {
            typingImgView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            typingImgView.setPadding(7, 0, 7, 7);
            actionView.setVisibility(View.VISIBLE);
            typingView.setVisibility(View.VISIBLE);
            seenText.setVisibility(View.GONE);
            seenImg.setVisibility(View.GONE);
        } else {
        	actionView.setVisibility(View.GONE);
        	typingView.setVisibility(View.GONE);
        }
            
	}

    /**
     * composing with time
     * @param delay
     */
	private void composingTimer(int delay) {
		Handler handler = new Handler(); 
	    handler.postDelayed(new Runnable() { 
	         public void run() { 
	        	 setTypingState(view, false, true);
	         } 
	    }, delay); 
		
	}

	@Override
	public void onDestroy() {
		try {
			getActivity().unregisterReceiver(mHandleMessageReceiver);
			getActivity().unregisterReceiver(mHandleStickerReceiver);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		user = (User) getActivity().getApplicationContext();
		// phrase manager
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		// init value
		userId = null;
		imageUser = null;
		lastId = null;
		page = 1;
		showSticker = true;
		midId = null;
		display = getActivity().getWindowManager().getDefaultDisplay();
		sizeDisplay = (int) Math.ceil((display.getWidth() * 2) / 5);
		
		// get data
		if (getArguments() != null) {
			userId = getArguments().getString("user_id");
			imageUser = getArguments().getString("image");
		}
		
		imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        this.imageGetter = new ImageGetter(getActivity().getApplicationContext());
		super.onCreate(savedInstanceState);
	}

	/**
	 * Check sticker size
	 * @param sizeSticker
	 * @return 
	 */
	public boolean checkSize(int sizeSticker) {
		if (sizeSticker > sizeDisplay)
			return false;

		return true;
	}
	
	/**
	 * chat adapter class
	 */
	public class ChatAdapter extends ArrayAdapter<ChatMessage> {
		
		public ChatAdapter(Context context) {
			super(context, 0);
		}
		
		@Override
		public int getItemViewType(int position) {
			return getItem(position).isLogged() ? 0 : 1;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public boolean isEnabled(int position) {
			return !getItem(position).isLogged();
		}
		
		@Override
		public void remove(ChatMessage object) {
			// TODO Auto-generated method stub
			super.remove(object);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ChatMessage item = getItem(position);
			ChatViewHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.conversation_list_row;
				
				if (item.isLogged()) 
					layout = R.layout.conversation_list_row_right;
				
				view = LayoutInflater.from(getContext()).inflate(layout, null);
				// call element from xml
				ImageView icon = (ImageView) view.findViewById(R.id.image_conversation);
                TextView content = (TextView) view.findViewById(R.id.content_conversation);
				TextView time = (TextView) view.findViewById(R.id.time_conversation);
				ImageView sticker = (ImageView) view.findViewById(R.id.sticker_conversation);
				LinearLayout gifView = (LinearLayout) view.findViewById(R.id.gifView);
				
				view.setTag(new ChatViewHolder(icon, content, time, sticker, gifView));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof ChatViewHolder) {
					holder = (ChatViewHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				
				view.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						 showSticker = false;
						 displaySticker(getActivity().getApplicationContext(), showSticker);
						 imm.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
					}
				});
				
				if (holder.imageHolder != null) {
					networkUntil.drawImageUrl(holder.imageHolder, item.getImage(), R.drawable.loading);
				}
				
				//set content
				if (holder.text != null) {
					if (item.getMessage() != null && !"".equals(item.getMessage())) {
						if (item.isLogged()) {
							changeBackgroundText(holder.text, user.getColor());
						}

                        // interesting part starts from here here:
                        Html.ImageGetter ig = imageGetter.create(position, item.getMessage(), holder.text);

                        holder.text.setTag(position);
                        holder.text.setText(Html.fromHtml(item.getMessage(), ig, null));

                        holder.text.setTextColor(Color.BLACK);
						holder.text.setLinkTextColor(Color.BLUE);
						holder.text.setVisibility(View.VISIBLE);
						
						holder.sticker.setVisibility(View.GONE);
						holder.gifView.setVisibility(View.GONE);
						
						MovementMethod m = holder.text.getMovementMethod();
						
						if ((m == null) || !(m instanceof LinkMovementMethod)) {
							if (holder.text.getLinksClickable()) {
								holder.text.setMovementMethod(LinkMovementMethod.getInstance());
							}
						}
						
					}
				}
				
				//set sticker
				if (holder.sticker != null || holder.gifView != null) {
					if (item.getSticker() != null) {
						
						holder.sticker.getLayoutParams().width = item.getStickerWidth() * Config.CHAT_STICKER;
						holder.sticker.getLayoutParams().height = item.getStickerHeight() * Config.CHAT_STICKER;
						
						if (!checkSize(holder.sticker.getLayoutParams().width)) {
							holder.sticker.getLayoutParams().width = sizeDisplay;
							holder.sticker.getLayoutParams().height = sizeDisplay;
						}
						
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
							holder.gifView.getLayoutParams().width = item.getStickerWidth() * Config.CHAT_STICKER;
							holder.gifView.getLayoutParams().height = item.getStickerHeight() * Config.CHAT_STICKER;
							
							holder.sticker.setVisibility(View.GONE);
							holder.gifView.setVisibility(View.VISIBLE);
						} else {
							holder.sticker.setVisibility(View.VISIBLE);
							holder.gifView.setVisibility(View.GONE);
						}

						
						holder.text.setVisibility(View.GONE);
						if (!item.getStickerType().equals("gif") || Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
							holder.sticker.setVisibility(View.VISIBLE);
							holder.gifView.setVisibility(View.GONE);
							networkUntil.drawImageUrl(holder.sticker, item.getSticker(), R.drawable.loading);
						} else {
							holder.sticker.setVisibility(View.GONE);
							holder.gifView.setVisibility(View.VISIBLE);
							getGifView(holder, item.getSticker());	
						}
						
					}
				}
			}
			
			return view;
		}
		
		/**
		 * Get gif view from url
		 * @param holder
		 * @param stickerUrl
		 */
		public void getGifView(final ChatViewHolder holder, String stickerUrl) {
			
			URI url = General.uriFromString(stickerUrl);
			CacheManager.getInstance(getActivity().getApplicationContext())
				.makeRequest(new CacheRequest(url, user.getFullname(),
						null, -400, 0, 
						CacheRequest.DownloadType.IF_NECESSARY, 201,
						false, false,
						getActivity().getApplicationContext()) {
					
					private void setContentView(View v) {
						holder.gifView.removeAllViews();
						holder.gifView.addView(v);
						v.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
						v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
					}

					
					@Override
					protected void onSuccess(final ReadableCacheFile cacheFile, long timestamp, UUID session, boolean fromCache, String mimetype) {
						
						try {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									public void run() {
										try {
											final GIFView gifView = new GIFView(getActivity(), cacheFile.getInputStream());
											setContentView(gifView);
										} catch(Exception e) {
											e.printStackTrace();
										}
									}
								});
							} else {
//								gifThread = new GifDecoderThread(cacheFile.getInputStream(), new GifDecoderThread.OnGifLoadedListener() {
//
//									public void onGifLoaded() {
//										new Handler(Looper.getMainLooper()).post(new Runnable() {
//											public void run() {
//												gifImageView = new ImageView(context);
//												gifImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//												setContentView(gifImageView);
//												gifThread.setView(gifImageView);
//											}
//										});
//									}
//
//									public void onOutOfMemory() {
//									}
//
//									public void onGifInvalid() {
//									}
//								});
//								
//								gifThread.start();
							}
							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
					@Override
					protected void onProgress(long bytesRead, long totalBytes) {
					}
					
					@Override
					protected void onFailure(RequestFailureType type, Throwable t,
							StatusLine status, String readableMessage) {
					}
					
					@Override
					protected void onDownloadStarted() {
					}
					
					@Override
					protected void onDownloadNecessary() {
					}
					
					@Override
					protected void onCallbackException(Throwable t) {
					}
				});
		}
		
		
	}
	
	/**
	 * Class conversation view holder
	 */
	public class ChatViewHolder {
		public final ImageView imageHolder;
		public final TextView text;
		public final ImageView sticker;
		public final TextView time;
		public final LinearLayout gifView;

		public ChatViewHolder(ImageView icon, TextView text, TextView time,
				ImageView sticker, LinearLayout gifView) {
			this.imageHolder = icon;
			this.text = text;
			this.time = time;
			this.sticker = sticker;
			this.gifView = gifView;
		}
	}

	/**
	 * Convert time stamp
	 * 
	 * @param timestamp
	 * @return
	 */
	private String convertTime(long timestamp) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return sdf.format(new Date(timestamp * 1000));
	}

}
