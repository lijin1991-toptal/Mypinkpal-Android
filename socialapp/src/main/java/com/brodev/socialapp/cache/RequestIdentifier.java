package com.brodev.socialapp.cache;

import java.net.URI;
import java.util.UUID;

public class RequestIdentifier {
	private final URI url;
	private final UUID session;
	private final boolean unique;

	public RequestIdentifier(final URI url, final UUID session, final boolean unique) {

		if(url == null) throw new NullPointerException("URL must not be null");

		this.url = url;
		this.session = session;
		this.unique = unique;
	}

	@Override
	public boolean equals(final Object o) {

		if(this == o) return true;

		if(o == null) return false;
		if(!(o instanceof RequestIdentifier)) return false;

		final RequestIdentifier other = (RequestIdentifier)o;

		if(!unique || !other.unique) return false;

		if(!other.url.equals(url)) return false;

		return other.session == session || other.session.equals(session);

	}

	public URI getUrl() {
		return url;
	}
}
