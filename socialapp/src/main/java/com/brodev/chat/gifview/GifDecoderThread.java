package com.brodev.chat.gifview;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

@SuppressLint("HandlerLeak")
public class GifDecoderThread extends Thread {
	private volatile boolean playing = true;
	private final InputStream is;
	private ImageView view;
	private final OnGifLoadedListener listener;

	final AtomicBoolean loaded = new AtomicBoolean(false);
	final AtomicBoolean failed = new AtomicBoolean(false);

	final GifDecoder decoder = new GifDecoder();
	
	public void setView(ImageView view) {
		this.view = view;
	}
	
	public interface OnGifLoadedListener {
		public void onGifLoaded();

		public void onOutOfMemory();

		public void onGifInvalid();
	}

	private final Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			if (playing && view != null)
				view.setImageBitmap((Bitmap) msg.obj);
		}
	};

	public GifDecoderThread(InputStream is, OnGifLoadedListener listener) {
		super("GIF playing thread");
		this.is = is;
		this.listener = listener;
	}

	public void stopPlaying() {
		playing = false;
		interrupt();

		try {
			is.close();
		} catch (Throwable t) {
		}
	}

	@Override
	public void run() {
		new Thread("GIF decoding thread") {
			@Override
			public void run() {
				try {
					decoder.read(is);
					loaded.set(true);
				} catch (Throwable t) {
					t.printStackTrace();
					failed.set(true);
				}
			}
		}.start();

		try {
			if (!playing)
				return;

			listener.onGifLoaded();

			int frame = 0;

			long start = System.currentTimeMillis();
			long end = start + 3*1000; // 3 seconds * 1000 ms/sec
			
			while (System.currentTimeMillis() < end) {

				while (decoder.getFrameCount() <= frame + 1 && !loaded.get()
						&& !failed.get()) {
					try {
						sleep(300);
					} catch (InterruptedException e) {
						return;
					}
				}

				frame = frame % decoder.getFrameCount();

				final Bitmap img = decoder.getFrame(frame);

				final Message msg = new Message();
				msg.obj = img;
				handler.sendMessage(msg);

				try {
					sleep(Math.max(32, decoder.getDelay(frame)));
				} catch (InterruptedException e) {
					return;
				}

				if (failed.get()) {
					listener.onGifInvalid();
					return;
				}

				frame++;
			}
			
		} catch (OutOfMemoryError e) {
			listener.onOutOfMemory();

		} catch (Throwable t) {
			listener.onGifInvalid();
		}
	}
}
