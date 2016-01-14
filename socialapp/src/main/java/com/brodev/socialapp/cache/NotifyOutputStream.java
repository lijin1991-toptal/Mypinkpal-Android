package com.brodev.socialapp.cache;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NotifyOutputStream extends FilterOutputStream {
	private final Listener listener;

	public NotifyOutputStream(final OutputStream out, final Listener listener) {
		super(out);
		this.listener = listener;
	}

	@Override
	public void close() throws IOException {
		super.close();
		listener.onClose();
	}

	public interface Listener {
		public void onClose() throws IOException;
	}
}
