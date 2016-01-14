package com.brodev.socialapp.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CachingInputStream extends InputStream {

	private final InputStream in;
	private final OutputStream out;

	private long bytesRead = 0;
	private final BytesReadListener listener;

	private boolean stillRunning = true;

	public CachingInputStream(final InputStream in, final OutputStream out, final BytesReadListener listener) {
		this.in = in;
		this.out = out;
		this.listener = listener;
	}

	public interface BytesReadListener {
		public void onBytesRead(long total);
	}

	private void notifyOnBytesRead() {
		if(listener != null) listener.onBytesRead(bytesRead);
	}

	@Override
	public void close() throws IOException {

		if(stillRunning) {
			in.close();
			throw new RuntimeException("Closing CachingInputStream before the input stream has ended");
		}
	}

	@Override
	public int read() throws IOException {

		final int byteRead = in.read();

		if(byteRead >= 0) {
			out.write(byteRead);
			bytesRead++;
			notifyOnBytesRead();

		} else {
			terminate();
		}

		return byteRead;
	}

	@Override
	public int read(final byte[] buffer, final int offset, final int length) throws IOException {

		final int result = in.read(buffer, offset, length);

		if(result > 0) {
			out.write(buffer, offset, result);
			bytesRead += result;
			notifyOnBytesRead();

		} else {
			terminate();
		}

		return result;
	}

	private void terminate() throws IOException {

		if(stillRunning) {
			stillRunning = false;
			out.flush();
			out.close();
			in.close();
		}
	}
}
