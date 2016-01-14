package com.brodev.chat;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.IOCallback;
import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

public class ChatCallback implements IOCallback, IOAcknowledge {
	private ChatCallbackAdapter callback;

	public ChatCallback(ChatCallbackAdapter callback) {
		this.callback = callback;
	}

	@Override
	public void ack(Object... data) {
		try {
			callback.callback(new JSONArray(Arrays.asList(data)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisconnect() {
		callback.onDisconnect();

	}

	@Override
	public void onConnect() {
		callback.onConnect();

	}

	@Override
	public void onMessage(String message, IOAcknowledge ack) {
		callback.onMessage(message);
	}

	@Override
	public void onMessage(JSONObject json, IOAcknowledge ack) {
		callback.onMessage(json);
	}

	@Override
	public void on(String event, IOAcknowledge ack, Object... data) {
		callback.on(event, (JSONObject) data[0]);
	}

	@Override
	public void onError(SocketIOException socketIOException) {
		socketIOException.printStackTrace();
		callback.onConnectFailure();

	}
}
