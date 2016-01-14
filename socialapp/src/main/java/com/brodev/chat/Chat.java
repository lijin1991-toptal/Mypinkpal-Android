package com.brodev.chat;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.brodev.socialapp.entity.User;

import android.content.Context;
import io.socket.SocketIO;

public class Chat extends Thread {
	
	private SocketIO socket;
	private ChatCallback callback;
	private Context context;
	private User user;
	private String urlChatServer = null; 

	public Chat(ChatCallbackAdapter callback, Context context) {
		this.context = context;
		this.callback = new ChatCallback(callback);
		user = (User) this.context;
	}

	@Override
	public void run() {
		try {
			urlChatServer = user.getChatServerUrl();
			if (urlChatServer != null) {
				if (!urlChatServer.startsWith("http://")) 
					urlChatServer = "http://" + urlChatServer;
				
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("token", user.getTokenChatServer()));
				
				socket = new SocketIO(urlChatServer, callback, pairs);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send message to user
	 * @param userId
	 * @param message
	 */
	public void sendMessage(String userId, String message, String image, String fullname, String agentInfo) {
		try {
			JSONObject json = new JSONObject();
			json.putOpt("message", message);
			json.putOpt("userId", userId);
			json.putOpt("avatarUrl", image);
			json.putOpt("fullName", fullname);
			json.putOpt("agentInfo", agentInfo);
			json.putOpt("sendFrom", "android");
			
			socket.emit("send:message", callback, json);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Join chat
	 */
	public void joinChat() {
		try {
			JSONObject json = new JSONObject();
			socket.emit("commit:online", callback, json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Composing chat
	 * @param userId
	 */
	public void composingChat(String userId) {
		try {
			JSONObject json = new JSONObject();
			json.putOpt("userId", userId);
			socket.emit("send:composing", callback, json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * read message
	 * @param userId
	 */
	public void read(String userId) {
		try {
			JSONObject json = new JSONObject();
			json.putOpt("userId", userId);
			socket.emit("send:read", callback, json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * block user
	 * @param userId
	 */
	public String block(String userId) {
		String event = "commit:block";
		try {
			JSONObject json = new JSONObject();
			json.putOpt("userId", userId);
			json.putOpt("sendEvent", false);
			socket.emit(event, callback, json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return event;
	}
	
	/**
	 * Unblock user
	 * @param userId
	 */
	public String unblock(String userId) {
		String event = "commit:unblock";
		try {
			JSONObject json = new JSONObject();
			json.putOpt("userId", userId);
			json.putOpt("sendEvent", false);
			socket.emit(event, callback, json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return event;
	}
	
	/**
	 * Delete chat conversation
	 * @param userId
	 */
	public String deleteChat(String userId) {
		String event = "commit:delete";
		try {
			JSONObject json = new JSONObject();
			json.putOpt("userId", userId);
			json.putOpt("sendEvent", false);
			socket.emit(event, callback, json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return event;
	}
	
	/**
	 * Send sticker
	 * @param userId
	 * @param stickerId
	 * @return
	 */
	public String sendSticker(String userId, String stickerId, String image, String fullname, String agentInfo) {
		String event = "send:sticker";
		try {
			JSONObject json = new JSONObject();
			json.putOpt("userId", userId);
			json.putOpt("id", stickerId);
			json.putOpt("avatarUrl", image);
			json.putOpt("fullName", fullname);
			json.putOpt("agentInfo", fullname);
			json.putOpt("sendFrom", "android");
			
			socket.emit(event, callback, json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return event;
	}
	
}
